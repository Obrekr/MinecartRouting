package MinecartRouting;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import MinecartRouting.Flags.ActionTimes;

public class MinecartRoutingMinecart {
	
	private static MinecartRouting plugin = (MinecartRouting) Bukkit.getPluginManager().getPlugin("MinecartRouting");
	
	private Minecart cart;
	private Player owner;
	private Route route;
	private Location lastPreBlock;
	private BlockFace lastPreDirection;
	private Location lastOnBlock;
	private BlockFace lastOnDirection;;
	private Location catchedBlock;
	private boolean hasReachedDestination = false;
	
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
	
	public boolean hasPositionChanged(Block b, ActionTimes time)
	{
		Location loc = null;
		BlockFace dir = null;
		switch (time)
		{
		case ONBLOCK:
			loc =  lastOnBlock;
			dir = lastOnDirection;
			break;
		case PREBLOCK:
			loc = lastPreBlock;
			dir = lastPreDirection;
			break;
		}
		if (loc == null || dir == null)
			return true;
		Location newloc = b.getLocation();
		BlockFace incdir = plugin.util.velocityToDirection(cart.getVelocity().normalize());
		if (!incdir.equals(dir)  || !newloc.equals(loc))
			return true;
		return false;
	}

	public boolean hasReachedDestination()
	{
		return hasReachedDestination;
	}
	
	public boolean hasReachedDestination(int id)
	{
		if (hasReachedDestination == true)
			return false;
		if (!hasRoute())
			return false;
		if (route.getDestination() == id)
		{
			removeRoute();
			hasReachedDestination = true;
			return true;
		}
		return false;
	}
	
	public Route getRoute()
	{
		return route;
	}
	
	public  Player getOwner()
	{
		return owner;
	}
	
	public Vector getVelocity()
	{
		return cart.getVelocity();
	}
	
	public Entity getPassenger()
	{
		return cart.getPassenger();
	}
	
	public void removeRoute()
	{
		route = null;
	}
	
	public int getId()
	{
		return cart.getEntityId();
	}
	
	public Minecart getCart()
	{
		return cart;
	}
	
	public void setDestination(Integer destid)
	{
		route =  new Route(destid);
		hasReachedDestination = false;
	}
	
	public void setLastBlock(RoutingBlock routingBlock, ActionTimes time)
	{
		BlockFace incdir = plugin.util.velocityToDirection(cart.getVelocity().normalize());
		switch (time)
		{
		case ONBLOCK:
			lastOnBlock =  routingBlock.getLocation();
			lastOnDirection = incdir;
			break;
		case PREBLOCK:
			lastPreBlock = routingBlock.getLocation();
			lastPreDirection = incdir;
			break;
		}
	}
	
	public void unsetLastBlock(ActionTimes time)
	{
		switch (time)
		{
		case ONBLOCK:
			lastOnBlock =  null;
			lastOnDirection = null;
			break;
		case PREBLOCK:
			lastPreBlock = null;
			lastPreDirection = null;
			break;
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

	public boolean isEmpty()
	{
		return cart.isEmpty();
	}
	
	public void catchToBlock(RoutingBlock b)
	{
		catchedBlock = b.getLocation();
		recatch();
	}
	
	public void recatch()
	{
		cart.setVelocity(new Vector(0,0,0));
		Location loc = catchedBlock.clone();
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