package pl.asie.endernet.block;

import mods.immibis.redlogic.api.wiring.IBundledEmitter;
import mods.immibis.redlogic.api.wiring.IBundledUpdatable;
import mods.immibis.redlogic.api.wiring.IBundledWire;
import mods.immibis.redlogic.api.wiring.IConnectable;
import mods.immibis.redlogic.api.wiring.IWire;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public abstract class TileEntityBase extends TileEntity implements IBundledEmitter, IBundledUpdatable, IConnectable {
	private int redstoneValue = 0;
	
	public abstract boolean canConnectRedstone();
	public abstract int getRedstoneValue();
	public abstract boolean setRedstoneValue(int value);
	
	protected int getRedstoneInternal() {
		return this.redstoneValue;
	}
	
	protected void setRedstoneInternal(int value) {
		this.redstoneValue = value;
	}

	// RedLogic compatibility
	
	@Override
	public boolean connects(IWire wire, int blockFace, int fromDirection) {
		return canConnectRedstone();
	} 

	@Override
	public boolean connectsAroundCorner(IWire wire, int blockFace,
			int fromDirection) {
		return false;
	}

	@Override
	public byte[] getBundledCableStrength(int blockFace, int toDirection) {
		byte[] values = new byte[16];
		for(int i = 0; i < 16; i++) {
			values[i] = (getRedstoneValue() & (1<<i)) > 0 ? (byte)255 : (byte)0;
		}
		return values;
	}
	
	@Override
	public void onBundledInputChanged() {
		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
			TileEntity te = worldObj.getBlockTileEntity(xCoord+dir.offsetX, yCoord+dir.offsetY, zCoord+dir.offsetZ);
			if(te instanceof IBundledWire) {
				IBundledWire wire = (IBundledWire)te;
				for(int face = -1; face < 6; face++) {
					if(wire.wireConnectsInDirection(face, dir.ordinal())) {
						int value = 0;
						byte[] data = wire.getBundledCableStrength(face, dir.ordinal());
						for(int i = 0; i < 16; i++) {
							if(data[i] != 0) value |= 1<<i;
						}
						setRedstoneValue(value);
						return;
					}
				}
			}
		}
	}
}
