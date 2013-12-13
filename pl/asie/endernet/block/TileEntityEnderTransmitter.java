package pl.asie.endernet.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;

public class TileEntityEnderTransmitter extends TileEntity implements IInventory {
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
				return itemstack;
			} else {
				itemstack = this.inventory[slot].splitStack(amount);

				if (this.inventory[slot].stackSize == 0)
					this.inventory[slot] = null;
				
				return itemstack;
			}
		} else return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = getStackInSlot(slot);
        if(stack == null) return null;
        inventory[slot] = null;
        return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory[slot] = stack;
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
	public void openChest() { }

	@Override
	public void closeChest() { }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 1) { // Fuel
			// Check if is fuel
			if(TileEntityFurnace.getItemBurnTime(stack) == 0) {
				// TODO: check for batteries and stuff
				return false;
			}
		}
		return true;
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
    }
}
