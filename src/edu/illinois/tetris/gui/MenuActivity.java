package edu.illinois.tetris.gui;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import edu.illinois.engr.courses.cs241.honors.tetris.*;

public class MenuActivity extends Activity
{
    private static final int ENABLE_BT = 1;
    private BluetoothAdapter mDefaultAdapter;
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		
		// disable multiplayer option if bluetooth is not supported
		mDefaultAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mDefaultAdapter == null)
		    findViewById(R.id.multi_player_button).setEnabled(false);
	}
	
	public void onSinglePlayer(View v)
	{
	    // tells game activity to start on single player mode
	    Intent singlePlayerIntent = new Intent(this, GameActivity.class);
	    singlePlayerIntent.putExtra(TetrisApplication.MODE_KEY, TetrisApplication.GameMode.SINGLE.toString());
	    startActivity(singlePlayerIntent);
	}

	public void onMultiPlayer(View v)
    {
	    // go to multiplayer menu screen if bluetooth is enabled
	    if (mDefaultAdapter.isEnabled())
	        startActivity(new Intent(this, MultiplayerMenuActivity.class));
	    else
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // check whether bluetooth was enabled
        if (requestCode == ENABLE_BT)
        {
            if (resultCode == RESULT_OK)
                startActivity(new Intent(this, MultiplayerMenuActivity.class));
            else
                Toast.makeText(this, "Multi-Player mode required Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }
	
    public void onScore(View v)
    {
        // to implement later
        Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
    }

    public void onHelp(View v)
    {
        // to implement later
        Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
    }
}