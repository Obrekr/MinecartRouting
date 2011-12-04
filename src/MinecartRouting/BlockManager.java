package MinecartRouting;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;


public class BlockManager {
	
	private MinecartRouting plugin;
	
	public BlockManager(MinecartRouting instance)
	{
		plugin = instance;
	}
	
	public boolean isRightPowered(Block b, Boolean ignoreredstone, Boolean redstone)
	{
		if (ignoreredstone)
			return true;
		if (redstone == false && !(b.isBlockIndirectlyPowered() || b.getRelative(BlockFace.UP).isBlockIndirectlyPowered()))
			return true;
		if  (redstone == true && (b.isBlockIndirectlyPowered() || b.getRelative(BlockFace.UP).isBlockIndirectlyPowered()))
			return true;
		return false;
	}
	
	public RoutingBlock getRoutingBlock(Block b)
	{
		return plugin.settingsManager.routingblocks.get(b.getTypeId());
	}
	
	public Boolean hasSignConfig(Block b)
	{
		RoutingBlock rb = getRoutingBlock(b);
		if (rb == null)
			return false;
		return rb.hasSignConfig();
	}
	
	public Boolean hasPermission(RoutingBlock block, Player p, boolean create, boolean own)
	{
		List<String> names = block.getTypesStringList();
		if (names == null)
			return false;
		Boolean result = false;
		for (String name : names)
		{
			if (create)
			{
				if (own)
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
			}else{
				if (own)
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
		}
		return result;
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
	
	public BlockFace getDirection(Block b, MinecartRoutingMinecart v)
	{
		return getDirection(b, v, false, false);
	}
	
	public BlockFace getDirection(Block b, MinecartRoutingMinecart v, Boolean launching, Boolean catching)
	{
		Player p = v.owner;
		if (v.hasRoute() && plugin.util.hasRecheadDestination(b, v))
		{	
			v.removeRoute();
			p.sendRawMessage(ChatColor.AQUA + "Your cart #"+v.cart.getEntityId()+" has reached its destination!");
		}
		
		// get Conditions
		String query = "SELECT conditions FROM mr_blocks WHERE x='"+b.getX()+"' AND y='"+b.getY()+"' AND z="+b.getZ()+" AND world='"+b.getWorld().getName()+"';";
		ResultSet result = plugin.database.select(query);
		String dbresult = null;
		try {
			result.next();
			dbresult = result.getString("conditions");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String[] lines = dbresult.split(";");
		
		BlockFace[] from = new BlockFace[lines.length];
		String[] conditions = new String[lines.length];
		BlockFace[] to = new BlockFace[lines.length];
		
		for (int i = 0; i < lines.length; i++)
		{
			String fromdirection = lines[i].split(":")[0];
			String todirection = lines[i].split(":")[2];
			String options = lines[i].split(":")[1];
			
			from[i] = plugin.util.StringToDirection(fromdirection.toLowerCase());
			to[i] = plugin.util.StringToDirection(todirection.toLowerCase());
			conditions[i] = options.toLowerCase();
		}

		BlockFace fromdirection = plugin.util.velocityToDirection(v.cart.getVelocity().normalize());
		plugin.debug("Origin: {0}", fromdirection);
		
		BlockFace destination = BlockFace.SELF;
		
		for (int d = 0; d < from.length; d++)
		{
			if (from[d] == fromdirection || launching)
			{
				if ( (v.cart.getPassenger() instanceof Player) && (conditions[d].equals("#"+((Player) v.cart.getPassenger()).getDisplayName())) )
				{
					destination = to[d];
					break;
				}
				
				if ( v.cart.getPassenger() instanceof Player && conditions[d].equals("player") )
				{
					destination = to[d];
					break;
				}
					
				if ( (v.cart.getPassenger() instanceof Animals || v.cart.getPassenger() instanceof Monster) && conditions[d].equals("mob") )
				{
					destination = to[d];
					break;
				}
					
				if ( v.cart.isEmpty()  && conditions[d].equals("empty"))
				{
					destination = to[d];
					break;
				}
					
				if (v instanceof StorageMinecart  && conditions[d].equals("storage"))
				{
					destination = to[d];
					break;
				}
					
				if (v instanceof PoweredMinecart  && conditions[d].equals("powered"))
				{
					destination = to[d];
					break;
				}
					
				if (conditions[d].equals("default"))
				{
					destination = to[d];
					break;
				}
				
				if (conditions[d].equals("auto") && v.hasRoute())
				{
					plugin.debug("Starting AutoRouting...");
					destination = plugin.automanager.getDirection(b, p, v);
					if (!(destination == BlockFace.SELF))
						break;
				}
			}
		}
		
		if (catching && destination != BlockFace.SELF)
			destination = BlockFace.UP;
		
		plugin.debug("Destination: {0}", destination);
		
		return destination;	
	}
	

	

	
	public boolean exists(Block b)
	{
		String query = "SELECT id FROM mr_blocks WHERE x='"+b.getX()+"' AND y='"+b.getY()+"' AND z="+b.getZ()+" AND world='"+b.getWorld().getName()+"';";
		ResultSet result = plugin.database.select(query);
		try {
			if (result.next())
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean add(Block b, Player p)
	{
		RoutingBlock rb = getRoutingBlock(b);
		if (!hasPermission(rb, p, true, true))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to create this RoutingBlock");
			return false;
		}

		String owner = p.getName();
		String[] signs = readSigns(b);
		String conditions = signs[0].toLowerCase();
		String name = signs[1].toLowerCase();
		int x = b.getX();
		int y = b.getY();
		int z = b.getZ();
		String world = b.getWorld().getName();
		String type = rb.getTypeString();
		
		if (rb.hasSignConfig() && plugin.util.nameExists(name))
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
		
		String query = "INSERT INTO `mr_blocks` ('x', 'y', 'z','world', 'type', 'owner', 'conditions', 'name') VALUES("+x+","+y+","+z+",'"+world+"','"+type+"','"+owner+"','"+conditions+"', '"+name+"');";
		plugin.database.insert(query);
		
		if (rb.hasSignConfig())
			plugin.automanager.updateBlockInAllDirections(b);
		
		plugin.updateGraph();
		
		p.sendRawMessage(ChatColor.AQUA + "Block added!");
		plugin.debug("Block added: {0}", b.getLocation().toString());
		return true;
	}
	
	public boolean remove(Block b, Player p)
	{
		RoutingBlock rb = getRoutingBlock(b);
		if (!( (hasPermission(rb, p, false, true) && plugin.util.isOwner(b, p)) || hasPermission(rb, p, false, false) ))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to delete this RoutingBlock");
			return false;
		}
		
		List<Integer> toupdate = new ArrayList<Integer>();
		if (rb.hasSignConfig())
		{
			int blockid = plugin.util.getIdByBlock(b);
			String query = "SELECT id FROM mr_blocks WHERE north="+blockid+" OR west="+blockid+" OR east="+blockid+" OR south="+blockid+";";
			ResultSet result = plugin.database.select(query);
			try {
				while(result.next())
				{
					toupdate.add(result.getInt("id"));
				}
				plugin.debug("Blocks needing update: {0}", toupdate.toString());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		String query = "DELETE FROM `mr_blocks` WHERE x='"+b.getX()+"' AND y='"+b.getY()+"' AND z="+b.getZ()+" AND world='"+b.getWorld().getName()+"';";
		plugin.database.delete(query);
		
		if (rb.hasSignConfig())
			plugin.automanager.remove(toupdate);
		
		plugin.updateGraph();
		
		p.sendRawMessage(ChatColor.AQUA + "Block removed!");
		plugin.debug("Block removed: {0}", b.getLocation().toString());
		return true;
	}
	
	public boolean update(Block b, Player p)
	{
		RoutingBlock rb = getRoutingBlock(b);
		if (!( (hasPermission(rb, p, true, true) && plugin.util.isOwner(b, p)) ||hasPermission(rb, p, true, false) ))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to modify this RoutingBlock");
			return false;
		}
		
		String[] signs = readSigns(b);
		String conditions = signs[0].toLowerCase();
		String name = signs[1].toLowerCase();
		
		if (!(!plugin.util.nameExists(name) || plugin.util.getIdByName(name) == plugin.util.getIdByBlock(b)))
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
		
		String query = "UPDATE `mr_blocks` SET conditions='"+conditions+"', name='"+name+"' WHERE x='"+b.getX()+"' AND y='"+b.getY()+"' AND z="+b.getZ()+" AND world='"+b.getWorld().getName()+"';";
		plugin.database.update(query);
		
		if (rb.hasSignConfig())
			plugin.automanager.updateBlockInAllDirections(b);
		
		plugin.updateGraph();
		
		p.sendRawMessage(ChatColor.AQUA + "Block updated!");
		plugin.debug("Block updated: {0}", b.getLocation().toString());
		return true;
	}
	
	public boolean updateRails(Block b, Player p)
	{
		RoutingBlock rb = getRoutingBlock(b);
		if (!( (hasPermission(rb, p, true, true) && plugin.util.isOwner(b, p)) || hasPermission(rb, p, true, false) ))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to modify this RoutingBlock");
			return false;
		}
		
		plugin.debug("Updating only Rails...");
		plugin.automanager.updateBlockInAllDirections(b);
		plugin.updateGraph();
		
		p.sendRawMessage(ChatColor.AQUA + "Rails updated!");
		return true;
	}
	
	private String[] readSigns(Block b)
	{
		String[] retrn = {"", ""};

		String[] fromdirections = {"north", "east", "south", "west"};
		int signradius = plugin.settingsManager.getConfig().getInt("sign-radius");
		
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
								
								String fromdir = fromdirections[d];
								String[] string = lines[i].split(":");
								String condition = string[0].substring(1);
								String todir = string[1].substring(0, string[1].length() - 1);
								
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
