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

import MinecartRouting.DoActionRunnable;
import MinecartRouting.MinecartRouting;
import MinecartRouting.MinecartRoutingMinecart;
import MinecartRouting.RoutingBlock;

public class MinecartRoutingBlockListener extends BlockListener {
	
	private MinecartRouting plugin;
	
	public MinecartRoutingBlockListener(MinecartRouting instance)
	{
		plugin = instance;
	}
	
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (!plugin.settingsManager.isRoutingBlock(event.getBlock()))
			return;
		
		if (!plugin.blockmanager.exists(event.getBlock()))
			return;
		plugin.debug("Removeing...");
		
		boolean success = plugin.blockmanager.remove(event.getBlock(), event.getPlayer());
		if (!success)
			event.setCancelled(true);
		return;
	}

	public void onBlockRedstoneChange(BlockRedstoneEvent event)
	{
		if (!(event.getOldCurrent() == 0 && event.getNewCurrent() > 0))
			return;

		Block b = findRoutingBlock(event.getBlock());
		if (b == null)
			return;
		
		RoutingBlock rb = plugin.blockmanager.getRoutingBlock(b);
		if (rb == null)
			return;
		
		plugin.debug("Redstone changed!");

		Location loc = b.getRelative(BlockFace.UP).getLocation();
		loc.setX(loc.getX() + 0.5);
		loc.setY(loc.getY() + 0.5);
		loc.setZ(loc.getZ() + 0.5);
		
		double closest = Double.MAX_VALUE;
    	Minecart closestCart = null;
    	for (Entity le : b.getChunk().getEntities())
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
    		return;
    	if (!plugin.settingsManager.vehicles.containsKey(closestCart.getEntityId()))
    		plugin.settingsManager.vehicles.put(closestCart.getEntityId(), new MinecartRoutingMinecart(closestCart));
    	
    	MinecartRoutingMinecart cart = plugin.settingsManager.vehicles.get(closestCart.getEntityId());
    	plugin.debug("Vehicle found! {0}", closest);
		
    	cart.lastOnBlock = null;

		DoActionRunnable run = new DoActionRunnable(rb, b, cart);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, run, 5);
	}
	
	private Block findRoutingBlock(Block start)
	{
		BlockFace[] directions = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		for (BlockFace dir : directions)
		{
			Block b = start.getRelative(dir);
			if (plugin.util.isRail(b))
				b = b.getRelative(BlockFace.DOWN);
			if (plugin.settingsManager.isRoutingBlock(b) && plugin.blockmanager.exists(b))
				return b;
		}
		return null;
	}
	
}
