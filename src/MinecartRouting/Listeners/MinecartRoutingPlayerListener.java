package MinecartRouting.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

import MinecartRouting.MinecartRouting;
import MinecartRouting.MinecartRoutingMinecart;
import MinecartRouting.RoutingBlock;

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
		
		// Signs
		if ( event.getAction() == Action.RIGHT_CLICK_BLOCK && (event.getClickedBlock().getType().equals(Material.WALL_SIGN) || event.getClickedBlock().getType().equals(Material.SIGN_POST)) )
			signActions(event.getClickedBlock(), event.getPlayer());
	
		Block b = null;
		if (plugin.settingsmanager.isRoutingBlockType(event.getClickedBlock()))
			b = event.getClickedBlock();
		else if (plugin.util.isRail(event.getClickedBlock()) && plugin.settingsmanager.isRoutingBlockType(event.getClickedBlock().getRelative(BlockFace.DOWN)))
			b = event.getClickedBlock().getRelative(BlockFace.DOWN);
		else if (event.getClickedBlock().getRelative(BlockFace.DOWN).getState() instanceof Sign)
			b = event.getClickedBlock();
		else if (event.getClickedBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getState() instanceof Sign)
			b = event.getClickedBlock().getRelative(BlockFace.DOWN);
		if (b == null)
			return;
		
		// RoutingBlock adding/updating
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.hasItem() && event.getItem().getTypeId() == plugin.settingsmanager.toolitem)
		{
			if (!plugin.settingsmanager.isRoutingBlock(b.getLocation()))
			{
				if (event.getPlayer().isSneaking())
				{
					plugin.debug("Adding...");
					plugin.blockmanager.add(b, event.getPlayer());
				}
			}else{
				RoutingBlock block = plugin.settingsmanager.getRoutingBlock(b.getLocation());
				if (event.getPlayer().isSneaking())
				{
					plugin.debug("Updating Block...");	
					plugin.blockmanager.update(block, event.getPlayer());
				}else if (block.hasSignConfig()){
					plugin.blockmanager.updateRails(block, event.getPlayer());
				}
			}
		}
		
		// send info to Player
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.hasItem() && event.getItem().getTypeId() == plugin.settingsmanager.toolitem && plugin.settingsmanager.isRoutingBlock(b.getLocation()))
			plugin.util.sendInfo(plugin.settingsmanager.getRoutingBlock(b.getLocation()), event.getPlayer());
	}
	
	private void signActions(Block b, Player p)
	{
		Sign sign = (Sign) b.getState();
		String[] lines = sign.getLines();
		
		// set Destination sign
		if (lines[0].toLowerCase().matches("\\[destination\\]") && !lines[1].isEmpty())
		{
			Integer id = null;
			MinecartRoutingMinecart v = plugin.util.findNextOwnVehicle(p);
			if (lines[1].toLowerCase().matches("#[0-9]+"))	
				id = Integer.parseInt(lines[1].substring(1));
			else if (plugin.settingsmanager.isRoutingBlock(lines[1].toLowerCase()))
				id = plugin.settingsmanager.getRoutingBlock(lines[1].toLowerCase()).getId();
			
			if (v == null)
			{
				p.sendRawMessage(ChatColor.AQUA + "You must first place a vehicle!");
				return;
			}
			if (id == null)
			{
				p.sendRawMessage(ChatColor.AQUA + "Destination not found");
				return;
			}
			v.setDestination(id);
			p.sendRawMessage(ChatColor.AQUA + "Destination set to: "+plugin.settingsmanager.getRoutingBlock(id).getName()+" (#"+id+") for cart #"+v.getId());
		}
		
		// reset Destination sign
		if (lines[0].toLowerCase().matches("\\[reset\\]"))
		{
			MinecartRoutingMinecart v = plugin.util.findNextOwnVehicle(p);
			if (v == null)
			{
				p.sendRawMessage(ChatColor.AQUA + "No vehicle found!");
				return;
			}
			v.removeRoute();
			p.sendRawMessage(ChatColor.AQUA + "Destination reset!");
		}
	}
}
