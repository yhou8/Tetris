package edu.illinois.cs241.tetris;

import java.util.*;
import android.app.*;
import android.bluetooth.*;

public class TetrisApplication extends Application
{
    // use for connecting with others running Tetris
    public static final UUID TETRIS_UUID = UUID.nameUUIDFromBytes("edu.illinois.cs241.tetris.Tetris".getBytes());
    
    // use for storing in Bundle
    public static final String MODE_KEY = "edu.illinois.cs241.tetris.mode";
    public static final String NAME_KEY = "edu.illinois.cs241.tetris.name";
    public static final String FIELD_KEY = "edu.illinois.cs241.tetris.field";

    // represents mode of game
    public static final String SINGLE_MODE = "single";
    public static final String SEESAW_MODE = "seesaw";
    public static final String SABOTAGE_MODE = "sabotage";
    
    public static final String ACCEPT = "accept";
    public static final String REJECT = "reject";

    // messages for multiplayer mode
    public static final int UPDATE_FIELD = 0;
    public static final int CLEAR_ROW = 1;
    public static final int WIN = 2;
    public static final int LOSE = 3;
    public static final int ERROR = 4;
    
    // multiplayer game states
    public static final int WAITING = 0;
    public static final int IN_CONTROL = 1;
    public static final int HANDLING = 2;
    
    private static BluetoothSocket mBTSocket;

    public static BluetoothSocket getBTSocket()
    {
        return mBTSocket;
    }

    public static void setBTSocket(BluetoothSocket socket)
    {
        mBTSocket = socket;
    }
}
