package edu.illinois.cs241.tetris;

import java.io.*;
import java.util.*;
import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public class PlayActivity extends Activity implements Runnable
{
    private Tetromino mCurBlock;
    private Tetromino mNextBlock;
    private Playfield mMyPlayfield;
    private Playfield mOppPlayfield;
    
    private PlayfieldView mMyFieldView;
    private PlayfieldView mOppFieldView;
    private PlayfieldView mNextBlockView;
    
    private OutputHandler mOutputHandler;
    private InputHandler mInputHandler;
    private GestureDetector mGestureHandler;

    private String mGameMode;
    private String mOppName;
    private BluetoothSocket mBTsocket;
    
    private int mGameState;
    private ArrayList<Integer> mActionQueue;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        
        // set up views and game data
        mCurBlock = new Tetromino();
        mNextBlock = new Tetromino();
        mMyPlayfield = new Playfield(20, 10, 2);
        mOppPlayfield = new Playfield(20, 10, 0);
        
        mMyFieldView = (PlayfieldView)this.findViewById(R.id.play_field);
        mMyFieldView.setPlayfield(mMyPlayfield);
        mMyFieldView.setBlockHighlighted(true);

        mOppFieldView = (PlayfieldView)this.findViewById(R.id.opponent_view);
        mOppFieldView.setPlayfield(mOppPlayfield);
        mOppFieldView.setBlockHighlighted(false);

        mNextBlockView = (PlayfieldView)findViewById(R.id.next_piece);
        mNextBlockView.setPlayfield(new Playfield(4, 4));
        mNextBlockView.setBlockHighlighted(false);
        createNextBlock();
        
        mInputHandler = new InputHandler(getMainLooper());
        mGestureHandler = new GestureDetector(this, new TetrisGestureListener(this));
        mActionQueue = new ArrayList<Integer>();
        
        // extract Intent extras
        Intent intent = getIntent();
        mGameMode = intent.getStringExtra(TetrisApplication.MODE_KEY);
        if (!mGameMode.equals(TetrisApplication.SINGLE_MODE))
        {
            mOppName = intent.getStringExtra(TetrisApplication.NAME_KEY);
            mBTsocket = TetrisApplication.getBTSocket();

            try
            {
                InputThread inputThread = new InputThread(mInputHandler, mBTsocket.getInputStream());
                inputThread.start();
             
                HandlerThread outputThread = new HandlerThread("edu.illinois.cs241.outputThread");
                outputThread.start();
                mOutputHandler = new OutputHandler(outputThread.getLooper(), mBTsocket.getOutputStream(), mInputHandler);
            }
            catch (IOException e)
            {
                Toast.makeText(this, "Quitting due to connection errors", Toast.LENGTH_SHORT);
                finish();
            }
        }

        shiftNextBlock();
        Toast.makeText(this, "Starting...", Toast.LENGTH_SHORT).show();
        mGameState = TetrisApplication.WAITING;
        mInputHandler.post(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return mGestureHandler.onTouchEvent(event);
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        // close sockets, streams?
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        mInputHandler.removeCallbacks(this);
        super.onDestroy();
    }

    // create a random block and insert it into the next piece view
    private void createNextBlock()
    {
        mNextBlock.copyRandBlock();
        mNextBlock.getCenter().set(1, 2);
        mNextBlockView.setBlock(mNextBlock);
        mNextBlockView.invalidateBlock();
    }
    
    // move the next block into the playfield
    private void shiftNextBlock()
    {
        mNextBlock.getCenter().set((mMyPlayfield.getCols() - 1) / 2, -1);
        mMyFieldView.setBlock(mNextBlock);
        mNextBlock = mCurBlock;
        mCurBlock = mNextBlockView.getBlock();
        createNextBlock();
    }
    
    private void createRandBlock()
    {
        mCurBlock.copyRandBlock();
        int rotate = (int)(Math.random() * 4);
        for (int i = 0; i <= rotate; i++)
            mCurBlock.rotateCW();
        int translate = (int)(Math.random() * 9) - 4;
        moveBlock(translate, 0);
    }

    // try to shift block in direction, return if successful
    private boolean moveBlock(int dx, int dy)
    {
        mCurBlock.getCenter().offset(dx, dy);
        if (!mMyPlayfield.canInsert(mCurBlock))
        {
            mCurBlock.getCenter().offset(-dx, -dy);
            return false;
        }
        return true;
    }
    
    // rotate the block until it reaches a valid orientation
    private void rotateBlock()
    {
        mCurBlock.rotateCW();
        while (!mMyPlayfield.canInsert(mCurBlock))
            mCurBlock.rotateCW();
    }

    // run timed events
    @Override
    public void run()
    {
        if (mGameState == TetrisApplication.WAITING)
        {
            if (mActionQueue.isEmpty())
            {
                shiftNextBlock();
                mGameState = TetrisApplication.IN_CONTROL;
                mInputHandler.post(this);
                return;
            }
            else
            {
                switch (mActionQueue.remove(0))
                {
                    case TetrisApplication.CLEAR_ROW:
                        if (TetrisApplication.SEESAW_MODE.equals(mGameMode))
                        {
                            mMyPlayfield.insertRandRow();
                            mMyFieldView.invalidatePlayfield();
                            Message msg = mOutputHandler.obtainMessage(TetrisApplication.UPDATE_FIELD);
                            msg.obj = mMyPlayfield.exportField();
                            mOutputHandler.sendMessage(msg);
                            if (mMyPlayfield.reachedTop())
                            {
                                mNextBlockView.setBlock(null);
                                mNextBlockView.invalidatePlayfield();
                                mInputHandler.removeCallbacks(this);
                                Toast.makeText(this, mOppName + " wins!", Toast.LENGTH_SHORT);
                                Message msgLose = mOutputHandler.obtainMessage(TetrisApplication.LOSE);
                                mOutputHandler.sendMessage(msgLose);
                                try
                                {
                                    mBTsocket.getInputStream().close();
                                    mBTsocket.getOutputStream().close();
                                    mBTsocket.close();
                                } catch (IOException e){}
                                return;
                            }
                            else
                                mInputHandler.post(this);
                        }
                        else
                        {
                            mGameState = TetrisApplication.HANDLING;
                            createRandBlock();
                            mInputHandler.post(this);
                        }
                        return;
                    case TetrisApplication.LOSE:
                        mNextBlockView.setBlock(null);
                        mNextBlockView.invalidatePlayfield();
                        mInputHandler.removeCallbacks(this);
                        Toast.makeText(this, "You win!", Toast.LENGTH_SHORT);
                        try
                        {
                            mBTsocket.getInputStream().close();
                            mBTsocket.getOutputStream().close();
                            mBTsocket.close();
                        } catch (IOException e){}
                        return;
                    case TetrisApplication.ERROR:
                        mNextBlockView.setBlock(null);
                        mNextBlockView.invalidatePlayfield();
                        mInputHandler.removeCallbacks(this);
                        Toast.makeText(this, "Connection error, stopping...", Toast.LENGTH_SHORT);
                        try
                        {
                            mBTsocket.getInputStream().close();
                            mBTsocket.getOutputStream().close();
                            mBTsocket.close();
                        } catch (IOException e){}
                        return;
                }
            }
        }
        
        else if (mGameState == TetrisApplication.IN_CONTROL || mGameState == TetrisApplication.HANDLING && TetrisApplication.SABOTAGE_MODE.equals(mGameMode))
        {
            // block reached bottom
            if (!moveBlock(0, 1))
            {
                if (mMyPlayfield.insertBlock(mCurBlock) == 0)
                {
                    if (!TetrisApplication.SINGLE_MODE.equals(mGameMode))
                    {
                        Message msg = mOutputHandler.obtainMessage(TetrisApplication.UPDATE_FIELD);
                        msg.obj = mMyPlayfield.exportField();
                        mOutputHandler.sendMessage(msg);
                    }
                    mMyFieldView.invalidateBlock();
                }
                else
                {
                    mMyFieldView.invalidatePlayfield();
                    if (!TetrisApplication.SINGLE_MODE.equals(mGameMode))
                    {
                        Message msg = mOutputHandler.obtainMessage(TetrisApplication.CLEAR_ROW);
                        msg.obj = mMyPlayfield.exportField();
                        mOutputHandler.sendMessage(msg);
                    }
                }
                
                // check game over
                if (mMyPlayfield.reachedTop())
                {
                    mNextBlockView.setBlock(null);
                    mNextBlockView.invalidatePlayfield();
                    if (TetrisApplication.SINGLE_MODE.equals(mGameMode))
                    {
                        Toast.makeText(this, "Game over", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else
                    {
                        Toast.makeText(this, mOppName + " wins!", Toast.LENGTH_SHORT).show();
                        Message msgLose = mOutputHandler.obtainMessage(TetrisApplication.LOSE);
                        mOutputHandler.sendMessage(msgLose);
                        try
                        {
                            mBTsocket.getInputStream().close();
                            mBTsocket.getOutputStream().close();
                            mBTsocket.close();
                        } catch (IOException e){}
                    }
                }
                else
                {
                    if (mGameState == TetrisApplication.IN_CONTROL)
                        shiftNextBlock();
                    mInputHandler.post(this);
                }
                mGameState = TetrisApplication.WAITING;
            }
            else
            {
                mMyFieldView.invalidateBlock();
                if (mGameState == TetrisApplication.IN_CONTROL)
                    mInputHandler.postDelayed(this, 500);
                else
                    mInputHandler.post(this);
            }
        }
    }
    
    private class TetrisGestureListener extends GestureDetector.SimpleOnGestureListener
    {
        private Runnable mRunner;
        
        public TetrisGestureListener(Runnable run)
        {
            mRunner = run;
        }
        
        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY)
        {
            if (mGameState == TetrisApplication.IN_CONTROL)
            {
                if (Math.abs(velocityX) > Math.abs(velocityY))
                {
                    if (velocityX > 0)
                        moveBlock(1, 0);
                    else
                        moveBlock(-1, 0);
                    mMyFieldView.invalidateBlock();
                }
                else
                {
                    if (velocityY > 0)
                    {
                        while (moveBlock(0, 1));
                        mInputHandler.removeCallbacks(mRunner);
                        mInputHandler.postDelayed(mRunner, 500);
                        mMyFieldView.invalidatePlayfield();
                    }
                    else
                    {
                        rotateBlock();
                        mMyFieldView.invalidateBlock();
                    }
                }
            }
            return true;
        }
        
    }
    
    class InputHandler extends Handler
    {
        public InputHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == TetrisApplication.UPDATE_FIELD || msg.what == TetrisApplication.CLEAR_ROW)
            {
                byte[] buffer =(byte[]) msg.obj;
                mOppPlayfield.importField(buffer);
                mOppFieldView.invalidatePlayfield();
                
                if (msg.what == TetrisApplication.CLEAR_ROW)
                    mActionQueue.add(msg.what);
            }
            else
            {
                mActionQueue.clear();
                mActionQueue.add(msg.what);
                mGameState = TetrisApplication.WAITING;
            }
        }
    }
}

