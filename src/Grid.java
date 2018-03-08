//==================================================================
//  Name:  Christina Yu 
//  Class: CS 351L
//  Date:  5/17/2015
//
//  The Grid class sets up the 10000 by 100000 grid as boolean[][], 
//  each cell has its position and its boolean value--true as alive, 
//  false as dead.
//==================================================================
public class Grid 
{
  private static final int NUM_OF_ROWS = 10000;
  private static final int NUM_OF_COLUMNS = 10000;
  private static final int SEEDS_PERCENTAGE = 50;
  private boolean[][] grid, nextGrid;

  public Grid() 
  {
	grid = new boolean[NUM_OF_ROWS][NUM_OF_COLUMNS];
	nextGrid = new boolean[NUM_OF_ROWS][NUM_OF_COLUMNS];
	grid = initializeGrid();
	
  }

//==================================================================
//  initializeGrid()
//  This method initialize the grid, randomly choose 50% of the cells 
//  to be alive and the rest cells to be dead.  
//  Parameter: None
//  Return: boolean[][]  
//==================================================================
  public boolean[][] initializeGrid()
  {
	boolean[][] newGrid = new boolean[NUM_OF_ROWS][NUM_OF_COLUMNS];
	for(int i = 0; i < NUM_OF_ROWS; i++)
	{
	  for(int j = 0; j < NUM_OF_COLUMNS; j++)
	  {
		if(Math.random()*100 < SEEDS_PERCENTAGE)
		{
		  newGrid[i][j] = true;
		  // Set the border cells always be false
		  newGrid[0][j] = newGrid[NUM_OF_ROWS-1][j] = newGrid[i][0]
				        = newGrid[i][NUM_OF_COLUMNS-1] = false;
		}
	  }
	}
//	newGrid[3][1] = newGrid[3][2] = newGrid[3][3] = newGrid[2][3] = newGrid[1][2] = true;
	
	return newGrid;
  }

//==================================================================
//  getGrid()
//  This method returns the current grid.  
//  Parameter: None
//  Return: boolean[][]  
//==================================================================
  public boolean[][] getGrid()
  {
	return grid;
  }

//==================================================================
//  toggleCell(int i, int j, boolean value)
//  This method change the cell status to the given boolean value.  
//  Parameter: None
//  Return: None  
//==================================================================
  public synchronized void toggleCell(int i, int j, boolean value)
  {
	grid[i][j] = value;
  }

  public boolean[][] getNextGrid()
  {
	return nextGrid;
  }
//==================================================================
//  nextGeneration(boolean[][] newGrid)
//  This method replace the old grid with the given new grid.  
//  Parameter: boolean[][] 
//  Return: None  
//==================================================================
  public synchronized void nextGeneration()
  {
	boolean[][] temp;
	temp = nextGrid;
	nextGrid = grid;
	grid = temp;
  }
  
  public synchronized void replaceGrid(boolean[][] newGrid)
  {
	nextGrid = newGrid;
  }
	

//==================================================================
//  getValue(int i, int j)
//  This method returns cell's boolean value accroding to the given
//  position.   
//  Parameter: int i, int j -- cell position 
//  Return: boolean -- cell's boolean value  
//==================================================================  
  public synchronized boolean getValue(int i, int j)
  {
	return grid[i][j];
  }

//==================================================================
//  getDisplayedGrid(int top, int left, int bottom, int right)
//  This method returns part of the grid which is displayed by the
//  600 by 600 panel.
//  Parameter: int top, int left, int bottom, int right-- starting
//  and ending position for the partial grid
//  Return: boolean[][] -- displayed Grid  
//==================================================================
  public synchronized boolean[][] getDisplayedGrid(int top, int left, int bottom, int right)
  {
	int x = 0;
	boolean[][] displayedGrid = new boolean[(bottom - top + 1)][(right - left + 1)];
	for(int i = top; i <= bottom; i++)
	{
	  for(int j = 0; j<displayedGrid[x].length; j++)
	  {
	    displayedGrid[x][j] = grid[i][left+j];
	  }
	  x++;
	}
	return displayedGrid;
  }
}
