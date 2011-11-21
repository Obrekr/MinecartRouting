package MinecartRouting;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jgrapht.Graphs;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;

public class AutoManager 
{
	MinecartRouting plugin;
	
	public AutoManager(MinecartRouting instance)
	{
		plugin = instance;
	}
	
	public void updateBlockInAllDirections(Block b)
	{
		// Update Block itself
		updateBlocksInAutoDirection(b, true);
		 // Find + Update all other blocks
		String[] to = {"north", "east", "south", "west"};
		
		for (String dir : to)
		{
			boolean blockfound = false;
			Block rail = b.getRelative(0, 1, 0);
			Block nextroutingblock = null;
			String nextdir = dir;
			
			while (!blockfound)
			{
				Integer[] offset = directionToOffset(nextdir);
				
				rail = rail.getRelative(offset[0], offset[1], offset[2]);
				if (!(rail.getTypeId() == 66 || rail.getTypeId() == 27 || rail.getTypeId() == 28))
				{	
					plugin.debug("No more rails found!");
					break;
				}
				
				if (plugin.settingsManager.hasSignConfig(rail.getRelative(0, -1, 0)) && plugin.blockmanager.exists(rail.getRelative(0, -1, 0)))
				{
					nextroutingblock = rail.getRelative(0, -1, 0);
					blockfound = true;
					
					plugin.debug("Next RoutingBlock found: {0}", nextroutingblock.getLocation().toString());
					
					updateBlocksInAutoDirection(nextroutingblock, true);
				}else{
					Byte data = rail.getData();
					if (data == 0 || data == 4 || data == 5)		// East-West-Rail
					{
						if (nextdir.equals("north"))
							nextdir = "west";
						if (nextdir.equals("east"))
							nextdir = "east";
						if (nextdir.equals("south"))
							nextdir = "west";
						if (nextdir.equals("west"))
							nextdir = "west";
					}
					if (data == 1 || data == 2 || data == 3)	// North-South-Rail
					{
						if (nextdir.equals("north"))
							nextdir = "north";
						if (nextdir.equals("east"))
							nextdir = "south";
						if (nextdir.equals("south"))
							nextdir = "south";
						if (nextdir.equals("west"))
							nextdir = "south";
					}
					
					if (rail.getTypeId() == 66)	// normal Rail
					{
						if (data == 8)		// North-East-Rail
						{
							if (nextdir.equals("north"))
								nextdir = "north";
							if (nextdir.equals("east"))
								nextdir = "east";
							if (nextdir.equals("south"))
								nextdir = "east";
							if (nextdir.equals("west"))
								nextdir = "north";
						}
						if (data == 9)		// South-East-Rail
						{
							if (nextdir.equals("north"))
								nextdir = "east";
							if (nextdir.equals("east"))
								nextdir = "east";
							if (nextdir.equals("south"))
								nextdir = "south";
							if (nextdir.equals("west"))
								nextdir = "south";
						}
						if (data == 6)		// South-West-Rail
						{
							if (nextdir.equals("north"))
								nextdir = "west";
							if (nextdir.equals("east"))
								nextdir = "south";
							if (nextdir.equals("south"))
								nextdir = "south";
							if (nextdir.equals("west"))
								nextdir = "west";
						}
						if (data == 7)		// North-West-Rail
						{
							if (nextdir.equals("north"))
								nextdir = "north";
							if (nextdir.equals("east"))
								nextdir = "north";
							if (nextdir.equals("south"))
								nextdir = "west";
							if (nextdir.equals("west"))
								nextdir = "west";
						}
					}
					
				}
			}
		}
	}
	
	public void updateBlocksInAutoDirection(Block b)
	{
		updateBlocksInAutoDirection(b, false);
	}
	
