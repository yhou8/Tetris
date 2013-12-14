package edu.illinois.cs241.tetris;

import java.io.*;
import java.util.*;
import android.app.*;
import android.bluetooth.*;

public class TetrisApplication extends Application
{
    // use for connecting with others running Tetris
    public static final UUID TETRIS_UUID = UUID.nameUUIDFromBytes("edu.illinois.tetris.Tetris".getBytes());
    
    // use for storing in Bundle
    public static final String MODE_KEY = "edu.illinois.tetris.mode";
    public static final String NAME_KEY = "edu.illinois.tetris.name";

    // represents mode of game
    public static final String SINGLE_MODE = "single";
    public static final String SEESAW_MODE = "seesaw";
    public static final String SABOTAGE_MODE = "sabotage";
    
    // messages to send in multiplayer mode
    public static final String CLEAR_ROW = "clear";
    public static final String WIN = "win";
    public static final String LOSE = "lose";
    
    public static final String ACCEPT = "accept";
    public static final String REJECT = "reject";
    
    private static BluetoothSocket mBTSocket;

    public static BluetoothSocket getBTSocket()
    {
        return mBTSocket;
    }

    public static void setBTSocket(BluetoothSocket socket)
    {
        if (mBTSocket != null)
        {
            try
            {
                mBTSocket.close();
            }
            catch (IOException ex)
            {
            }
        }
        mBTSocket = socket;
    }
}
