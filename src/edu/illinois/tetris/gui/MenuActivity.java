package edu.illinois.tetris.gui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import edu.illinois.engr.courses.cs241.honors.tetris.R;
import edu.illinois.engr.courses.cs241.honors.tetris.R.id;
import edu.illinois.engr.courses.cs241.honors.tetris.R.layout;
import edu.illinois.engr.courses.cs241.honors.tetris.R.menu;
import edu.illinois.tetris.model.Playfield;

public class MenuActivity extends Activity
{
	private Playfield field;
	private FieldView fieldView;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		
		field = new Playfield(20, 10);
		
		fieldView = (FieldView)findViewById(R.id.field_view);
		fieldView.setField(field);
		fieldView.setOnTouchListener(new FieldTouchListener(field, fieldView));
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.in_game_menu, menu);
		return true;
	}

}
