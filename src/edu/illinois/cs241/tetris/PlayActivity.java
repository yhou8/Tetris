package edu.illinois.cs241.tetris;

import java.io.*;
import java.util.*;
import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public class PlayActivity extends Activity implements Runnable, Handler.Callback
{
    private Tetromino mCurBlock;
    private Tetromino mNextBlock;
    private Playfield mMyPlayfield;
    private Playfield mOppPlayfield;
    
    private PlayfieldView mMyFieldView;
    private PlayfieldView mOppFieldView;
    private PlayfieldView mNextBlockView;
    
    private OutputHandler mOutputHandler;
    private Handler mInputHandler;
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
        mOppPlayfield = new Playfield(20, 10);
        
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
        
        mInputHandler = new Handler(getMainLooper(), this);
        mGestureHandler = new GestureDetector(this, new TetrisGestureListener());
        
        // extract Intent extras
        Intent intent = getIntent();
        mGameMode = intent.getStringExtra(TetrisApplication.MODE_KEY);
        if (!TetrisApplication.SINGLE_MODE.equals(mGameMode))
        {
            mOppName = intent.getStringExtra(TetrisApplication.NAME_KEY);
            mActionQueue = new ArrayList<Integer>();
            mBTsocket = TetrisApplication.getBTSocket();

            try
            {
                InputThread inputThread = new InputThread(mInputHandler, mBTsocket.getInputStream(), mMyPlayfield.getExportSize());
                inputThread.start();
             
                HandlerThread outputThread = new HandlerThread("edu.illinois.cs241.outputThread");
                outputThread.start();
                mOutputHandler = new OutputHandler(outputThread.getLooper(), mBTsocket.getOutputStream(), mInputHandler);
            }
            catch (IOException e)
            {
                Toast.makeText(this, "Connection Error, Closing", Toast.LENGTH_SHORT);
                finish();
            }
        }

        shiftNextBlock();
        Toast.makeText(this, "Starting...", Toast.LENGTH_SHORT).show();
        mGameState = TetrisApplication.WAITING;
        mInputHandler.post(this);
    }

    @Override
    protected void onPause()
    {
        if (mBTsocket != null)
            endConnection();
        stopRunning();
        super.onPause();
    }

    // run timed events
    @Override
    public void run()
    {
        if (TetrisApplication.SINGLE_MODE.equals(mGameMode))
            runSingleMode();
        else if (TetrisApplication.SEESAW_MODE.equals(mGameMode))
            runSeesawMode();
        else if (TetrisApplication.SABOTAGE_MODE.equals(mGameMode))
            runSabotageMode();
    }
    
    private void runSingleMode()
    {
        if (mGameState == TetrisApplication.WAITING)
        {
            mGameState = TetrisApplication.IN_CONTROL;
            shiftNextBlock();
            mInputHandler.post(this);
        }
        // check if block can be moved down
        else if (!moveBlock(0, 1))
        {
            if (mMyPlayfield.insertBlock(mCurBlock) == 0)
                mMyFieldView.invalidateBlock();
            else
                mMyFieldView.invalidatePlayfield();
            
            // check game over
            if (mMyPlayfield.reachedTop())
            {
                Toast.makeText(this, "Game over", Toast.LENGTH_SHORT).show();
                stopRunning();
            }
            else
            {
                mGameState = TetrisApplication.WAITING;
                mInputHandler.post(this);
            }
        }
        else
        {
            mMyFieldView.invalidateBlock();
            mInputHandler.postDelayed(this, 500);
        }
    }

    private void runSeesawMode()
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
                        mMyPlayfield.insertRandRow();
                        mMyFieldView.invalidatePlayfield();
                        sendUpdateField();
                        if (mMyPlayfield.reachedTop())
                        {
                            Toast.makeText(this, mOppName + " wins!", Toast.LENGTH_SHORT).show();
                            mOutputHandler.sendMessage(mOutputHandler.obtainMessage(TetrisApplication.LOSE));
                            stopRunning();
                        }
                        mInputHandler.post(this);
                        return;
                    case TetrisApplication.LOSE:
                        Toast.makeText(this, "You win!", Toast.LENGTH_SHORT).show();
                        stopRunning();
                        endConnection();
                        return;
                    case TetrisApplication.CONNECTION_ERROR:
                        Toast.makeText(this, "Lost Connection, Stopping...", Toast.LENGTH_SHORT).show();
                        stopRunning();
                        endConnection();
                        return;
                    case TetrisApplication.CLEAN_UP:
                        endConnection();
                        return;
                }
            }
        }
        else
            runMultiMode();
    }

    private void runSabotageMode()
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
                        createRandBlock();
                        mGameState = TetrisApplication.HANDLING;
                        mInputHandler.post(this);
                        return;
                    case TetrisApplication.LOSE:
                        Toast.makeText(this, "You win!", Toast.LENGTH_SHORT).show();
                        stopRunning();
                        endConnection();
                        return;
                    case TetrisApplication.CONNECTION_ERROR:
                        Toast.makeText(this, "Lost Connection, Stopping...", Toast.LENGTH_SHORT).show();
                        stopRunning();
                        endConnection();
                        return;
                    case TetrisApplication.CLEAN_UP:
                        endConnection();
                        return;
                }
            }
        }
        else if (mGameState == TetrisApplication.IN_CONTROL || mGameState == TetrisApplication.HANDLING)
            runMultiMode();
        else
            endConnection();
    }

    private void runMultiMode()
    {
        if (!moveBlock(0, 1))
        {
            int rowsCleared = mMyPlayfield.insertBlock(mCurBlock); 
            if (rowsCleared == 0)
                mMyFieldView.invalidateBlock();
            else
            {
                for (int i = 0; i < rowsCleared; i++)
                    mOutputHandler.sendMessage(mOutputHandler.obtainMessage(TetrisApplication.CLEAR_ROW));
                mMyFieldView.invalidatePlayfield();
            }
            sendUpdateField();
            
            // check game over
            if (mMyPlayfield.reachedTop())
            {
                Toast.makeText(this, mOppName + " wins!", Toast.LENGTH_SHORT).show();
                mOutputHandler.sendMessage(mOutputHandler.obtainMessage(TetrisApplication.LOSE));
                stopRunning();
            }
            else
            {
                mGameState = TetrisApplication.WAITING;
                mInputHandler.post(this);
            }
        }
        else
        {
            mMyFieldView.invalidateBlock();
            if (mGameState == TetrisApplication.HANDLING)
                mInputHandler.postDelayed(this, 50);
            else
                mInputHandler.postDelayed(this, 500);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return mGestureHandler.onTouchEvent(event);
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
        mNextBlock = mCurBlock;
        mCurBlock = mNextBlockView.getBlock();
        mCurBlock.getCenter().set((mMyPlayfield.getCols() - 1) / 2, -1);
        mMyFieldView.setBlock(mCurBlock);
        createNextBlock();
    }

    private void createRandBlock()
    {
        mCurBlock.copyRandBlock();
        mCurBlock.getCenter().set((mMyPlayfield.getCols() - 1) / 2, -1);
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
        {
            if (moveBlock(-1, 0))
                return;
            else if (moveBlock(1, 0))
                return;
            else 
                mCurBlock.rotateCW();
        }
    }

    private void sendUpdateField()
    {
        Message msg = mOutputHandler.obtainMessage(TetrisApplication.UPDATE_FIELD);
        msg.obj = mMyPlayfield.exportField();
        mOutputHandler.sendMessage(msg);
    }
    
    private void stopRunning()
    {
        mNextBlockView.setBlock(null);
        mNextBlockView.invalidatePlayfield();
        mInputHandler.removeCallbacks(this);
    }
    
    private void endConnection()
    {
        try {mBTsocket.getInputStream().close();} catch (IOException e){}
        try {mBTsocket.getOutputStream().close();} catch (IOException e){}
        try {mBTsocket.close();} catch (IOException e){}
    }
    
    private class TetrisGestureListener extends GestureDetector.SimpleOnGestureListener
    {
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
                        mInputHandler.removeCallbacks(PlayActivity.this);
                        mInputHandler.postDelayed(PlayActivity.this, 500);
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
    
    @Override
    public boolean handleMessage(Message msg)
    {
        if (msg.what == TetrisApplication.UPDATE_FIELD)
        {
            mOppPlayfield.importField((byte[])msg.obj);
            mOppFieldView.invalidatePlayfield();
        }
        else if (msg.what == TetrisApplication.CLEAR_ROW)
            mActionQueue.add(msg.what);
        else
        {
            mActionQueue.clear();
            mActionQueue.add(0, msg.what);
            mGameState = TetrisApplication.WAITING;
        }
            
        return true;
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
            if (msg.what == TetrisApplication.UPDATE_FIELD)
            {
                byte[] buffer = (byte[]) msg.obj;
                mOutputStream.write(buffer, 0, buffer.length);
            }
            mOutputStream.flush();
            // last message to send
            if (msg.what == TetrisApplication.WIN || msg.what == TetrisApplication.LOSE)
            {
                mInputHandler.sendMessage(mInputHandler.obtainMessage(TetrisApplication.CLEAN_UP));
                try {mOutputStream.close();} catch (IOException e){}
                ((HandlerThread)getLooper().getThread()).quit();
            }
        }
        catch (IOException e)
        {
            mInputHandler.sendMessageDelayed(mInputHandler.obtainMessage(TetrisApplication.CONNECTION_ERROR), 500);
            try {mOutputStream.close();} catch (IOException ex){}
            ((HandlerThread)getLooper().getThread()).quit();
        }
    }
}

