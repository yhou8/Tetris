package edu.illinois.tetris;

import java.util.*;
import android.app.*;

public class TetrisApplication extends Application
{
    // use for connecting with others running Tetris
    public static final UUID TETRIS_UUID = UUID.nameUUIDFromBytes("Tetris".getBytes());
    
    // use for storing in Bundle
    public static final String MODE_KEY = "mode";
    public static final String NAME_KEY = "name";
    public static final String DEVICE_KEY = "device";

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
}
