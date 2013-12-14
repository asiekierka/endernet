package pl.asie.endernet.block;

import pl.asie.endernet.EnderNet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class TileEntityEnder extends TileEntity {
	public int enderNetID = -1;

	@Override
	public void updateEntity() {
		super.updateEntity();
		if(!this.worldObj.isRemote && enderNetID == -1) enderNetID = EnderNet.registry.getEntityID(this);
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		enderNetID = tagCompound.hasKey("eid") ? tagCompound.getInteger("eid") : -1;
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setInteger("eid", enderNetID);
	}
}
