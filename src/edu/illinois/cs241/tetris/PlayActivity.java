package edu.illinois.cs241.tetris;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import edu.illinois.cs241.tetris.*;

public class PlayActivity extends Activity implements View.OnTouchListener, Runnable
{
    private Tetromino block;
    private Tetromino next;
    private Playfield playfield;
    
    private ViewGroup layout;
    private PlayfieldView oppFieldView;
    private PlayfieldView nextBlockView;
    private PlayfieldView playfieldView;
    private Handler handler;

    private String mode;
    private String oName;
    private BluetoothDevice oDevice;
    private BluetoothSocket socket;
    
    private float downX, downY;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        
        block = new Tetromino();
        next = new Tetromino();
        
        layout = (ViewGroup)this.findViewById(R.id.field_layout);
        layout.setOnTouchListener(this);
        
        playfieldView = (PlayfieldView)this.findViewById(R.id.play_field);
        playfield = new Playfield(20, 10, 2);
        playfieldView.setPlayfield(playfield);
        playfieldView.setFaintBlock(true);

        nextBlockView = (PlayfieldView)findViewById(R.id.next_piece);
        nextBlockView.setPlayfield(new Playfield(4, 4));
        nextBlockView.setFaintBlock(false);
        createNextBlock();
        
        oppFieldView = (PlayfieldView)this.findViewById(R.id.opponent_view);
        oppFieldView.setPlayfield(new Playfield(20, 10, 2));
        oppFieldView.setFaintBlock(false);
        
        // extract Intent extras
/*        Intent intent = getIntent();
        mode = intent.getStringExtra(TetrisApplication.MODE_KEY);
        if (!mode.equals(TetrisApplication.SINGLE_MODE))
        {
            oName = intent.getStringExtra(TetrisApplication.NAME_KEY);
            if (oDevice != null)
            {
                // set up client task
            }
            else
            {
                // set up server task
            }
        }
        else*/
        {
            Toast.makeText(this, "Starting...", Toast.LENGTH_SHORT).show();
            shiftNextBlock();
            handler = new Handler();
            handler.postDelayed(this, 500);
        }
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
        handler.removeCallbacks(this);
        super.onDestroy();
    }

    // create a random block and insert it into the next piece view
    private void createNextBlock()
    {
        next.copyRandBlock();
        next.getCenter().set(1, 2);
        nextBlockView.setBlock(next);
        nextBlockView.invalidateBlock();
    }
    
    // move the next block into the playfield
    private void shiftNextBlock()
    {
        next.getCenter().set((playfield.getCols() - 1) / 2, -1);
        playfieldView.setBlock(next);
        next = block;
        block = nextBlockView.getBlock();
        createNextBlock();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        // stores where the finger touches the screen
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN)
        {
            downX = event.getRawX();
            downY = event.getRawY();
        }

        // figure out touch motion
        if (event.getActionMasked() == MotionEvent.ACTION_UP)
        {
            float dX = event.getRawX() - downX, dXabs = Math.abs(dX), dY = event.getRawY() - downY, dYabs = Math.abs(dY);
            int minDist = Math.min(v.getWidth(), v.getHeight()) / 3;
            
            // check if finger moved more horizontally, distance at least a third of screen
            if (dXabs / dYabs >= 2 && dXabs >= minDist)
            {
                // check if finger moved right
                if (dX > 0)
                {
                    // move block right
                    moveBlock(1, 0);
                }
                else
                {
                    // move block left
                    moveBlock(-1, 0);
                }
                playfieldView.invalidateBlock();
            }
            else if (dYabs / dXabs >= 2 && dYabs >= minDist)
            {
                // check if finger moved down
                if (dY > 0)
                {
                    // drop block to bottom
                    while (moveBlock(0, 1));
                    handler.removeCallbacks(this);
                    handler.postDelayed(this, 500);
                    playfieldView.invalidatePlayfield();
                }
                else
                {
                    // rotate block
                    rotateBlock();
                    playfieldView.invalidateBlock();
                }
                
            }
        }
        return true;
    }
    
    // try to shift block in direction, return if successful
    private boolean moveBlock(int dx, int dy)
    {
        block.getCenter().offset(dx, dy);
        if (!playfield.canInsert(block))
        {
            block.getCenter().offset(-dx, -dy);
            return false;
        }
        return true;
    }
    
    // rotate the block until it reaches a valid orientation
    private void rotateBlock()
    {
        block.rotateCW();
        while (!playfield.canInsert(block))
            block.rotateCW();
    }

    // run timed events
    @Override
    public void run()
    {
        // block reached bottom
        if (!moveBlock(0, 1))
        {
            if (playfield.insertBlock(block) == 0)
                playfieldView.invalidateBlock();
            else
                playfieldView.invalidatePlayfield();

            // check game over
            if (playfield.reachedTop())
            {
                nextBlockView.setBlock(null);
                nextBlockView.invalidatePlayfield();
                layout.setOnTouchListener(null);
                handler.removeCallbacks(this);
                Toast.makeText(this, "Game over", Toast.LENGTH_SHORT).show();
                return;
            }
            else
                shiftNextBlock();
        }
        else
            playfieldView.invalidateBlock();

        handler.postDelayed(this, 500);
    }
}
