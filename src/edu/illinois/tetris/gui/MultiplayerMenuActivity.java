package edu.illinois.tetris.gui;

import java.io.*;
import java.util.*;
import android.app.*;
import android.bluetooth.*;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.*;
import edu.illinois.engr.courses.cs241.honors.tetris.*;

public class MultiplayerMenuActivity extends Activity
{
    private final static UUID SOCKET_UUID = UUID.randomUUID();
    public final static String SEESAW_MODE = "seesaw";
    public final static String SABOTAGE_MODE = "sabotage";
    public final static String MODE_KEY = "mode";
    
    private Button seesawMode;
    private Button sabotageMode;
    private ListView deviceListView;
    private ArrayAdapter<BluetoothDevice> deviceList; 

    // create view and set button listeners
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_menu_multiplayer);
        
        seesawMode = (Button)findViewById(R.id.seesaw_mode);
        seesawMode.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(MultiplayerMenuActivity.this, deviceListView.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
            }
        });
        
        sabotageMode = (Button)findViewById(R.id.sabotage_mode);
        sabotageMode.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(MultiplayerMenuActivity.this, deviceListView.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        deviceListView = (ListView)findViewById(R.id.connections_list);
        deviceListView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3)
            {
                Toast.makeText(MultiplayerMenuActivity.this, deviceList.getItem(arg2).getName(), Toast.LENGTH_SHORT).show();
                
            }
        });
        Set<BluetoothDevice> bondedDevicesSet = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        BluetoothDevice[] bondedDevices = new BluetoothDevice[0];
        if (bondedDevicesSet != null)
            bondedDevices = bondedDevicesSet.toArray(bondedDevices);
        
        deviceList = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1, bondedDevices)
        {
            // returns a TextView that displays the device's name and address
            @Override
            public View getView (int position, View convertView, ViewGroup parent)
            {
                BluetoothDevice device = this.getItem(position);
                TextView view = (TextView)super.getView(position, convertView, parent);
                view.setText(device.getName() + '@' + device.getAddress());
                return view;
            }
        };
        
        deviceListView.setAdapter(deviceList);
    }
    
    // fill list view with bonded devices
    @Override
    protected void onStart()
    {
        super.onStart();
    }
    
    // start server socket
    @Override
    protected void onResume()
    {
        super.onResume();
//        new ServerTask().execute("");
    }
    
    // stores the information needed to start a game
    private class GameParams
    {
        private String mode;
        private BluetoothDevice device;
        
        public GameParams(String mode, BluetoothDevice device)
        {
            this.mode = mode;
            this.device = device;
        }

        public String getMode()
        {
            return mode;
        }

        public BluetoothDevice getDevice()
        {
            return device;
        }
    }
    
    private class ServerTask extends AsyncTask<String, String, BluetoothSocket>
    {
        private BluetoothServerSocket serverSocket;
        private BluetoothSocket clientSocket;
        
        @Override
        protected BluetoothSocket doInBackground(String... params)
        {
            while (true)
            {
                try
                {
                    serverSocket = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord("Tetris", SOCKET_UUID);
                    while (true)
                    {
                        clientSocket = serverSocket.accept();
                        // read game mode, device name
                    }
                }
                catch (IOException e)
                {
                }
            }
        }
    }
    
    private class ClientTask extends AsyncTask<GameParams, String, BluetoothSocket>
    {
        private BluetoothDevice device;
        private BluetoothSocket clientSocket;
        
        @Override
        protected BluetoothSocket doInBackground(GameParams... params)
        {
            device = params[0].getDevice();

                    // attempt to connect to selected device 
            try
            {
                clientSocket = device.createRfcommSocketToServiceRecord(SOCKET_UUID);
                clientSocket.connect();
                // write game mode, maybe device name on stream
                return clientSocket;
            }
            catch (IOException e)
            {
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(BluetoothSocket result)
        {
            if (result == null)
                Toast.makeText(MultiplayerMenuActivity.this, "Cannot connect with " + device.getName() + '@' + device.getAddress(), Toast.LENGTH_SHORT).show();
            else
            {
                // start game
            }
        }
    }
}
