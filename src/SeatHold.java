import java.util.ArrayList;
import java.util.List;


public class SeatHold 
{
	private String customer  = "";
	private List<Seat> seats = new ArrayList<Seat>();
		
	public SeatHold(String customer, List<Seat> seats)
	{
		this.customer = customer;
		this.seats = seats;
	}
	
	public String getCustomer()
	{
		return customer;
	}
	
	public List<Seat> getSeats()
	{
		return seats;
	}
}
