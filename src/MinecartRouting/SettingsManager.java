package MinecartRouting;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import MinecartRouting.RoutingBlockType.RoutingBlockTypes;


public class SettingsManager {

	private MinecartRouting plugin;
	
	private static FileConfiguration config;
	private static Map<RoutingBlockTypes, Integer> flagcounts = new HashMap<RoutingBlockTypes, Integer>();
	
	public Map<Integer, MinecartRoutingMinecart> vehicles = new HashMap<Integer, MinecartRoutingMinecart>();
	public Map<Integer, RoutingBlock> routingblocks = new HashMap<Integer, RoutingBlock>();
	
	public boolean debug;
	public int toolitem;
	public int signradius;
	public int maxspeed;
	public boolean slowwhenempty;
	
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
		
		addRoutingBlocks(configroutingblocks);
		plugin.log("Config loaded!");
	}
	
	private String getConfigFileContent(File main) {
		try
		{
			String result = "";
			
			for (RoutingBlockTypes type : RoutingBlockTypes.values())
				flagcounts.put(type, 0);
			
			FileInputStream file = new FileInputStream(main);
			DataInputStream in = new DataInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null)
			{
				for (RoutingBlockTypes type : RoutingBlockTypes.values())
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

	private void addRoutingBlocks(List<LinkedHashMap<String, Object>> maps)
	{
		if (maps == null)
			return;
		for (LinkedHashMap<String, Object> map : maps)
		{
			RoutingBlock block = new RoutingBlock(map, flagcounts, plugin);
			if (block.isValid())	 
				routingblocks.put(block.blockid, block);
			else
				plugin.debug("invalid block found: {0}\n {1}", block.toString(), map);
		}
		plugin.debug("Blocks: {0}\n", routingblocks.toString());
	}
	
	public FileConfiguration getConfig()
	{
		return config;
	}
	
	public boolean isRoutingBlock(Block b)
	{
		return routingblocks.containsKey(b.getTypeId());
	}
}

