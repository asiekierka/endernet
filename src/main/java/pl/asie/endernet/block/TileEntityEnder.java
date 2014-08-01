package pl.asie.endernet.block;

import java.util.ArrayList;
import java.util.HashSet;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import dan200.computer.api.IPeripheral;
import pl.asie.endernet.EnderNet;
import pl.asie.endernet.api.IEnderRedstone;
import mods.immibis.redlogic.api.wiring.IBundledEmitter;
import mods.immibis.redlogic.api.wiring.IBundledUpdatable;
import mods.immibis.redlogic.api.wiring.IBundledWire;
import mods.immibis.redlogic.api.wiring.IConnectable;
import mods.immibis.redlogic.api.wiring.IWire;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityEnder extends TileEntityBase implements IEnderRedstone, IPeripheral {
	private int enderNetID = -1;
	protected String address = "local.0";

	// API functions
	public boolean canTransmit() {
		return !EnderNet.isDimensionBlacklisted(this.worldObj.provider.dimensionId);
	}
	
	public boolean canReceive() {
		return !EnderNet.isDimensionBlacklisted(this.worldObj.provider.dimensionId);
	}
	
	public String getAddress() { return address; }
	public void setAddress(String address) {
		this.address = address;
	}
	
	@Override
	public int getRedstoneValue() {
		return 0;
	}

	@Override
	public boolean setRedstoneValue(int value) {
		return false;
	}
	
	@Override
	public boolean receiveRedstoneValue(int value) {
		return false;
	}
	
	public int getEnderNetID() {
		return enderNetID;
	}
	
	// Used for reading NBT data remotely.
	@SideOnly(Side.CLIENT)
	public void setEnderNetIDClient(int id) {
		enderNetID = id;
	}
	
	protected void initWithEnderID(int eid) {
		enderNetID = EnderNet.registry.forceEntityID(this, eid);
	}
	
	// Minecraft functions 
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if(!this.worldObj.isRemote && enderNetID == -1) enderNetID = EnderNet.registry.getEntityID(this);
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
	
	protected void writeNBTEnderData(NBTTagCompound tagCompound) {
		tagCompound.setInteger("eid", enderNetID);
        tagCompound.setString("a", address);
	}
	
	// COMPUTERCRAFT COMPATIBILITY BEGIN
	HashSet<IComputerAccess> computers = new HashSet<IComputerAccess>();
	
	@Override
	public String getType() {
		return "endernet_device";
	}
	
	@Override
	public String[] getMethodNames() {
		String[] names = new String[]{"getAddress", "setAddress", "getID", "canReceive", "canTransmit"};
		return names;
	}
	
	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,
			int method, Object[] arguments) throws Exception {
		switch(method) {
			case 0: { // getAddress
				String[] out = new String[1];
				out[0] = this.getAddress();
				return out; }
			case 1: // setAddress
				if(arguments.length < 1 || !(arguments[0] instanceof String)) break;
				this.setAddress((String)arguments[0]);
				break;
			case 2: { // getID
				Integer[] out = new Integer[1];
				out[0] = this.enderNetID;
				return out; }
			case 3: { // canReceive
				Boolean[] out = new Boolean[1];
				out[0] = canReceive();
				return out; }
			case 4: { // canTransmit
				Boolean[] out = new Boolean[1];
				out[0] = canTransmit();
				return out; }
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
