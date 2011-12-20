package MinecartRouting;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;


public class BlockManager {
	
	private static MinecartRouting plugin;
	
	public BlockManager(MinecartRouting instance)
	{
		plugin = instance;
	}
	
	public Boolean hasCreatePermission(RoutingBlockType block, Player p)
	{
		Boolean result = false;
		List<String> names = block.getFlagStringList();
		for (String name : names)
		{
			if (p.hasPermission("minecartrouting.create."+name+".own"))
				result = true;
			else
				return false;
		}
		return result;
	}
	
	public Boolean hasUpdatePermission(RoutingBlock block, Player p)
	{
		Boolean result = false;
		List<String> names = block.getFlagStringList();
		for (String name : names)
		{
			if (block.isOwner(p))
			{
				if (p.hasPermission("minecartrouting.create."+name+".own"))
					result = true;
				else
					return false;
			}else{
				if (p.hasPermission("minecartrouting.create."+name+".other"))
					result = true;
				else
					return false;
			}
		}
		return result;
	}
	
	public Boolean hasBreakPermission(RoutingBlock block, Player p)
	{
		Boolean result = false;
		List<String> names = block.getFlagStringList();
		for (String name : names)
		{
			if (block.isOwner(p))
			{
				if (p.hasPermission("minecartrouting.break."+name+".own"))
					result = true;
				else
					return false;
			}else{
				if (p.hasPermission("minecartrouting.break."+name+".other"))
					result = true;
				else
					return false;
			}
		}
		return result;
	}
	
	public boolean isRightPowered(RoutingBlock rb, Boolean ignoreredstone, Boolean redstone)
	{
		Block b = rb.getLocation().getBlock();
		if (ignoreredstone)
			return true;
		if (redstone == false && !(b.isBlockIndirectlyPowered() || b.getRelative(BlockFace.UP).isBlockIndirectlyPowered()))
			return true;
		if  (redstone == true && (b.isBlockIndirectlyPowered() || b.getRelative(BlockFace.UP).isBlockIndirectlyPowered()))
			return true;
		return false;
	}

	public void setRail(Block rail, BlockFace one, BlockFace two)
	{
		byte raildata = rail.getData();
		if ( (one == BlockFace.NORTH && two == BlockFace.SOUTH ) || (one == BlockFace.SOUTH && two == BlockFace.NORTH) )
			raildata = 1;	// North-South
		if ( (one == BlockFace.EAST && two == BlockFace.WEST) || (one == BlockFace.WEST && two == BlockFace.EAST) )
			raildata = 0;	// East-West
		if ( (one == BlockFace.NORTH && two == BlockFace.EAST) || (one == BlockFace.EAST && two == BlockFace.NORTH) )
			raildata = 8;	// North-East-Corner
		if ( (one == BlockFace.SOUTH && two == BlockFace.EAST) || (one == BlockFace.EAST && two == BlockFace.SOUTH) )
			raildata = 9;	// South-East-Corner
		if ( (one == BlockFace.SOUTH && two == BlockFace.WEST) || (one == BlockFace.WEST && two == BlockFace.SOUTH) )
			raildata = 6;	// South-West-Corner
		if ( (one == BlockFace.NORTH && two == BlockFace.WEST) || (one == BlockFace.WEST && two == BlockFace.NORTH) )
			raildata = 7;	// North-West-Corner
		rail.setData(raildata);
	}
	
	public BlockFace getDirection(RoutingBlock b, MinecartRoutingMinecart v)
	{
		Player p = v.getOwner();
		BlockFace fromdirection = plugin.util.velocityToDirection(v.getVelocity().normalize());
		plugin.debug("Origin: {0}", fromdirection);
		
		BlockFace destination = null;
		
		if (!b.getFrom_options().containsKey(fromdirection))
			return null;
		for (String option : b.getFrom_options().get(fromdirection))
		{
			if (option.equals("auto") && v.hasRoute())
			{
				plugin.debug("Starting AutoRouting...");
				destination = plugin.automanager.getDirection(b, p, v);
				if (destination != null)
					break;
			}
			
			if (optionMatches(option, b, v))
				destination = b.getToDirection(fromdirection, option);
		}
		
		if (destination != null)
			plugin.debug("Destination: {0}", destination);
		else
			plugin.debug("No destination!");
		return destination;	
	}
	
