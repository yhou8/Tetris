package edu.illinois.tetris.gui;

import java.util.*;
import android.app.*;

public class TetrisApplication extends Application
{
    public static final UUID TETRIS_UUID = UUID.nameUUIDFromBytes("Tetris".getBytes());
    public static final String MODE_KEY = "mode";
    public static final String NAME_KEY = "name";
    public static final String DEVICE_KEY = "device";
    
    public enum GameMode
    {
        SINGLE,
        SEESAW,
        SABOTAGE;
    };
}
