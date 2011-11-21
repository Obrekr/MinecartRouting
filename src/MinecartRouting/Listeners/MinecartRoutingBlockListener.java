package MinecartRouting.Listeners;


import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;

import MinecartRouting.MinecartRouting;
import MinecartRouting.RoutingBlocks;

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

		Block redstone = event.getBlock();
		Block b = null;
		if (plugin.settingsManager.isRoutingBlock(redstone.getRelative(BlockFace.NORTH)))
				b = redstone.getRelative(BlockFace.NORTH);
		if (plugin.settingsManager.isRoutingBlock(redstone.getRelative(BlockFace.EAST)))
			b = redstone.getRelative(BlockFace.EAST);
		if (plugin.settingsManager.isRoutingBlock(redstone.getRelative(BlockFace.SOUTH)))
			b = redstone.getRelative(BlockFace.SOUTH);
		if (plugin.settingsManager.isRoutingBlock(redstone.getRelative(BlockFace.WEST)))
			b = redstone.getRelative(BlockFace.WEST);
		
		if (b == null)
			return;
		
		plugin.debug("Redstone changed!");

		Location loc = b.getRelative(BlockFace.UP).getLocation();
		loc.setX(loc.getX() + 0.5);
		loc.setY(loc.getY() + 0.5);
		loc.setZ(loc.getZ() + 0.5);
		
		double closest = Double.MAX_VALUE;
    	Minecart closestCart = null;
    	for (Entity le : event.getBlock().getWorld().getEntities())
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
		if (closestCart != null && closest < 0.4)
		{	
			plugin.debug("Vehicle found! {0}", closest);
			plugin.settingsManager.passedBlocks.remove(closestCart);
			// Boost if enabled
	    	if ( plugin.util.isEnabled(RoutingBlocks.BOOSTER) && (plugin.util.getType(b) == RoutingBlocks.BOOSTER) )
	    		plugin.blockmanager.booster(b, closestCart, true);
	    	
	    	//Brake if enabled
	    	if ( plugin.util.isEnabled(RoutingBlocks.BRAKE) && (plugin.util.getType(b) == RoutingBlocks.BRAKE) )
	    		plugin.blockmanager.brake(b, closestCart, true);
	    		
	    	//Catch if enabled
	    	if ( plugin.util.isEnabled(RoutingBlocks.CATCHER) && (plugin.util.getType(b) == RoutingBlocks.CATCHER) )
	    		plugin.blockmanager.catcher(b, closestCart, true);
	    	
	    	//Launch if enabled
	    	if ( plugin.util.isEnabled(RoutingBlocks.LAUNCHER) && (plugin.util.getType(b) == RoutingBlocks.LAUNCHER) )
	    		plugin.blockmanager.launcher(b, closestCart, true);
	    	
	    	//Switch if enabled
	    	if ( plugin.util.isEnabled(RoutingBlocks.SWITCH) && (plugin.util.getType(b) == RoutingBlocks.SWITCH) )
	    		plugin.blockmanager.switcher(b, closestCart, true);
		}
	}
	
}