class InputThread extends Thread
{
    private Handler mInputHandler;
    private DataInputStream mInputStream;
    private int mFieldExportSize;
    
    public InputThread(Handler inputHandler, InputStream inputStream, int fieldExportSize)
    {
        super("edu.illinois.cs241.inputThread");
        mInputHandler = inputHandler;
        mInputStream = new DataInputStream(new BufferedInputStream(inputStream));
        mFieldExportSize = fieldExportSize;
    }

    // read message from opponent and send message to handler on UI thread
    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                Message msg = mInputHandler.obtainMessage(mInputStream.readInt());
                
                // update field message has the field attached
                if (msg.what == TetrisApplication.UPDATE_FIELD)
                {
                    byte[] buffer = new byte[mFieldExportSize];
                    mInputStream.read(buffer);
                    msg.obj = buffer;
                }
                // this is the last expected message
                else if (msg.what == TetrisApplication.WIN || msg.what == TetrisApplication.LOSE)
                {
                    mInputHandler.removeMessages(TetrisApplication.CONNECTION_ERROR);
                    mInputHandler.sendMessageAtFrontOfQueue(msg);
                    try {mInputStream.close();} catch (IOException e){}
                    return;
                }
                mInputHandler.sendMessage(msg);
            }
            catch(IOException e)
            {
                mInputHandler.sendMessage(mInputHandler.obtainMessage(TetrisApplication.CONNECTION_ERROR));
                try {mInputStream.close();} catch (IOException ex){}
                return;
            }
        }
    }
}