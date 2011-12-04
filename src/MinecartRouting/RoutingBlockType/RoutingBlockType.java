package MinecartRouting.RoutingBlockType;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import MinecartRouting.MinecartRouting;
import MinecartRouting.MinecartRoutingMinecart;

public interface RoutingBlockType {
	
	MinecartRouting plugin = (MinecartRouting) Bukkit.getServer().getPluginManager().getPlugin("MinecartRouting");
	
	void doAction(Block b, MinecartRoutingMinecart cart);

	RoutingBlockTypes getBlockType();
	RoutingBlockActionTimes getActionTime();
	boolean hasSignConfig();
	public boolean isValid();
	public String toString();

}
