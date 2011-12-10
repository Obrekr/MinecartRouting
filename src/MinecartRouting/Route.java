package MinecartRouting;

import java.util.ArrayList;
import java.util.List;

public class Route {
	
	private Integer destination = -1;
	private List<Integer> path = new ArrayList<Integer>();
	
	public Route(Integer dest, List<Integer> l)
	{
		destination = dest;
		path = l;
	}
	
	public Route(int dest)
	{
		destination = dest;
	}
	
	public void setPath(List<Integer> l)
	{
		path = l;
	}
	
	public void setDestination(int dest)
	{
		destination = dest;
	}
	
	public Integer getNextDestination(int id)
	{
		if (!hasPath() || !path.contains(id))
			return null;
		int pos = path.indexOf(id);
		if (pos == path.lastIndexOf(destination))
			return null;
		return path.get(pos+1);
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
