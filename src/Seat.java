public class Seat 
{
	private int levelNo, rowNo, seatNo;
	
	public Seat(int levelNo, int rowNo, int seatNo) 
	{
		this.levelNo = levelNo;
		this.rowNo = rowNo;
		this.seatNo = seatNo;
	}

	public int getLevelNo() {
		return levelNo;
	}

	public void setLevelNo(int levelNo) {
		this.levelNo = levelNo;
	}

	public int getRowNo() {
		return rowNo;
	}

	public void setRowNo(int rowNo) {
		this.rowNo = rowNo;
	}

	public int getSeatNo() {
		return seatNo;
	}

	public void setSeatNo(int seatNo) {
		this.seatNo = seatNo;
	}
	
	
	

}
