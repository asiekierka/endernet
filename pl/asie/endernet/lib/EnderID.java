package pl.asie.endernet.lib;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

public class EnderID {
	public String modId;
	public String name;
	public int metadata, stackSize;
	public NBTTagCompound compound;
	
	public static final ArrayList<String> blacklistedItems;
	public static final ArrayList<String> whitelistedNBTItems;
	
	static {
		blacklistedItems = new ArrayList<String>();
		whitelistedNBTItems = new ArrayList<String>();
	}
	
	public EnderID(ItemStack stack) throws BlockConversionException {
		UniqueIdentifier itemID = GameRegistry.findUniqueIdentifierFor(stack.getItem());
		if(itemID == null) throw new BlockConversionException("", "unknown", "Can't find unique identifier");
		this.modId = itemID.modId;
		this.name = itemID.name;
		this.metadata = stack.getItemDamage();
		this.stackSize = stack.stackSize;
		if(blacklistedItems.contains(getItemIdentifier())) throw new BlockConversionException(modId, name, "Blacklisted!");
		if(stack.hasTagCompound()) {
			if(!isAllowedTagCompound()) throw new BlockConversionException(modId, name, "NBT tag compound cannot be sent");
			this.compound = stack.getTagCompound();
		}
	}
	
	public String getItemIdentifier() {
		return this.modId + "|" + this.name;
	}
	
	public boolean isReceiveable() {
		if(GameRegistry.findItem(modId, name) == null) return false;
		return true;
	}
	
	public ItemStack createItemStack() {
		ItemStack stack = GameRegistry.findItemStack(modId, name, stackSize);
		stack.setItemDamage(metadata);
		stack.setTagCompound(compound);
		return stack;
	}
	
	public boolean isAllowedTagCompound() {
		if(this.modId.equals("minecraft")) return true; // All vanilla items are allowed NBTs by default
		if(whitelistedNBTItems.contains(getItemIdentifier())) return true;
		return false;
	}
}
