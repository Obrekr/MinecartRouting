package MinecartRouting;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import MinecartRouting.Flags.ActionTimes;
import MinecartRouting.Flags.Flag;


public class RoutingBlock
{
	private static MinecartRouting plugin = (MinecartRouting) Bukkit.getServer().getPluginManager().getPlugin("MinecartRouting");
	private Integer id;
	private Integer blockid;
	private String name;
	private RoutingBlockType type;
	private Location location;
	private String owner;
	private String conditions;
	private Date lastRedstoneTime;
	
	private Map<BlockFace, List<String>> from_options = new HashMap<BlockFace, List<String>>();
	private Map<String, BlockFace> fromoptions_to = new HashMap<String, BlockFace>();
	
	private List<BlockFace> autodirections = new ArrayList<BlockFace>();
	private Map<Integer, BlockFace> nextface = new HashMap<Integer, BlockFace>();
	private Map<BlockFace, Integer> nextid = new HashMap<BlockFace, Integer>();
	private Map<BlockFace, Integer> nextdistance = new HashMap<BlockFace, Integer>();
	
	public RoutingBlock(Location loc, Player p, String cond, String nam)
	{
		location = loc;
		blockid = loc.getBlock().getTypeId();
		owner = p.getName();
		name = nam;
		conditions = cond;
		type = plugin.settingsmanager.getRoutingBlockType(blockid);
	}
	
	public RoutingBlock(Integer i)
	{
		id = i;
		reloadFromId();
	}
	
	public RoutingBlock(Block b)
	{
		location = b.getLocation();
		reloadFromLoc();
	}
	
	public void doActions(MinecartRoutingMinecart cart, ActionTimes time)
	{
		for (Flag flag : type.getFlags())
		{
			if (flag.getActionTime().equals(time))
				flag.doAction(this, cart);
		}
		cart.setLastBlock(this, time);
		
	}

	public void reload()
	{
		from_options.clear();
		fromoptions_to.clear();
		
		autodirections.clear();
		nextface.clear();
		nextid.clear();
		nextdistance.clear();
		
		if (id != null)
			reloadFromId();
		else if (location != null)
			reloadFromLoc();
	}
	
	public void save()
	{
		if (id == null)
		{	String query = "INSERT INTO `mr_blocks` ('x', 'y', 'z','world', 'owner') VALUES("+location.getBlockX()+","+location.getBlockY()+","+location.getBlockZ()+",'"+location.getWorld().getName()+"','"+owner+"');";
			plugin.database.insert(query);
		}

		String query = "UPDATE `mr_blocks` SET conditions='"+conditions+"', name='"+name+"' WHERE x='"+location.getBlockX()+"' AND y='"+location.getBlockY()+"' AND z="+location.getBlockZ()+" AND world='"+location.getWorld().getName()+"';";
		plugin.database.update(query);

		BlockFace[] dir = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		for (BlockFace face : dir)
		{
			if (nextid.containsKey(face))
			{
				if (!nextdistance.containsKey(face))
					nextdistance.put(face, 0);
				String quer = "UPDATE `mr_blocks` SET "+face.toString().toLowerCase()+"='"+nextid.get(face)+"', "+face.toString().toLowerCase()+"_length='"+nextdistance.get(face)+"' WHERE x='"+location.getBlockX()+"' AND y='"+location.getBlockY()+"' AND z="+location.getBlockZ()+" AND world='"+location.getWorld().getName()+"';";
				plugin.database.update(quer);
			}else{
				String quer = "UPDATE `mr_blocks` SET "+face.toString().toLowerCase()+"=NULL, "+face.toString().toLowerCase()+"_length=NULL WHERE x='"+location.getBlockX()+"' AND y='"+location.getBlockY()+"' AND z="+location.getBlockZ()+" AND world='"+location.getWorld().getName()+"';";
				plugin.database.update(quer);
			}
		}	
	}
	
	public void delete()
	{
		String query = "DELETE FROM `mr_blocks` WHERE id='"+id+"';";
		plugin.database.delete(query);
		plugin.settingsmanager.removeRoutingBlock(this);
	}
	
	public void clearNextBlocks()
	{
		nextdistance.clear();
		nextface.clear();
		nextid.clear();
		
		plugin.debug("AutoDirections: {0}", autodirections.toString());
	}
	
	public int getId()
	{
		return id;
	}
	
	public Block getBlock()
	{
		return location.getBlock();
	}
	
	public int getNextId(BlockFace face)
	{
		return nextid.get(face);
	}
	
	public BlockFace getNextFace(int id)
	{
		plugin.debug("faces: {0}", nextface.toString());
		return nextface.get(id);
	}
	
