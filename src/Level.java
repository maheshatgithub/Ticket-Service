public class Level 
{
	int id, price;
	String name;
    int[][] seating;
    
    public Level(int id, String name, int price, int[][] seating)
    {
    	this.id = id;
    	this.name = name;
    	this.price = price;
        this.seating = seating;
    }

    public int getId()
    {
    	return id;
    }
    
    public String getName()
    {
    	return name;
    }
    
    public int getPrice()
    {
    	return price;
    }
    
    public int[][] getSeating()
    {
    	return seating;
    }
    
    
}
