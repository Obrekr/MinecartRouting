package MinecartRouting;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

public class BlockManager {
	
	private MinecartRouting plugin;
	
	public BlockManager(MinecartRouting instance)
	{
		plugin = instance;
	}
	
	public void booster(Block b, Vehicle v, Boolean isPowered)
	{
		if (!positionChanged(v, b))
			return;
		
		Player p = plugin.settingsManager.owner.get(v);
		if (!( (p.hasPermission("minecartrouting.benefit.booster.own") && plugin.util.isOwner(b, p)) || p.hasPermission("minecartrouting.benefit.booster.other")))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to use this RoutingBlock");
			return;
		}
		
		// booster needs redstone and if is powered
		if (!plugin.settingsManager.getConfig().getBoolean("booster.needredstone") || b.isBlockPowered())
		{
			plugin.debug("Boosting...");
			speed(v, (double) plugin.settingsManager.getConfig().getInt("booster.speed"), plugin.settingsManager.getConfig().getBoolean("booster.relative"));
		}
		
		//booster needs redstone, is unpowered, brakewhenunpowered is enabled
		if (plugin.settingsManager.getConfig().getBoolean("booster.needredstone") && plugin.settingsManager.getConfig().getBoolean("booster.brakewhenunpowered.enable") && !b.isBlockPowered())
		{
			plugin.debug("Brakeing...");
			speed(v, (double) plugin.settingsManager.getConfig().getInt("booster.brakewhenunpowered.speed"), plugin.settingsManager.getConfig().getBoolean("boost.brakewhenunpowered.relative"));
		}
	}
	
	public void brake(Block b, Vehicle v, Boolean isPowered)
	{
		if (!positionChanged(v, b))
			return;
		
		Player p = plugin.settingsManager.owner.get(v);
		if (!( (p.hasPermission("minecartrouting.benefit.brake.own") && plugin.util.isOwner(b, p)) || p.hasPermission("minecartrouting.benefit.brake.other")))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to use this RoutingBlock");
			return;
		}
		
		// Brake needs redstone and if is powered
		if (!plugin.settingsManager.getConfig().getBoolean("brake.needredstone") || isPowered)
		{
			plugin.debug("Brakeing...");
			speed(v, (double) plugin.settingsManager.getConfig().getInt("brake.speed"), plugin.settingsManager.getConfig().getBoolean("brake.relative"));
		}
		
		//brake needs redstone, is unpowered, boostwhenunpowered is enabled
		if (plugin.settingsManager.getConfig().getBoolean("brake.needredstone") && plugin.settingsManager.getConfig().getBoolean("brake.boostwhenunpowered.enable") && !isPowered)
		{
			plugin.debug("Boosting...");
			speed(v, (double) plugin.settingsManager.getConfig().getInt("brake.boostwhenunpowered.speed"), plugin.settingsManager.getConfig().getBoolean("brake.boostwhenunpowered.relative"));
		}
	}
	
	public void launcher(Block b, Vehicle v, Boolean isPowered)
	{
		Player p = plugin.settingsManager.owner.get(v);
		if (!( (p.hasPermission("minecartrouting.benefit.launcher.own") && plugin.util.isOwner(b, p)) || p.hasPermission("minecartrouting.benefit.launcher.other")))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to use this RoutingBlock");
			return;
		}
		
		// Launcher needs redstone and if is powered
		if (!plugin.settingsManager.getConfig().getBoolean("launcher.needredstone") || isPowered)
		{
			if (!positionChanged(v, b))
				return;
			plugin.debug("Launching...");
			launch(v,  plugin.util.directionToVector(getDirection(b,v, true, false)), (double) plugin.settingsManager.getConfig().getInt("launcher.speed"), plugin.settingsManager.getConfig().getBoolean("launcher.relative"));
		}
		//Launcher needs redstone, is unpowered, catchwhenunpowered is enabled
		if (plugin.settingsManager.getConfig().getBoolean("launcher.needredstone") && plugin.settingsManager.getConfig().getBoolean("launcher.catchwhenunpowered.enable") && !isPowered)
		{
			if (!positionChanged(v, b, true))
				return;
			plugin.debug("Catching...");
			stop(v, b, getDirection(b,v, false, true));		
		}
	}
	
	public void catcher(Block b, Vehicle v, Boolean isPowered)
	{
		Player p = plugin.settingsManager.owner.get(v);
		if (!( (p.hasPermission("minecartrouting.benefit.catcher.own") && plugin.util.isOwner(b, p)) || p.hasPermission("minecartrouting.benefit.catcher.other")))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to use this RoutingBlock");
			return;
		}
		
		// Catcher needs redstone and if is powered
		if (!plugin.settingsManager.getConfig().getBoolean("catcher.needredstone") || isPowered)
		{
			if (!positionChanged(v, b, true))
				return;
			plugin.debug("Catching...");
			stop(v, b, getDirection(b, v, false, true));
		}
		// Catcher needs redstone, is unpowered, launchwhenunpowered is enabled
		if (plugin.settingsManager.getConfig().getBoolean("catcher.needredstone") && plugin.settingsManager.getConfig().getBoolean("catcher.launchwhenunpowered.enable") && !isPowered)
		{
			if (!positionChanged(v, b))
				return;
			plugin.debug("Launching...");
			launch(v, plugin.util.directionToVector(getDirection(b, v, true, false)), (double) plugin.settingsManager.getConfig().getInt("catcher.launchwhenunpowered.speed"), plugin.settingsManager.getConfig().getBoolean("catcher.launchwhenunpowered.relative"));		
		}
	}
	
	public void switcher(Block b, Vehicle v, Boolean isPowered)
	{
		if (!positionChanged(v, b))
			return;
		
		Player p = plugin.settingsManager.owner.get(v);
		if (!( (p.hasPermission("minecartrouting.benefit.switch.own") && plugin.util.isOwner(b, p)) || p.hasPermission("minecartrouting.benefit.switch.other")))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to use this RoutingBlock");
			return;
		}
		
		
		if (!(!plugin.settingsManager.getConfig().getBoolean("switch.needredstone") || isPowered))
				return;
		plugin.debug("Switching...");
		
		BlockFace destination = getDirection(b, v);
		BlockFace origin = plugin.util.velocityToDirection(v.getVelocity().normalize());
		Block rail = b.getRelative(0,1, 0);
		
		byte raildata = rail.getData();
		if ( (destination == BlockFace.NORTH && origin == BlockFace.SOUTH ) || (destination == BlockFace.SOUTH && origin == BlockFace.NORTH) )
			raildata = 1;	// North-South
		if ( (destination == BlockFace.EAST && origin == BlockFace.WEST) || (destination == BlockFace.WEST && origin == BlockFace.EAST) )
			raildata = 0;	// East-West
		if ( (destination == BlockFace.NORTH && origin == BlockFace.EAST) || (destination == BlockFace.EAST && origin == BlockFace.NORTH) )
			raildata = 8;	// North-East-Corner
		if ( (destination == BlockFace.SOUTH && origin == BlockFace.EAST) || (destination == BlockFace.EAST && origin == BlockFace.SOUTH) )
			raildata = 9;	// South-East-Corner
		if ( (destination == BlockFace.SOUTH && origin == BlockFace.WEST) || (destination == BlockFace.WEST && origin == BlockFace.SOUTH) )
			raildata = 6;	// South-West-Corner
		if ( (destination == BlockFace.NORTH && origin == BlockFace.WEST) || (destination == BlockFace.WEST && origin == BlockFace.NORTH) )
			raildata = 7;	// North-West-Corner
		rail.setData(raildata);
		plugin.debug("Raildata: {0}", raildata);
		plugin.settingsManager.vehicles.forcePut(v, v.getLocation());
		
	}
	
	private void speed(Vehicle v, double speed, boolean relative)
	{
		// new speed relative to previous speed 
		if (relative)
		{
			double multiplier = speed / 100.0;
			Vector newVelocity = v.getVelocity().multiply(multiplier);
			v.setVelocity(newVelocity);
		}else{// new speed non-relative to previous speed
			double maxmultiplier = plugin.settingsManager.getConfig().getInt("max-speed") / 20.0;
			double multiplier = speed / 100.0 * maxmultiplier;
			Vector newVelocity = v.getVelocity().normalize().multiply(multiplier);
			v.setVelocity(newVelocity);

			plugin.debug("Speed: {0}, Multiplier: {1}", speed, multiplier);
		}
		
		plugin.settingsManager.vehicles.forcePut(v, v.getLocation());
	}
	
	private void launch(Vehicle v, Vector direction, double speed, boolean relative)
	{
		plugin.debug("Direction-Vector: {0}", direction.toString());
		v.setVelocity(direction);
		speed(v, speed, relative);	
		
		plugin.settingsManager.vehicles.forcePut(v, v.getLocation());
	}
	
	private void stop(Vehicle v, Block b, BlockFace direction)
	{
		if (!(direction == BlockFace.UP))
			return;
		
		v.setVelocity(new Vector(0,0,0));
		plugin.settingsManager.vehicles.forcePut(v, v.getLocation());
			
		Location loc = b.getRelative(BlockFace.UP).getLocation();
		loc.setX(loc.getX() + 0.5);
		loc.setY(loc.getY() + 0.5);
		loc.setZ(loc.getZ() + 0.5);
		v.teleport(loc);
			
		plugin.debug("Stopped! Location: {0}", loc.toString());
		
	}
	
	private BlockFace getDirection(Block b, Vehicle v)
	{
		return getDirection(b, v, false, false);
	}
	
	private BlockFace getDirection(Block b, Vehicle v, Boolean launching, Boolean catching)
	{
		// Reached Destination?
		if (v.getPassenger() instanceof Player)
		{
			Player p = (Player) v.getPassenger();
			if (plugin.settingsManager.routes.containsKey(p) && plugin.util.hasRecheadDestination(b, p))
			{	
				plugin.settingsManager.routes.remove(p);
				p.sendRawMessage(ChatColor.AQUA + "You have reached your destination!");
			}
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
			
			from[i] = plugin.util.StringToDirection(fromdirection);
			to[i] = plugin.util.StringToDirection(todirection);
			conditions[i] = options;
			
			plugin.debug("From{0}: {1}", i, fromdirection);
			plugin.debug("Option{0}: {1}", i, options);
			plugin.debug("To{0}: {1}", i, todirection);
		}

		BlockFace fromdirection = plugin.util.velocityToDirection(v.getVelocity().normalize());
		plugin.debug("Origin: {0}", fromdirection);
		
		BlockFace destination = BlockFace.SELF;
		
		for (int d = 0; d < from.length; d++)
		{
			if (from[d] == fromdirection || launching)
			{
				if ( (v.getPassenger() instanceof Player) && (conditions[d].equals("#"+((Player) v.getPassenger()).getDisplayName())) )
				{
					destination = to[d];
					break;
				}
				
				if ( v.getPassenger() instanceof Player && conditions[d].equals("player") )
				{
					destination = to[d];
					break;
				}
					
				if ( (v.getPassenger() instanceof Animals || v.getPassenger() instanceof Monster) && conditions[d].equals("mob") )
				{
					destination = to[d];
					break;
				}
					
				if ( v.isEmpty()  && conditions[d].equals("empty"))
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
				
				if (conditions[d].equals("auto") && v.getPassenger() instanceof Player)
				{
					plugin.debug("Starting AutoRouting...");
					Player p = (Player) v.getPassenger();
					if (plugin.settingsManager.routes.containsKey(p))
					{
						destination = plugin.automanager.getDirection(b, p);
						if (!(destination == BlockFace.SELF))
							break;
					}
				}
			}
		}
		
		if (catching && destination != BlockFace.SELF)
			destination = BlockFace.UP;
		
		plugin.debug("Destination: {0}", destination);
		
		return destination;	
	}
	

	

	
	private boolean positionChanged(Vehicle v, Block b)
	{
		return positionChanged(v, b, false);
	}
	
	private boolean positionChanged(Vehicle v, Block b, boolean catcher)
	{
		Location oldloc = plugin.settingsManager.vehicles.get(v);
		Location oldblock = plugin.settingsManager.passedBlocks.get(v);
		
		if (b.getLocation().equals(oldblock))
		{
				plugin.debug("Same RoutingBlock!");
		}else{
		
			plugin.debug("New RoutingBlock!");
			plugin.settingsManager.passedBlocks.remove(v);
			plugin.settingsManager.passedBlocks.put(v, b.getLocation());
			return true;
		}
		
		if (catcher && v.getLocation().distance(oldloc) > 0.2 && b.getBlockPower() == 0)
		{
			plugin.debug("ReCatching: \n{0}\n{1}", oldloc.toString(), v.getLocation().toString());
			return true;
		}
		return false;
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
		if (!p.hasPermission("minecartrouting.create."+plugin.util.getTypeString(b)+".own"))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to create this RoutingBlock");
			return false;
		}
		
		String owner = p.getName();
		String[] signs = readSigns(b);
		String conditions = signs[0];
		String name = signs[1];
		int x = b.getX();
		int y = b.getY();
		int z = b.getZ();
		String world = b.getWorld().getName();
		String type = plugin.util.getTypeString(b);
		
		if (plugin.util.nameExists(name))
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
		
		if (plugin.settingsManager.hasSignConfig(b))
			plugin.automanager.updateBlockInAllDirections(b);
		
		p.sendRawMessage(ChatColor.AQUA + "Block added!");
		plugin.debug("Block added: {0}", b.getLocation().toString());
		return true;
	}
	
	public boolean remove(Block b, Player p)
	{
		if (!( (p.hasPermission("minecartrouting.break."+plugin.util.getTypeString(b)+".own") && plugin.util.isOwner(b, p)) || p.hasPermission("minecartrouting.benefit."+plugin.util.getTypeString(b)+".other")))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to delete this RoutingBlock");
			return false;
		}
		
		List<Integer> toupdate = new ArrayList<Integer>();
		if (plugin.settingsManager.hasSignConfig(b))
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
		
		if (plugin.settingsManager.hasSignConfig(b))
			plugin.automanager.remove(toupdate);
		
		p.sendRawMessage(ChatColor.AQUA + "Block removed!");
		plugin.debug("Block removed: {0}", b.getLocation().toString());
		return true;
	}
	
	public boolean update(Block b, Player p)
	{
		if (!( (p.hasPermission("minecartrouting.create."+plugin.util.getTypeString(b)+".own") && plugin.util.isOwner(b, p)) || p.hasPermission("minecartrouting.create."+plugin.util.getTypeString(b)+".other")))
		{	
			p.sendMessage(ChatColor.DARK_RED + "Don't have permission to modify this RoutingBlock");
			return false;
		}
		
		String[] signs = readSigns(b);
		String conditions = signs[0];
		String name = signs[1];
		
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
		
		if (plugin.settingsManager.hasSignConfig(b))
			plugin.automanager.updateBlockInAllDirections(b);
		
		p.sendRawMessage(ChatColor.AQUA + "Block updated!");
		plugin.debug("Block updated: {0}", b.getLocation().toString());
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
