package edu.illinois.tetris.gui;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import edu.illinois.tetris.model.*;

public class PlayfieldView extends View
{
    private Playfield playfield;
    private Tetromino block;
	private Rect bounds;
	
	public PlayfieldView(Context context)
	{
		super(context);
        bounds = new Rect();
		getDrawingRect(bounds);
	}

	public PlayfieldView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
        bounds = new Rect();
        getDrawingRect(bounds);
	}

	public PlayfieldView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
        bounds = new Rect();
        getDrawingRect(bounds);
	}

	public void setPlayfield(Playfield field)
	{
		this.playfield = field;
	}
	
	public Playfield getPlayfield()
	{
	    return playfield;
	}

    public void setBlock(Tetromino block)
    {
        this.block = block;
        invalidate();
    }

    public Tetromino getBlock()
    {
        return block;
    }

    // draw the playfield and block on the canvas
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		if (playfield != null)
		{
            int rows = playfield.getRows(), cols = playfield.getCols();
	        float x0 = bounds.left, y0 = bounds.top;

	        float rowLength = (float)bounds.height() / rows;
	        float colLength = (float)bounds.width() / cols;
	        float cellLength;
	        
	        // make shorter length the size of each cell square and center drawing bounds
	        if (rowLength <= colLength)
	        {
	            cellLength = rowLength;
	            x0 += (bounds.width() - cellLength * cols) / 2;
	        }
	        else
	        {
	            cellLength = colLength;
	            y0 += (bounds.height() - cellLength * rows) / 2;
	        }
	        
	        // draw playfield
	        for (int r = 0; r < rows; r++)
	        {
	            for (int c = 0; c < cols; c++)
	            {
	                int color = playfield.getColor(r, c);
	                
	                // not transparent
	                if (color != 0)
	                {
	                    float x = x0 + cellLength * c, y = y0 + cellLength * r;
	                    canvas.drawRect(x, y, x + cellLength, y + cellLength, Tetromino.getPaint(color));
	                }
	            }
	        }

            // draw block
	        if (block != null)
	        {
    	        Paint paint = Tetromino.getPaint(block.getColor());
    	        Point[] pts = block.getCoords();
    	        for (int i = 0; i < pts.length; i++)
    	        {
    	            Point p = pts[i];
    	            
    	            // check if the part of block is inside bounds
    	            if (p.x >= 0 || p.x < cols || p.y >= 0 || p.y < rows)
    	            {
    	                float x = x0 + cellLength * p.x, y = y0 + cellLength * p.y;
    	                canvas.drawRect(x, y, x + cellLength, y + cellLength, paint);
    	            }
    	        }
	        }
		}
	}
}