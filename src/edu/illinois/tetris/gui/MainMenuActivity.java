package edu.illinois.tetris.gui;

import edu.illinois.engr.courses.cs241.honors.tetris.R;
import edu.illinois.engr.courses.cs241.honors.tetris.R.layout;
import edu.illinois.engr.courses.cs241.honors.tetris.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.content.Intent;

public class MainMenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
	    return true;
	}
	public void startPlaying(View view) {
		Intent toPlay = new Intent(this,MenuActivity.class);
		startActivity(toPlay);
	}
	

}
