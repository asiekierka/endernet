package pl.asie.endernet.block;

import pl.asie.endernet.EnderNet;
import pl.asie.endernet.lib.SlotEnergy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerEnderTransmitter extends Container {
	public ContainerEnderTransmitter(TileEntityEnderTransmitter entity, InventoryPlayer inventoryPlayer) {
		super();
		addSlotToContainer(new Slot(entity, 0, 67, 34));
		if(EnderNet.enableEnergy) addSlotToContainer(new SlotEnergy(entity, 1, 93, 34));
        bindPlayerInventory(inventoryPlayer, 8, 84);
        entity.openChest();
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		ItemStack stack = null;
		Slot slotObject = (Slot)inventorySlots.get(slot);
		if(slotObject != null && slotObject.getHasStack()) {
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();
			if(slot < 2) {
				if(!this.mergeItemStack(stackInSlot, 2, inventorySlots.size(), true)) {
					return null;
				}
			}
			else if(!this.mergeItemStack(stackInSlot, 0, 2, false)) {
				return null;
			}
			if(stackInSlot.stackSize == 0) {
				slotObject.putStack(null);
			} else {
				slotObject.onSlotChanged();
			}
		}
		return stack;
	}
	
	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer, int startX, int startY) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
						startX + j * 18, startY + i * 18));
			}
		}
		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, startX + i * 18, startY + 58));
		}
	}
}
