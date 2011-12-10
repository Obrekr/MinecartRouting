package MinecartRouting.Flags;

import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import MinecartRouting.MinecartRoutingMinecart;
import MinecartRouting.RoutingBlock;

public class Catcher implements Flag
{
	private Flags type = Flags.CATCHER;
	private ActionTimes time = ActionTimes.ONBLOCK;
	private Boolean ignoreredstone;
	private Boolean redstone;	
	
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
	public void doAction(RoutingBlock b, MinecartRoutingMinecart cart)
	{
		Player p = cart.getOwner();
		if (!( (p.hasPermission("minecartrouting.benefit.catcher.own") && b.isOwner(p)) || p.hasPermission("minecartrouting.benefit.catcher.other")))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to benefit from catcher flags");
			return;
		}
		
		if (!plugin.blockmanager.isRightPowered(b, ignoreredstone, redstone))
			return;
		
		plugin.debug("Catching...");
		BlockFace dir = plugin.blockmanager.getDirection(b, cart);
		if (dir != null)
			cart.catchToBlock(b);
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

}
