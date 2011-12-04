package MinecartRouting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.block.Block;

import MinecartRouting.RoutingBlockType.Acceleration;
import MinecartRouting.RoutingBlockType.Catcher;
import MinecartRouting.RoutingBlockType.Launcher;
import MinecartRouting.RoutingBlockType.RoutingBlockActionTimes;
import MinecartRouting.RoutingBlockType.RoutingBlockType;
import MinecartRouting.RoutingBlockType.RoutingBlockTypes;
import MinecartRouting.RoutingBlockType.Switch;

public class RoutingBlock {
	String title;
	Integer blockid;
	List<RoutingBlockType> flags = new ArrayList<RoutingBlockType>();
	
	@SuppressWarnings("unchecked")
	public RoutingBlock(LinkedHashMap<String, Object> map, Map<RoutingBlockTypes, Integer> flagcounts, MinecartRouting plugin)
	{
		if (map.get("title") instanceof String)
			title = (String) map.get("title");
		if (map.get("block") instanceof Integer)
			blockid = (Integer) map.get("block");
		
		for (RoutingBlockTypes type : RoutingBlockTypes.values())
		{
			
			for (Integer i = 1; i <= flagcounts.get(type); i++)
			{
				String name = type.toString().toLowerCase() + i.toString();
				List<LinkedHashMap<String, Object> > data = (List<LinkedHashMap<String, Object> >) map.get(name);
				if (data != null)
				{
					RoutingBlockType flag = null;
					switch (type)
					{
					case ACCELERATION:
						flag = new Acceleration(data);
						break;
					case LAUNCHER:
						flag = new Launcher(data);
						break;
					case CATCHER:
						flag = new Catcher(data);
						break;
					case SWITCH:
						flag = new Switch(data);
						break;
					}
					
					if (flag != null && flag.isValid())
						flags.add(flag);
					else if (flag != null)
						plugin.debug("invalid flag found: {0}", flag.toString());			
				}
			}
		}
	}
	
	public void doActions(Block b, MinecartRoutingMinecart cart, RoutingBlockActionTimes time)
	{
		for (RoutingBlockType rb : flags)
		{
			if (rb.getActionTime().equals(time))
				rb.doAction(b, cart);
		}
		cart.setLastBlock(b, RoutingBlockActionTimes.ONBLOCK);
	}

	public boolean isValid() {
		if (title != null && blockid != null && !flags.isEmpty())
			return true;
		return false;
	}
	
	public String toString()
	{
		return "Titel: "+title+" Blockid: "+blockid.toString()+" Flags: "+flags.toString();
	}
	
	public List<RoutingBlockTypes> getRoutingBlockTypes()
	{
		List<RoutingBlockTypes> types = new ArrayList<RoutingBlockTypes>();
		
		for (RoutingBlockType bt : flags)
		{
			types.add(bt.getBlockType());
		}
		
		if (types.isEmpty())
			return null;
		return types;
	}
	
	public List<String> getTypesStringList()
	{
		List<String> names = new ArrayList<String>();
		List<RoutingBlockTypes> types = getRoutingBlockTypes();
		if (types == null)
			return null;
		for (RoutingBlockTypes type : getRoutingBlockTypes())
		{
			names.add(type.toString().toLowerCase());
		}
		if (names.isEmpty())
			return null;
		return names;
	}
	
	public String getTypeString()
	{
		String names = "";
		List<RoutingBlockTypes> types = getRoutingBlockTypes();
		for (int i = 0; i < types.size(); i++)
		{
			RoutingBlockTypes type = types.get(i);
			names += type.toString().substring(0, 1).toUpperCase() + type.toString().substring(1).toLowerCase();
			
			if (i != types.size() -1)
				names += ", ";
		}
		return "{" + names + "}";
	}

	public boolean hasSignConfig()
	{
		for (RoutingBlockType type : flags)
		{
			if (type.hasSignConfig())
				return true;
		}
		return false;
	}

}
