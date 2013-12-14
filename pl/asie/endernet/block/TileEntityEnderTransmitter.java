package pl.asie.endernet.block;

import pl.asie.endernet.EnderNet;
import pl.asie.endernet.lib.BlockConversionException;
import pl.asie.endernet.lib.EnderID;
import pl.asie.endernet.lib.EnderRedirector;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;

public class TileEntityEnderTransmitter extends TileEntityEnder implements IInventory {
	protected ItemStack[] inventory = new ItemStack[2];
	
	@Override
	public int getSizeInventory() {
		return 2;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if(i < 0 || i >= 2) return null;
		return inventory[i];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		if (this.inventory[slot] != null) {
			ItemStack itemstack;

			if (this.inventory[slot].stackSize <= amount) {
				itemstack = this.inventory[slot];
				this.inventory[slot] = null;
		        onSlotUpdate(slot);
				return itemstack;
			} else {
				itemstack = this.inventory[slot].splitStack(amount);

				if (this.inventory[slot].stackSize == 0)
					this.inventory[slot] = null;
				
		        onSlotUpdate(slot);
				
				return itemstack;
			}
		} else return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = getStackInSlot(slot);
        if(stack == null) return null;
        inventory[slot] = null;
        onSlotUpdate(slot);
        return stack;
	}
	
	private int progress;
	
	public int getProgress() { return progress; }
	
	public int getMaxProgress() {
		if(inventory[0] == null) return 4; // random number, chosen by fair dice roll
		else return inventory[0].stackSize * 35;
	}
	
	private boolean isReceiveable = true;
	private boolean startSending = true;
	
	public boolean canReceive() {
		return isReceiveable;
	}
	
	private boolean updateReceive() {
		if(inventory[0] == null) return true; // I can always receive air, you know... :3
		if(this.worldObj.isRemote) return true; // No pinging on the client
		return EnderRedirector.canReceive("0.0", inventory[0]);
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if(!canReceive()) progress = 0;
		if(inventory[0] != null && startSending && canReceive()) {
			if(progress < getMaxProgress()) {
				progress++;
			} else { // Finished
				progress = 0;
				this.setInventorySlotContents(0, null);
				updateReceive();
			}
		}
	}
	
	private void onSlotUpdate(int slot) {
		super.onInventoryChanged();
        if(slot == 0) { // Item changed!
    		startSending = false;
    		if(inventory[0] == null) {
    			progress = 0; // Reset progress if no item
    		}
        	boolean newr = updateReceive();
        	isReceiveable = newr;
        	this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        	startSending = true;
        }
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		if(inventory[slot] != null && stack != null) {
			if((inventory[slot].itemID != stack.itemID)
					|| (inventory[slot].getItemDamage() != stack.getItemDamage())) {
				progress = 0; // Reset progress if item changed
				this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}
        inventory[slot] = stack;
        onSlotUpdate(slot);
	}

	@Override
	public String getInvName() {
		return "asietweaks.endertransmitter.inventory";
	}

	@Override
	public boolean isInvNameLocalized() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
        return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this
                ? false : player.getDistanceSq( (double)this.xCoord+0.5D,
                                                (double)this.yCoord+0.5D,
                                                (double)this.zCoord+0.5D ) <= 64.0D;
	}

	@Override
	public void openChest() {
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		EnderNet.log.info("My id is " + this.enderNetID);
	}

	@Override
	public void closeChest() { }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return true;
	}

	private void writeNBTProgress(NBTTagCompound tagCompound) {
		tagCompound.setBoolean("r", canReceive());
		tagCompound.setShort("p", (short)progress);
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		writeNBTProgress(tagCompound);
		tagCompound.setInteger("eid", enderNetID);
		return new Packet132TileEntityData(xCoord, yCoord, zCoord, 0, tagCompound);
	}

	@Override
	public void onDataPacket(INetworkManager networkManager, Packet132TileEntityData packet) {	 
		this.enderNetID = packet.data.getInteger("eid");
		this.isReceiveable = packet.data.getBoolean("r");
		GuiScreen gui = FMLClientHandler.instance().getClient().currentScreen;
		if (gui != null && gui instanceof GuiEnderTransmitter) {
			GuiEnderTransmitter get = (GuiEnderTransmitter)gui;
			get.syncNBTFromClient(packet.data);
		}
	}
	
    // http://www.minecraftforge.net/wiki/Containers_and_GUIs
    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
            super.readFromNBT(tagCompound);
            NBTTagList tagList = tagCompound.getTagList("Inventory");
            this.inventory = new ItemStack[this.getSizeInventory()];
            for (int i = 0; i < tagList.tagCount(); i++) {
                    NBTTagCompound tag = (NBTTagCompound) tagList.tagAt(i);
                    int slot = tag.getByte("Slot") & 255;
                    if (slot >= 0 && slot < inventory.length) {
                            inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
                    }
            }
            this.isReceiveable = tagCompound.getBoolean("r");
            this.progress = tagCompound.getShort("p");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
            super.writeToNBT(tagCompound);
            NBTTagList itemList = new NBTTagList();
            for (int i = 0; i < inventory.length; i++) {
                    ItemStack stack = inventory[i];
                    if (stack != null) {
                            NBTTagCompound tag = new NBTTagCompound();
                            tag.setByte("Slot", (byte) i);
                            stack.writeToNBT(tag);
                            itemList.appendTag(tag);
                    }
            }
            tagCompound.setTag("Inventory", itemList);
            writeNBTProgress(tagCompound);
    }
}
