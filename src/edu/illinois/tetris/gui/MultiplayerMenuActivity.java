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
        
        sabotageMode = (Button)findViewById(R.id.sabotage_mode);

        deviceListView = (ListView)findViewById(R.id.connections_list);
        deviceListView.setOnItemClickListener(new OnItemClickListener()
        {
            // start client task 
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3)
            {
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
    
    // start server socket
    @Override
    protected void onResume()
    {
        super.onResume();
        new ServerTask().execute("");
    }
    
    private class ServerTask extends AsyncTask<String, String, BluetoothSocket>
    {
        private BluetoothServerSocket serverSocket;
        private BluetoothSocket clientSocket;
        
        // try accept connection
        // if connect, read name:mode, send name
        // start playactivity, maybe in ui thread
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
        // try connect as client
        // connection error, tell cannot connect, maybe range error
        // if connect, write mode: name
        // if server declines, stop and tell
        // else start playActivity, send mode, name, device
        protected BluetoothSocket doInBackground(GameParams... params)
        {
            device = params[0].getDevice();

                    // attempt to connect to selected device 
            try
            {
                clientSocket = device.createRfcommSocketToServiceRecord(SOCKET_UUID);
                clientSocket.connect();
                clientSocket.
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
