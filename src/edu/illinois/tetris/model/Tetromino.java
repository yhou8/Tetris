package edu.illinois.tetris.model;

import android.graphics.*;
import android.util.*;

public class Tetromino implements Cloneable
{
	private static Tetromino blockTypes[];
	private static int colorSet[];
	private static SparseArray<Paint> paints;
	
	private int color;
	private Point center;
	private Point relativePts[];
	private Point realPts[];
	private boolean cornerCentered;
	
	static
	{
		colorSet = new int[] {Color.CYAN, Color.BLUE, Color.rgb(255, 127, 0), Color.YELLOW, Color.GREEN, Color.rgb(127, 0, 127), Color.RED};

		blockTypes = new Tetromino[7];
		blockTypes[0] = new Tetromino(colorSet[0], true, new Point[] {new Point(-2, 1), new Point(-1, 1), new Point(1, 1), new Point(2, 1)});
		blockTypes[1] = new Tetromino(colorSet[1], false, new Point[] {new Point(-1, -1), new Point(-1, 0), new Point(0, 0), new Point(1, 0)});
		blockTypes[2] = new Tetromino(colorSet[2], false, new Point[] {new Point(1, -1), new Point(-1, 0), new Point(0, 0), new Point(1, 0)});
		blockTypes[3] = new Tetromino(colorSet[3], true, new Point[] {new Point(-1, -1), new Point(1, -1), new Point(-1, 1), new Point(1, 1)});
		blockTypes[4] = new Tetromino(colorSet[4], false, new Point[] {new Point(0, -1), new Point(1, -1), new Point(-1, 0), new Point(0, 0)});
		blockTypes[5] = new Tetromino(colorSet[5], false, new Point[] {new Point(0, -1), new Point(-1, 0), new Point(0, 0), new Point(1, 0)});
		blockTypes[6] = new Tetromino(colorSet[6], false, new Point[] {new Point(-1, -1), new Point(0, -1), new Point(0, 0), new Point(1, 0)});
		
		paints = new SparseArray<Paint>(colorSet.length);
        for (int i = 0; i < colorSet.length; i++)
        {
            Paint p = new Paint();
            p.setColor(colorSet[i]);
            paints.put(colorSet[i], p);
        }
	}
	
	// returns Paint associated with given color
    public static Paint getPaint(int color)
    {
        return paints.get(color);
    }
    
	// Creates a random tetromino block
	public static Tetromino randBlock()
	{
        int randIdx = (int)(blockTypes.length * Math.random());
		try
		{
			return (Tetromino)blockTypes[randIdx].clone();
		}
		catch (CloneNotSupportedException e)
		{
		    // Tetromino supports clone
	        return null;
		}
	}
	
    public Tetromino(int color, boolean cornerCentered, Point coords[])
    {
        relativePts = coords;
        realPts = new Point[coords.length];
        this.cornerCentered = cornerCentered;
        this.color = color;
        center = new Point();
        updateCoords();
    }
    
	@Override
    protected Object clone() throws CloneNotSupportedException
    {
        return new Tetromino(color, cornerCentered, relativePts);
    }

	public void setCenter(Point p)
	{
		center = p;
		updateCoords();
	}
	
	public Point getCenter()
	{
		return center;
	}
	
	public int getColor()
	{
		return color;
	}

	public Point[] getCoords()
	{
		return realPts;
	}
	
	// Rotate the block by 90 degree clockwise
	public void rotateCW()
	{
		for (int i = 0; i < relativePts.length; i++)
			relativePts[i] = new Point(-relativePts[i].y, relativePts[i].x);
		updateCoords();
	}

	// calculates the block's coordinates using its center and coordinates relative to center
	public void updateCoords()
	{
		if (!cornerCentered)
		{
			for (int i = 0; i < realPts.length; i++)
				realPts[i] = new Point(relativePts[i].x + center.x, relativePts[i].y + center.y);
		}
		else
		{
			for (int i = 0; i < realPts.length; i++)
			{
				realPts[i] = new Point();

				if (relativePts[i].x > 0)
                    realPts[i].x = relativePts[i].x + center.x;
				else
                    realPts[i].x = relativePts[i].x - 1 + center.x;
				
				if (relativePts[i].y < 0)
                    realPts[i].y = relativePts[i].y + center.y;
				else
                    realPts[i].y = relativePts[i].y - 1 + center.y;
			}
		}
	}
}
