package edu.illinois.tetris.model;

import android.graphics.*;

public class Playfield
{
	private int score;
	private int rows, cols;
	private int[][] field;
	private Tetromino curBlock, nextBlock;
	
	public Playfield(int rows, int cols)
	{
		this.rows = rows + 2;
		this.cols = cols;
		score = 0;
		
		field = new int[this.rows][this.cols];
		for (int r = 0; r < this.rows; r++)
			for (int c = 0; c < this.cols; c++)
				field[r][c] = Color.WHITE;
		
		curBlock = Tetromino.randBlock();
		curBlock.setCenter(new Point(cols / 2, 1));
		nextBlock = Tetromino.randBlock();
	}
	
	// rotate block until it reaches a valid orientation, might be original orientation
	public void rotateBlock()
	{
		if (!isGameOver())
		{
			do
			{
				curBlock.rotate();
			} while (!isValidPos());
		}
	}
	
	// does not move if moving causes collision 
	public void moveBlockLeft()
	{
		if (!isGameOver())
		{
			Point ctr = curBlock.getCenter();
			curBlock.setCenter(new Point(ctr.x - 1, ctr.y));
			if (!isValidPos())
				curBlock.setCenter(ctr);
		}
	}
	
	// does not move if moving causes collision 
	public void moveBlockRight()
	{
		if (!isGameOver())
		{
			Point ctr = curBlock.getCenter();
			curBlock.setCenter(new Point(ctr.x + 1, ctr.y));
			if (!isValidPos())
				curBlock.setCenter(ctr);
		}
	}

	// returns true if the block moved down, false if it locked or game is over
	public boolean moveBlockDown()
	{
		if (!isGameOver())
		{
			Point ctr = curBlock.getCenter();
			curBlock.setCenter(new Point(ctr.x, ctr.y + 1));
			if (!isValidPos())
			{
				curBlock.setCenter(ctr);
				lockBlock();
				return false;
			}
			return true;
		}
		return false;
	}
	
	// moves the block down until it locks
	public void dropBlock()
	{
		while (moveBlockDown());
	}

	public int getScore()
	{
		return score;
	}
	
	public boolean isGameOver()
	{
		return reachedTop();
	}
	
	// makes sure current block is in field and does not collide with other blocks
	private boolean isValidPos()
	{
		Point coords[] = curBlock.getCoords();
		for (int i = 0; i < coords.length; i++)
		{
			Point p = coords[i];
			// check coordinates are within the field
			if (p.x < 0 || p.x >= cols)
				return false;
			if (p.y < 0 || p.y >= rows)
				return false;
			if (field[p.y][p.x] != Color.WHITE)
				return false;
		}
		return true;
	}

	private void lockBlock()
	{
		Point coords[] = curBlock.getCoords();
		int upperRow = rows, lowerRow = -1;
		int rowsCleared = 0;

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
				rowsCleared++;
			}
		}

		changeScore(rowsCleared);
		
		// prepares next block
		if (!isGameOver())
		{
			curBlock = nextBlock;
			curBlock.setCenter(new Point(cols / 2, 1));
			nextBlock = Tetromino.randBlock();
		}
	}
	
	// changes score based on how many rows were cleared
	private void changeScore(int rowsCleared)
	{
		score += rowsCleared;
	}
	
	// returns whether the blocks have locked above the visible screen
	private boolean reachedTop()
	{
		for (int i = 0; i < cols; i++)
		{
			if (field[0][i] != Color.WHITE || field[1][i] != Color.WHITE)
				return true;
		}
		return false;
	}
	
	// draw the field on the given canvas in the bounds defined by the given rect
	public void draw(Canvas canvas, Rect bounds)
	{
		float x0 = bounds.left, y0 = bounds.top;
		float length = bounds.height() / (rows - 2);
		
		// field bounded by canvas width
		if (bounds.width() / cols < length)
		{
			length = bounds.width() / cols;
			y0 = y0 + (bounds.height() - length * (rows - 2)) / 2;
		}
		else
		{
			x0 = x0 + (bounds.width() - length * cols) / 2;
		}
		
		// draw field
		for (int r = 2; r < rows; r++)
		{
			for (int c = 0; c < cols; c++)
			{
				float x = x0 + length * c, y = y0 + length * (r - 2);
				canvas.drawRect(x, y, x + length, y + length, Tetromino.paints.get(field[r][c]));
			}
		}
		
		// draw current block
		if (curBlock != null)
		    curBlock.drawPiece(canvas, bounds, rows - 2, cols);
	}
}
