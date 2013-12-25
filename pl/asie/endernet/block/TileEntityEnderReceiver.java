package pl.asie.endernet.block;

import buildcraft.api.transport.IPipeTile;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import mods.immibis.redlogic.api.wiring.IBundledEmitter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import cofh.api.transport.IItemConduit;
import net.minecraftforge.common.ForgeDirection;
import pl.asie.endernet.api.IEnderRedstone;
import pl.asie.endernet.api.IEnderStringReceiver;
import pl.asie.endernet.lib.EnderID;
import pl.asie.endernet.lib.EnderServer;
import pl.asie.endernet.lib.MiscUtils;

public class TileEntityEnderReceiver extends TileEntityEnderModem implements IEnderRedstone, IEnderStringReceiver, IInventory {
	private boolean updateNextTick;

	public TileEntityEnderReceiver() {
		super(true, false); // receive only
	}
	
	private static final int[][] DIRECTIONS = {
		{0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}
	};
	
	private static int[] opposite(int[] dir) {
		int[] out = new int[3];
		out[0] = 0-dir[0]; out[1] = 0-dir[1]; out[2] = 0-dir[2];
		return out;
	}
	
	private static int opposite(int dir) {
		return dir^1;
	}
	
	// SOURCE: OpenModsLib/openmods/utils/InventoryUtils.java
	// LICENSE: https://github.com/OpenMods/OpenModsLib/blob/master/LICENSE
	// Slight patches done by asie
	public static void tryMergeStacks(IInventory targetInventory, int slot, ItemStack stack) {
		if(stack.stackSize == 0) return;
		if (targetInventory.isItemValidForSlot(slot, stack)) {
			ItemStack targetStack = targetInventory.getStackInSlot(slot);
			if (targetStack == null) {
				targetInventory.setInventorySlotContents(slot, stack.copy());
				stack.stackSize = 0;
			} else {
				boolean valid = targetInventory.isItemValidForSlot(slot, stack);
				if (valid
						&& stack.isItemEqual(targetStack)
						&& targetStack.stackSize < targetStack.getMaxStackSize()) {
					int space = targetStack.getMaxStackSize()
							- targetStack.stackSize;
					int mergeAmount = Math.min(space, stack.stackSize);
					ItemStack copy = targetStack.copy();
					copy.stackSize += mergeAmount;
					targetInventory.setInventorySlotContents(slot, copy);
					stack.stackSize -= mergeAmount;
				}
			}
		}
	}
	
	public int receiveItem(EnderID item, String endpoint) {
		if(!canReceive()) return 0;
		ItemStack stack = item.createItemStack();
		if(stack == null) return 0;
		int amountPre = stack.stackSize;
		int predefinedSide = MiscUtils.getSideFromName(endpoint);
		for(int side = 0; side < 6; side++) {
			if(predefinedSide >= 0 && side != predefinedSide) continue;
			if(stack == null || stack.stackSize == 0) continue;
			int[] dir = DIRECTIONS[side];
			TileEntity entity = worldObj.getBlockTileEntity(
					xCoord+dir[0], yCoord+dir[1], zCoord+dir[2]
							);
			if(entity == null || (entity instanceof TileEntityEnderTransmitter)) continue;
			if(entity instanceof ISidedInventory) {
				ISidedInventory inv = (ISidedInventory)entity;
				int[] slots = inv.getAccessibleSlotsFromSide(opposite(side));
				if(slots != null)
					for(int slot: slots) {
						if(inv.canInsertItem(slot, stack, opposite(side))) {
							tryMergeStacks(inv, slot, stack);
						}
					}
			} else if(entity instanceof IItemConduit) { // TE3 compatibility
				IItemConduit conduit = (IItemConduit)entity;
				ForgeDirection from = ForgeDirection.getOrientation(opposite(side));
				stack = conduit.insertItem(from, stack, false);
			} else if(entity instanceof IPipeTile) { // BC compatibility
				IPipeTile pipe = (IPipeTile)entity;
				ForgeDirection from = ForgeDirection.getOrientation(opposite(side));
				int received = pipe.injectItem(stack, true, from);
				stack.stackSize -= received;
			} else if(entity instanceof IInventory) {
				IInventory inv = (IInventory)entity;
				for(int slot = 0; slot < inv.getSizeInventory(); slot++) {
					tryMergeStacks(inv, slot, stack);
				}
			}
		}
		int amountPost = stack != null ? stack.stackSize : 0;
		return amountPre - amountPost;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if(updateNextTick) {
			updateNextTick = false;
	        this.worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, this.worldObj.getBlockId(xCoord, yCoord, zCoord));
		}
	}
	
	@Override
	public String getType() {
		return "endernet_receiver";
	}
	
	/* THERMAL EXPANSION ITEMDUCT COMPATIBILITY BEGIN
	   TE3 needs all ItemDuct-interfacing items to have a dummy IInventory. -- asie */
	   
	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return null;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		return null;
	}
	
	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return null;
	}
	
	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) { }
	
	@Override
	public String getInvName() {
		return "asietweaks.enderreceiver.inventory";
	}

	@Override
	public boolean isInvNameLocalized() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 0;
	}
	
	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
    	return false;
    }
    
    @Override
	public void openChest() { }
	@Override
	public void closeChest() { }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return false;
	}

	@Override
	public int getRedstoneValue() {
		return this.getRedstoneInternal();
	}
	
	@Override
	public boolean receiveRedstoneValue(int value) {
		this.setRedstoneInternal(value);
		this.updateNextTick = true;
		return true;
	}
}
