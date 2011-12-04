package MinecartRouting.RoutingBlockType;

import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import MinecartRouting.MinecartRoutingMinecart;

public class Catcher implements RoutingBlockType
{
	RoutingBlockTypes type = RoutingBlockTypes.CATCHER;
	RoutingBlockActionTimes time = RoutingBlockActionTimes.ONBLOCK;
	Boolean ignoreredstone;
	Boolean redstone;	
	
	public Catcher(List<LinkedHashMap<String, Object>> options)
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
	public boolean isValid() {
		if (redstone != null && ignoreredstone != null)
			return true;
		return false;
	}
	
	@Override
	public String toString()
	{
		return "Catcher: ignoreredstone: "+ignoreredstone.toString()+" redstone: "+redstone.toString()+";";
	}
	
	@Override
	public void doAction(Block b, MinecartRoutingMinecart cart)
	{
		Player p = cart.owner;
		if (!( (p.hasPermission("minecartrouting.benefit.catcher.own") && plugin.util.isOwner(b, p)) || p.hasPermission("minecartrouting.catcher.switch.other")))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to benefit from catcher flags");
			return;
		}
		
		if (!plugin.blockmanager.isRightPowered(b, ignoreredstone, redstone))
			return;
		
		plugin.debug("Catching...");
		BlockFace dir = plugin.blockmanager.getDirection(b, cart, false, true);
		if (dir.equals(BlockFace.UP))
			cart.catchToBlock(b);
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
