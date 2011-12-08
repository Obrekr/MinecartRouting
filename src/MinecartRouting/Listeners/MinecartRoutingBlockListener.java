package MinecartRouting.Listeners;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;

import MinecartRouting.DoActionsRunnable;
import MinecartRouting.MinecartRouting;
import MinecartRouting.MinecartRoutingMinecart;
import MinecartRouting.RoutingBlock;
import MinecartRouting.Flags.ActionTimes;

public class MinecartRoutingBlockListener extends BlockListener {
	
	private MinecartRouting plugin;
	
	public MinecartRoutingBlockListener(MinecartRouting instance)
	{
		plugin = instance;
	}
	
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (!plugin.settingsmanager.isRoutingBlockType(event.getBlock()))
			return;
		
		if (!plugin.settingsmanager.isRoutingBlock(event.getBlock().getLocation()))
			return;
		
		plugin.debug("Removeing...");
		RoutingBlock b = plugin.settingsmanager.getRoutingBlock(event.getBlock().getLocation());
		
		boolean success = plugin.blockmanager.remove(b, event.getPlayer());
		if (!success)
			event.setCancelled(true);
		return;
	}

	public void onBlockRedstoneChange(BlockRedstoneEvent event)
	{
		if (!(event.getOldCurrent() == 0 && event.getNewCurrent() > 0))
			return;

		RoutingBlock b = findRoutingBlock(event.getBlock());
		if (b == null)
			return;
		
		plugin.debug("Redstone changed!");

		Location loc = b.getLocation().clone();
		loc.setX(loc.getX() + 0.5);
		loc.setY(loc.getY() + 1.5);
		loc.setZ(loc.getZ() + 0.5);
		
		double closest = Double.MAX_VALUE;
    	Minecart closestCart = null;
    	for (Entity le : b.getLocation().getBlock().getChunk().getEntities())
    	{
    		if (le instanceof Minecart)
    		{
    			double distance = le.getLocation().toVector().distance(loc.toVector());
    			if (distance < closest)
    			{
    				closestCart = (Minecart)le;
    				closest = distance;
    			}
    		}
    	}
    	
    	if (closestCart == null || closest > 0.4)
    	{	
    		plugin.debug("no cart found or distance too high: {0}", closest);
    		return;
    	}
    	if (!plugin.settingsmanager.isMinecart(closestCart))
    		plugin.settingsmanager.putMinecart(new MinecartRoutingMinecart(closestCart));
    	
    	MinecartRoutingMinecart cart = plugin.settingsmanager.getMinecart(closestCart);
    	plugin.debug("Vehicle found! {0}", closest);
		
    	cart.unsetLastBlock(ActionTimes.ONBLOCK);
    	
    	DoActionsRunnable action = new DoActionsRunnable(b, cart);
    	Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, action, 2);
	}
	
	private RoutingBlock findRoutingBlock(Block start)
	{
		BlockFace[] directions = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		for (BlockFace dir : directions)
		{
			Block b = start.getRelative(dir);
			if (plugin.util.isRail(b) && plugin.settingsmanager.isRoutingBlock(b.getRelative(BlockFace.DOWN).getLocation()))
				return plugin.settingsmanager.getRoutingBlock(b.getRelative(BlockFace.DOWN).getLocation());
			if (plugin.settingsmanager.isRoutingBlock(b.getLocation()))
				return plugin.settingsmanager.getRoutingBlock(b.getLocation());
		}
		return null;
	}
	
}
