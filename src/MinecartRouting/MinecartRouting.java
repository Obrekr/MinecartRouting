package MinecartRouting;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import MinecartRouting.Listeners.MinecartRoutingBlockListener;
import MinecartRouting.Listeners.MinecartRoutingPlayerListener;
import MinecartRouting.Listeners.MinecartRoutingVehicleListener;

public class MinecartRouting extends JavaPlugin{
	
	private static Logger logger = Logger.getLogger("Minecraft");
	
	private MinecartRoutingVehicleListener vehicleListener = new MinecartRoutingVehicleListener(this);
	private MinecartRoutingPlayerListener playerListener = new MinecartRoutingPlayerListener(this);
	private MinecartRoutingBlockListener blockListener = new MinecartRoutingBlockListener(this);
	
	public SettingsManager settingsManager = new SettingsManager(this);
	public AutoManager automanager = new AutoManager(this);
	public BlockManager blockmanager = new BlockManager(this);
	public UtilManager util = new UtilManager(this);
	public SQLiteCore database;
	public WeightedGraph<Integer,DefaultWeightedEdge> graph = new DirectedWeightedMultigraph<Integer,DefaultWeightedEdge>(DefaultWeightedEdge.class);

	
	public void onEnable()
	{ 	
		settingsManager.load();
		settingsManager.save();
		database = new SQLiteCore("MinecartRouting", this.getDataFolder().toString());
		if (!database.existsTable("mr_blocks"))
		{
			database.execute("CREATE TABLE IF NOT EXISTS 'mr_blocks' ('id' INTEGER PRIMARY KEY, 'x' int(11) NOT NULL, 'y' int(11) NOT NULL, 'z' int(11) NOT NULL, 'world' varchar(25) NOT NULL, 'type' varchar(25) NOT NULL, 'name' varchar(25) Default NULL, 'owner' varchar(16) Default NULL, 'conditions' TEXT Default NULL, 'north' INT Default NULL, 'north_length' INT Default NULL, 'east' INT Default NULL, 'east_length' INT Default NULL, 'south' INT Default NULL, 'south_length' INT Default NULL, 'west' INT Default NULL, 'west_length' INT Default NULL, UNIQUE('x', 'y', 'z', 'world'))");
			log("Creating table mr_blocks");
		}

		updateGraph();
		registerEvents();
		
		if (settingsManager.getConfig().getBoolean("debug"))
			log("Debugging enabled!");
    	log("Version {0} enabled.", getDescription().getVersion());
    }
     
    public void onDisable()
    {
    	database.close();
    	log("Version {0} disabled", getDescription().getVersion());
    }

    private void registerEvents()
    {
    	getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, Event.Priority.Normal, this);
    	getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_CREATE, vehicleListener, Event.Priority.Normal, this);
    	getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_DESTROY, vehicleListener, Event.Priority.Normal, this);
    	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
    	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
    	getServer().getPluginManager().registerEvent(Event.Type.REDSTONE_CHANGE, blockListener, Event.Priority.Normal, this);
    }
    
	public void log(String msg, Object... arg)
    {
		logger.log(Level.INFO, new StringBuilder().append("[MinecartRouting] ").append(MessageFormat.format(msg, arg)).toString());
    }
	
	public void debug(String msg, Object... arg)
	{
		if (settingsManager.debug)
			logger.log(Level.INFO, new StringBuilder().append("[MinecartRouting] ").append(MessageFormat.format(msg, arg)).toString());
	}
	
	public void updateGraph()
	{
		String query = "SELECT id, north, north_length, east, east_length, south, south_length, west, west_length FROM mr_blocks;";
		ResultSet result = database.select(query);
		try {
			while (result.next())
			{
				int id = result.getInt("id");
				int north = result.getInt("north");
				int east = result.getInt("east");
				int south = result.getInt("south");
				int west = result.getInt("west");
				int n = result.getInt("north_length");
				int e = result.getInt("east");
				int s = result.getInt("south_length");
				int w = result.getInt("west_length");
				
				graph.addVertex(id);
				graph.addVertex(north);
				graph.addVertex(east);
				graph.addVertex(south);
				graph.addVertex(west);
				
				if (util.getBlockById(north) != null && settingsManager.hasSignConfig(util.getBlockById(north)))
				{
					DefaultWeightedEdge edge = graph.addEdge(id, north);
					graph.setEdgeWeight(edge, n);
				}
				
				if (util.getBlockById(east) != null && settingsManager.hasSignConfig(util.getBlockById(east)))
				{
					DefaultWeightedEdge edge = graph.addEdge(id, east);
					graph.setEdgeWeight(edge, e);
				}
				
				if (util.getBlockById(south) != null && settingsManager.hasSignConfig(util.getBlockById(south)))
				{
					DefaultWeightedEdge edge = graph.addEdge(id, south);
					graph.setEdgeWeight(edge, s);
				}
				
				if (util.getBlockById(west) != null && settingsManager.hasSignConfig(util.getBlockById(west)))
				{
					DefaultWeightedEdge edge = graph.addEdge(id, west);
					graph.setEdgeWeight(edge, w);
				}	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public static Logger getLogger()
	{
		return logger;
	}
}
