package pl.asie.endernet.block;

import java.util.ArrayList;
import java.util.HashSet;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;
import pl.asie.endernet.EnderNet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class TileEntityEnder extends TileEntity implements IPeripheral {
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
	
	// COMPUTERCRAFT COMPATIBILITY BEGIN
	HashSet<IComputerAccess> computers = new HashSet<IComputerAccess>();
	
	@Override
	public String getType() {
		return "endernet_device";
	}
	
	@Override
	public String[] getMethodNames() {
		String[] names = new String[2];
		names[0] = "getAddress";
		names[1] = "setAddress";
		return names;
	}
	
	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,
			int method, Object[] arguments) throws Exception {
		switch(method) {
			case 0: // getAddress
				String[] out = new String[1];
				out[0] = this.getAddress();
				return out;
			case 1: // setAddress
				if(arguments.length < 1 || !(arguments[0] instanceof String)) break;
				this.setAddress((String)arguments[0]);
				break;
		}
		return null;
	}
	
	@Override
	public boolean canAttachToSide(int side) {
		return true;
	}
	
	@Override
	public void attach(IComputerAccess computer) {
		computers.add(computer);
	}
	@Override
	public void detach(IComputerAccess computer) {
		computers.remove(computer);
	}
}
