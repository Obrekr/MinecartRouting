package MinecartRouting.Flags;

import org.bukkit.Bukkit;

import MinecartRouting.MinecartRouting;
import MinecartRouting.MinecartRoutingMinecart;
import MinecartRouting.RoutingBlock;

public interface Flag {
	
	MinecartRouting plugin = (MinecartRouting) Bukkit.getServer().getPluginManager().getPlugin("MinecartRouting");
	
	void doAction(RoutingBlock routingBlock, MinecartRoutingMinecart cart);

	Flags getBlockType();
	ActionTimes getActionTime();
	boolean hasSignConfig();
	public boolean isValid();
	public String toString();

}
