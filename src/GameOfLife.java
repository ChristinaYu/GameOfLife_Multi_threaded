//==================================================================
//  Name:  Christina Yu 
//  Class: CS 351L
//  Date:  5/17/2015
// 
//  GameOfLife class generates a JFrame which contains the grid panel 
//  to display the cells, the button panel for the game control and a
//  scrollPanel to scroll window through full grid using arrow keys. 
//  When the game is running, it creates thread(s) in given number
//  to saparate the 10000 by 10000 grid into multiple row blocks to 
//  work row parallelly.  
//==================================================================
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;

public class GameOfLife extends JFrame
{
  static GameOfLife gameOfLife;
  private static final int NUM_OF_ROWS = 10000;
  private static final int NUM_OF_COLUMNS = 10000;
  private JPanel mainPanel, gridPanel, buttonPanel, scrollPanel;
  private JButton startButton, nextButton, resetButton;
  private JScrollBar hLevel, vLevel;
  private int cellSize = 5;
  private int numOfThread = 1;
  //if it's first time start, create workers
  private boolean isStarted = false;
  private boolean isNextButton = false;
  private boolean updateValue; //for toggling cell
  private Grid grid;
  private Worker worker;
  
  
  //For arrow keys 
  private Action left = new AbstractAction()
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
//      System.out.println("left");
      hLevel.setValue(Math.min(hLevel.getValue() - 10, 
	                  hLevel.getMaximum() - hLevel.getWidth()));
	  hLevel.setVisibleAmount(hLevel.getWidth());
    }
  };
  private Action right = new AbstractAction()
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
//      System.out.println("right");
      hLevel.setValue(Math.min(hLevel.getValue() + 10, 
    		          hLevel.getMaximum() - hLevel.getWidth()));
	  hLevel.setVisibleAmount(hLevel.getWidth());
    }
  };
  private Action up = new AbstractAction()
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
//      System.out.println("up");
      vLevel.setValue(Math.min(vLevel.getValue() - 10, 
      	              vLevel.getMaximum() - vLevel.getHeight()));
	  vLevel.setVisibleAmount(vLevel.getHeight());
    }
  };
  private Action down = new AbstractAction()
  {
    @Override
    public void actionPerformed(ActionEvent e)
    {
//    	System.out.println("down");
      vLevel.setValue(Math.min(vLevel.getValue() + 10, 
      	              vLevel.getMaximum() - vLevel.getHeight()));
	  vLevel.setVisibleAmount(vLevel.getHeight());
    }
  };

//==================================================================
//  The main method creates an GameOfLife object and launch the game.
//==================================================================
  public static void main(String[] args) 
  {
	SwingUtilities.invokeLater(new Runnable() 
	{
	  public void run() 
	  {
		GameOfLife gameOfLife = new GameOfLife();
	  }
	});
  }

//==================================================================
//  Constructor starts to create the grid panel and button panel and
//  their commponents.
//==================================================================
  public GameOfLife() 
  {
	setVisible(true);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(200, 100, 600, 600);
	setBackground(Color.GRAY);
	setResizable(false);
    grid = new Grid();
	gameOfLife = this;
	mainPanel = new JPanel();
	mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
	mainPanel.setLayout(new BorderLayout(0, 0));
	mainPanel.setBackground(Color.GRAY);
	setContentPane(mainPanel);
	
    initButtonPanel();
    initGridAndScrollPanel();
    
  }

