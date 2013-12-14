package pl.asie.endernet.lib;

import net.minecraft.tileentity.TileEntity;

public class EntityCoord {
	public int dimensionID, x, y, z;
	
	public EntityCoord(TileEntity entity) {
		this.x = entity.xCoord;
		this.y = entity.yCoord;
		this.z = entity.zCoord;
		this.dimensionID = entity.getWorldObj().provider.dimensionId;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null || !(other instanceof EntityCoord)) return false;
		if(other == this) return true;
		EntityCoord e = (EntityCoord)other;
		return (e.x == this.x && e.y == this.y && e.z == this.z && e.dimensionID == this.dimensionID);
	}
}
