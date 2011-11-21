package MinecartRouting.Listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import MinecartRouting.MinecartRouting;
import MinecartRouting.RoutingBlocks;

public class MinecartRoutingVehicleListener extends VehicleListener{
    
	public final MinecartRouting plugin;
    
    public MinecartRoutingVehicleListener(MinecartRouting instance) 
    {
        plugin = instance;
    }
    
    public void onVehicleMove(VehicleMoveEvent event){
    	
    	// Minecart?
    	if (!(event.getVehicle() instanceof Minecart))
    		return;
    	
    	Block fromBlock = event.getFrom().getBlock().getRelative(0, -1, 0);
    	Block toBlock = event.getTo().getBlock().getRelative(0, -1, 0);
    	
    	// Minecart-Routing-Block under Rail?
    	if (!(plugin.settingsManager.isFromBlockDependent(fromBlock) || plugin.settingsManager.isToBlockDependet(toBlock)))
    		return;
    	
        plugin.debug("RoutingBlock found! {0}", fromBlock.getLocation().toString());

		if (!(plugin.blockmanager.exists(fromBlock) || (plugin.util.getType(toBlock) == RoutingBlocks.SWITCH)))
			return;

    		
    	// Boost if enabled
    	if ( plugin.util.isEnabled(RoutingBlocks.BOOSTER) && (plugin.util.getType(fromBlock) == RoutingBlocks.BOOSTER) )
    		plugin.blockmanager.booster(fromBlock, event.getVehicle(), fromBlock.isBlockPowered());
    	
    	//Brake if enabled
    	if ( plugin.util.isEnabled(RoutingBlocks.BRAKE) && (plugin.util.getType(fromBlock) == RoutingBlocks.BRAKE) )
    		plugin.blockmanager.brake(fromBlock, event.getVehicle(), fromBlock.isBlockPowered());
    		
    	//Catch if enabled
    	if ( plugin.util.isEnabled(RoutingBlocks.CATCHER) && (plugin.util.getType(fromBlock) == RoutingBlocks.CATCHER) )
    		plugin.blockmanager.catcher(fromBlock, event.getVehicle(), fromBlock.isBlockPowered());
    	
    	//Launch if enabled
    	if ( plugin.util.isEnabled(RoutingBlocks.LAUNCHER) && (plugin.util.getType(fromBlock) == RoutingBlocks.LAUNCHER) )
    		plugin.blockmanager.launcher(fromBlock, event.getVehicle(), fromBlock.isBlockPowered());
    	
    	//Switch if enabled
    	if ( plugin.util.isEnabled(RoutingBlocks.SWITCH) && (plugin.util.getType(toBlock) == RoutingBlocks.SWITCH) )
    		plugin.blockmanager.switcher(toBlock, event.getVehicle(), toBlock.isBlockPowered());
    }
    
    public void onVehicleCreate(VehicleCreateEvent event)
    {
    	if (!(event.getVehicle() instanceof Minecart))
			return;
		
    	Minecart v = (Minecart) event.getVehicle();
    	double speed = (double) plugin.settingsManager.getConfig().getInt("max-speed") / 20.0;
    	v.setMaxSpeed(speed);
    	
    	plugin.settingsManager.vehicles.put(event.getVehicle(), v.getLocation());	
    	
    	double closest = Double.MAX_VALUE;
    	Player closestPlayer = null;
    	for (LivingEntity le : v.getWorld().getLivingEntities())
    	{
    		if (le instanceof Player)
    		{
    			double distance = le.getLocation().toVector().distance(v.getLocation().toVector());
    			if (distance < closest)
    			{
    				closestPlayer = (Player)le;
    				closest = distance;
    			}
    		}
    	}
    	if (closestPlayer != null)
    	{
    		plugin.settingsManager.owner.put(v, closestPlayer);
    	}
    	
    	if (plugin.settingsManager.getConfig().getBoolean("debug"))
			plugin.log("Vehicle created, Owner: {0}", v.toString());
    	
    }
    
    public void onVehicleDestroy(VehicleDestroyEvent event)
    {
    	if (!(event.getVehicle() instanceof Minecart))
			return;
    	
    	Vehicle v = event.getVehicle();
    	plugin.settingsManager.vehicles.remove(v);
    	plugin.settingsManager.passedBlocks.remove(v);
    	plugin.settingsManager.owner.remove(v);
    	
    	if (plugin.settingsManager.getConfig().getBoolean("debug"))
			plugin.log("Vehicle removed: {0}", v.toString());
    }
}
