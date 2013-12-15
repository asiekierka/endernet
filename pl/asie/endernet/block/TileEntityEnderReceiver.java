package pl.asie.endernet.block;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import pl.asie.endernet.lib.EnderID;

public class TileEntityEnderReceiver extends TileEntityEnder {
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
	
	public boolean receiveItem(EnderID item) {
		ItemStack stack = item.createItemStack();
		for(int side = 0; side < 6; side++) {
			int[] dir = DIRECTIONS[side];
			TileEntity entity = worldObj.getBlockTileEntity(
					xCoord+dir[0], yCoord+dir[1], zCoord+dir[2]
							);
			if(entity == null || (entity instanceof TileEntityEnderTransmitter)) continue;
			if(entity instanceof ISidedInventory) {
				ISidedInventory inv = (ISidedInventory)entity;
				for(int slot: inv.getAccessibleSlotsFromSide(opposite(side))) {
					if(inv.canInsertItem(slot, stack, opposite(side))) {
						tryMergeStacks(inv, slot, stack);
						if(stack.stackSize == 0) return true;
					}
				}
			} else if(entity instanceof IInventory) {
				IInventory inv = (IInventory)entity;
				for(int slot = 0; slot < inv.getSizeInventory(); slot++) {
					tryMergeStacks(inv, slot, stack);
					if(stack.stackSize == 0) return true;			
				}
			}
		}
		if(stack.stackSize == 0) return true;	
		return false;
	}
}
