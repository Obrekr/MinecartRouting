package MinecartRouting;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;

import MinecartRouting.Flags.Flags;


public class SettingsManager {

	private static MinecartRouting plugin;
	
	private static FileConfiguration config;
	private static Map<Flags, Integer> flagcounts = new HashMap<Flags, Integer>();
	
	private Map<Integer, MinecartRoutingMinecart> vehicles = new HashMap<Integer, MinecartRoutingMinecart>();
	private Map<Integer, RoutingBlockType> routingblocktypes = new HashMap<Integer, RoutingBlockType>();
	private Map<String, RoutingBlockType> routingblocktypenames = new HashMap<String, RoutingBlockType>();
	private Map<Location, RoutingBlock> blocksbylocation = new HashMap<Location, RoutingBlock>();
	private Map<Integer, RoutingBlock> blocksbyid = new HashMap<Integer, RoutingBlock>();
	private Map<String, RoutingBlock> blocksbyname = new HashMap<String, RoutingBlock>();
	
	public boolean debug;
	public int toolitem;
	public int signradius;
	public int maxspeed;
	public boolean slowwhenempty;
	public boolean signcandefinetype;

	public SettingsManager(MinecartRouting instance)
	{
		plugin = instance;
	}
		
	@SuppressWarnings("unchecked")
	public void load()
	{
		config = plugin.getConfig();
	    File main = new File(plugin.getDataFolder() + File.separator + "config.yml");

	    boolean exists = (main).exists();

	    if (exists)
	    {
	    	try
	        {
	    		config.options().copyDefaults(true);
	            String content = getConfigFileContent(main);
	    		config.loadFromString(content);       
	        }
	        catch (Exception e)
	        {
	                e.printStackTrace();
	        }
	     }
	     else
	     {
	        config.options().copyDefaults(true);
	        try {
				config.save(main);
			} catch (IOException e) {
				e.printStackTrace();
			}
	        plugin.log("Config created!");
	     } 
	    
	    List<LinkedHashMap<String, Object>> configroutingblocks = (List<LinkedHashMap<String, Object>>) config.get("routingblocks");
	    debug = config.getBoolean("debug");
	    toolitem = config.getInt("tool-item");
	    signradius = config.getInt("sign-radius");
		maxspeed = config.getInt("max-speed");
		slowwhenempty = config.getBoolean("slow-when-empty");
		signcandefinetype = config.getBoolean("sign-can-define-type");
		
		addRoutingBlockTypes(configroutingblocks);
		loadRoutingBlocks();
		plugin.log("Config loaded!");
	}
	
	private String getConfigFileContent(File main) {
		try
		{
			String result = "";
			
			for (Flags type : Flags.values())
				flagcounts.put(type, 0);
			
			FileInputStream file = new FileInputStream(main);
			DataInputStream in = new DataInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null)
			{
				for (Flags type : Flags.values())
				{
					String name = type.toString().toLowerCase();
					if (line.contains(name))
					{	
						Integer count = flagcounts.get(type);
						count++;
						line = line.replace(name, name + count.toString());
						flagcounts.put(type, count);
					}
				}
				result += line + "\n";
			}
			return result;
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	private void addRoutingBlockTypes(List<LinkedHashMap<String, Object>> maps)
	{
		if (maps == null)
			return;
		for (LinkedHashMap<String, Object> map : maps)
		{
			RoutingBlockType block = new RoutingBlockType(map, flagcounts, plugin);
			if (block.isValid())	 
			{	
				routingblocktypes.put(block.getBlockId(), block);
				routingblocktypenames.put(block.getTitel().toLowerCase(), block);
			}
			else
				plugin.debug("invalid block found: {0}\n {1}", block.toString(), map);
		}
		plugin.debug("Blocks: {0}\n", routingblocktypes.toString());
		plugin.debug("All type names: {0}", routingblocktypenames.keySet());
	}
	
	public void loadRoutingBlocks()
	{
		blocksbylocation.clear();
		blocksbyid.clear();
		blocksbyname.clear();
		String query = "SELECT id FROM mr_blocks";
		ResultSet result = plugin.database.select(query);
		try {
			while (result.next())
			{
				RoutingBlock block = new RoutingBlock(result.getInt("id"));
				if (block.isValid())
					putRoutingBlock(block);
				else
					plugin.debug("invalid block found on loading");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void putRoutingBlock(RoutingBlock b)
	{
		blocksbylocation.put(b.getLocation(), b);
		blocksbyid.put(b.getId(), b);
		if (b.hasName())
			blocksbyname.put(b.getName(), b);
	}
	
	public void putMinecart(MinecartRoutingMinecart c)
	{
		vehicles.put(c.getId(), c);
	}
	
	public void removeRoutingBlock(RoutingBlock b)
	{
		blocksbylocation.remove(b.getLocation());
		blocksbyid.remove(b.getId());
		if (b.hasName())
			blocksbyname.remove(b.getName());
	}
	
	public void removeMinecart(Vehicle v)
	{
		vehicles.remove(v.getEntityId());
	}

	public boolean isRoutingBlock(Location loc)
	{
		if (blocksbylocation.containsKey(loc))
			return true;
		return false;
	}
	
	public boolean isRoutingBlock(int id)
	{
		if (blocksbyid.containsKey(id))
			return true;
		return false;
	}
	
	public boolean isRoutingBlock(String name)
	{
		if (blocksbyname.containsKey(name))
			return true;
		return false;
	}
	
	public boolean isMinecart(Vehicle v)
	{
		if (v instanceof Minecart && vehicles.containsKey(v.getEntityId()))
			return true;
		return false;
	}
	
	public boolean isMinecart(Entity e)
	{
		if (e instanceof Minecart && vehicles.containsKey(e.getEntityId()))
			return true;
		return false;
	}
	
	public boolean isRoutingBlockType(Block b)
	{
		return routingblocktypes.containsKey(b.getTypeId());
	}
	
	public boolean isRoutingBlockType(String s)
	{
		return routingblocktypenames.containsKey(s);
	}
	
	public RoutingBlock getRoutingBlock(Location loc)
	{
		return blocksbylocation.get(loc);
	}
	
	public RoutingBlock getRoutingBlock(int id)
	{
		return blocksbyid.get(id);
	}
	
	public RoutingBlock getRoutingBlock(String name)
	{
		return blocksbyname.get(name);
	}
	
	public MinecartRoutingMinecart getMinecart(Vehicle v)
	{
		return vehicles.get(v.getEntityId());
	}
	
	public MinecartRoutingMinecart getMinecart(Entity e)
	{
		return vehicles.get(e.getEntityId());
	}
	
	public RoutingBlockType getRoutingBlockType(int id)
	{
		return routingblocktypes.get(id);
	}
	
	public RoutingBlockType getRoutingBlockType(String s)
	{
		return routingblocktypenames.get(s);
	}
	
	public int getNumberOfLoadedBlocks()
	{
		return blocksbyid.size();
	}
	
	public Collection<RoutingBlock> getAllRoutingBlocks()
	{
		return blocksbyid.values();
	}
	
}

