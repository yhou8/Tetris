package edu.illinois.tetris.gui;

import android.app.*;
import android.bluetooth.*;
import android.os.*;
import edu.illinois.engr.courses.cs241.honors.tetris.*;

public class PlayActivity extends Activity
{
    private SinglePieceView nextPiece;
    private PlayfieldView playfield;
    private String mode;
    private String oName;
    private BluetoothDevice oDevice;
    private BluetoothSocket socket;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        
        nextPiece = (SinglePieceView)this.findViewById(R.id.next_piece);
        playfield = (PlayfieldView)this.findViewById(R.id.play_field);

        // extract Bundle information
        mode = savedInstanceState.getString(TetrisApplication.MODE_KEY);
        if (!mode.equals(TetrisApplication.SINGLE_MODE))
        {
            oName = savedInstanceState.getString(TetrisApplication.NAME_KEY);
            oDevice = savedInstanceState.getParcelable(TetrisApplication.DEVICE_KEY);
            if (oDevice != null)
            {
                // set up client task
            }
            else
            {
                // set up server task
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }
    
    
}
