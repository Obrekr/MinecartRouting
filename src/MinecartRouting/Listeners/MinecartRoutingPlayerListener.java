package MinecartRouting.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
		
		// RoutingBlock adding/updating
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.hasItem() && event.getItem().getTypeId() == plugin.settingsManager.toolitem && plugin.settingsManager.isRoutingBlock(event.getClickedBlock()))
		{
			if (!plugin.blockmanager.exists(event.getClickedBlock()))
			{
				if (event.getPlayer().isSneaking())
				{
					plugin.debug("Adding...");
					plugin.blockmanager.add(event.getClickedBlock(), event.getPlayer());
				}
			}else{
				if (event.getPlayer().isSneaking())
				{
					plugin.debug("Updating Signs/Rails...");	
					plugin.blockmanager.update(event.getClickedBlock(), event.getPlayer());
				}else{
					plugin.debug("Updating only Rails...");
					plugin.automanager.updateBlockInAllDirections(event.getClickedBlock());
					event.getPlayer().sendRawMessage(ChatColor.AQUA + "Rails updated!");
				}
			}
		}
		
		// send info to Player
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.hasItem() && event.getItem().getTypeId() == plugin.settingsManager.toolitem && plugin.blockmanager.exists(event.getClickedBlock()))
			plugin.util.sendInfo(event.getClickedBlock(), event.getPlayer());
		
		return;
	}
}
