package MinecartRouting.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

import MinecartRouting.MinecartRouting;

public class MinecartRoutingPlayerListener extends PlayerListener {
	
	private MinecartRouting plugin;
	
	public MinecartRoutingPlayerListener(MinecartRouting instance)
	{
		plugin = instance;
	}

	
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (!(event.getClickedBlock() instanceof Block))
				return;
		
		// DestinationSign: Destination set
		if ( event.getAction() == Action.RIGHT_CLICK_BLOCK && (event.getClickedBlock().getType().equals(Material.WALL_SIGN) || event.getClickedBlock().getType().equals(Material.SIGN_POST)) )
		{
			plugin.automanager.setDestination(event.getClickedBlock(), event.getPlayer());
		}
		
		Block b = null;
		if (plugin.settingsManager.isRoutingBlock(event.getClickedBlock()))
			b = event.getClickedBlock();
		if (plugin.util.isRail(event.getClickedBlock()) && plugin.settingsManager.isRoutingBlock(event.getClickedBlock().getRelative(BlockFace.DOWN)))
			b = event.getClickedBlock().getRelative(BlockFace.DOWN);
		if (b == null)
			return;
		
		// RoutingBlock adding/updating
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.hasItem() && event.getItem().getTypeId() == plugin.settingsManager.toolitem)
		{
			if (!plugin.blockmanager.exists(b))
			{
				if (event.getPlayer().isSneaking())
				{
					plugin.debug("Adding...");
					plugin.blockmanager.add(b, event.getPlayer());
				}
			}else{
				if (event.getPlayer().isSneaking())
				{
					plugin.debug("Updating Block...");	
					plugin.blockmanager.update(b, event.getPlayer());
				}else if (plugin.settingsManager.hasSignConfig(b)){
					plugin.debug("Updating only Rails...");
					plugin.automanager.updateBlockInAllDirections(b);
					event.getPlayer().sendRawMessage(ChatColor.AQUA + "Rails updated!");
				}
			}
		}
		
		// send info to Player
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.hasItem() && event.getItem().getTypeId() == plugin.settingsManager.toolitem && plugin.blockmanager.exists(b))
			plugin.util.sendInfo(b, event.getPlayer());
		
		return;
	}
}
