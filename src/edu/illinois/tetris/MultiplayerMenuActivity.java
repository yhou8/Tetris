package edu.illinois.tetris;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import android.app.*;
import android.bluetooth.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.*;
import edu.illinois.engr.courses.cs241.honors.tetris.*;

public class MultiplayerMenuActivity extends Activity {
	private Button seesawMode;
	private Button sabotageMode;
	private ListView deviceListView;
	private ArrayAdapter<BluetoothDevice> deviceList;
	private BluetoothDevice choosedBTD = null;
	private String choosedMode = null;
	private BluetoothSocket cclientSocket = null;
	private BluetoothServerSocket sserverSocket = null;
	private BluetoothSocket sclientSocket = null;
	private ServerTask mySTask = null;
	private ClientTask myCTask = null;

	// hey

	@Override
	protected void onPause() {
		super.onPause();
		try {
			if (myCTask != null) {
				myCTask.cancel(true);
			}
			if (mySTask != null) {
				mySTask.cancel(true);
			}
			if (sclientSocket != null) {
				sclientSocket.close();
			}
			if (sserverSocket != null) {
				sserverSocket.close();
			}
			if (cclientSocket != null) {
				cclientSocket.close();
			}
		} catch (IOException e) {
			Log.d("myApp", "Error in onPause, when closing sockets");
		}
	}

