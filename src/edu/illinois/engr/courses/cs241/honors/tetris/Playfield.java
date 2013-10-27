package edu.illinois.engr.courses.cs241.honors.tetris;

import android.graphics.*;
import android.util.SparseArray;

public class Playfield
{
	private int score;
	private int rows, cols;
	private int[][] field;
	private Tetromino curBlock;
	private SparseArray<Paint> paints;
	
	public Playfield(int rows, int cols)
	{
		this.rows = rows + 2;
		this.cols = cols;
		score = 0;
		field = new int[this.rows][this.cols];
		for (int r = 0; r < this.rows; r++)
			for (int c = 0; c < this.cols; c++)
				field[r][c] = Color.WHITE;
		
		int colors[] = Tetromino.colorSet;
		int numColors = colors.length;
		paints = new SparseArray<Paint>(numColors);
		Paint p;
		
		for (int i = 0; i < numColors; i++)
		{
			p = new Paint();
			p.setColor(colors[i]);
			paints.put(colors[i], p);
		}
		
		p = new Paint();
		p.setColor(Color.WHITE);
		paints.put(Color.WHITE, p);
	}
	
	public void newBlock()
	{
		if (!reachedTop())
		{
			curBlock = Tetromino.randBlock();
			curBlock.setCenter(new Point(cols / 2, 1));
		}
	}
	
	// rotate block until it reaches a valid orientation, might be original orientation
	public void rotateBlock()
	{
		if (curBlock != null && !reachedTop())
		do
		{
			curBlock.rotate();
		} while (!isValidPos());
	}
	
	public void moveBlockLeft()
	{
		if (curBlock != null && !reachedTop())
		{
			Point ctr = curBlock.getCenter();
			curBlock.setCenter(new Point(ctr.x - 1, ctr.y));
			if (!isValidPos())
				curBlock.setCenter(ctr);
		}
	}
	
	public void moveBlockRight()
	{
		if (curBlock != null && !reachedTop())
		{
			Point ctr = curBlock.getCenter();
			curBlock.setCenter(new Point(ctr.x + 1, ctr.y));
			if (!isValidPos())
				curBlock.setCenter(ctr);
		}
	}

	// locks block if it cannot move down
	public void moveBlockDown()
	{
		if (curBlock != null && !reachedTop())
		{
			Point ctr = curBlock.getCenter();
			curBlock.setCenter(new Point(ctr.x, ctr.y + 1));
			if (!isValidPos())
			{
				curBlock.setCenter(ctr);
				Point coords[] = curBlock.getCoords();
				int upperRow = rows, lowerRow = -1;
				for (int i = 0; i < coords.length; i++)
				{
					Point p = coords[i];
					field[p.y][p.x] = curBlock.getColor();
					upperRow = Math.min(upperRow, p.y);
					lowerRow = Math.max(lowerRow, p.y);
				}
				
				for (int i = upperRow; i <= lowerRow; i++)
				{
					boolean rowFilled = true;
					for (int j = 0; j < cols; j++)
					{
						if (field[i][j] == Color.WHITE)
							rowFilled = false;
					}
					
					if (rowFilled)
					{
						int[] clearedRow = field[i];
						
						for (int j = 0; j < cols; j++)
							clearedRow[j] = Color.WHITE;
						
						for (int j = i; j > 0; j--)
							field[j] = field[j - 1];
						
						field[0] = clearedRow;
						score++;
					}
				}
				
				curBlock = null;
			}
		}
	}
	
	// moves the block down until it locks
	public void dropBlock()
	{
		if (reachedTop())
			return;
		while (curBlock != null)
			moveBlockDown();
	}

	public int getScore()
	{
		return score;
	}
	
	public boolean reachedTop()
	{
		for (int i = 0; i < cols; i++)
		{
			if (field[0][i] != Color.WHITE || field[1][i] != Color.WHITE)
				return true;
		}
		return false;
	}
	
	public boolean hasBlock()
	{
		return curBlock != null;
	}
	
	// makes sure current block is in field and does not collide with other blocks
	private boolean isValidPos()
	{
		Point coords[] = curBlock.getCoords();
		for (int i = 0; i < coords.length; i++)
		{
			Point p = coords[i];
			if (p.x < 0 || p.x >= cols)
				return false;
			if (p.y < 0 || p.y >= rows)
				return false;
			if (field[p.y][p.x] != Color.WHITE)
				return false;
		}
		return true;
	}
	
	// pass in canvas to draw on and edges of what is visible
	// canvas's bounds do not correspond exactly with visible space
	public void draw(Canvas canvas, int left, int top, int width, int height)
	{
		float x0 = left, y0 = top;
		float length = height / (rows - 2);
		
		// field bounded by canvas width
		if (width / cols < length)
		{
			length = width / cols;
			y0 = y0 + (height - length * (rows - 2)) / 2;
		}
		else
		{
			x0 = x0 + (width - length * cols) / 2;
		}
		
		// draw field
		for (int r = 2; r < rows; r++)
		{
			for (int c = 0; c < cols; c++)
			{
				float x = x0 + length * c, y = y0 + length * (r - 2);
				canvas.drawRect(x, y, x + length, y + length, paints.get(field[r][c]));
			}
		}
		
		// draw current block
		if (curBlock != null)
		{
			Point coords[] = curBlock.getCoords();
			Paint curPaint = paints.get(curBlock.getColor());
			
			for (int i = 0; i < coords.length; i++)
			{
				Point p = coords[i];
				float x = x0 + length * p.x, y = y0 + length * (p.y - 2);
				canvas.drawRect(x, y, x + length, y + length, curPaint);
			}
		}
	}
}
