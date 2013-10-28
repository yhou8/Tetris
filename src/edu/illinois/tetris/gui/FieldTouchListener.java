package edu.illinois.tetris.gui;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import edu.illinois.tetris.model.Playfield;

public class FieldTouchListener implements OnTouchListener
{
	private Playfield field;
	private float downX, downY;
	private Handler handler;
	private Runnable blockDownAction;
	private View fieldView;
	
	public FieldTouchListener(Playfield field, View fieldView)
	{
		this.field = field;
		this.fieldView = fieldView;
		if (field != null)
			setUpTimer();
	}
	
	private void setUpTimer()
	{
		handler = new Handler();
		blockDownAction = new Runnable()
		{
			public void run()
			{
				field.moveBlockDown();
				fieldView.invalidate();
				if (!field.isGameOver())
					handler.postDelayed(blockDownAction, 1000);
			}
		};
		handler.postDelayed(blockDownAction, 1000);
	}

	@Override
	public boolean onTouch(View v, MotionEvent e)
	{
		if (field == null)
			return false;
		// stores where the finger touches the screen
		if (e.getActionMasked() == MotionEvent.ACTION_DOWN)
		{
			downX = e.getRawX();
			downY = e.getRawY();
		}

		if (e.getActionMasked() == MotionEvent.ACTION_UP)
		{
			float dX = e.getRawX() - downX, dXabs = Math.abs(dX), dY = e.getRawY() - downY, dYabs = Math.abs(dY);
			int minDist = Math.min(v.getWidth(), v.getHeight()) / 3;
			
			// check if finger moved more horizontally and moved long distance	
			if (dXabs / dYabs >= 2 && dXabs >= minDist)
			{
				// check finger moved right
				if (dX > 0)
					field.moveBlockRight();
				else
					field.moveBlockLeft();
			}
			else if (dYabs / dXabs >= 2 && dYabs >= minDist)
			{
				// check finger moved down
				if (dY > 0)
					field.dropBlock();
				else
					field.rotateBlock();
			}
			else if (dX == 0 && dY == 0)
				field.moveBlockDown();
			
			fieldView.invalidate();
		}
			
		return true;
	}
}
