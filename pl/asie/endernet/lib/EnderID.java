package pl.asie.endernet.lib;

import java.io.IOException;
import java.util.ArrayList;

import pl.asie.endernet.EnderNet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

public class EnderID {
	public String modId;
	public String name;
	public int metadata, stackSize;
	public byte[] compound;
	
	public static final ArrayList<String> blacklistedItems;
	public static final ArrayList<String> whitelistedNBTItems;
	
	static {
		blacklistedItems = new ArrayList<String>();
		whitelistedNBTItems = new ArrayList<String>();
		blacklistedItems.add("Minecraft|item.map"); // Maps do not use NBT, data is stored on the server side, can't send.
		
		// SUPPORT
		// AsieTweaks
		whitelistedNBTItems.add("asietweaks|asietweaks.dyedBook");
		// OpenBlocks
		whitelistedNBTItems.add("OpenBlocks|openblocks.crayonGlasses");
		whitelistedNBTItems.add("OpenBlocks|openblocks.imaginary");
		whitelistedNBTItems.add("OpenBlocks|openblocks.paintbrush");
		// Thermal Expansion 3
		whitelistedNBTItems.add("ThermalExpansion|EnergyCell");
		whitelistedNBTItems.add("ThermalExpansion|Tesseract");
		whitelistedNBTItems.add("Minecraft|item.thermalexpansion.capacitor");
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
			NBTTagCompound compound = stack.getTagCompound();
			try {
				this.compound = CompressedStreamTools.compress(compound);
			} catch(Exception e) { e.printStackTrace(); throw new BlockConversionException(modId, name, "Could not compress NBT tag compound!"); }
			if(!isAllowedTagCompound(compound)) throw new BlockConversionException(modId, name, "NBT tag compound cannot be sent!");
		}
	}
	
	public String getItemIdentifier() {
		return this.modId + "|" + this.name;
	}
	
	public boolean isReceiveable() {
		return (createItemStack() != null);
	}
	
	public void setTagCompound(NBTTagCompound compound) throws IOException {
		this.compound = CompressedStreamTools.compress(compound);
	}
	
	public NBTTagCompound getTagCompound() {
		try {
			return CompressedStreamTools.decompress(compound);
		} catch(Exception e) { e.printStackTrace(); return null; }
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
			stack.setTagCompound(getTagCompound());
		}
		return stack;
	}
	
	public boolean isAllowedTagCompound(NBTTagCompound compound) {
		if(this.modId.equals("Minecraft")) return true; // All vanilla items are allowed NBTs by default (BUG - also works for badly registered items)
		if(whitelistedNBTItems.contains(getItemIdentifier())) return true;
		/* This routine checks for common patterns that are whitelisted. */
		boolean onlyAllowed = true;
		int bookCount = 0;
		for(Object o: compound.getTags()) {
			NBTBase base = (NBTBase)o;
			if(base.getName().equals("ench")) { }
			else if(base.getName().equals("author") || base.getName().equals("title") || base.getName().equals("pages")) {
				bookCount++;
			}
			else onlyAllowed = false;
			break;
		}
		if(bookCount != 0 && bookCount != 3) return false; // Books have all of these tags or none.
		return onlyAllowed;
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
