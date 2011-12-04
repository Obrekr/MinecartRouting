package MinecartRouting.RoutingBlockType;

import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import MinecartRouting.MinecartRoutingMinecart;

public class Acceleration implements RoutingBlockType{

	RoutingBlockTypes type = RoutingBlockTypes.ACCELERATION;
	RoutingBlockActionTimes time = RoutingBlockActionTimes.ONBLOCK;
	Boolean ignoreredstone;
	Boolean redstone;
	Integer speedmodifier;
	Boolean relative;
	
	public Acceleration(List<LinkedHashMap<String, Object>> options)
	{
		for (LinkedHashMap<String, Object> opt : options)
		{
			if (opt.containsKey("ignoreredstone"))
				ignoreredstone = (Boolean) opt.get("ignoreredstone");
			if (opt.containsKey("redstone"))
				redstone = (Boolean) opt.get("redstone");
			if (opt.containsKey("speed"))
				speedmodifier = (Integer) opt.get("speed");
			if (opt.containsKey("relative"))
				relative = (Boolean) opt.get("relative");
		}
	}	

	@Override
	public boolean isValid()
	{
		if (redstone != null && ignoreredstone != null && speedmodifier != null && relative != null)
			return true;
		return false;
	}
	
	@Override
	public String toString()
	{
		return "Acceleration: ignoreredstone: "+ignoreredstone.toString()+" redstone: "+redstone.toString()+" speed: "+speedmodifier.toString()+" relative: "+relative.toString()+";";
	}

	@Override
	public void doAction(Block b, MinecartRoutingMinecart cart)
	{
		Player p = cart.owner;
		if (!( (p.hasPermission("minecartrouting.benefit.acceleration.own") && plugin.util.isOwner(b, p)) || p.hasPermission("minecartrouting.benefit.acceleration.other")))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to benefit from speed modifing flags");
			return;
		}
		
		if (!plugin.blockmanager.isRightPowered(b, ignoreredstone, redstone))
			return;
		
		plugin.debug("Modifing speed...");
		cart.setSpeed(speedmodifier, relative);
	}

	@Override
	public RoutingBlockTypes getBlockType()
	{
		return type;
	}

	@Override
	public RoutingBlockActionTimes getActionTime()
	{
		return time;
	}

	@Override
	public boolean hasSignConfig()
	{
		return false;
	}

}
