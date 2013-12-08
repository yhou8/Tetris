package edu.illinois.tetris.gui;

import android.app.*;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import edu.illinois.engr.courses.cs241.honors.tetris.*;

public class MultiplayerMenuActivity extends Activity
{
    private Button seesawMode;
    private Button sabotageMode;
    private ListView connectionsList;

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
            }
        });
        
        sabotageMode = (Button)findViewById(R.id.sabotage_mode);
        sabotageMode.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            }
        });

        connectionsList = (ListView)findViewById(R.id.connections_list);
        connectionsList.getAdapter();
    }
}
