package edu.illinois.tetris.gui;

import edu.illinois.tetris.model.Playfield;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class FieldView extends View
{
	private Playfield field;
	
	public FieldView(Context context)
	{
		super(context);
		setBackgroundColor(Color.GRAY);
	}

	public FieldView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setBackgroundColor(Color.GRAY);
	}

	public FieldView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		setBackgroundColor(Color.GRAY);
	}

	public void setField(Playfield field)
	{
		this.field = field;
	}
	
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		Rect bound = new Rect();
		getDrawingRect(bound);
		if (field != null)
			field.draw(canvas, bound);
	}
}
