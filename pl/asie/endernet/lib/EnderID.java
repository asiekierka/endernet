package pl.asie.endernet.lib;

import java.util.ArrayList;

import net.minecraft.item.Item;
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
		if(!(stack instanceof ItemStack)) throw new BlockConversionException("", "unknown", "Custom item stacks unsupported!");
		UniqueIdentifier itemID = GameRegistry.findUniqueIdentifierFor(stack.getItem());
		if(itemID == null) {
			this.modId = "Minecraft";
			this.name = stack.getItem().getUnlocalizedName();
		} else {
			this.modId = itemID.modId;
			this.name = itemID.name;
		}
		this.metadata = stack.getItemDamage();
		this.stackSize = stack.stackSize;
		if(blacklistedItems.contains(getItemIdentifier())) throw new BlockConversionException(modId, name, "Blacklisted!");
		if(stack.hasTagCompound()) {
			if(!isAllowedTagCompound()) throw new BlockConversionException(modId, name, "NBT tag compound cannot be sent!");
			this.compound = stack.getTagCompound();
		}
	}
	
	public String getItemIdentifier() {
		return this.modId + "|" + this.name;
	}
	
	public boolean isReceiveable() {
		return (createItemStack() != null);
	}
	
	public ItemStack createItemStack() {
		ItemStack stack = null;
		if(this.modId.equals("Minecraft")) {
			for(Item i: Item.itemsList) {
				if(i == null) continue;
				if(i.getUnlocalizedName().equals(this.name)) {
					stack = new ItemStack(i, stackSize);
				}
			}
			if(stack == null) return null;
		} else stack = GameRegistry.findItemStack(modId, name, stackSize);
		if(stack != null) {
			stack.setItemDamage(metadata);
			stack.setTagCompound(compound);
		}
		return stack;
	}
	
	public boolean isAllowedTagCompound() {
		if(this.modId.equals("Minecraft")) return true; // All vanilla items are allowed NBTs by default
		if(whitelistedNBTItems.contains(getItemIdentifier())) return true;
		return false;
	}
	
	public static String getItemIdentifierFor(Item item) {
		UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(item);
		if(uid == null) {
			return "Minecraft|" + item.getUnlocalizedName();
		} else return uid.modId + "|" + uid.name;
	}
	
	public static String getItemIdentifierFor(ItemStack itemStack) {
		if(!(itemStack instanceof ItemStack)) return "unknown";
		else return getItemIdentifierFor(itemStack.getItem());
	}
}
