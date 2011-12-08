package MinecartRouting.Listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import MinecartRouting.MinecartRouting;
import MinecartRouting.MinecartRoutingMinecart;
import MinecartRouting.RoutingBlock;
import MinecartRouting.Flags.ActionTimes;

public class MinecartRoutingVehicleListener extends VehicleListener{
    
	public final MinecartRouting plugin;
    
    public MinecartRoutingVehicleListener(MinecartRouting instance) 
    {
        plugin = instance;
    }
    
    public void onVehicleMove(VehicleMoveEvent event)
    {
    	if (!(event.getVehicle() instanceof Minecart))
    		return;
  
    	Block fromBlock = event.getFrom().getBlock().getRelative(0, -1, 0);
    	Block toBlock = event.getTo().getBlock().getRelative(0, -1, 0);
    	
    	if (plugin.settingsmanager.isRoutingBlock(fromBlock.getLocation()))	
    	{	
    		RoutingBlock b = plugin.settingsmanager.getRoutingBlock(fromBlock.getLocation());
    		routingBlockFound(b, event.getVehicle(), ActionTimes.ONBLOCK);
    	}
   
    	if (plugin.settingsmanager.isRoutingBlock(toBlock.getLocation()))
    	{	
    		RoutingBlock b = plugin.settingsmanager.getRoutingBlock(toBlock.getLocation());
    		routingBlockFound(b, event.getVehicle(), ActionTimes.PREBLOCK);
    	}
  	
    }
    
    public void onVehicleCreate(VehicleCreateEvent event)
    {
    	if (!(event.getVehicle() instanceof Minecart))
			return;
		
    	Minecart v = (Minecart) event.getVehicle();
    	double speed = (double) plugin.settingsmanager.maxspeed / 20.0;
    	v.setMaxSpeed(speed);
    	v.setSlowWhenEmpty(plugin.settingsmanager.slowwhenempty);
    	addVehicleToSettings(v);
		plugin.debug("Vehicle created, Owner: {0}", plugin.settingsmanager.getMinecart(v).getOwner().getDisplayName());
    }
    
    public void onVehicleDestroy(VehicleDestroyEvent event)
    {
    	if (!(event.getVehicle() instanceof Minecart))
			return;
    	
    	Minecart v = (Minecart) event.getVehicle();
    	plugin.settingsmanager.removeMinecart(v);
    	
		plugin.debug("Minecart removed: {0}", v.toString());
    }

    private void routingBlockFound(RoutingBlock b, Vehicle v, ActionTimes time)
    {
    	if (!plugin.settingsmanager.isMinecart(v))
    		addVehicleToSettings((Minecart) v);
    	MinecartRoutingMinecart cart = plugin.settingsmanager.getMinecart(v);

		if (cart.isCatched())
		{	
			plugin.debug("Recatching...");
			cart.recatch();
		}
		
		if (!cart.hasPositionChanged(b.getBlock(), time))	
			return;
		
		plugin.debug("RoutingBlock found! {0}", b.getId());
		b.doActions(cart, time);
    }
    
	private void addVehicleToSettings(Minecart v)
	{
		plugin.settingsmanager.putMinecart(new MinecartRoutingMinecart(v));
	}
}
