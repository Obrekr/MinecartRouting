package MinecartRouting;


import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jgrapht.Graphs;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;

public class AutoManager 
{
	private static MinecartRouting plugin;
	
	public AutoManager(MinecartRouting instance)
	{
		plugin = instance;
	}
	
	public BlockFace getDirection(RoutingBlock b, Player p, MinecartRoutingMinecart v)
	{
		if (!v.getRoute().hasPath())
		{
			int start = b.getId();
			int end = v.getRoute().getDestination();
			if (start == -1 || end == -1)
				return null;
			plugin.debug("Route: Start: {0}; End:{1}", start, end);
			
			DijkstraShortestPath<Integer, DefaultWeightedEdge> alg = new DijkstraShortestPath<Integer, DefaultWeightedEdge>(plugin.graph, start, end);
	
			if (alg.getPath() == null)
			{
				p.sendRawMessage(ChatColor.AQUA + "No route to your destination!");
				return null;
			}else{
				List<Integer> path = Graphs.getPathVertexList(alg.getPath());
				v.getRoute().setPath(path);
				plugin.debug("Path: {0}", path.toString());
			}
		}
		
		Integer nextid = v.getRoute().getNextDestination();
		plugin.debug("Next RoutingBlock: {0}", nextid);
		
		if (nextid == null)
			return null;
		
		if (nextid == -1)
		{	
			v.removeRoute();
			v.getOwner().sendRawMessage(ChatColor.AQUA + "Your cart #"+v.getId()+" has reached its destination!");
			return BlockFace.SELF;
		}
		
		BlockFace fa = b.getNextFace(nextid);
		if (fa == null)
			plugin.debug("failed to get AutoDirection: {0}", nextid);
		return fa;
	}

	public void update(RoutingBlock b)
	{
		// ON UPDATE:
		// Clear existing entries
		// For all directions
		// 1. Find from b next routing block c in direction
		// 2. if next is auto reachable: set next as next block for direction
		// 3. if b is auto reachable from next: set b as next block for incoming direction^-1 in next
		b.clearNextBlocks();
		
		BlockFace[] directions = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		for (BlockFace face : directions)
		{
			NextRoutingBlock next = getNextRoutingBlock(b, face);
			if (next.isValid())
			{
				plugin.debug("next block found: #{0} in {1}", next.getRoutingBlock().getId(), face);
				if (b.isAutoDirection(face))
					b.setNext(face, next.getRoutingBlock().getId(), next.getDistance());
				
				if (next.getDirection() != null && next.getRoutingBlock().isAutoDirection(next.getDirection()))
				{
					next.getRoutingBlock().setNext(next.getDirection().getOppositeFace(), b.getId(), next.getDistance());
					next.getRoutingBlock().save();
					next.getRoutingBlock().reload();
					plugin.debug("block saved!");
				}
			}else
				plugin.debug("no block found in {0}", face);
		}
		b.save();
		b.reload();
		plugin.debug("own block saved!");
	}
	
	public void remove(RoutingBlock b)
	{
		// ON REMOVE		
		// For all direction
		// 1. find from b next routing block next in direction
		// 2. unset b in next for direction
		BlockFace[] directions = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		for (BlockFace face : directions)
		{
			NextRoutingBlock next = getNextRoutingBlock(b, face);
			if (next.isValid())
			{
				if (next.getDirection() != null && next.getRoutingBlock().isAutoDirection(next.getDirection()))
				{
					next.getRoutingBlock().unsetNext(next.getDirection());
					plugin.debug("unset for direction {0}", next.getDirection());
					next.getRoutingBlock().save();
					next.getRoutingBlock().reload();
				}
			}
		}

	}
	
