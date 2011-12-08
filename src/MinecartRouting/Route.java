package MinecartRouting;

import java.util.ArrayList;
import java.util.List;

public class Route {
	
	private Integer destination = -1;
	private List<Integer> path = new ArrayList<Integer>();
	private Integer position = 1;
	
	public Route(Integer dest, List<Integer> l, Integer pos)
	{
		destination = dest;
		path = l;
		position = pos;
	}
	
	public Route(int dest)
	{
		destination = dest;
	}
	
	public void setPath(List<Integer> l)
	{
		path = l;
	}
	
	public void setPosition(int pos)
	{
		position = pos;
	}
	
	public void setDestination(int dest)
	{
		destination = dest;
	}
	
	public Integer getNextDestination()
	{
		if (!hasPath())
			return null;
		if (position == path.size())
			return -1;
		int next = path.get(position);
		position++;
		return next;
	}
	
	public int getDestination()
	{
		return destination;
	}
	
	public boolean hasPath()
	{
		if (!path.isEmpty())
			return true;
		return false;
	}
}
