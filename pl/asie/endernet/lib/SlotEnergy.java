package pl.asie.endernet.lib;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

public class SlotEnergy extends Slot {
	public SlotEnergy(IInventory par1iInventory, int par2, int par3, int par4) {
		super(par1iInventory, par2, par3, par4);
	}
	
	public static boolean isValid(ItemStack stack) {
		if(TileEntityFurnace.getItemBurnTime(stack) == 0) {
			// TODO: check for batteries and stuff
			return false;
		}
		return true;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return isValid(stack);
	}
}