	public BlockFace getLaunchingDirection(RoutingBlock b, MinecartRoutingMinecart v)
	{
		Player p = v.getOwner();
		BlockFace destination = null;
		BlockFace[] directions = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		alldirections:
		for (BlockFace direction : directions)
		{
			if (b.getFrom_options().containsKey(direction))
			{
				for (String option : b.getFrom_options().get(direction))
				{
					if (option.equals("auto") && v.hasRoute())
					{
						plugin.debug("Starting AutoRouting...");
						destination = plugin.automanager.getDirection(b, p, v);
						if (destination != null)	
							break alldirections;
					}
					
					if (optionMatches(option, b, v))
						destination = b.getToDirection(direction, option);
				}
			}
		}
		return destination;
	}
	
	private boolean optionMatches(String option, RoutingBlock b, MinecartRoutingMinecart v)
	{
		if ( v.getPassenger() instanceof Player && option.equals("#"+((Player) v.getPassenger()).getDisplayName()) )
			return true;
		
		if ( option.equals("player") && v.getPassenger() instanceof Player )
			return true;
			
		if ( option.equals("mob") && (v.getPassenger() instanceof Animals || v.getPassenger() instanceof Monster) )
			return true;
			
		if ( option.equals("empty") && v.isEmpty() )
			return true;
			
		if ( option.equals("storage") && v.getCart() instanceof StorageMinecart)
			return true;
			
		if ( option.equals("powered") && v.getCart() instanceof PoweredMinecart)
			return true;
			
		if ( option.equals("default") )
			return true;
		return false;
	}
	
	public boolean add(Block b, Player p)
	{
		RoutingBlockType type = plugin.settingsmanager.getRoutingBlockType(b.getTypeId());
		boolean bysign = false;
		if (type == null && plugin.settingsmanager.signcandefinetype)
		{	
			BlockState block = b.getRelative(BlockFace.DOWN).getState();
			if (block instanceof Sign)
			{
				Sign s = (Sign) block;
				if (s.getLine(0).toLowerCase().matches("\\[type\\]"))
				{
					type = plugin.settingsmanager.getRoutingBlockType(s.getLine(1).toLowerCase());
					bysign = true;
				}
			}
		}
		
		if (type == null)
		{	
			plugin.debug("no type found!");
			return false;
		}
		
		if (!hasCreatePermission(type, p))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to create this RoutingBlock");
			return false;
		}
		String[] signs = readSigns(b);
		
		String conditions = signs[0].toLowerCase();
		String name = signs[1].toLowerCase();
		Location location = b.getLocation();
		
		if (type.hasSignConfig() && plugin.settingsmanager.isRoutingBlock(name))
		{	
			p.sendRawMessage(ChatColor.AQUA + "Name already exists!");
			plugin.debug("name already exists: {0}", name);
			return false;
		}
		if (name.matches("#[a-zA-Z0-9]*"))
		{
			p.sendRawMessage(ChatColor.AQUA + "Invalid name!");
			plugin.debug("name shall not beginn with #: {0}", name);
			return false;
		}
		
		plugin.debug("{0},{1}", type.getTitel(), bysign);
		RoutingBlock rb = new RoutingBlock(location, p, conditions, name, type, bysign);
		rb.save();
		rb.reload();
		plugin.settingsmanager.putRoutingBlock(rb);
		
		plugin.debug("Block added");
		
		if (rb.hasSignConfig())
		{	
			plugin.automanager.update(rb);
			plugin.updateGraph();
		}
		
