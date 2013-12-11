package edu.illinois.tetris.gui;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import edu.illinois.tetris.model.*;

public class PlayfieldView extends View
{
	private Playfield field;
	private Rect bounds;
	
	public PlayfieldView(Context context)
	{
		super(context);
		getDrawingRect(bounds);
	}

	public PlayfieldView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
        getDrawingRect(bounds);
	}

	public PlayfieldView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
        getDrawingRect(bounds);
	}

	public void setField(Playfield field)
	{
		this.field = field;
	}
	
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		if (field != null)
			field.draw(canvas, bounds);
	}
}
