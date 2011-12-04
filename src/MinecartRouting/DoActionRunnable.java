package MinecartRouting;

import org.bukkit.block.Block;

import MinecartRouting.RoutingBlockType.RoutingBlockActionTimes;

public class DoActionRunnable implements Runnable
{
	RoutingBlock rb;
	MinecartRoutingMinecart cart;
	Block b;
	
	public DoActionRunnable(RoutingBlock routingblock, Block block, MinecartRoutingMinecart v)
	{
		rb = routingblock;
		b = block;
		cart = v;
	}
	
	@Override
	public void run()
	{
		if (rb == null || b == null || cart == null)
			return;
		rb.doActions(b, cart, RoutingBlockActionTimes.ONBLOCK);
	}
}