	// create view and set button listeners
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_menu_multiplayer);

		seesawMode = (Button) findViewById(R.id.seesaw_mode);

		sabotageMode = (Button) findViewById(R.id.sabotage_mode);

		deviceListView = (ListView) findViewById(R.id.connections_list);
		deviceListView.setOnItemClickListener(new OnItemClickListener() {
			// start client task
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d("myApp", "Clicked!");
				choosedBTD = (BluetoothDevice) parent
						.getItemAtPosition(position);// here btd is the device
				// Toast.makeText(MultiplayerMenuActivity.this,
				// "Clicked " + choosedBTD.getName() + '@'
				// + choosedBTD.getAddress(), Toast.LENGTH_SHORT)
				// .show();
			}
		});

		Set<BluetoothDevice> bondedDevicesSet = BluetoothAdapter
				.getDefaultAdapter().getBondedDevices();
		BluetoothDevice[] bondedDevices = new BluetoothDevice[0];
		if (bondedDevicesSet != null)
			bondedDevices = bondedDevicesSet.toArray(bondedDevices);

		deviceList = new ArrayAdapter<BluetoothDevice>(this,
				android.R.layout.simple_list_item_single_choice, bondedDevices) {
			// returns a TextView that displays the device's name and address
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				BluetoothDevice device = this.getItem(position);
				TextView view = (TextView) super.getView(position, convertView,
						parent);
				view.setText(device.getName() + '@' + device.getAddress());
				return view;
			}
		};

		deviceListView.setAdapter(deviceList);
	}

	// start server socket
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("myApp", "onResume run");
		mySTask = new ServerTask();
		mySTask.execute("");
	}

	/*
	 * @Override public void onBackPressed() {
	 * 
	 * android.os.Process.killProcess(android.os.Process.myPid()); // This above
	 * line close correctly }
	 */

	private class ServerTask extends AsyncTask<String, String, String[]> {

		private final AlertDialog dialog = new ProgressDialog(
				MultiplayerMenuActivity.this);
		private boolean waitforcon = true;

		@Override
		protected void onPreExecute() {
			Log.d("myApp", "onPreexcute");
			try {
				sserverSocket = BluetoothAdapter.getDefaultAdapter()
						.listenUsingRfcommWithServiceRecord("Tetris",
								TetrisApplication.TETRIS_UUID);
			} catch (IOException e) {

			}

		}

		// try accept connection
		// if connect, read name:mode, send name
		// start playactivity, maybe in ui thread
		@Override
		protected String[] doInBackground(String... params) {
			String[] outputString = null;
			try {
				Log.d("myApp", "Server trying to receive connect");
				sclientSocket = sserverSocket.accept();

				InputStream inputStream = sclientSocket.getInputStream();
				Log.d("myApp", "Server got input Stream");
				/*
				 * server trying to get from output stream
				 */
				byte[] buffer = new byte[1024];
				int bytes = 0;
				StringBuilder curMsg = new StringBuilder();
				bytes = inputStream.read(buffer);
				curMsg.append(new String(buffer, 0, bytes));
				outputString = curMsg.toString().split(",");
				Log.d("myApp", "Server get input Stream " + outputString[0]
						+ " " + outputString[1]);
				// inputStream.close();
				waitforcon = false;
			} catch (IOException e) {
			}

			return outputString;
		}

		@Override
		protected void onPostExecute(String[] outputString) {
			if (outputString != null) { // if we get a connection
				final String QMode = outputString[0];
				final String QUser = outputString[1];

				// read game mode, device name
				// prompt the ultimate question, accept or not!?
				this.dialog.setMessage(QUser + " wants to play with you in "
						+ QMode + " mode, Do you want to accept?");
				this.dialog.setButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								waitforcon = false;
								try {
									// first send message to client
									OutputStream writeModeDeviceStream = sclientSocket
											.getOutputStream();
									writeModeDeviceStream.flush();
									String output = new String("Y");
									Log.d("myApp", "Client writing string"
											+ output);
									writeModeDeviceStream.write(output
											.getBytes());
								} catch (IOException e) {
									Log.d("myApp",
											"Error when trying to send message as server");
								}
								try {
									sserverSocket.close();
								} catch (IOException e) {
									Log.d("myApp",
											"In server when closing serversocket, error");
								}
								Toast.makeText(
										getApplicationContext(),
										"Here we will start the activity as server",
										Toast.LENGTH_SHORT).show();
								
								Intent multiPlayerIntent = new Intent(
										MultiplayerMenuActivity.this,
										PlayActivity.class);
								
								/*multiPlayerIntent.putExtra(
										TetrisApplication.MODE_KEY, QMode);
								multiPlayerIntent.putExtra(
										TetrisApplication.NAME_KEY, QUser);*/
								multiPlayerIntent.putExtra(
										TetrisApplication.MODE_KEY,TetrisApplication.SINGLE_MODE);
								startActivity(multiPlayerIntent);
								

							}
						});
				this.dialog.setButton2("No",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								waitforcon = true;
								try {
									// first send message to client
									OutputStream writeModeDeviceStream = sclientSocket
											.getOutputStream();
									writeModeDeviceStream.flush();
									String output = new String("N");
									Log.d("myApp", "Client writing string"
											+ output);
									writeModeDeviceStream.write(output
											.getBytes());
								} catch (IOException e) {
									Log.d("myApp",
											"Error when trying to send message as server");
								}
								// then close the serverSocket and start again
								try {
									sserverSocket.close();
								} catch (IOException e) {
									Log.d("myApp",
											"In server when closing serversocket, error");
								}
								new ServerTask().execute("");

							}
						});
				this.dialog.show();
			} else {
				try {
					sserverSocket.close();
				} catch (IOException e) {
					Log.d("myApp", "In server when closing serversocket, error");
				}
			}
		}
	}

	private class GameParams {
		private BluetoothDevice device;
		private String mode;

		public GameParams(BluetoothDevice device, String mode) {
			this.device = device;
			this.mode = mode;
		}

		public BluetoothDevice getDevice() {
			return this.device;
		}

		public String getMode() {
			return this.mode;
		}
	}

	private class ClientTask extends
			AsyncTask<GameParams, String, BluetoothSocket> {
		private BluetoothDevice device;

		private final AlertDialog dialog = new ProgressDialog(
				MultiplayerMenuActivity.this);

		@Override
		protected void onPreExecute() {
			this.dialog.setMessage("Waiting for server...");
			this.dialog.show();
		}

		@Override
		// try connect as client
		// connection error, tell cannot connect, maybe range error
		// if connect, write mode: name
		// if server declines, stop and tell
		// else start playActivity, send mode, name, device
		protected BluetoothSocket doInBackground(GameParams... params) {
			device = params[0].getDevice();
			String mode = params[0].getMode();

			// attempt to connect to selected device
			try {
				Log.d("myApp", "Client now try to connect");
				cclientSocket = device
						.createRfcommSocketToServiceRecord(TetrisApplication.TETRIS_UUID);
				cclientSocket.connect();
				Log.d("myApp", "Client Connected");
				// write game mode, maybe device name on stream
				OutputStream writeModeDeviceStream = cclientSocket
						.getOutputStream();
				writeModeDeviceStream.flush();
				String output = new String(mode + ","
						+ BluetoothAdapter.getDefaultAdapter().getName());
				Log.d("myApp", "Client writing string" + output);
				writeModeDeviceStream.write(output.getBytes());
				return cclientSocket;
			} catch (IOException e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(BluetoothSocket result) {

			if (result == null) {
				Toast.makeText(
						MultiplayerMenuActivity.this,
						"Cannot connect with " + device.getName() + '@'
								+ device.getAddress(), Toast.LENGTH_SHORT)
						.show();
				this.dialog.dismiss();
			}
				
			else {
				// check if the server wants to play, get input
				/*
				 * try { cclientSocket.connect(); } catch (IOException e) {
				 * Log.d("myApp",
				 * "in Client, the other side (server user) close");
				 * this.dialog.dismiss();
				 * Toast.makeText(getApplicationContext(),
				 * "The other user close the software",
				 * Toast.LENGTH_SHORT).show(); return; }
				 */
				String outputString = null;
				try {
					InputStream inputStream = cclientSocket.getInputStream();
					Log.d("myApp", "Client get input stream to check");
					byte[] buffer = new byte[1024];
					int bytes = 0;
					StringBuilder curMsg = new StringBuilder();
					bytes = inputStream.read(buffer);
					curMsg.append(new String(buffer, 0, bytes));
					outputString = curMsg.toString();
					Log.d("myApp", "Client get input stream to check: "
							+ outputString);
					inputStream.close();
				} catch (IOException e) {
					Log.d("myApp",
							"Error crash when client tries to get input stream "
									+ outputString);
				}
				if (outputString.equals("Y")) {
					this.dialog.dismiss();
					Toast.makeText(getApplicationContext(),
							"Here we will start the activity as client",
							Toast.LENGTH_SHORT).show();
					
					Intent multiPlayerIntent = new Intent(
							MultiplayerMenuActivity.this,
							PlayActivity.class);
					//do we need to close every thing?
					/*multiPlayerIntent.putExtra(
							TetrisApplication.MODE_KEY, QMode);
					multiPlayerIntent.putExtra(
							TetrisApplication.NAME_KEY, QUser);*/
					multiPlayerIntent.putExtra(
							TetrisApplication.MODE_KEY,TetrisApplication.SINGLE_MODE);
					startActivity(multiPlayerIntent);
					
					return;
				}
				if (outputString.equals("N")) {
					this.dialog.dismiss();
					Toast.makeText(getApplicationContext(),
							"Sorry he doesn't want to play :(",
							Toast.LENGTH_SHORT).show();
					return;
				}
				Log.d("myApp",
						"ERROR, in client, UNrecognized char in server's message");

			}

		}

	}

	public void seesawOnclick(View view) {
		if (choosedBTD != null) {
			myCTask = new ClientTask();
			myCTask.execute(new GameParams(choosedBTD, "seesaw"));
		} else {
			Toast.makeText(getApplicationContext(),
					"Please select you component", Toast.LENGTH_SHORT).show();
		}
	}

	public void sabotageOnclick(View view) {
		if (choosedBTD != null) {
			myCTask = new ClientTask();
			myCTask.execute(new GameParams(choosedBTD, "sabotage"));
		} else {
			Toast.makeText(getApplicationContext(),
					"Please select you component", Toast.LENGTH_SHORT).show();

		}
	}
}
