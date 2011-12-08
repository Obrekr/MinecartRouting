package MinecartRouting.Flags;

import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import MinecartRouting.MinecartRoutingMinecart;
import MinecartRouting.RoutingBlock;

public class Switch implements Flag{

	private Flags type = Flags.SWITCH;
	private ActionTimes time = ActionTimes.PREBLOCK;
	private Boolean ignoreredstone;
	private Boolean redstone;
	
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
	public void doAction(RoutingBlock b, MinecartRoutingMinecart cart)
	{
		Player p = cart.getOwner();
		if (!( (p.hasPermission("minecartrouting.benefit.switch.own") && b.isOwner(p)) || p.hasPermission("minecartrouting.benefit.switch.other")))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to benefit from switch flags");
			return;
		}
		
		if (!plugin.blockmanager.isRightPowered(b, ignoreredstone, redstone))
			return;
		
		plugin.debug("Switching...");
		BlockFace origin = plugin.util.velocityToDirection(cart.getVelocity());
		BlockFace destination = plugin.blockmanager.getDirection(b, cart);
		if (destination == null)
			return;
		plugin.blockmanager.setRail(b.getLocation().getBlock().getRelative(BlockFace.UP), origin, destination);
	}
	
	@Override
	public Flags getBlockType()
	{
		return type;
	}

	@Override
	public ActionTimes getActionTime()
	{
		return time;
	}

	@Override
	public boolean hasSignConfig()
	{
		return true;
	}
}
