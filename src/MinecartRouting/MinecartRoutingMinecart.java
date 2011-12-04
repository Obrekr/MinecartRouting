package MinecartRouting;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import MinecartRouting.RoutingBlockType.RoutingBlockActionTimes;

public class MinecartRoutingMinecart {
	
	public Minecart cart;
	public Player owner;
	public Route route;
	public Location lastPreBlock;
	public Location lastOnBlock;
	public Block catchedBlock;
	
	public MinecartRoutingMinecart (Minecart minecart){
		cart = minecart;
		findOwner();
	}
	
	private void findOwner()
	{
		double closest = Double.MAX_VALUE;
    	Player closestPlayer = null;
    	for (LivingEntity le : cart.getWorld().getLivingEntities())
    	{
    		if (le instanceof Player)
    		{
    			double distance = le.getLocation().toVector().distance(cart.getLocation().toVector());
    			if (distance < closest)
    			{
    				closestPlayer = (Player)le;
    				closest = distance;
    			}
    		}
    	}
    	if (closestPlayer != null)
    	{
    		owner = closestPlayer;
    	}
	}
	
	public boolean hasRoute()
	{
		if (route != null)
			return true;
		return false;
	}
	
	public void removeRoute()
	{
		route = null;
	}
	
	public boolean hasPositionChanged(Block b, RoutingBlockActionTimes time)
	{
		Location loc = null;
		switch (time)
		{
		case ONBLOCK:
			loc =  lastOnBlock;
			break;
		case PREBLOCK:
			loc = lastPreBlock;
		}
		if (loc == null)
			return true;
		Location newloc = b.getLocation();
		if (!(newloc.getWorld().getUID() == loc.getWorld().getUID() && newloc.getBlockX() == loc.getBlockX() && newloc.getBlockY() == loc.getBlockY() && newloc.getBlockZ() == loc.getBlockZ()))
			return true;
		return false;
	}
	
	public void setLastBlock(Block b, RoutingBlockActionTimes time)
	{
		switch (time)
		{
		case ONBLOCK:
			lastOnBlock =  b.getLocation();
			break;
		case PREBLOCK:
			lastPreBlock = b.getLocation();
		}
	}
	
	public void setSpeed(Integer percentage, Boolean relative)
	{
		if (relative)
		{
			double multiplier = percentage / 100.0;
			Vector newVelocity = cart.getVelocity().multiply(multiplier);
			cart.setVelocity(newVelocity);
		}else{
			double maxspeed = cart.getMaxSpeed();
			double multiplier = percentage / 100.0 * maxspeed;
			Vector newVelocity = cart.getVelocity().normalize().multiply(multiplier);
			cart.setVelocity(newVelocity);
		}
	}
	
	public boolean isCatched()
	{
		if (catchedBlock != null)
			return true;
		return false;
	}

	public void catchToBlock(Block b)
	{
		catchedBlock = b;
		recatch();
	}
	
	public void recatch()
	{
		cart.setVelocity(new Vector(0,0,0));
		Location loc = catchedBlock.getLocation();
		loc.setX(loc.getX() + 0.5);
		loc.setY(loc.getY() + 1.5);
		loc.setZ(loc.getZ() + 0.5);
		cart.teleport(loc);
	
	}

	public void launch(Vector direction, Integer speed, Boolean relative)
	{
		catchedBlock = null;
		cart.setVelocity(direction);
		setSpeed(speed, relative);
	}
}