package pl.asie.endernet.block;

import java.util.Random;

import pl.asie.endernet.EnderNet;
import pl.asie.endernet.http.HTTPResponse;
import pl.asie.endernet.lib.BlockConversionException;
import pl.asie.endernet.lib.EnderID;
import pl.asie.endernet.lib.EnderRedirector;
import pl.asie.endernet.lib.EnderServer;
import pl.asie.endernet.lib.SlotEnergy;
import cpw.mods.fml.client.FMLClientHandler;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
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

public class TileEntityEnderTransmitter extends TileEntityEnderModem implements IInventory {
	public TileEntityEnderTransmitter() {
		super(false, true); // transmit only
	}
	
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
			ItemStack stack;
			if (this.inventory[slot].stackSize <= amount) {
				stack = this.inventory[slot];
				this.inventory[slot] = null;
		        onSlotUpdate(slot);
				return stack;
			} else {
				stack = this.inventory[slot].splitStack(amount);

				if (this.inventory[slot].stackSize == 0)
					this.inventory[slot] = null;
				
		        onSlotUpdate(slot);
				
				return stack;
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
		if(inventory[0] == null) return 35;
		else return 35 + ((inventory[0].stackSize - 1) * 10);
	}
	
	private boolean isReceiveable = true;
	private boolean startSending = true;
	
	public boolean canReceive() {
		return isReceiveable;
	}
	
	private boolean updateReceive(boolean checkRemotely) {
		if(EnderNet.isDimensionBlacklisted(this.worldObj.provider.dimensionId)) return false;
		if(inventory[0] == null) return true; // I can always receive air, you know... :3
		if(this.worldObj.isRemote) return true; // No pinging on the client
		if(checkRemotely) return EnderRedirector.canReceive(address, inventory[0]);
		else return isReceiveable;
	}
	
	private int clientRenderMessage = 0; // 1 - spawn particles
	
	protected void sendToReceiver() {
		HTTPResponse response = EnderRedirector.receive(address, inventory[0]);
		if(response.success) {
			this.decrStackSize(0, response.amountSent);
			this.isReceiveable = updateReceive(true);
			this.clientRenderMessage = 1;
		} else this.isReceiveable = false;
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	private class ReceiveThread extends Thread {
        private TileEntityEnderTransmitter entity;
        private int action;
        
        ReceiveThread(TileEntityEnderTransmitter t, int a) {
            this.entity = t;
            this.action = a;
        }

        public void run() {
            if(action == 1) entity.sendToReceiver();
            else if(action == 2) {
            	entity.isReceiveable = entity.updateReceive(true);
            	entity.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            	entity.startSending = true;
            }
        }
    }
	
	private ReceiveThread rt = null;
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if(!canReceive()) progress = 0;
		if(inventory[0] != null && startSending && canReceive()) {
			if(progress < getMaxProgress()) {
				progress++;
			} else { // Finished
				if(!this.worldObj.isRemote) {
					if(rt == null) {
						rt = new ReceiveThread(this, 1);
						rt.start();
					} else if(!rt.isAlive()) rt = null;
				}
			}
		}
	}
	
	private Random random = new Random();
	
	public void spawnSuccessParticles() {
		if(!EnderNet.spawnParticles) return;
		int count = 56 + random.nextInt(24);
		if(inventory[0] != null) count +=  (2 * inventory[0].stackSize);
		for(; count >= 0; count--) {
			double randX = ((random.nextDouble() * 0.8D) - 0.4D) + 0.5D + (double)this.xCoord;
			double randY = (double)this.yCoord + 0.9D + (random.nextDouble() * 0.2D);
			double randZ = ((random.nextDouble() * 0.8D) - 0.4D) + 0.5D + (double)this.zCoord;
			double randVX = (random.nextDouble() * 0.15D) - 0.075D;
			double randVY = (random.nextDouble() * 0.1D) + 0.02D; 
			double randVZ = (random.nextDouble() * 0.15D) - 0.075D;
			this.worldObj.spawnParticle("smoke", randX, randY, randZ, randVX, randVY, randVZ);	
		}
	}
	
	private void onSlotUpdate(int slot) {
		super.onInventoryChanged();
        if(slot == 0) { // Item changed!
    		startSending = false;
    		if(inventory[0] == null) {
    			progress = 0; // Reset progress if no item
    		}
			ReceiveThread rt = new ReceiveThread(this, 2);
			rt.start();
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
        this.updateReceive(false); // Fix the possibility of blacklisted dim not showing
		this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void closeChest() { }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return (slot != 1 ? true : SlotEnergy.isValid(stack));
	}

	private void writeNBTProgress(NBTTagCompound tagCompound) {
		tagCompound.setBoolean("r", canReceive());
		tagCompound.setShort("p", (short)progress);
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		writeNBTProgress(tagCompound);
		writeNBTEnderData(tagCompound);
		tagCompound.setByte("c", (byte)clientRenderMessage);
		this.clientRenderMessage = 0;
		return new Packet132TileEntityData(xCoord, yCoord, zCoord, 0, tagCompound);
	}

	@Override
	public void onDataPacket(INetworkManager networkManager, Packet132TileEntityData packet) {
		switch((int)packet.data.getByte("c")) {
			case 1:
				this.spawnSuccessParticles();
				break;
		}
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

	public void setProgress(int i) {
		this.progress = i;
	}
	
	// ComputerCraft compat begin
	
	@Override
	public String[] getMethodNames() {
		String[] names = new String[]{ "getAddress", "setAddress", "getID", "send", "hasItem", "canSendItem" };
		return names;
	}
	
	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,
			int method, Object[] arguments) throws Exception {
		if(method < 4) return super.callMethod(computer, context, method, arguments);
		else switch(method) {
			case 4: { // hasItem
				Boolean[] output = new Boolean[1];
				output[0] = inventory[0] != null && inventory[0].stackSize > 0;
				return output; }
			case 5: { // canSendItem
				Boolean[] output = new Boolean[1];
				output[0] = canReceive();
				return output; }
		}
		return null;
	}
}