//==================================================================
//  initButtonPanel()
//  Creates JButton components
//  Parameter: None
//  Return: None
//==================================================================  
  private void initButtonPanel()
  {
	buttonPanel = new JPanel();
	FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
	flowLayout.setAlignment(FlowLayout.LEFT);
	buttonPanel.setBackground(Color.GRAY);
	mainPanel.add(buttonPanel, BorderLayout.NORTH);
		
	//creates start/pause button
	startButton = new JButton("Start");
	startButton.addActionListener(new ActionListener() 
	{
	  @Override
	  public void actionPerformed(ActionEvent e) 
	  {
		if(isStarted)//workerThreads has already created
		{
		  isStarted = false;
		  startButton.setText("Start");
		  worker.setPause();
		}
		else startGame();
	  }
	});
	buttonPanel.add(startButton);
		
	//create next button
	nextButton = new JButton("Next");
	nextButton.addActionListener(new ActionListener() 
	{
	  @Override
	  public void actionPerformed(ActionEvent e) 
	  {
		if(!isStarted)
		{
		  isNextButton = true;
		  repaintGrid();
		}
	  }
	});
	buttonPanel.add(nextButton); 
		
	//create reset button
	resetButton = new JButton("Reset");
	resetButton.addActionListener(new ActionListener() 
	{
	  @Override
	  public void actionPerformed(ActionEvent e) 
	  {
		isStarted = false;
		startButton.setText("Start");
		worker.setPause();
		grid.replaceGrid(grid.initializeGrid());
		hLevel.setValue(0);
		vLevel.setValue(0);
		repaintGrid();
	  }
	});
	buttonPanel.add(resetButton);
	
	JRadioButton rad1 = new JRadioButton("Single Thread", true);
    JRadioButton rad2 = new JRadioButton("Multi-Thread");
    rad1.setOpaque(false);
    rad2.setOpaque(false);
    rad1.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e) 
      {
        numOfThread = 1;
        if(worker != null) worker.setNumOfThread(numOfThread);
      } 
    });
    rad2.addItemListener(new ItemListener() 
    {
      public void itemStateChanged(ItemEvent e) 
      {
    	numOfThread = 4;
    	if(worker != null) worker.setNumOfThread(numOfThread);
      } 
    });
    ButtonGroup group = new ButtonGroup();
    group.add(rad1);
    group.add(rad2);
    buttonPanel.add(rad1);
    buttonPanel.add(rad2);
	buttonPanel.setFocusable(false);
  }
  
//==================================================================
//  initGridAndScrollPanel()
//  This method creates the gridPanel and handles toggling the cells
//  Parameter: None
//  Return: None
//==================================================================  
  private void initGridAndScrollPanel()
  {
	//create scroll panel
	scrollPanel = new JPanel();
	mainPanel.add(scrollPanel, BorderLayout.CENTER);
	scrollPanel.requestFocus();
	GridBagLayout gbl = new GridBagLayout();
	scrollPanel.setLayout(gbl);
	
	//create grid panel
	gridPanel = new GridPanel(); 
	gridPanel.setBackground(Color.GRAY);
	gridPanel.addMouseListener(new MouseListener() 
	{
	  @Override
	  public void mouseReleased(MouseEvent e) {}
		
	  @Override
	  public void mousePressed(MouseEvent e) 
	  {//handle toggling cells
		  
		int x = e.getX() + hLevel.getValue();
		int y = e.getY() + vLevel.getValue();
		if(x >= 0 && x < hLevel.getMaximum() && y >= 0 && 
		   y < vLevel.getMaximum() && !isStarted)
		{
		  int i = y / cellSize;
		  int j = x / cellSize;
		  updateValue = !grid.getValue(i, j);
		  grid.toggleCell(i, j, updateValue);
		  gridPanel.repaint();
		}
	  }
				
	  @Override
	  public void mouseExited(MouseEvent e) {}
			
	  @Override
	  public void mouseEntered(MouseEvent e) {}
		
	  @Override
	  public void mouseClicked(MouseEvent e) {}
	});
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.weighty = 1.0;
	gbc.weightx = 1.0;
	gbc.insets = new Insets(0, 0, 0, 0);
	gbc.fill = GridBagConstraints.BOTH;
	gbc.gridx = 0;
	gbc.gridy = 0;
	scrollPanel.add(gridPanel, gbc);	
				
	//handle scrolling window through full grid 
	hLevel = new JScrollBar();
	hLevel.setOrientation(JScrollBar.HORIZONTAL);
	hLevel.setMaximum(NUM_OF_COLUMNS * cellSize);
	hLevel.addAdjustmentListener(new AdjustmentListener() 
	{
	  @Override
	  public void adjustmentValueChanged(AdjustmentEvent e) 
	  {
		gridPanel.repaint();
	  }
	});

	hLevel.addComponentListener(new ComponentListener() 
	{
	  @Override
	  public void componentShown(ComponentEvent e) {}
	  @Override
	  public void componentResized(ComponentEvent e) 
	  {
		hLevel.setValue(Math.min(hLevel.getValue(), hLevel.getMaximum() - hLevel.getWidth()));
		hLevel.setVisibleAmount(hLevel.getWidth());
	  }	
	  @Override
	  public void componentMoved(ComponentEvent e) {}			
	  @Override
	  public void componentHidden(ComponentEvent e) {}
	});
	GridBagConstraints h = new GridBagConstraints();
	h.insets = new Insets(0, 0, 0, 0);
	h.fill = GridBagConstraints.HORIZONTAL;
	h.gridx = 0;
	h.gridy = 0;
	scrollPanel.add(hLevel, h);
	
	//binding keys
	hLevel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,0), "left key pressed");
    hLevel.getActionMap().put("left key pressed", left);

    hLevel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,0), "right key pressed");
    hLevel.getActionMap().put("right key pressed", right);

	vLevel = new JScrollBar();
	vLevel.setMaximum(NUM_OF_ROWS * cellSize);
	vLevel.addAdjustmentListener(new AdjustmentListener() 
	{
	  @Override
	  public void adjustmentValueChanged(AdjustmentEvent e) 
	  {
		gridPanel.repaint();
	  }
	});
	vLevel.addComponentListener(new ComponentListener() 
	{
	  @Override
	  public void componentShown(ComponentEvent e) {}
			
	  @Override
	  public void componentResized(ComponentEvent e) 
	  {
		vLevel.setValue(Math.min(vLevel.getValue(), vLevel.getMaximum() - vLevel.getHeight()));
		vLevel.setVisibleAmount(vLevel.getHeight());
	  }
			
	  @Override
	  public void componentMoved(ComponentEvent e) {}
			
	  @Override
	  public void componentHidden(ComponentEvent e) {}
	});
	GridBagConstraints v = new GridBagConstraints();
	v.fill = GridBagConstraints.VERTICAL;
	v.gridx = 0;
	v.gridy = 0;
	scrollPanel.add(vLevel, v);

	//binding keys
    vLevel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,0), "up key pressed");
    vLevel.getActionMap().put("up key pressed", up);
    
    vLevel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0), "down key pressed");
    vLevel.getActionMap().put("down key pressed", down);
  }
  
