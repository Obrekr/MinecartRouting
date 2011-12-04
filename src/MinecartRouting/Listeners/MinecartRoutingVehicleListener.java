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
import MinecartRouting.RoutingBlockType.RoutingBlockActionTimes;

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
    	
    	if (plugin.settingsManager.isRoutingBlock(fromBlock) && plugin.blockmanager.exists(fromBlock))	
    		routingBlockFound(fromBlock, event.getVehicle(), RoutingBlockActionTimes.ONBLOCK);
   
    	if (plugin.settingsManager.isRoutingBlock(toBlock) && plugin.blockmanager.exists(toBlock))
    		routingBlockFound(toBlock, event.getVehicle(), RoutingBlockActionTimes.PREBLOCK);
  	
    }
    
    public void onVehicleCreate(VehicleCreateEvent event)
    {
    	if (!(event.getVehicle() instanceof Minecart))
			return;
		
    	Minecart v = (Minecart) event.getVehicle();
    	double speed = (double) plugin.settingsManager.maxspeed / 20.0;
    	v.setMaxSpeed(speed);
    	v.setSlowWhenEmpty(plugin.settingsManager.slowwhenempty);
    	addVehicleToSettings(v);
		plugin.debug("Vehicle created, Owner: {0}", plugin.settingsManager.vehicles.get(v.getEntityId()).owner.getDisplayName());
    }
    
    public void onVehicleDestroy(VehicleDestroyEvent event)
    {
    	if (!(event.getVehicle() instanceof Minecart))
			return;
    	
    	Minecart v = (Minecart) event.getVehicle();
    	plugin.settingsManager.vehicles.remove(v.getEntityId());
    	
		plugin.debug("Minecart removed: {0}", v.toString());
    }

    private void routingBlockFound(Block b, Vehicle v, RoutingBlockActionTimes time)
    {
    	if (!plugin.settingsManager.vehicles.containsKey(v.getEntityId()))
    		addVehicleToSettings((Minecart) v);
    	MinecartRoutingMinecart cart = plugin.settingsManager.vehicles.get(v.getEntityId());

		if (cart.isCatched())
		{	
			plugin.debug("Recatching...");
			cart.recatch();
		}
		
		if (!cart.hasPositionChanged(b, time))	
			return;
		
		plugin.debug("RoutingBlock found! {0}", b.getLocation().toString());
		RoutingBlock rb = plugin.blockmanager.getRoutingBlock(b);
		if (rb == null)
			return;
		rb.doActions(b, cart, time);
    }
    
	private void addVehicleToSettings(Minecart v)
	{
		plugin.settingsManager.vehicles.put(v.getEntityId(), new MinecartRoutingMinecart(v));
	}
}
