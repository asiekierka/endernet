package pl.asie.endernet.block;

import pl.asie.endernet.EnderNet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class TileEntityEnder extends TileEntity {
	public int enderNetID = -1;
	protected String address = "local.0";
	
	public String getAddress() { return address; }
	public void setAddress(String address) {
		this.address = address;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if(!this.worldObj.isRemote && enderNetID == -1) enderNetID = EnderNet.registry.getEntityID(this);
	}
	
	protected void writeNBTEnderData(NBTTagCompound tagCompound) {
		tagCompound.setInteger("eid", enderNetID);
        tagCompound.setString("a", address);
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		enderNetID = tagCompound.hasKey("eid") ? tagCompound.getInteger("eid") : -1;
        if(tagCompound.hasKey("a"))
        	this.address = tagCompound.getString("a");
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		writeNBTEnderData(tagCompound);
	}
}