	private NextRoutingBlock getNextRoutingBlock(RoutingBlock start, BlockFace dir)
	{
		Block rail = start.getLocation().getBlock().getRelative(BlockFace.UP).getRelative(dir);
		BlockFace nextdirection = dir;
		int distance = 0;
		RoutingBlock nextroutingblock = null;
		
		while (nextroutingblock == null)
		{
			if (distance > 10000)
			{
				plugin.log("Rail loop!");
				break;
			}
			if (!plugin.util.isRail(rail))
			{
				if (plugin.util.isRail(rail.getRelative(BlockFace.DOWN)) && isDecending(rail.getRelative(BlockFace.DOWN), nextdirection))	
					rail = rail.getRelative(BlockFace.DOWN);
				else if (plugin.util.isRail(rail.getRelative(nextdirection.getOppositeFace())) && isAscending(rail.getRelative(nextdirection.getOppositeFace()), nextdirection))
					rail = rail.getRelative(BlockFace.UP);
				else
					break;
			}
			
			RoutingBlock block = plugin.settingsmanager.getRoutingBlock(rail.getRelative(BlockFace.DOWN).getLocation());
			if (block != null && block.hasSignConfig())
			{	
				nextroutingblock = block;
				break;
			}
			
			nextdirection = getNextDirection(rail, nextdirection);
			rail = rail.getRelative(nextdirection);
			distance++;
		}
		return new NextRoutingBlock(nextroutingblock, nextdirection, distance);
	}

	private BlockFace getNextDirection(Block rail, BlockFace nextdirection)
	{
		Byte data = rail.getData();
		BlockFace nextdir = null;
		if (data == 0 || data == 4 || data == 5)		// East-West-Rail
		{
			if (nextdirection.equals(BlockFace.NORTH))
				nextdir = BlockFace.WEST;
			if (nextdirection.equals(BlockFace.EAST))
				nextdir =  BlockFace.EAST;
			if (nextdirection.equals(BlockFace.SOUTH))
				nextdir =  BlockFace.WEST;
			if (nextdirection.equals(BlockFace.WEST))
				nextdir =  BlockFace.WEST;
		}
		if (data == 1 || data == 2 || data == 3)	// North-South-Rail
		{
			if (nextdirection.equals(BlockFace.NORTH))
				nextdir =  BlockFace.NORTH;
			if (nextdirection.equals(BlockFace.EAST))
				nextdir =  BlockFace.SOUTH;
			if (nextdirection.equals(BlockFace.SOUTH))
				nextdir =  BlockFace.SOUTH;
			if (nextdirection.equals(BlockFace.WEST))
				nextdir =  BlockFace.SOUTH;
		}
		
		if (rail.getTypeId() == 66)	// normal Rail
		{
			if (data == 8)		// North-East-Rail
			{
				if (nextdirection.equals(BlockFace.NORTH))
					nextdir =  BlockFace.NORTH;
				if (nextdirection.equals(BlockFace.EAST))
					nextdir =  BlockFace.EAST;
				if (nextdirection.equals(BlockFace.SOUTH))
					nextdir =  BlockFace.EAST;
				if (nextdirection.equals(BlockFace.WEST))
					nextdir =  BlockFace.NORTH;
			}
			if (data == 9)		// South-East-Rail
			{
				if (nextdirection.equals(BlockFace.NORTH))
					nextdir =  BlockFace.EAST;
				if (nextdirection.equals(BlockFace.EAST))
					nextdir =  BlockFace.EAST;
				if (nextdirection.equals(BlockFace.SOUTH))
					nextdir =  BlockFace.SOUTH;
				if (nextdirection.equals(BlockFace.WEST))
					nextdir =  BlockFace.SOUTH;
			}
			if (data == 6)		// South-West-Rail
			{
				if (nextdirection.equals(BlockFace.NORTH))
					nextdir =  BlockFace.WEST;
				if (nextdirection.equals(BlockFace.EAST))
					nextdir =  BlockFace.SOUTH;
				if (nextdirection.equals(BlockFace.SOUTH))
					nextdir =  BlockFace.SOUTH;
				if (nextdirection.equals(BlockFace.WEST))
					nextdir =  BlockFace.WEST;
			}
			if (data == 7)		// North-West-Rail
			{
				if (nextdirection.equals(BlockFace.NORTH))
					nextdir =  BlockFace.NORTH;
				if (nextdirection.equals(BlockFace.EAST))
					nextdir =  BlockFace.NORTH;
				if (nextdirection.equals(BlockFace.SOUTH))
					nextdir =  BlockFace.WEST;
				if (nextdirection.equals(BlockFace.WEST))
					nextdir =  BlockFace.WEST;
			}
		}
		return nextdir;
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
}
