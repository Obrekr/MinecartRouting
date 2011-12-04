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
		BlockFace[] to = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		
		for (BlockFace dir : to)
		{
			boolean blockfound = false;
			Block rail = b.getRelative(0, 1, 0).getRelative(dir);
			BlockFace last = dir;
			Block nextroutingblock = null;
			
			while (!blockfound)
			{
				if (!plugin.util.isRail(rail))
				{	
					if (plugin.util.isRail(rail.getRelative(BlockFace.DOWN)) && isDecending(rail.getRelative(BlockFace.DOWN), last))	
						rail = rail.getRelative(BlockFace.DOWN);
					else if (plugin.util.isRail(rail.getRelative(last.getOppositeFace())) && isAscending(rail.getRelative(last.getOppositeFace()), last))
						rail = rail.getRelative(BlockFace.UP);
					else
					{
						plugin.debug("No more rails found: {0}", dir);
						break;
					}
				}
				
				if (plugin.blockmanager.hasSignConfig(rail.getRelative(0, -1, 0)) && plugin.blockmanager.exists(rail.getRelative(0, -1, 0)))
				{
					nextroutingblock = rail.getRelative(0, -1, 0);
					blockfound = true;
					
					plugin.debug("Next RoutingBlock found: {0} (#{1})", plugin.util.getNameByBlock(nextroutingblock), plugin.util.getIdByBlock(nextroutingblock));
					
					updateBlocksInAutoDirection(nextroutingblock, true);
				}else{
					last = followRail(rail, last);
					rail = rail.getRelative(last);
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
		plugin.debug("single update for: {0} (#{1})", plugin.util.getNameByBlock(b), plugin.util.getIdByBlock(b));

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
		
		List<BlockFace> to = new ArrayList<BlockFace>();
				
		for (int i = 0; i < lines.length; i++)
		{
			String todirection = lines[i].split(":")[2];
			String options = lines[i].split(":")[1];
					
			if (options.equals("auto"))
				to.add(plugin.util.StringToDirection(todirection));
		}
		
		for (BlockFace dir : to)
		{
			boolean blockfound = false;
			Block rail = b.getRelative(0, 1, 0).getRelative(dir);
			BlockFace last = dir;
			Block nextroutingblock = null;
			int length = 0;
			
			while (!blockfound)
			{
				length++;
				
				if (!plugin.util.isRail(rail))
				{	
					if (plugin.util.isRail(rail.getRelative(BlockFace.DOWN)) && isDecending(rail.getRelative(BlockFace.DOWN), last))	
						rail = rail.getRelative(BlockFace.DOWN);
					else if (plugin.util.isRail(rail.getRelative(last.getOppositeFace())) && isAscending(rail.getRelative(last.getOppositeFace()), last))
						rail = rail.getRelative(BlockFace.UP);
					else
					{
						plugin.debug("No more rails found: {0}", dir);
						break;
					}
				}
				
				if (plugin.blockmanager.hasSignConfig(rail.getRelative(0, -1, 0)) && plugin.blockmanager.exists(rail.getRelative(0, -1, 0)))
				{
					nextroutingblock = rail.getRelative(0, -1, 0);
					blockfound = true;
					
					plugin.debug("SINGLE MODE: next routingBlock found: {0} (#{1})", plugin.util.getNameByBlock(nextroutingblock), plugin.util.getIdByBlock(nextroutingblock));
					
					if (!reupdate)
						updateBlocksInAutoDirection(nextroutingblock, true);
				}else{
					last = followRail(rail, last);
					rail = rail.getRelative(last);
				}
			}
			
			setNextRoutingBlock(b, nextroutingblock, dir, length);
		}
	}
	
	private boolean isAscending(Block rail, BlockFace last)
	{
		Byte data = rail.getData();
		if (last.equals(BlockFace.NORTH) && data == 3)
			return true;
		if (last.equals(BlockFace.EAST) && data == 4)
			return true;
		if (last.equals(BlockFace.SOUTH) && data == 2)
			return true;
		if (last.equals(BlockFace.WEST) && data == 5)
			return true;
		return false;
	}
	
	private boolean isDecending(Block rail, BlockFace last)
	{
		Byte data = rail.getData();
		if (last.equals(BlockFace.NORTH) && data == 2)
			return true;
		if (last.equals(BlockFace.EAST) && data == 5)
			return true;
		if (last.equals(BlockFace.SOUTH) && data == 3)
			return true;
		if (last.equals(BlockFace.WEST) && data == 4)
			return true;
		return false;
	}
	
	private BlockFace followRail(Block rail, BlockFace incoming)
	{
		Byte data = rail.getData();
		BlockFace nextdir = null;
		if (data == 0 || data == 4 || data == 5)		// East-West-Rail
		{
			if (incoming.equals(BlockFace.NORTH))
				nextdir = BlockFace.WEST;
			if (incoming.equals(BlockFace.EAST))
				nextdir =  BlockFace.EAST;
			if (incoming.equals(BlockFace.SOUTH))
				nextdir =  BlockFace.WEST;
			if (incoming.equals(BlockFace.WEST))
				nextdir =  BlockFace.WEST;
		}
		if (data == 1 || data == 2 || data == 3)	// North-South-Rail
		{
			if (incoming.equals(BlockFace.NORTH))
				nextdir =  BlockFace.NORTH;
			if (incoming.equals(BlockFace.EAST))
				nextdir =  BlockFace.SOUTH;
			if (incoming.equals(BlockFace.SOUTH))
				nextdir =  BlockFace.SOUTH;
			if (incoming.equals(BlockFace.WEST))
				nextdir =  BlockFace.SOUTH;
		}
		
		if (rail.getTypeId() == 66)	// normal Rail
		{
			if (data == 8)		// North-East-Rail
			{
				if (incoming.equals(BlockFace.NORTH))
					nextdir =  BlockFace.NORTH;
				if (incoming.equals(BlockFace.EAST))
					nextdir =  BlockFace.EAST;
				if (incoming.equals(BlockFace.SOUTH))
					nextdir =  BlockFace.EAST;
				if (incoming.equals(BlockFace.WEST))
					nextdir =  BlockFace.NORTH;
			}
			if (data == 9)		// South-East-Rail
			{
				if (incoming.equals(BlockFace.NORTH))
					nextdir =  BlockFace.EAST;
				if (incoming.equals(BlockFace.EAST))
					nextdir =  BlockFace.EAST;
				if (incoming.equals(BlockFace.SOUTH))
					nextdir =  BlockFace.SOUTH;
				if (incoming.equals(BlockFace.WEST))
					nextdir =  BlockFace.SOUTH;
			}
			if (data == 6)		// South-West-Rail
			{
				if (incoming.equals(BlockFace.NORTH))
					nextdir =  BlockFace.WEST;
				if (incoming.equals(BlockFace.EAST))
					nextdir =  BlockFace.SOUTH;
				if (incoming.equals(BlockFace.SOUTH))
					nextdir =  BlockFace.SOUTH;
				if (incoming.equals(BlockFace.WEST))
					nextdir =  BlockFace.WEST;
			}
			if (data == 7)		// North-West-Rail
			{
				if (incoming.equals(BlockFace.NORTH))
					nextdir =  BlockFace.NORTH;
				if (incoming.equals(BlockFace.EAST))
					nextdir =  BlockFace.NORTH;
				if (incoming.equals(BlockFace.SOUTH))
					nextdir =  BlockFace.WEST;
				if (incoming.equals(BlockFace.WEST))
					nextdir =  BlockFace.WEST;
			}
		}
		return nextdir;
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
	
	private void setNextRoutingBlock(Block b, Block nextroutingblock, BlockFace dir, Integer length)
	{
		if (nextroutingblock != null)
		{
			int id = plugin.util.getIdByBlock(nextroutingblock);
			String direction = dir.toString().toLowerCase();
			String query = "UPDATE `mr_blocks` SET "+direction+"="+id+", "+direction+"_length="+length+" WHERE x='"+b.getX()+"' AND y='"+b.getY()+"' AND z="+b.getZ()+" AND world='"+b.getWorld().getName()+"';";
			plugin.database.update(query);
			plugin.debug("Block set in database for direction: {0}", direction);
		}else{
			String query = "UPDATE `mr_blocks` SET "+dir+"=NULL, "+dir+"_length=NULL WHERE x='"+b.getX()+"' AND y='"+b.getY()+"' AND z="+b.getZ()+" AND world='"+b.getWorld().getName()+"';";
			plugin.database.update(query);
			plugin.debug("Block Unset in database for direction: {0}", dir);
		}
	}

	public void setDestination(Block b, Player p, MinecartRoutingMinecart v)
	{
		if (!(b.getState() instanceof Sign))
			return;
		
		Sign s = (Sign) b.getState();
		String[] lines = s.getLines();
		
		if (lines[0].toLowerCase().matches("\\[destination\\]") && !lines[1].isEmpty())
		{
			int id = -1;
			if (lines[1].toLowerCase().matches("#[0-9]+"))
			{	
				id = Integer.parseInt(lines[1].substring(1));
			}else{
				id = plugin.util.getIdByName(lines[1]);
			}
			
			if (id != -1)
			{
				v.route =  new Route(id);
				p.sendRawMessage(ChatColor.AQUA + "Destination set to: "+plugin.util.getNameById(id)+" (#"+id+") for cart #"+v.cart.getEntityId());
				plugin.debug("Destination set to: {0} for cart: {1}", lines[1], v.cart.getEntityId());
			}else{
				p.sendRawMessage(ChatColor.AQUA + "Destination not found!");
			}
		}
		
		if (lines[0].toLowerCase().matches("\\[reset\\]"))
		{
			v.removeRoute();
			p.sendRawMessage(ChatColor.AQUA + "Destination reset!");
			plugin.debug("Destination reset for player {0}", p.getDisplayName());
		}
	}

	public BlockFace getDirection(Block b, Player p, MinecartRoutingMinecart v)
	{
		if (!v.route.hasPath())
		{
			int start = plugin.util.getIdByBlock(b);
			int end = v.route.getDestination();
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
				v.route.setPath(path);
				plugin.debug("Path: {0}", path.toString());
			}
		}
		
		int nextid = v.route.getNextDestination();
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
