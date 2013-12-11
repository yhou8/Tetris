package edu.illinois.tetris.gui;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import edu.illinois.tetris.model.*;

public class SinglePieceView extends View
{
    private Tetromino nextPiece;
    private Rect bounds;
    
    public SinglePieceView(Context context)
    {
        super(context);
        getDrawingRect(bounds);
    }
    
    public SinglePieceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        getDrawingRect(bounds);
    }
    
    public SinglePieceView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        getDrawingRect(bounds);
    }

    public Tetromino getNextPiece()
    {
        return nextPiece;
    }

    public void setNextPiece(Tetromino nextPiece)
    {
        this.nextPiece = nextPiece;
        nextPiece.setCenter(new Point(1, 2));
        invalidate();
    }
    
    @Override
    public void draw(Canvas canvas)
    {
        super.draw(canvas);
        if (nextPiece != null)
            nextPiece.drawPiece(canvas, bounds, 4, 4);
    }

}
