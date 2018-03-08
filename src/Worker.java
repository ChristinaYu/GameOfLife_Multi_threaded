//==================================================================
//  Name:  Christina Yu 
//  Class: CS 351L
//  Date:  5/17/2015
//
//  The Worker class takes the given 10000 by 10000 grid, separates 
//  it into multiple row blocks and creates the same number of 
//  threads. The threads calculate the number of neighbors for each 
//  cell in their parallel block, generate the next generation for 
//  the grid and replace the old grid with the new one.
//==================================================================
public class Worker extends Thread
{
  private static final int NUM_OF_ROWS = 10000;
  private static final int NUM_OF_COLUMNS = 10000;
  private boolean[][] currentGrid,nextGrid;//2 copies of grid
  private boolean isPaused = false;
  private int numOfThread = 1;
  private Grid grid;
  private GameOfLife delegate;

//==================================================================
//  Constructor takes 2 parameters:
//  Gird panel and GameOfLife delegate
//==================================================================  
  public Worker(Grid grid, GameOfLife delegate)
  {
	this.grid = grid;
	this.delegate = delegate;
  }

//==================================================================
//  run() 
//  It runs the play method accroding to the status of the buttons
//  Parameter: None
//  Return: None
//==================================================================
  @Override
  public void run()
  {
	while(!isInterrupted())
	{
	  if(isPaused && delegate.getIsNextButton())
	  { //while paused and next button is clicked
		play();
		delegate.setIsNextButton(false);
	  }
	  while(isPaused == false) 
	  {//while it's running
		play();
	  }
	}
  }
	
//==================================================================
//  play() 
//  It separates the grid into multiple row blocks accroding to the 
//  number of threads the user chose, starts the same number of 
//  workers to work parallelly.
//  Parameter: None
//  Return: None
//==================================================================
  private void play()
  {
//	System.out.println("work.play()");
	currentGrid = grid.getGrid();
	nextGrid = grid.getNextGrid();	
	ParallelWorker[] parallelWorkers = new ParallelWorker[numOfThread];
	int blockSize = NUM_OF_ROWS / numOfThread;
	int rowStart, rowEnd;		
	for(int i = 0; i < numOfThread; i++)
	{
	  rowStart = i * blockSize;
	  if(i == numOfThread - 1) rowEnd = NUM_OF_ROWS;
	  else rowEnd = (i+1) * blockSize;
	  parallelWorkers[i] = new ParallelWorker(rowStart, rowEnd);
	  parallelWorkers[i].start();
	}
	
	for(int i = 0; i < parallelWorkers.length; i++)
	{
	  try 
	  {
		parallelWorkers[i].join();
	  } 
	  catch (InterruptedException e) 
	  {
		e.printStackTrace();
	  }
	}
			
//	grid.replaceGrid(currentGrid);
	grid.nextGeneration();

	delegate.repaintGrid();
  }

//==================================================================
//  setPause()
//  This method sets the boolean isPaused to true when the pause 
//  button is clicked.
//  Parameter: None
//  Return: None
//==================================================================
  public synchronized void setPause()
  {
	isPaused = true;
  }

//==================================================================
//  setNumOfThread(int value)
//  This method sets the int numOfThread to the given value.
//  Parameter: int vaue
//  Return: None
//==================================================================
  public void setNumOfThread(int value)
  {
	numOfThread = value;
  }

//==================================================================
//  The ParallelWorker class handles calculating the next generation 
//  in the given block.
//==================================================================
  class ParallelWorker extends Thread
  {
	private int rowStart, rowEnd;
	public ParallelWorker(int rowStart, int rowEnd)
	{
	  this.rowStart = rowStart;
	  this.rowEnd = rowEnd;
	}
	
	@Override
	public void run() 
	{
	  //go through the row block
	  for(int i = rowStart; i < rowEnd; i++)
	  {
	    for(int j = 0; j < NUM_OF_COLUMNS; j++)
	    {
		  int neighbors = 0;
		  //go through each cell
		  for(int x = Math.max(0, i - 1); x < Math.min(i + 2, NUM_OF_ROWS); x++)
		  {
		    for(int y = Math.max(0, j - 1); y < Math.min(j + 2, currentGrid[i].length); y++)
		    {
			  if(x == i && y == j) continue;
			  if(currentGrid[x][y]) neighbors++;
		    }
		  }
		  // Set the border cells never update
		  nextGrid[0][j] = nextGrid[NUM_OF_ROWS-1][j] = nextGrid[i][0]
		            = nextGrid[i][NUM_OF_COLUMNS-1] = false;
	    if(neighbors < 2 || neighbors > 3) nextGrid[i][j] = false;
	    else if(neighbors == 3) nextGrid[i][j] = true;
	    else nextGrid[i][j] = currentGrid[i][j];
		  
	    }
	  }
	}
  } 
}
