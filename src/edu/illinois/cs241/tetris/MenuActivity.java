package edu.illinois.cs241.tetris;

import java.io.DataOutputStream;
import java.io.IOException;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;


public class MenuActivity extends Activity
{
    private static final int ENABLE_BT = 0;
    private BluetoothAdapter mDefaultAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        // disable multiplayer option if bluetooth is not supported
        mDefaultAdapter = BluetoothAdapter.getDefaultAdapter();
        findViewById(R.id.multi_player_button).setEnabled(
                mDefaultAdapter != null);
    }
    
    public void onSinglePlayer(View v)
    {
        // tells game activity to start on single player mode
        Intent singlePlayerIntent = new Intent(this, PlayActivity.class);
        singlePlayerIntent.putExtra(TetrisApplication.MODE_KEY,
                TetrisApplication.SINGLE_MODE);
        startActivity(singlePlayerIntent);
    }
    
    public void onMultiPlayer(View v)
    {
        // ask to enable bluetooth if not enabled, else to to multiplayer menu
        if (mDefaultAdapter.isEnabled())
            startActivity(new Intent(this, MultiplayerMenuActivity.class));
        else
        {
            Intent enableBTIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, ENABLE_BT);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // go to multiplayer menu if bluetooth enabled
        if (requestCode == ENABLE_BT)
        {
            if (resultCode == RESULT_OK)
                startActivity(new Intent(this, MultiplayerMenuActivity.class));
            else
                Toast.makeText(this, "Multi-Player mode required Bluetooth",
                        Toast.LENGTH_SHORT).show();
        }
    }
    
    
    public void onHelp(View v)
    {
    	startActivity(new Intent(this, HelpActivity.class));
    }
    public void onCredit (View v)
    {
    	  AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	  builder.setMessage("Built By:\nYusheng Hou and Jiaxin Lin\n(Press back button to dismiss)");
    	  builder.create().show();
    }
}
