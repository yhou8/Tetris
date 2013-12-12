package edu.illinois.tetris.model;

import android.graphics.*;

public class Playfield
{
	private int rows, hidRows, cols;
	private int[][] field;
//	private Tetromino curBlock, nextBlock;
	
	public Playfield(int rows, int cols)
	{
	    this(rows, cols, 0);
	}
	
	public Playfield(int rows, int cols, int hiddenRows)
	{
        this.rows = rows;
        this.cols = cols;
        
        // set all colors to transparent
        field = new int[rows + hidRows][cols];
        for (int r = 0; r < rows + hidRows; r++)
            for (int c = 0; c < this.cols; c++)
                field[r][c] = 0;
	}
	
	public int getRows()
    {
        return rows;
    }

    public int getHiddenRows()
    {
        return hidRows;
    }

    public int getCols()
    {
        return cols;
    }

	public int getColor(int row, int col)
	{
	    return field[row + hidRows][col];
	}
	
	// checks whether the block can be inserted into the field
    public boolean canInsert(Tetromino block)
    {
        Point coords[] = block.getCoords();
        for (int i = 0; i < coords.length; i++)
        {
            Point p = coords[i];
            
            // check coordinates are within the field
            if (p.x < 0 || p.x >= cols)
                return false;
            if (p.y + hidRows < 0 || p.y + hidRows >= rows)
                return false;
            if (field[p.y + hidRows][p.x] != 0)
                return false;
        }
        return true;
    }
    
    // inserts the block into the field and return how many rows was cleared
    public int insertBlock(Tetromino block)
    {
        int upperRow = hidRows + rows; 
        int lowerRow = -1;
        Point coords[] = block.getCoords();

        // set colors in field to same colors as block, find row span of piece
        for (int i = 0; i < coords.length; i++)
        {
            Point p = coords[i];
            field[p.y + hidRows][p.x] = block.getColor();
            upperRow = Math.min(upperRow, p.y + hidRows);
            lowerRow = Math.max(lowerRow, p.y + hidRows);
        }
        
        // check whether block completed any rows
        int rowsCleared = 0;
        for (int r = upperRow; r <= lowerRow; r++)
        {
            boolean rowFilled = true;
            for (int c = 0; c < cols; c++)
            {
                if (field[r][c] == 0)
                    rowFilled = false;
            }
            
            if (rowFilled)
            {
                int[] clearedRow = field[r];
                
                // set cleared row's color to transparent
                for (int c = 0; c < cols; c++)
                    clearedRow[c] = 0;
                
                // shift above rows down 1
                for (int i = r; i > 0; i--)
                    field[i] = field[i - 1];
                
                field[0] = clearedRow;
                rowsCleared++;
            }
        }
        
        return rowsCleared;
    }

    // returns whether there are blocks in hidden rows
    public boolean reachedTop()
    {
    	for (int r = 0; r < hidRows; r++)
    	{
    	    for (int c = 0; c < cols; c++)
    	    {
    			if (field[r][c] != 0)
    				return true;
    	    }
    	}
    	return false;
    }

}
