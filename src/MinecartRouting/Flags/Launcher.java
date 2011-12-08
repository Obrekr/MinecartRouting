package MinecartRouting.Flags;

import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import MinecartRouting.MinecartRoutingMinecart;
import MinecartRouting.RoutingBlock;

public class Launcher implements Flag
{
	private Flags type = Flags.LAUNCHER;
	private ActionTimes time = ActionTimes.ONBLOCK;
	private Boolean ignoreredstone;
	private Boolean redstone;
	private Integer speedmodifier;
	private Boolean relative;
	
	public Launcher(List<LinkedHashMap<String, Object>> options)
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
	public void doAction(RoutingBlock b, MinecartRoutingMinecart cart)
	{
		Player p = cart.getOwner();
		if (!( (p.hasPermission("minecartrouting.benefit.launcher.own") && b.isOwner(p)) || p.hasPermission("minecartrouting.benefit.launcher.other")))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to benefit from launcher flags");
			return;
		}
		
		if (!plugin.blockmanager.isRightPowered(b, ignoreredstone, redstone))
			return;
		
		plugin.debug("Launching...");
		BlockFace dir = plugin.blockmanager.getLaunchingDirection(b, cart);
		if (dir == null)
			return;
		Vector direction = plugin.util.directionToVector(dir);
		cart.launch(direction, speedmodifier, relative);
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
		return "Launcher: ignoreredstone: "+ignoreredstone.toString()+" redstone: "+redstone.toString()+" speed: "+speedmodifier.toString()+" relative: "+relative.toString()+";";
	}

}