//==================================================================
//  startGame()
//  When start button is clicked, this method is called to get the 
//  number of the thread user selected.
//  Parameter: None
//  Return: None
//==================================================================  
  private void startGame()
  {
	startGame(numOfThread);
  }

//==================================================================
//  startGame(int NumOfThread)
//  This method is called to create threads to start the game.
//  Parameter: int NumOfThread--number of threads
//  Return: None   
//================================================================== 
  private void startGame(int NumOfThread)
  {
	isStarted = true;
    startButton.setText("Pause");
    worker = new Worker(grid, this);
    worker.setNumOfThread(numOfThread);
    worker.start();
  }

//==================================================================
//  getIsNextButton()
//  It passes the isNextButton value to the Worker class which calls 
//  this method.  
//  Parameter: None
//  Return: isNextButton(boolean)  
//==================================================================
  public boolean getIsNextButton()
  {
    return isNextButton;
  }

//==================================================================
//  setIsNextButton(boolean b)
//  It sets the isNextButton to the given boolean value. 
//  Parameter: isNextButton(boolean)
//  Return: None  
//==================================================================
  public void setIsNextButton(boolean b)
  {
    isNextButton = b;
  }

//==================================================================
//  repaintGrid()
//  It calls the repaint method, for grid panel only 
//  Parameter: None
//  Return: None  
//==================================================================
  public void repaintGrid()
  {
	SwingUtilities.invokeLater(new Runnable()
	{
	  @Override
	  public void run() 
	  {
		gridPanel.repaint();
	  }
	});
  }

//==================================================================
//  This class handles painting the grid for the game 
//================================================================== 
  class GridPanel extends JPanel
  {
	@Override
	protected void paintComponent(Graphics g) 
	{
	  super.paintComponent(g);

	  int left = hLevel.getValue() / cellSize;
	  int top = vLevel.getValue() / cellSize;
	  int right = Math.min(left + hLevel.getVisibleAmount() / cellSize + 1, NUM_OF_COLUMNS - 1);
	  int bottom = Math.min(top + vLevel.getVisibleAmount() / cellSize + 1, NUM_OF_ROWS - 1);
	  boolean[][] displayedGrid = grid.getDisplayedGrid(top, left, bottom, right);
	  int x = (hLevel.getValue() % cellSize);
	  for(int j = 0; j < displayedGrid[0].length; j++)
	  {  
		int y = (vLevel.getValue() % cellSize);
		for(int i = 0; i < displayedGrid.length; i++)
		{
  		  g.setColor(Color.PINK);
		  g.drawRect(x, y, cellSize, cellSize);
		  if(displayedGrid[i][j] == true)
		  {
		    g.setColor(Color.WHITE);
		    g.fillRect(x, y, cellSize, cellSize);
		  }
		  y += cellSize;
		}
		x += cellSize;
	  }
	}
  }
}

