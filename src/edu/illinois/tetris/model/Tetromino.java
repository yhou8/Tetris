package edu.illinois.tetris.model;

import android.graphics.Color;
import android.graphics.Point;

public class Tetromino implements Cloneable
{
	private static Tetromino blockTypes[];
	public static int colorSet[];
	
	private int color;
	private Point center;
	private Point relativeCoordinates[];
	private Point realCoordinates[];
	private boolean cornerCentered;
	
	static
	{
		colorSet = new int[] {Color.CYAN, Color.BLUE, Color.rgb(255, 127, 0), Color.YELLOW, Color.GREEN, Color.rgb(127, 0, 127), Color.RED};
		
		blockTypes = new Tetromino[7];
		blockTypes[0] = new Tetromino(Color.CYAN, true, new Point[] {new Point(-2, -1), new Point(-1, -1), new Point(1, -1), new Point(2, -1)});
		blockTypes[1] = new Tetromino(Color.BLUE, false, new Point[] {new Point(-1, -1), new Point(-1, 0), new Point(0, 0), new Point(1, 0)});
		blockTypes[2] = new Tetromino(Color.rgb(255, 127, 0), false, new Point[] {new Point(1, -1), new Point(-1, 0), new Point(0, 0), new Point(1, 0)});
		blockTypes[3] = new Tetromino(Color.YELLOW, true, new Point[] {new Point(-1, -1), new Point(1, -1), new Point(-1, 1), new Point(1, 1)});
		blockTypes[4] = new Tetromino(Color.GREEN, false, new Point[] {new Point(0, -1), new Point(1, -1), new Point(-1, 0), new Point(0, 0)});
		blockTypes[5] = new Tetromino(Color.rgb(127, 0, 127), false, new Point[] {new Point(0, -1), new Point(-1, 0), new Point(0, 0), new Point(1, 0)});
		blockTypes[6] = new Tetromino(Color.RED, false, new Point[] {new Point(-1, -1), new Point(0, -1), new Point(0, 0), new Point(1, 0)});
	}
	
	public static int[] getColorSet()
	{
		return colorSet;
	}
	
	// Creates a random tetromino block
	public static Tetromino randBlock()
	{
		try
		{
			int randInd = (int)(7 * Math.random());
			return (Tetromino)blockTypes[randInd].clone();
		}
		catch (CloneNotSupportedException e)
		{
		}
		return null;
	}
	
	private Tetromino(int color, boolean cornerCentered, Point coords[])
	{
		this.relativeCoordinates = coords;
		realCoordinates = new Point[4];
		this.cornerCentered = cornerCentered;
		this.color = color;
	}
	
	public void setCenter(Point p)
	{
		center = p;
		calcCoordinates();
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
		return realCoordinates;
	}
	
	// Rotate the block by 90 degree clockwise
	public void rotate()
	{
		for (int i = 0; i < 4; i++)
			relativeCoordinates[i] = new Point(-relativeCoordinates[i].y, relativeCoordinates[i].x);
		calcCoordinates();
	}

	// calculates the block's coordinates using its center and coordinates relative to center
	private void calcCoordinates()
	{
		if (!cornerCentered)
		{
			for (int i = 0; i < 4; i++)
				realCoordinates[i] = new Point(relativeCoordinates[i].x + center.x, relativeCoordinates[i].y + center.y);
		}
		else
		{
			for (int i = 0; i < 4; i++)
			{
				realCoordinates[i] = new Point();

				if (relativeCoordinates[i].x > 0)
					realCoordinates[i].x = relativeCoordinates[i].x - 1 + center.x;
				else
					realCoordinates[i].x = relativeCoordinates[i].x + center.x;
				
				if (relativeCoordinates[i].y > 0)
					realCoordinates[i].y = relativeCoordinates[i].y  - 1 + center.y;
				else
					realCoordinates[i].y = relativeCoordinates[i].y + center.y;
			}
		}
	}
}