	public Set<BlockFace> getAllNextFaces()
	{
		return nextid.keySet();
	}
	
	public int getNextDistance(BlockFace face)
	{
		return nextdistance.get(face);
	}
	
	public Location getLocation()
	{
		return location;
	}

	public List<String> getFlagStringList()
	{
		return type.getFlagStringList();
	}
	
	public Map<BlockFace, List<String>> getFrom_options()
	{
		return from_options;
	}
	
	public BlockFace getToDirection(BlockFace from, String option)
	{
		return fromoptions_to.get((from.toString()+option).toLowerCase());
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getOwner()
	{
		return owner;
	}
	
	public String getFlagsString()
	{
		return type.getFlagsString();
	}
	
	public String getConditions()
	{
		return conditions;
	}
	
	public void setName(String n)
	{
		name = n;
	}
	
	public void setConditions(String c)
	{
		conditions = c;
	}
	
	public void setLastRedstoneTime()
	{
		lastRedstoneTime = new Date();
	}
	
	public void setNext(BlockFace face, int id, int distance)
	{
		nextid.put(face, id);
		nextdistance.put(face, distance);
	}
	
	public void unsetNext(BlockFace face)
	{
		nextid.remove(face);
		nextdistance.remove(face);
	}
	
	public boolean hasSignConfig()
	{
		return type.hasSignConfig();
	}
	
	public boolean hasName()
	{
		if (!name.equals(""))
			return true;
		return false;
	}
	
	public boolean isAutoDirection(BlockFace face)
	{
		if (autodirections.contains(face))
			return true;
		return false;
	}
	
	public boolean isOwner(Player p)
	{
		if (owner.equals(p.getName()))
			return true;
		return false;
	}
	
	public boolean isValid()
	{
		if (id != null && blockid != null && type != null && location != null && owner != null && name != null && conditions != null)
			return true;
		
		return false;
	}

	public boolean isRedstoneTime()
	{
		if (lastRedstoneTime == null)
			return true;
		if (new Date().getTime() - lastRedstoneTime.getTime() > 350)
			return true;
		return false;
	}
	
	private void reloadFromLoc()
	{
		String query = "SELECT * FROM mr_blocks WHERE x='"+location.getBlockX()+"' AND y='"+location.getBlockY()+"' AND z="+location.getBlockZ()+" AND world='"+location.getWorld().getName()+"';";
		ResultSet result = plugin.database.select(query);
		try {
			if (result.next())
				setFromResult(result);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void reloadFromId()
	{
		String query = "SELECT * FROM mr_blocks WHERE id='"+id+"';";
		ResultSet result = plugin.database.select(query);
		try {
			if (result.next())
				setFromResult(result);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void setFromResult(ResultSet result) throws SQLException
	{
		id = result.getInt("id");
		owner = result.getString("owner");
		if (result.getString("conditions") != null)
			conditions = result.getString("conditions");
		else
			conditions = "";
		
		if (result.getString("name") != null)
			name = result.getString("name");
		else
			name = "";
		
		BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		for (BlockFace face : faces)
		{
			if (result.getString(face.toString().toLowerCase()) != null)
			{	
				nextid.put(face, result.getInt(face.toString().toLowerCase()));
				nextface.put(result.getInt(face.toString().toLowerCase()), face);
				nextdistance.put(face, result.getInt(face.toString().toLowerCase()+"_length"));
			}
		}
		
		Integer x = result.getInt("x");
		Integer y = result.getInt("y");
		Integer z = result.getInt("z");
		String world = result.getString("world");
		if (x != null && y != null && z != null && world != null)
		{
			location = new Location(Bukkit.getWorld(world), x, y, z);
			blockid = location.getBlock().getTypeId();
			type = plugin.settingsmanager.getRoutingBlockType(blockid);
		}
		
		parseConditions();
		
	}
	
	private void parseConditions()
	{
		if (conditions.equals(""))
			return;
		
		String[] lines = conditions.split(";");
		for (int i = 0; i < lines.length; i++)
		{
			BlockFace fromdirection = plugin.util.StringToDirection(lines[i].split(":")[0].toLowerCase());
			BlockFace todirection = plugin.util.StringToDirection(lines[i].split(":")[2].toLowerCase());
			String option = lines[i].split(":")[1];
			
			if (option.toLowerCase().equals("auto"))
				autodirections.add(todirection);
			
			if (!from_options.containsKey(fromdirection))
				from_options.put(fromdirection, new ArrayList<String>());

			List<String> options = from_options.get(fromdirection);
			options.add(option);
			fromoptions_to.put((fromdirection.toString()+option).toLowerCase(), todirection);
		}
	}
}
