import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Timer;

public class TicketServiceImpl implements TicketService
{
	private Properties configFile;
	
	// Constants
	private final int AVAILABLE = 0;
	private final int HOLD = 1;
	private final int RESERVE = 2;	
	
	// SeatHold Timeout
	private final int TIMEOUT = 60;

	// List of levels with Id, Name, Price, Rows and Seats
	List<Level> levels =new ArrayList<Level>();
	
	// Seats held or reserved by SeatHoldId
	private Map<Integer, SeatHold> seatsHeld = new HashMap<Integer, SeatHold>();	
	
	// Map which stores TimerTasks by seatHoldId
	private Map<Integer, SeatHoldTimerTask> timerTasks = new HashMap<Integer, SeatHoldTimerTask>();
	
	
	public TicketServiceImpl()
	{
		// Configuration File 
		//loadConfigurationFile();

		levels.add(new Level( 1, "Orchestra", 100, new int[25][50] ));
		levels.add(new Level( 2, "Main", 75, new int[20][100]));
		levels.add(new Level( 3, "Balcony-I", 50, new int[15][100]));
		levels.add(new Level( 4, "Balcony-II ", 40, new int[15][100]));
	}
	
	/************************************************************
	 * Method returns availableSeats by level 
	 * 
	 * Orchestra: 1 ::: Main: 2 ::: Balcony 1: 3 ::: Balcony 2: 4 ::: All Levels: 0
	 * 
	 ************************************************************/
	public int numSeatsAvailable(int whichLevel)
	{
		int availableSeats = 0;
		int minLevel;
		int maxLevel;
		
		// To support particular level and all levels
		if (whichLevel == 0)
		{
			minLevel = 0;
			maxLevel = levels.size();
		}
		else
		{
			minLevel = whichLevel - 1;
			maxLevel = whichLevel;
		}
				
		for ( int levelIdx = minLevel; levelIdx < maxLevel; levelIdx++ ) 
		{
			Level level = levels.get(levelIdx);
			int[][] seating = level.getSeating();
			for (int row = 0; row < seating.length; row++)
			{
				for (int col = 0; col < seating[0].length; col++)
				{
					if ( seating[row][col] == AVAILABLE )
							availableSeats++; 
				}
			}
		}
		return availableSeats;
		
	}

	/********************************************************************
	 * Method reserves the block of seats already held provided with the seatHoldID
	 * 
	 * Throws a RuntimeException if seatHold is not already done
	 * 
	 ********************************************************************/
	public String reserveSeats(int seatHoldId) 
	{
		// Validate seatHold
		if (!timerTasks.containsKey(seatHoldId))
		{
			throw new RuntimeException("UNABLE to find SeatHold for SeatHoldID '" + seatHoldId + "' ... probably has expired.");
		}
		// Cancel the timertask as reservation requested
		timerTasks.remove(seatHoldId).cancel();		
		// Mark the seats as reserved (with value 2)
		return changeStatus(seatHoldId, RESERVE);
	}
	
	/*******************************************************************
	 * Method releases seats after seatHold timeout
	 * 
	 *******************************************************************/
	public String releaseSeats(int seatHoldId)
	{
		timerTasks.remove(seatHoldId);
		return changeStatus(seatHoldId, AVAILABLE);
	}
	
	/********************************************************************
	 * Method changes the status of seats based on reserve or release seats
	 ********************************************************************/
	public String changeStatus(int seatHoldId, int status) 
	{
		
		SeatHold seatHeld = seatsHeld.get(seatHoldId);
		int numSeatsAffected = 0, totalAmount = 0;
		for (Seat seat: seatHeld.getSeats() )
		{
			for (int i =0; i < levels.size(); i++ )
			{
				Level level = levels.get(i);
				int[][] seating = level.getSeating();
				if ( level.getId() == seat.getLevelNo() )
				{				
					for (int row = 0; row < seating.length; row++)
					{
						for (int col = 0; col < seating[0].length; col++)
						{
							if (seat.getRowNo() == row && seat.getSeatNo() == col)
							{
								seating[row][col] = status;
								totalAmount = totalAmount + level.getPrice();
								numSeatsAffected++;
							}
						}
					}
				}
			}
		}
		
		if (status == AVAILABLE)
		{
			System.out.println( numSeatsAffected + " seats released for SeatHoldID: '" + seatHoldId + "'.");
		}
		else
		{
			System.out.println( numSeatsAffected + " seats reserved for SeatHoldID: '" + seatHoldId + "'.");
			System.out.println("Please pay : $"+totalAmount + " either by cash / credit card.");
			System.out.println("***** Enjoy the show, " + seatHeld.getCustomer() + " *****");
		}
		return "Success";
	}

