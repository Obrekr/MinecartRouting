package MinecartRouting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import MinecartRouting.Flags.Acceleration;
import MinecartRouting.Flags.Catcher;
import MinecartRouting.Flags.Launcher;
import MinecartRouting.Flags.Flag;
import MinecartRouting.Flags.Flags;
import MinecartRouting.Flags.Switch;

public class RoutingBlockType {
	private String title;
	private Integer blockid;
	private List<Flag> flags = new ArrayList<Flag>();
	private boolean hasSignConfig = false;
	
	@SuppressWarnings("unchecked")
	public RoutingBlockType(LinkedHashMap<String, Object> map, Map<Flags, Integer> flagcounts, MinecartRouting plugin)
	{
		if (map.get("title") instanceof String)
			title = (String) map.get("title");
		if (map.get("block") instanceof Integer)
			blockid = (Integer) map.get("block");
		
		for (Flags type : Flags.values())
		{
			
			for (Integer i = 1; i <= flagcounts.get(type); i++)
			{
				String name = type.toString().toLowerCase() + i.toString();
				List<LinkedHashMap<String, Object> > data = (List<LinkedHashMap<String, Object> >) map.get(name);
				if (data != null)
				{
					Flag flag = null;
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
		
		for (Flag type : flags)
		{
			if (type.hasSignConfig())
				hasSignConfig = true;
		}
		
	}

	public List<Flags> getRoutingBlockFlags()
	{
		List<Flags> types = new ArrayList<Flags>();
		
		for (Flag bt : flags)
		{
			types.add(bt.getBlockType());
		}
		
		if (types.isEmpty())
			return null;
		return types;
	}
	
	public List<String> getFlagStringList()
	{
		List<String> names = new ArrayList<String>();
		List<Flags> types = getRoutingBlockFlags();
		if (types == null)
			return null;
		for (Flags type : getRoutingBlockFlags())
		{
			names.add(type.toString().toLowerCase());
		}
		if (names.isEmpty())
			return null;
		return names;
	}
	
	public String getFlagsString()
	{
		String names = "";
		List<Flags> types = getRoutingBlockFlags();
		for (int i = 0; i < types.size(); i++)
		{
			Flags type = types.get(i);
			names += type.toString().substring(0, 1).toUpperCase() + type.toString().substring(1).toLowerCase();
			
			if (i != types.size() -1)
				names += ", ";
		}
		return "{" + names + "}";
	}

	public boolean hasSignConfig()
	{
		
		return hasSignConfig;
	}
	
	public List<Flag> getFlags()
	{
		return flags;
	}
	
	public int getBlockId()
	{
		return blockid;
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

}
