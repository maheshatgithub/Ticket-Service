import java.util.TimerTask;

class SeatHoldTimerTask extends TimerTask
{
	private int seatHoldId = 0;
	private TicketServiceImpl tsi = null;
		
	SeatHoldTimerTask(int seatHoldId, TicketServiceImpl tsi)
	{
		this.seatHoldId = seatHoldId;
		this.tsi = tsi;
	}

	public void run() {
		System.err.println("Seats held for SeatHoldID '" + seatHoldId + "' has expired!");
		tsi.releaseSeats(seatHoldId);
	}
}