	/*********************************************************************************************
	 * Method finds and holds seats for the given range of levels..Level 1 being the best and Level 4 being the least
	 * 
	 * Arguments: minLevel  (Should be less than the maxLevel)						ERROR NOT HANDLED
	 * 					  maxLevel (Should be greater than or equal to the minLevel)	ERROR NOT HANDLED
	 * 
	 * After seats are held...BlockID would be displayed on the console.
	 * User also reminded about reserving the block before the timeout(in seconds) 
	 * 
	 ********************************************************************************************/
	public SeatHold findAndHoldSeats(int numSeats, int minLevel, int maxLevel, String customer) 
	{
		String seatsHeldStr = "FOLLOWING SEATS ARE HELD: \n";
		List<Seat> seats = new ArrayList<Seat>();
		
		outerloop:
		for ( int levelIdx = minLevel-1; levelIdx < maxLevel; levelIdx++ ) 
		{
			Level level = levels.get(levelIdx);
			int[][] seating = level.getSeating();
			for (int row = 0; row < seating.length; row++)
			{
				for (int col = 0; col < seating[0].length; col++)
				{
					if ( seating[row][col] == AVAILABLE )
					{
						seating[row][col] = HOLD;
						Seat seat = new Seat(level.getId(), row,col);
						seats.add(seat);
						seatsHeldStr += "Level ::: " + level.getName() + " - ROW ::: " + row + " - SEAT ::: " + col + "\n"; 
						if (seats.size() == numSeats)
						{
							break outerloop;
						}
					}
				}
			}	
		}
		if (seats.size() > 0)
		{
			SeatHold seatsHold = new SeatHold(customer, seats);
			seatsHeld.put(SeatHoldCounter.nextId(), seatsHold);
			System.out.println(seatsHeldStr);
			System.out.println(customer +", Your SeatHoldID is '" + SeatHoldCounter.getId() + "'");
			//int expiresIn = Integer.parseInt( configFile.getProperty("holdTimeout"));
			if (seats.size() != numSeats)
			{
				System.out.println("Only " + seats.size() + " out of " + numSeats + ""
						+ " seats held for the given criteria.");
			}
			else
			{
				System.out.println( seats.size() + " seats are held.");
			}
			System.err.println("Please CONFIRM your SeatHold within '"+ TIMEOUT + "' seconds.");
			SeatHoldTimerTask seatHoldTimer = new SeatHoldTimerTask(SeatHoldCounter.getId(), this);
			timerTasks.put(SeatHoldCounter.getId(), seatHoldTimer);
			Timer timer = new Timer();
			timer.schedule(seatHoldTimer,  TIMEOUT * 1000);
			
		}
		else
		{
			throw new RuntimeException("ERROR: Required seats not available.");
		}
		return null;
	}
	
	/****************************************************
	 * Method loads the configuration file for TicketService
	 * 
	 ***************************************************/
//	private void loadConfigurationFile()
//	{
//		configFile = new Properties();
//		InputStream configStream = null;
//		try
//		{
//			configStream = new FileInputStream("TicketService.config");
//			configFile.load(configStream);
//			configStream.close();
//		} catch (IOException ioe) 
//		{
//			ioe.printStackTrace();
//		} 
//	}
	
	/******************************************************
	 * TicketServiceImpl Main Method
	 * 
	 * Method handles the options available for Ticket Service
	 * 
	 *****************************************************/
	public static void main(String[] args) 
	{
		TicketServiceImpl tsi = new TicketServiceImpl();
		
		while(true)
		{
			System.out.println("\nWELCOME TO TICKET SERVICE by Mahendra K Devaraj");
			System.out.println("Enter one of the following options...");
			System.out.println("1. Number of Seats Available.");
			System.out.println("2. Hold Seats.");
			System.out.println("3. Reserve Seats.");
			System.out.println("4. Exit.\n");
			Scanner scan = new Scanner(System.in);
			int option = scan.nextInt();
			switch(option)
			{
				case 1:
					System.out.println("Orchestra: 1 ::: Main: 2 ::: Balcony-I: 3 ::: Balcony-II: 4 ::: All Levels ::: 0 ");
					System.out.println("Enter Level : " );
					int level = scan.nextInt();
					try
					{
						System.err.println("INFO: Available Seats : "+tsi.numSeatsAvailable(level));
					}
					catch(RuntimeException rte)
					{
						System.err.println(rte.getMessage());
					}
					break;
				case 2:
					try
					{
						System.out.println("Number of Seats : ");
						int reqSeats = scan.nextInt();
						System.out.println("Min Level : ");
						System.out.println("Orchestra: 1 ::: Main: 2 ::: Balcony-1: 3 ::: Balcony-2: 4");
						int minLevel = scan.nextInt();
						System.out.println("Max Level : ");
						System.out.println("Orchestra: 1 ::: Main: 2 ::: Balcony-1: 3 ::: Balcony-2: 4");
						int maxLevel = scan.nextInt();
						System.out.println("Customer : ");
						String customer = scan.next();
						
						tsi.findAndHoldSeats(reqSeats, minLevel, maxLevel, customer);
					}
					catch(RuntimeException rte)
					{
						System.err.println(rte.getMessage());
					}
					break;
				case 3:
					System.out.println("Enter the SeatHoldID : ");
					int blockId = scan.nextInt();
					try
					{
						tsi.reserveSeats(blockId);
					}
					catch(RuntimeException rte)
					{
						System.err.println(rte.getMessage());
					}
					break;
				case 4:
					System.exit(0);
					scan.close();
					break;
			} // Switch statement
			
		} // While statement

	} // Main method

} // End of Main Class




