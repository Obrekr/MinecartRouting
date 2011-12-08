package MinecartRouting;


import MinecartRouting.Flags.ActionTimes;

public class DoActionsRunnable implements Runnable
{
	RoutingBlock rb;
	MinecartRoutingMinecart cart;
	
	public DoActionsRunnable(RoutingBlock routingblock, MinecartRoutingMinecart v)
	{
	rb = routingblock;
	cart = v;
	}
	
	@Override
	public void run()
	{
		if (rb == null || cart == null)
			return;
		rb.doActions(cart, ActionTimes.ONBLOCK);
	}
}
