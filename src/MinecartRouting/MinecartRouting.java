package MinecartRouting;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.block.BlockFace;
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
	
	public SettingsManager settingsmanager = new SettingsManager(this);
	public AutoManager automanager = new AutoManager(this);
	public BlockManager blockmanager = new BlockManager(this);
	public UtilManager util = new UtilManager(this);
	public SQLiteCore database;
	public WeightedGraph<Integer,DefaultWeightedEdge> graph;

	
	public void onEnable()
	{
		database = new SQLiteCore("MinecartRouting", this.getDataFolder().toString());
		updateDatabase();
		
		settingsmanager.load();
		updateGraph();
		registerEvents();
		
		debug("Debugging enabled!");
    	log("Version {0} enabled. {1} Blocks loaded!", getDescription().getVersion(), settingsmanager.getNumberOfLoadedBlocks());
    }
     
    public void onDisable()
    {
    	database.close();
    	log("Version {0} disabled", getDescription().getVersion());
    }

    private void registerEvents()
    {
    	getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, Event.Priority.High, this);
    	getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_CREATE, vehicleListener, Event.Priority.Monitor, this);
    	getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_DESTROY, vehicleListener, Event.Priority.Monitor, this);
    	getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Monitor, this);
    	getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.High, this);
    	getServer().getPluginManager().registerEvent(Event.Type.REDSTONE_CHANGE, blockListener, Event.Priority.Monitor, this);
    }
    
	public void log(String msg, Object... arg)
    {
		logger.log(Level.INFO, new StringBuilder().append("[MinecartRouting] ").append(MessageFormat.format(msg, arg)).toString());
    }
	
	public void debug(String msg, Object... arg)
	{
		if (settingsmanager.debug)
			logger.log(Level.INFO, new StringBuilder().append("[MinecartRouting] ").append(MessageFormat.format(msg, arg)).toString());
	}
	
	public void updateGraph()
	{
		graph = new DirectedWeightedMultigraph<Integer,DefaultWeightedEdge>(DefaultWeightedEdge.class);
		for (RoutingBlock b : settingsmanager.getAllRoutingBlocks())
		{
			if (b.hasSignConfig())
			{
				graph.addVertex(b.getId());
				for (BlockFace face : b.getAllNextFaces())
				{
					Integer nextid = b.getNextId(face);
					Integer nextdistance = b.getNextDistance(face);
					graph.addVertex(nextid);
					DefaultWeightedEdge edge = graph.addEdge(b.getId(), nextid);
					graph.setEdgeWeight(edge, nextdistance);
				}
			}
		}
	}
	
	public static Logger getLogger()
	{
		return logger;
	}
	
	private void updateDatabase()
	{
		String createTabelOptions = "('id' INTEGER PRIMARY KEY, 'x' int(11) NOT NULL, 'y' int(11) NOT NULL, 'z' int(11) NOT NULL, 'world' varchar(25) NOT NULL, 'name' varchar(25) Default NULL, 'owner' varchar(16) Default NULL, 'conditions' TEXT Default NULL, 'north' INT Default NULL, 'north_length' INT Default NULL, 'east' INT Default NULL, 'east_length' INT Default NULL, 'south' INT Default NULL, 'south_length' INT Default NULL, 'west' INT Default NULL, 'west_length' INT Default NULL, UNIQUE('x', 'y', 'z', 'world'))";
		
		if (!database.existsTable("mr_blocks"))
		{
			database.execute("CREATE TABLE IF NOT EXISTS 'mr_blocks'"+createTabelOptions+";");
			log("Created table 'mr_blocks'");
		}	
	}
}
