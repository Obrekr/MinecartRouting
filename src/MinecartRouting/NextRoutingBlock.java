package MinecartRouting;

import org.bukkit.block.BlockFace;

public class NextRoutingBlock {
	
	private RoutingBlock rb;
	private BlockFace nextdirection;
	private Integer distance;
	
	public NextRoutingBlock(RoutingBlock block, BlockFace f, Integer d)
	{
		rb = block;
		nextdirection = f;
		distance = d;
	}

	public boolean isValid()
	{
		if (rb != null && nextdirection != null && distance != null)
			return true;
		return false;
	}
	
	public Integer getDistance() {
		return distance;
	}

	public void setDistance(Integer distance) {
		this.distance = distance;
	}

	public RoutingBlock getRoutingBlock() {
		return rb;
	}

	public void setRoutingBlock(RoutingBlock block) {
		this.rb = block;
	}

	public BlockFace getDirection() {
		return nextdirection;
	}

	public void setDirection(BlockFace nextdirection) {
		this.nextdirection = nextdirection;
	}
	

}
