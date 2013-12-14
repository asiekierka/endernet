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
}