class OutputHandler extends Handler
{
    private DataOutputStream mOutputStream;
    private Handler mInputHandler;
    
    public OutputHandler(Looper looper, OutputStream output, Handler inputHandler)
    {
        super(looper);
        mOutputStream = new DataOutputStream(new BufferedOutputStream(output));
        mInputHandler = inputHandler;
    }

    @Override
    public void handleMessage(Message msg)
    {
        try
        {
            mOutputStream.writeInt(msg.what);
            if ( msg.what == TetrisApplication.UPDATE_FIELD || msg.what == TetrisApplication.CLEAR_ROW)
                mOutputStream.write((byte [])msg.obj, 0, 800);
            mOutputStream.flush();
        }
        catch (IOException e)
        {
            Message retMsg = mInputHandler.obtainMessage(TetrisApplication.ERROR);
            mInputHandler.sendMessage(retMsg);
        }
        
        if (msg.what == TetrisApplication.WIN || msg.what == TetrisApplication.LOSE || msg.what == TetrisApplication.ERROR)
        {
            try {mOutputStream.close();} catch (IOException e){}
            ((HandlerThread)getLooper().getThread()).quit();
        }
    }
}

class InputThread extends Thread
{
    private Handler mInputHandler;
    private DataInputStream mInputStream;
    private boolean exit;
    
    public InputThread(Handler inputHandler, InputStream inputStream)
    {
        super("edu.illinois.cs241.inputThread");
        mInputHandler = inputHandler;
        mInputStream = new DataInputStream(new BufferedInputStream(inputStream));
        exit = false;
    }

    @Override
    public void run()
    {
        while (!exit)
        {
            Message msg = mInputHandler.obtainMessage();
            try
            {
                int what = mInputStream.readInt();
                msg.what = what;
                if (what == TetrisApplication.CLEAR_ROW || what == TetrisApplication.UPDATE_FIELD)
                {
                    byte[] buffer = new byte[800];
                    mInputStream.read(buffer);
                    msg.obj = buffer;
                }
                else if (what == TetrisApplication.WIN || what == TetrisApplication.LOSE || what == TetrisApplication.ERROR)
                    exit = true;
                mInputHandler.sendMessage(msg);
            }
            catch(IOException e)
            {
                msg.what = TetrisApplication.ERROR;
                mInputHandler.sendMessage(msg);
                exit = true;
            }
        }
        try {mInputStream.close();} catch (IOException e){}
    }
}