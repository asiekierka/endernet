package pl.asie.endernet.lib;

import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

public class SlotEnergy extends Slot {
	public SlotEnergy(IInventory par1iInventory, int par2, int par3, int par4) {
		super(par1iInventory, par2, par3, par4);
	}
	
	public static boolean isValid(ItemStack stack) {
		return (stack.getItem() == Items.ender_pearl || stack.getItem() == Items.ender_eye);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return isValid(stack);
	}
}
