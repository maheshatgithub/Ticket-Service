
public interface TicketService 
{
	public int numSeatsAvailable(int level);
	
	public SeatHold findAndHoldSeats(int numSeats, int minLevel, int maxLevel, String customer);
	
	public String reserveSeats(int seatHoldId);
}
