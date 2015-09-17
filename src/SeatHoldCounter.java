class SeatHoldCounter 
{
    private static int counter = 0;
 
    public static int nextId() 
    {
        return ++counter;         
    }
    
    public static int getId()
    {
    	return counter;
    }
}

