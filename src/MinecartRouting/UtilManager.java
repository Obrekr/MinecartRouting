package MinecartRouting;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class UtilManager {
	
	private static MinecartRouting plugin;
	
	public UtilManager(MinecartRouting instance)
	{
		plugin = instance;
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
		
		vect.normalize();
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
	
	public void sendInfo(RoutingBlock b, Player p)
	{
		p.sendRawMessage(ChatColor.AQUA + "---------------------");
		p.sendRawMessage(ChatColor.GOLD + "Name: "+b.getName()+" (#"+b.getId()+")");
		p.sendRawMessage(ChatColor.GOLD + "Owner: "+b.getOwner()+"");
		p.sendRawMessage(ChatColor.GOLD + "Position: "+b.getLocation().getBlockX()+" | "+b.getLocation().getBlockY()+" | "+b.getLocation().getBlockZ()+"");
		p.sendRawMessage(ChatColor.GOLD + "Type: "+b.getFlagsString()+"");
		for (BlockFace face : b.getAllNextFaces())
		{
			String direction = face.toString().substring(0, 1).toUpperCase()+face.toString().substring(1).toLowerCase();
			Integer nextid = b.getNextId(face);
			String nextname = plugin.settingsmanager.getRoutingBlock(nextid).getName();
			Integer nextlength = b.getNextDistance(face);
			p.sendRawMessage(ChatColor.GOLD + direction+": "+nextname+" (#"+nextid.toString()+")("+nextlength.toString()+" blocks)");
		}
		if (b.hasSignConfig())
			p.sendRawMessage(ChatColor.GOLD + "Conditions: "+b.getConditions());
		p.sendRawMessage(ChatColor.AQUA + "---------------------");
	}

	public boolean isRail(Block b) {
		if(b.getTypeId() == 66 || b.getTypeId() == 27 || b.getTypeId() == 28)
			return true;
		return false;
	}

	public MinecartRoutingMinecart findNextOwnVehicle(Player p) {
		double closest = Double.MAX_VALUE;
    	MinecartRoutingMinecart closestCart = null;
    	for (Entity le : p.getWorld().getEntities())
    	{
    		if (le instanceof Minecart)
    		{
    			double distance = le.getLocation().toVector().distance(p.getLocation().toVector());
    			if (plugin.settingsmanager.isMinecart(le) && plugin.settingsmanager.getMinecart(le).getOwner().equals(p) && distance < closest)
    			{
    				closestCart = plugin.settingsmanager.getMinecart(le);
    				closest = distance;
    			}
    		}
    	}
		return closestCart;
	}
}
