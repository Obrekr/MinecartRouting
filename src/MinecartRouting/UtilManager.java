package MinecartRouting;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class UtilManager {
	
	MinecartRouting plugin;
	
	public UtilManager(MinecartRouting instance)
	{
		plugin = instance;
	}
	
	public boolean isOwner(Block b, Player p)
	{
		String query = "SELECT owner FROM `mr_blocks` WHERE x='"+b.getX()+"' AND y='"+b.getY()+"' AND z="+b.getZ()+" AND world='"+b.getWorld().getName()+"';";
		ResultSet result = plugin.database.select(query);
		String owner = null;
		try {
			if (result.next())
				owner = result.getString("owner");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (owner.equals(p.getName()))
		{
			if (plugin.settingsManager.getConfig().getBoolean("debug"))
				plugin.log("Owner: {0}", owner);
			return true;
		}
		
		return false;
	}
	
	public int getIdByName(String name)
	{
		String query = "SELECT id FROM mr_blocks WHERE name='"+name+"';";
		ResultSet result = plugin.database.select(query);
		int id = -1;
		try {
			if (result.next())
				id = result.getInt("id");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}
	
	public int getIdByBlock(Block b)
	{
		String query = "SELECT id FROM mr_blocks WHERE x='"+b.getX()+"' AND y='"+b.getY()+"' AND z="+b.getZ()+" AND world='"+b.getWorld().getName()+"';";
		ResultSet result = plugin.database.select(query);
		int id = -1;
		try {
			if (result.next())
				id = result.getInt("id");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return id;
	}

	public boolean hasRecheadDestination(Block b, Player p)
	{
		int id = getIdByBlock(b);
		int iddest = plugin.settingsManager.routes.get(p).getDestination();
		
		plugin.debug("Reaching destination? RoutingBlock: {0}, Destination: {1}", id, iddest);
		
		if (id == iddest)
			return true;
		return false;
	}
	
	public BlockFace StringToDirection(String direction)
	{
		if (direction.equals("north"))
			return BlockFace.NORTH;
		if (direction.equals("east"))
			return BlockFace.EAST;
		if (direction.equals("south"))
			return BlockFace.SOUTH;
		if (direction.equals("west"))
			return BlockFace.WEST;
		return BlockFace.SELF;
	}

	public BlockFace velocityToDirection(Vector vect)
	{
		
		if (vect.getBlockX() == -1)
			return BlockFace.SOUTH;
		if (vect.getBlockX() == 1)
			return BlockFace.NORTH;
		if (vect.getBlockZ() == 1)
			return BlockFace.EAST;
		if (vect.getBlockZ() == -1)
			return BlockFace.WEST;
		return BlockFace.SELF;
	}
	
	public Vector directionToVector(BlockFace direction)
	{
		Vector dir = new Vector(0,0,0);
		switch (direction)
		{
		case NORTH:
			dir = new Vector(BlockFace.NORTH.getModX(), BlockFace.NORTH.getModY(), BlockFace.NORTH.getModZ());
			break;
		case EAST:
			dir = new Vector(BlockFace.EAST.getModX(), BlockFace.EAST.getModY(), BlockFace.EAST.getModZ());
			break;
		case SOUTH:
			dir = new Vector(BlockFace.SOUTH.getModX(), BlockFace.SOUTH.getModY(), BlockFace.SOUTH.getModZ());
			break;
		case WEST:
			dir = new Vector(BlockFace.WEST.getModX(), BlockFace.WEST.getModY(), BlockFace.WEST.getModZ());
			break;
		}
		
		return dir;
	}

	public RoutingBlocks getType(Block b)
	{
		if ( plugin.settingsManager.getConfig().getBoolean("booster.enable") && (b.getTypeId() ==  plugin.settingsManager.getConfig().getInt("booster.block")) )
			return RoutingBlocks.BOOSTER;
		
		if ( plugin.settingsManager.getConfig().getBoolean("brake.enable") && (b.getTypeId() ==  plugin.settingsManager.getConfig().getInt("brake.block")) )
			return RoutingBlocks.BRAKE;
		
		if ( plugin.settingsManager.getConfig().getBoolean("catcher.enable") && (b.getTypeId() ==  plugin.settingsManager.getConfig().getInt("catcher.block")) )
			return RoutingBlocks.CATCHER;
		
		if ( plugin.settingsManager.getConfig().getBoolean("launcher.enable") && (b.getTypeId() ==  plugin.settingsManager.getConfig().getInt("launcher.block")) )
			return RoutingBlocks.LAUNCHER;
		
		if ( plugin.settingsManager.getConfig().getBoolean("switch.enable") && (b.getTypeId() ==  plugin.settingsManager.getConfig().getInt("switch.block")) )
			return RoutingBlocks.SWITCH;
		return null;
	}
	
	public String getTypeString(Block b)
	{
		String type = "";
		switch (getType(b))
		{
		case BOOSTER:
			type = "booster";
			break;
		case BRAKE:
			type = "brake";
			break;
		case CATCHER:
			type = "catcher";
		case LAUNCHER:
			type = "launcher";
			break;
		case SWITCH:
			type = "switch";
			break;
		}
		return type;
	}
	
	public boolean isEnabled(RoutingBlocks block)
	{
		switch (block)
		{
		case BOOSTER:
			if (plugin.settingsManager.getConfig().getBoolean("booster.enable"))
				return true;
			break;
		case BRAKE:
			if (plugin.settingsManager.getConfig().getBoolean("brake.enable"))
				return true;
			break;
		case CATCHER:
			if (plugin.settingsManager.getConfig().getBoolean("catcher.enable"))
				return true;
			break;
		case LAUNCHER:
			if (plugin.settingsManager.getConfig().getBoolean("launcher.enable"))
				return true;
			break;
		case SWITCH:
			if (plugin.settingsManager.getConfig().getBoolean("switch.enable"))
				return true;
			break;
		}
		return false;
	}
	
	public String getToDirection(String fromdir, String todir)
	{
		if (todir.equals("n") || todir.equals("north"))
			return "north";
		if (todir.equals("e") || todir.equals("east"))
			return "east";
		if (todir.equals("s") || todir.equals("south"))
			return "south";
		if (todir.equals("w") || todir.equals("west"))
			return "west";
		
		if (fromdir.equals("north"))
		{
			if (todir.equals("l") || todir.equals("left"))
				return "east";
			if (todir.equals("r") || todir.equals("right"))
				return "west";
			if (todir.equals("a") || todir.equals("ahead"))
				return "south";
		}
		if (fromdir.equals("east"))
		{
			if (todir.equals("l") || todir.equals("left"))
				return "south";
			if (todir.equals("r") || todir.equals("right"))
				return "north";
			if (todir.equals("a") || todir.equals("ahead"))
				return "west";
		}
		if (fromdir.equals("south"))
		{
			if (todir.equals("l") || todir.equals("left"))
				return "west";
			if (todir.equals("r") || todir.equals("right"))
				return "east";
			if (todir.equals("a") || todir.equals("ahead"))
				return "north";
		}
		if (fromdir.equals("west"))
		{
			if (todir.equals("l") || todir.equals("left"))
				return "north";
			if (todir.equals("r") || todir.equals("right"))
				return "south";
			if (todir.equals("a") || todir.equals("ahead"))
				return "east";
		}
		
		return "";
	}

	public Block getBlockById(int id) {
		String query = "SELECT x, y, z, world FROM mr_blocks WHERE id="+id+";";
		ResultSet result = plugin.database.select(query);
		try {
			if (result.next())
			{
				int x = result.getInt("x");
				int y = result.getInt("y");
				int z = result.getInt("z");
				World w = plugin.getServer().getWorld(result.getString("world"));
				Location loc = new Location(w, x, y, z);
				return loc.getBlock();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String getNameById(int id)
	{
		String query = "SELECT name FROM mr_blocks WHERE id="+id+";";
		ResultSet result = plugin.database.select(query);
		try {
			if (result.next())
			{	String str = result.getString("name");
				if (str != null)
					return str;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public String getNameByBlock(Block b)
	{
		if (!plugin.settingsManager.hasSignConfig(b))
			return "";
		String query = "SELECT name FROM mr_blocks WHERE x='"+b.getX()+"' AND y='"+b.getY()+"' AND z="+b.getZ()+" AND world='"+b.getWorld().getName()+"';";
		ResultSet result = plugin.database.select(query);
		try {
			if (result.next())
				if (result.next())
				{	String str = result.getString("name");
					if (str != null)
						return str;
				}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public boolean nameExists(String name)
	{
		if (name.equals(""))
			return false;
		String query = "SELECT id FROM mr_blocks WHERE name='"+name+"';";
		ResultSet result = plugin.database.select(query);
		try {
			if (result.next())
				return true;	
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void sendInfo(Block b, Player p)
	{
		// owner, name(#id), position, type,  nextblocks (distance), conditions
		String owner = "";
		String name = "";
		int id = -1;
		int x = b.getX();
		int y = b.getY();
		int z = b.getZ();
		String type = getTypeString(b).substring(0, 1).toUpperCase() + getTypeString(b).substring(1);
		int north = -1;
		int east = -1;
		int south = -1;
		int west = -1;
		int north_length = -1;
		int east_length = -1;
		int south_length = -1;
		int west_length = -1;
		String conditions = "";
		
		String query = "SELECT * FROM mr_blocks WHERE x='"+b.getX()+"' AND y='"+b.getY()+"' AND z="+b.getZ()+" AND world='"+b.getWorld().getName()+"';";
		ResultSet result = plugin.database.select(query);
		try {
			if (result.next())
			{
				id = result.getInt("id");
				owner = result.getString("owner");
				if (result.getString("name") != null)
					name = result.getString("name");
				if (plugin.util.getBlockById(result.getInt("north")) != null)
				{	north = result.getInt("north");
					north_length = result.getInt("north_length");
				}
				if (plugin.util.getBlockById(result.getInt("east")) != null)
				{	east = result.getInt("east");
					east_length = result.getInt("east_length");
				}
				if (plugin.util.getBlockById(result.getInt("south")) != null)
				{	south = result.getInt("south");
					south_length = result.getInt("south_length");
				}
				if (plugin.util.getBlockById(result.getInt("west")) != null)
				{	west = result.getInt("west");
					west_length = result.getInt("west_length");
				}
				if (result.getString("conditions") != null)
					conditions = result.getString("conditions");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		p.sendRawMessage(ChatColor.AQUA + "---------------------");
		p.sendRawMessage(ChatColor.GOLD + "Name: "+name+" (#"+id+")");
		p.sendRawMessage(ChatColor.GOLD + "Owner: "+owner+"");
		p.sendRawMessage(ChatColor.GOLD + "Position: "+x+" | "+y+" | "+z+"");
		p.sendRawMessage(ChatColor.GOLD + "Type: "+type+"");
		if (north != -1)
			p.sendRawMessage(ChatColor.GOLD + "North: "+getNameById(north)+" (#"+north+")("+north_length+" blocks)");
		if (east != -1)
			p.sendRawMessage(ChatColor.GOLD + "East: "+getNameById(east)+" (#"+east+")("+east_length+" blocks)");
		if (south != -1)	
			p.sendRawMessage(ChatColor.GOLD + "South: "+getNameById(south)+" (#"+south+")("+south_length+" blocks)");
		if (west != -1)
			p.sendRawMessage(ChatColor.GOLD + "West: "+getNameById(west)+" (#"+west+")("+west_length+" blocks)");
		if (plugin.settingsManager.hasSignConfig(b))
			p.sendRawMessage(ChatColor.GOLD + "Conditions: "+conditions);
		p.sendRawMessage(ChatColor.AQUA + "---------------------");
	}

	public boolean isRail(Block b) {
		if(b.getTypeId() == 66 || b.getTypeId() == 27 || b.getTypeId() == 28)
			return true;
		return false;
	}
}
