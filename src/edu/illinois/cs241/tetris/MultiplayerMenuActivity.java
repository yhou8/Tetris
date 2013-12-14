package edu.illinois.cs241.tetris;

import java.io.*;
import java.util.*;
import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.content.DialogInterface.OnCancelListener;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class MultiplayerMenuActivity extends Activity
{
    private BluetoothAdapter              mBTadapter;
    private ArrayAdapter<BluetoothDevice> mBTlistAdapter;
    private ListView                      mBTdeviceList;
    private ServerConnectionTask          mServerTask;
    private ClientConnectionTask          mClientTask;
    private BluetoothDevice               mChosenDevice;
    
    // private BluetoothSocket cclientSocket = null;
    // private BluetoothServerSocket sserverSocket = null;
    // private BluetoothSocket sclientSocket = null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_multiplayer);
        mBTadapter = BluetoothAdapter.getDefaultAdapter();
        mBTlistAdapter = new BluetoothDeviceListAdapter(this,
                android.R.layout.simple_list_item_single_choice);
        mBTdeviceList = (ListView) findViewById(R.id.devices_list);
        mBTdeviceList.setAdapter(mBTlistAdapter);
        mBTdeviceList.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                // here btd is the device
                mChosenDevice = (BluetoothDevice) parent
                        .getItemAtPosition(position);
            }
        });
    }
    
    // start server socket
    @Override
    protected void onResume()
    {
        super.onResume();
        // check if Bluetooth was disabled
        if (!mBTadapter.isEnabled())
        {
            Toast.makeText(this,
                    "Returning to menu because Bluetooth became disabled",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // reset list of bonded devices
        mBTlistAdapter.clear();
        Set<BluetoothDevice> bondedDevices = mBTadapter.getBondedDevices();
        if (bondedDevices != null)
        {
            for (BluetoothDevice btDevice : bondedDevices)
                mBTlistAdapter.add(btDevice);
        }
        if (mServerTask == null)
        {
            mServerTask = new ServerConnectionTask(this, mBTadapter);
            mServerTask.execute();
        }
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        if (mServerTask != null)
            mServerTask.cancel(true);
        mServerTask = null; 
        // if (mClientTask != null)
        // mClientTask.cancel(true);
    }
    
    public void seesawOnclick(View view)
    {
        if (mChosenDevice != null)
        {
            if (mServerTask != null)
                mServerTask.cancel(true);
            mClientTask = new ClientConnectionTask(this, mChosenDevice,
                    TetrisApplication.SEESAW_MODE);
            mClientTask.execute();
        }
        else
        {
            Toast.makeText(getApplicationContext(),
                    "Please select an opponent", Toast.LENGTH_SHORT).show();
        }
    }
    
    public void sabotageOnclick(View view)
    {
        if (mChosenDevice != null)
        {
            if (mServerTask != null)
                mServerTask.cancel(true);
            mClientTask = new ClientConnectionTask(this, mChosenDevice,
                    TetrisApplication.SABOTAGE_MODE);
            mClientTask.execute();
        }
        else
        {
            Toast.makeText(getApplicationContext(),
                    "Please select an opponent", Toast.LENGTH_SHORT).show();
        }
    }
    
    private class ServerConnectionTask extends
    AsyncTask<String, String, Boolean>
    {
        private final Context          mContext;
        private final BluetoothAdapter mBTadapter;
        private BluetoothServerSocket  mServerSocket;
        private BluetoothSocket        mConnectionSocket;
        private String                 mOppName, mGameMode;
        
        public ServerConnectionTask(Context context, BluetoothAdapter BTadapter)
        {
            mContext = context;
            mBTadapter = BTadapter;
        }
        
        // try accept connection
        // if connect, read name:mode, send name
        // start playactivity, maybe in ui thread
        @Override
        protected Boolean doInBackground(String... params)
        {
            try
            {
                // create socket to listen
                mServerSocket = mBTadapter.listenUsingRfcommWithServiceRecord(
                        "Tetris", TetrisApplication.TETRIS_UUID);
            }
            catch (IOException e)
            {
                return false;
            }
            
            // try connecting to client
            while (mConnectionSocket == null)
            {
                if (isCancelled())
                {
                    try {mServerSocket.close();} catch (Exception e){}
                    return false;
                }
                try
                {
                    mConnectionSocket = mServerSocket.accept(4000);
                    mServerSocket.close();
                }
                catch (IOException e){}
            }
            
            try
            {
                /*
                 * server trying to get from output stream
                 */
                DataInputStream inputStream = new DataInputStream(
                        mConnectionSocket.getInputStream());
                mOppName = inputStream.readUTF();
                mGameMode = inputStream.readUTF();
            }
            catch (IOException e)
            {
                try
                {
                    mConnectionSocket.getInputStream().close();
                    mConnectionSocket.getOutputStream().close();
                    mConnectionSocket.close();
                }
                catch (IOException ex){}
                
                return false;
            }
            return true;
        }
        
        @Override
        protected void onPostExecute(Boolean connected)
        {
            if (connected)
            { // if we get a connection
                // read game mode, device name
                // prompt the ultimate question, accept or not!?
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Challenge!");
                builder.setMessage(mOppName + " challenges you to " + mGameMode
                        + " mode!");
                builder.setPositiveButton(TetrisApplication.ACCEPT,
                        new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog,
                            int which)
                    {
                        try
                        {
                            // send reply and start game
                            DataOutputStream outputStream = new DataOutputStream(mConnectionSocket
                                    .getOutputStream());
                            outputStream.writeUTF(mBTadapter.getName());
                            outputStream.writeUTF(TetrisApplication.ACCEPT);
                            outputStream.flush();
                            Toast.makeText(mContext, "Here we will start the activity as server", Toast.LENGTH_SHORT).show();
                            TetrisApplication.setBTSocket(mConnectionSocket);
                            Intent multiPlayerIntent = new Intent(mContext, PlayActivity.class);
                            multiPlayerIntent.putExtra(TetrisApplication.NAME_KEY, mOppName);
                            multiPlayerIntent.putExtra(TetrisApplication.MODE_KEY, mGameMode);
                            startActivity(multiPlayerIntent);
                        }
                        catch (IOException e)
                        {
                        }
                    }
                });
                builder.setNegativeButton(TetrisApplication.REJECT, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog,
                            int which)
                    {
                        try
                        {
                            // send reply
                            DataOutputStream outputStream = new DataOutputStream(mConnectionSocket
                                    .getOutputStream());
                            outputStream.writeUTF(mBTadapter.getName());
                            outputStream.writeUTF(TetrisApplication.REJECT);
                            outputStream.flush();
                            mConnectionSocket.getInputStream().close();
                            mConnectionSocket.getOutputStream().close();
                            mConnectionSocket.close();
                            mServerTask = new ServerConnectionTask(mContext, mBTadapter);
                            mServerTask.execute();
                        }
                        catch (IOException e)
                        {
                        }
                    }
                });
                builder.setOnCancelListener(new OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        try
                        {
                            // send reply
                            DataOutputStream outputStream = new DataOutputStream(mConnectionSocket
                                    .getOutputStream());
                            outputStream.writeUTF(mBTadapter.getName());
                            outputStream.writeUTF(TetrisApplication.REJECT);
                            outputStream.flush();
                            mConnectionSocket.getInputStream().close();
                            mConnectionSocket.getOutputStream().close();
                            mConnectionSocket.close();
                            mServerTask = new ServerConnectionTask(mContext, mBTadapter);
                            mServerTask.execute();
                        }
                        catch (IOException e)
                        {
                        }
                    }
                });
                builder.create().show();
            }
            else
            {
                mServerTask = new ServerConnectionTask(mContext, mBTadapter);
                mServerTask.execute();
            }
        }
    }
    
    private class ClientConnectionTask extends
    AsyncTask<String, String, Boolean>
    {
        private final Context         mContext;
        private final BluetoothDevice mBTdevice;
        private final String          mGameMode;
        private String                mOppName;
        private BluetoothSocket       mClientSocket;
        private final AlertDialog     mDialog;
        private String mToastMessage;
        private Intent mGameIntent; 
        
        public ClientConnectionTask(Context context, BluetoothDevice device,
                String mode)
        {
            mContext = context;
            mBTdevice = device;
            mGameMode = mode;
            mDialog = new ProgressDialog(context);
        }
        
        @Override
        protected void onPreExecute()
        {
            mDialog.show();
        }
        
        @Override
        protected Boolean doInBackground(String... params)
        {
            try
            {
                // attempt to create socket and connect
                mClientSocket = mBTdevice
                        .createRfcommSocketToServiceRecord(TetrisApplication.TETRIS_UUID);
                mClientSocket.connect();
            }
            catch (IOException e)
            {
                if (mClientSocket != null)
                {
                    try{mClientSocket.close();} catch(IOException ex){}
                }
                mToastMessage = "Cannot connect with " + mBTdevice.getName() + '@'
                        + mBTdevice.getAddress();
                return false;
            }
            
            String response = null;
            
            try
            {
                // write name and mode, read opp name and response
                DataOutputStream outputStream = new DataOutputStream(
                        mClientSocket.getOutputStream());
                outputStream.writeUTF(mBTadapter.getName());
                outputStream.writeUTF(mGameMode);
                outputStream.flush();
                // read response, maybe interrupt if cannot get response in 4 sec
                DataInputStream inputStream = new DataInputStream(
                        mClientSocket.getInputStream());
                mOppName = inputStream.readUTF();
                response = inputStream.readUTF();
            }
            catch (IOException e)
            {
            }
            
            if (response == null || TetrisApplication.REJECT.equals(response))
            {
                try
                {
                    mClientSocket.getInputStream().close();
                    mClientSocket.getOutputStream().close();
                    mClientSocket.close();
                }
                catch (IOException ex){}
                
                mToastMessage = "Sorry, your opponent doesn't want to play :(";
                return false;
            }
            else
            {
                TetrisApplication.setBTSocket(mClientSocket);
                mGameIntent = new Intent(mContext, PlayActivity.class);
                mGameIntent.putExtra(TetrisApplication.MODE_KEY,
                        mGameMode);
                mGameIntent.putExtra(TetrisApplication.NAME_KEY, mOppName);
                return true;
            }
        }
        
        @Override
        protected void onPostExecute(Boolean result)
        {
            mDialog.dismiss();
            if (result)
                startActivity(mGameIntent);
            else
            {
                Toast.makeText(mContext, mToastMessage, Toast.LENGTH_SHORT).show();
                mServerTask = new ServerConnectionTask(mContext, mBTadapter);
                mServerTask.execute();
            }
        }
    }
}

class BluetoothDeviceListAdapter extends ArrayAdapter<BluetoothDevice>
{
    public BluetoothDeviceListAdapter(Context context, int resource)
    {
        super(context, resource);
    }
    
    // returns a TextView that displays the device's name and address
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        TextView view = (TextView)super.getView(position, convertView, parent);
        BluetoothDevice device = getItem(position);
        view.setText(device.getName() + '@' + device.getAddress());
        return view;
    }
}
