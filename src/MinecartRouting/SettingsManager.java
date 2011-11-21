package MinecartRouting;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class SettingsManager {

	private MinecartRouting plugin;
	
	private static FileConfiguration config;
	
	public BiMap<Vehicle, Location> vehicles = HashBiMap.create();
	public Map<Vehicle, Location> passedBlocks = new HashMap<Vehicle, Location>();
	public Map<Vehicle, Player> owner = new HashMap<Vehicle, Player>();
	public Map<Player, Route> routes = new HashMap<Player, Route>();
	
	public boolean debug;
	public int toolitem;
	
	public SettingsManager(MinecartRouting instance) 
	{
		plugin = instance;
	}
		
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
	            config.load(main);
	            plugin.log("Config loaded!");
	        }
	        catch (Exception e)
	        {
	                e.printStackTrace();
	        }
	     }
	     else
	     {
	        config.options().copyDefaults(true);
	        plugin.log("Config created!");
	     } 
	    
	    debug = config.getBoolean("debug");
	    toolitem = config.getInt("tool-item");
	}
	
	public void save()
    {
    	File main = new File(plugin.getDataFolder() + File.separator + "config.yml");
    	try
        {
            config.save(main);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
	
	public FileConfiguration getConfig()
	{
		return config;
	}
	
	public boolean isRoutingBlock(Block b)
	{
		if (isAcceleration(b) || isStarter(b) || isSwitch(b))
			return true;
		return false;
	}
	
	public boolean isBooster(Block b)
	{
		if (config.getBoolean("booster.enable") && config.getInt("booster.block") == b.getTypeId())
			return true;
		return false;
	}
	
	public boolean isBrake(Block b)
	{
		if (config.getBoolean("brake.enable") && config.getInt("brake.block") == b.getTypeId())
			return true;
		return false;
	}
	
	public boolean isCatcher(Block b)
	{
		if (config.getBoolean("catcher.enable") && config.getInt("catcher.block") == b.getTypeId())
			return true;
		return false;
	}
	
	public boolean isLauncher(Block b)
	{
		if (config.getBoolean("launcher.enable") && config.getInt("launcher.block") == b.getTypeId())
			return true;
		return false;
	}
	
	public boolean isSwitch(Block b)
	{
		if (config.getBoolean("switch.enable") && config.getInt("switch.block") == b.getTypeId())
			return true;
		
		return false;
	}
	
	public boolean isStarter(Block b)
	{
		if (isCatcher(b) || isLauncher(b))
			return true;
		return false;
	}
	
	public boolean isAcceleration(Block b)
	{
		if (isBooster(b) || isBrake(b))
			return true;
		return false;
	}
	
	public boolean hasSignConfig(Block b)
	{
		if (isSwitch(b) || isStarter(b))
			return true;
		return false;
	}
	
	public boolean isFromBlockDependent(Block b)
	{
		if (isAcceleration(b) || isStarter(b))
			return true;
		return false;
	}
	
	public boolean isToBlockDependet(Block b)
	{
		if (isSwitch(b))
			return true;
		return false;
	}
}