		p.sendRawMessage(ChatColor.AQUA + "Block added!");
		plugin.debug("Block added: {0}", rb.getId());
		return true;
	}
	
	public boolean remove(RoutingBlock b, Player p)
	{
		if (!hasBreakPermission(b, p))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to delete this RoutingBlock");
			return false;
		}
		
		if (b.hasSignConfig())
			plugin.automanager.remove(b);
		
		b.delete();
		plugin.settingsmanager.removeRoutingBlock(b);
		plugin.updateGraph();
		
		p.sendRawMessage(ChatColor.AQUA + "Block removed!");
		plugin.debug("Block removed: {0}", b.getId());
		return true;
	}
	
	public boolean update(RoutingBlock b, Player p)
	{
		if (!hasUpdatePermission(b, p))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to modify this RoutingBlock");
			return false;
		}
		
		String[] signs = readSigns(b.getLocation().getBlock());
		String conditions = signs[0].toLowerCase();
		String name = signs[1].toLowerCase();
		
		if (!(!plugin.settingsmanager.isRoutingBlock(name) || plugin.settingsmanager.getRoutingBlock(name).getId() == b.getId()))
		{	
			p.sendRawMessage(ChatColor.AQUA + "Name already exists!");
			plugin.debug("name already exists: {0}", name);
			return false;
		}
		if (name.matches("#[a-zA-Z0-9]*"))
		{
			p.sendRawMessage(ChatColor.AQUA + "Invalid name. Name starts with #!");
			plugin.debug("name shall not beginn with #: {0}", name);
			return false;
		}
		
		if (b.isDefinedBySign())
			b.updateTypeBySign();
		b.setName(name);
		b.setConditions(conditions);
		b.save();
		b.reload();
		
		if (b.hasSignConfig())
			plugin.automanager.update(b);
		
		plugin.updateGraph();
		
		p.sendRawMessage(ChatColor.AQUA + "Block updated!");
		plugin.debug("Block updated: {0}", b.getId());
		return true;
	}
	
	public boolean updateRails(RoutingBlock b, Player p)
	{
		if (!hasUpdatePermission(b, p))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to modify this RoutingBlock");
			return false;
		}
		
		plugin.debug("Updating only Rails...");

		plugin.automanager.update(b);
		plugin.updateGraph();
		
		p.sendRawMessage(ChatColor.AQUA + "Rails updated!");
		return true;
	}
	
	private String[] readSigns(Block b)
	{
		String[] retrn = {"", ""};

		String[] fromdirections = {"north", "east", "south", "west"};
		int signradius = plugin.settingsmanager.signradius;
		
		for (int d = 0; d<4; d++)	// all directions
		{
			for(int r = 1; r <= signradius ; r++) //X or Z direction from block
			{
				int x = 0;
				int z = 0;
				
				switch (d)
				{
				case 0:
					x = - r;
					z = 0;
					break;
				case 1:
					x = 0;
					z = - r;
					break;
				case 2:
					x = r;
					z = 0;
					break;
				case 3:
					x = 0;
					z = r;
					break;
				}
				
				for (int h = - signradius; h <= signradius ;h++) // every height
				{
					Block block = b.getRelative(x, h, z);
					
					if (block.getState() instanceof Sign)
					{
						plugin.debug("Sign found!");
						
						Sign sign = (Sign) block.getState();
						String[] lines = sign.getLines();
						
						for (int i = 0; i < lines.length; i++) // all lines from sign
						{
							// Option line
							if (lines[i].matches("\\[[a-zA-Z0-9]+:[a-zA-Z0-9]+\\]"))
							{
								plugin.debug("RegExp match!");
								
								String fromdir = fromdirections[d].toLowerCase();
								String[] string = lines[i].split(":");
								String condition = string[0].substring(1).toLowerCase();
								String todir = string[1].substring(0, string[1].length() - 1).toLowerCase();
								
								todir = plugin.util.getToDirection(fromdir, todir);
								if (!todir.equals(""))
									retrn[0] += fromdir + ":" + condition + ":" + todir + ";";
								plugin.debug("temp result: {0}", retrn[0]);
							}
							
							// Name line
							if (lines[i].matches("\\[name\\]"))
							{
								retrn[1] = lines[i+1];
								i++;
								plugin.debug("Name: {0}", retrn[1]);
							}
						}
					}	
				}
			}
		}
		
		return retrn;
	}
}
