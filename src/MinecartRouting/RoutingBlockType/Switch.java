package MinecartRouting.RoutingBlockType;

import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import MinecartRouting.MinecartRoutingMinecart;

public class Switch implements RoutingBlockType{

	RoutingBlockTypes type = RoutingBlockTypes.SWITCH;
	RoutingBlockActionTimes time = RoutingBlockActionTimes.PREBLOCK;
	Boolean ignoreredstone;
	Boolean redstone;
	
	public Switch(List<LinkedHashMap<String, Object>> options)
	{
		for (LinkedHashMap<String, Object> opt : options)
		{
			if (opt.containsKey("ignoreredstone"))
				ignoreredstone = (Boolean) opt.get("ignoreredstone");
			if (opt.containsKey("redstone"))
				redstone = (Boolean) opt.get("redstone");
		}
	}	
	
	@Override
	public boolean isValid()
	{
		if (redstone != null && ignoreredstone != null)
			return true;
		return false;
	}
	
	@Override
	public String toString()
	{
		return "Switch: ignoreredstone: "+ignoreredstone.toString()+" redstone: "+redstone.toString()+";";
	}
	
	@Override
	public void doAction(Block b, MinecartRoutingMinecart cart)
	{
		Player p = cart.owner;
		if (!( (p.hasPermission("minecartrouting.benefit.switch.own") && plugin.util.isOwner(b, p)) || p.hasPermission("minecartrouting.benefit.switch.other")))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to benefit from switch flags");
			return;
		}
		
		if (!plugin.blockmanager.isRightPowered(b, ignoreredstone, redstone))
			return;
		
		plugin.debug("Switching...");
		BlockFace origin = plugin.util.velocityToDirection(cart.cart.getVelocity());
		BlockFace destination = plugin.blockmanager.getDirection(b, cart);
		plugin.blockmanager.setRail(b.getRelative(BlockFace.UP), origin, destination);
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
		return true;
	}
}