	public void updateBlocksInAutoDirection(Block b, Boolean reupdate)
	{
		// remove old entries
		removeAutoEntries(b);
		
		// get Conditions
		String query = "SELECT conditions FROM mr_blocks WHERE x='"+b.getX()+"' AND y='"+b.getY()+"' AND z="+b.getZ()+" AND world='"+b.getWorld().getName()+"';";
		ResultSet result = plugin.database.select(query);
		String dbresult = "";
		try {
			result.next();
			dbresult = result.getString("conditions");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String[] lines = dbresult.split(";");
		
		if (lines.length == 0)
			return;
		
		List<String> to = new ArrayList<String>();
				
		for (int i = 0; i < lines.length; i++)
		{
			String todirection = lines[i].split(":")[2];
			String options = lines[i].split(":")[1];
					
			if (options.equals("auto"))
				to.add(todirection);
		}
		
		for (String dir : to)
		{
			boolean blockfound = false;
			Block rail = b.getRelative(0, 1, 0);
			Block nextroutingblock = null;
			int length = 0;
			
			String nextdir = dir;
			
			while (!blockfound)
			{
				Integer[] offset = directionToOffset(nextdir);
				
				rail = rail.getRelative(offset[0], offset[1], offset[2]);
				length++;
				
				if (!(rail.getTypeId() == 66 || rail.getTypeId() == 27 || rail.getTypeId() == 28))
				{	
					plugin.debug("No more rails found!");
					break;
				}
				
				if (plugin.settingsManager.hasSignConfig(rail.getRelative(0, -1, 0)) && plugin.blockmanager.exists(rail.getRelative(0, -1, 0)))
				{
					nextroutingblock = rail.getRelative(0, -1, 0);
					blockfound = true;
					
					plugin.debug("Next RoutingBlock found: {0}", nextroutingblock.getLocation().toString());
					
					if (!reupdate)
						updateBlocksInAutoDirection(nextroutingblock, true);
				}else{
					Byte data = rail.getData();
					if (data == 0 || data == 4 || data == 5)		// East-West-Rail
					{
						if (nextdir.equals("north"))
							nextdir = "west";
						if (nextdir.equals("east"))
							nextdir = "east";
						if (nextdir.equals("south"))
							nextdir = "west";
						if (nextdir.equals("west"))
							nextdir = "west";
					}
					if (data == 1 || data == 2 || data == 3)	// North-South-Rail
					{
						if (nextdir.equals("north"))
							nextdir = "north";
						if (nextdir.equals("east"))
							nextdir = "south";
						if (nextdir.equals("south"))
							nextdir = "south";
						if (nextdir.equals("west"))
							nextdir = "south";
					}
					
					if (rail.getTypeId() == 66)	// normal Rail
					{
						if (data == 8)		// North-East-Rail
						{
							if (nextdir.equals("north"))
								nextdir = "north";
							if (nextdir.equals("east"))
								nextdir = "east";
							if (nextdir.equals("south"))
								nextdir = "east";
							if (nextdir.equals("west"))
								nextdir = "north";
						}
						if (data == 9)		// South-East-Rail
						{
							if (nextdir.equals("north"))
								nextdir = "east";
							if (nextdir.equals("east"))
								nextdir = "east";
							if (nextdir.equals("south"))
								nextdir = "south";
							if (nextdir.equals("west"))
								nextdir = "south";
						}
						if (data == 6)		// South-West-Rail
						{
							if (nextdir.equals("north"))
								nextdir = "west";
							if (nextdir.equals("east"))
								nextdir = "south";
							if (nextdir.equals("south"))
								nextdir = "south";
							if (nextdir.equals("west"))
								nextdir = "west";
						}
						if (data == 7)		// North-West-Rail
						{
							if (nextdir.equals("north"))
								nextdir = "north";
							if (nextdir.equals("east"))
								nextdir = "north";
							if (nextdir.equals("south"))
								nextdir = "west";
							if (nextdir.equals("west"))
								nextdir = "west";
						}
					}
					
				}
			}
			
			setNextRoutingBlock(b, nextroutingblock, dir, length);
		}
		plugin.updateGraph();
	}
	
	private void removeAutoEntries(Block b)
	{
		String query = "UPDATE mr_blocks SET north=NULL, east=NULL, south=NULL, west=NULL, north_length=NULL, east_length=NULL, south_length=NULL, west_length=NULL WHERE x='"+b.getX()+"' AND y='"+b.getY()+"' AND z="+b.getZ()+" AND world='"+b.getWorld().getName()+"';";
		plugin.database.update(query);
	}

	public void remove(List<Integer> toupdate)
	{
		plugin.log("Updateing...");
		for (int id : toupdate)
		{
			updateBlocksInAutoDirection(plugin.util.getBlockById(id), true);
		}
		
	}
	
	private void setNextRoutingBlock(Block b, Block nextroutingblock, String direction, Integer length)
	{
		if (nextroutingblock != null)
		{
			int id = plugin.util.getIdByBlock(nextroutingblock);
		
			String query = "UPDATE `mr_blocks` SET "+direction+"="+id+", "+direction+"_length="+length+" WHERE x='"+b.getX()+"' AND y='"+b.getY()+"' AND z="+b.getZ()+" AND world='"+b.getWorld().getName()+"';";
			plugin.database.update(query);
		}else{
			String query = "UPDATE `mr_blocks` SET "+direction+"=NULL, "+direction+"_length=NULL WHERE x='"+b.getX()+"' AND y='"+b.getY()+"' AND z="+b.getZ()+" AND world='"+b.getWorld().getName()+"';";
			plugin.database.update(query);
		}
	}
	
	private Integer[] directionToOffset(String direction)
	{
		Integer[] offset = {0, 0, 0};
		
		if (direction.equals("north"))
			offset[0] = -1;
		if (direction.equals("south"))
			offset[0] = 1;
		if (direction.equals("east"))
			offset[2] = -1;
		if (direction.equals("west"))
			offset[2] = 1;
		
		return offset;
	}

	public void setDestination(Block b, Player p)
	{
		if (!(b.getState() instanceof Sign))
			return;
		
		Sign s = (Sign) b.getState();
		String[] lines = s.getLines();
		
		if (lines[0].matches("\\[destination\\]") && !lines[1].isEmpty())
		{
			int id = -1;
			if (lines[1].matches("#[0-9]+"))
			{	
				id = Integer.parseInt(lines[1].substring(1));
			}else{
				id = plugin.util.getIdByName(lines[1]);
			}
			
			if (id != -1)
			{
				plugin.settingsManager.routes.put(p, new Route(id));
				p.sendRawMessage(ChatColor.AQUA + "Destination set to: "+plugin.util.getNameById(id)+" (#"+id+")");
				plugin.debug("Destination set to: {0} for player: {1}", lines[1], p.getDisplayName());
			}else{
				p.sendRawMessage(ChatColor.AQUA + "Destination not found!");
			}
		}
		
		if (lines[0].matches("\\[reset\\]"))
		{
			plugin.settingsManager.routes.remove(p);
			p.sendRawMessage(ChatColor.AQUA + "Destination reset!");
			plugin.debug("Destination reset for player {0}", p.getDisplayName());
		}
	}

	public BlockFace getDirection(Block b, Player p)
	{
		if (!plugin.settingsManager.routes.get(p).hasPath())
		{
			int start = plugin.util.getIdByBlock(b);
			int end = plugin.settingsManager.routes.get(p).getDestination();
			if (start == -1 || end == -1)
				return BlockFace.SELF;
			plugin.debug("Route: Start: {0}; End:{1}", start, end);
			
			DijkstraShortestPath<Integer, DefaultWeightedEdge> alg = new DijkstraShortestPath<Integer, DefaultWeightedEdge>(plugin.graph, start, end);

			if (alg.getPath() == null)
			{
				p.sendRawMessage(ChatColor.AQUA + "No route to your destination!");
				plugin.debug("No route was found! Graph: {0}", plugin.graph);
				return BlockFace.SELF;
			}else{
				List<Integer> path = Graphs.getPathVertexList(alg.getPath());
				plugin.settingsManager.routes.get(p).setPath(path);
				plugin.debug("Path: {0}", path.toString());
			}
		}
		
		int nextid = plugin.settingsManager.routes.get(p).getNextDestination();
		plugin.debug("Next RoutingBlock: {0}", nextid);
		
		if (nextid == -1)
			return BlockFace.SELF;
			
		
		String query = "SELECT north, east, south, west FROM mr_blocks WHERE x='"+b.getX()+"' AND y='"+b.getY()+"' AND z="+b.getZ()+" AND world='"+b.getWorld().getName()+"';";
		ResultSet result = plugin.database.select(query);
		int east = -1;
		int west = -1;
		int south = -1;
		int north = -1;
		try {
			result.next();
			east = result.getInt("east");
			west = result.getInt("west");
			south = result.getInt("south");
			north = result.getInt("north");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (nextid == east)
			return BlockFace.EAST;
		if (nextid == west)
			return BlockFace.WEST;
		if (nextid == north)
			return BlockFace.NORTH;
		if (nextid == south)
			return BlockFace.SOUTH;
		
		return BlockFace.SELF;
	}
}
