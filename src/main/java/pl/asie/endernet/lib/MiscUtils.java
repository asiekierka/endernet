package pl.asie.endernet.lib;

import java.util.Random;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MiscUtils {
	private static final Random rand = new Random();
	
	public static int getSideFromName(String endpoint) {
		endpoint = endpoint.toLowerCase();
		if(endpoint.equalsIgnoreCase("up") || endpoint.equalsIgnoreCase("top")) return 1;
		if(endpoint.equalsIgnoreCase("down") || endpoint.equalsIgnoreCase("bottom")) return 0;
		if(endpoint.equalsIgnoreCase("left") || endpoint.equalsIgnoreCase("west")) return 4;
		if(endpoint.equalsIgnoreCase("right") || endpoint.equalsIgnoreCase("east")) return 5;
		if(endpoint.equalsIgnoreCase("front") || endpoint.equalsIgnoreCase("forward") || endpoint.equalsIgnoreCase("north")) return 2;
		if(endpoint.equalsIgnoreCase("back") || endpoint.equalsIgnoreCase("south")) return 3;
		return -1;
	}

    public static void dropItems(World world, int x, int y, int z) {
    	TileEntity tileEntity = world.getTileEntity(x, y, z);
    	if (tileEntity == null || !(tileEntity instanceof IInventory)) {
    		return;
    	}
    	IInventory inventory = (IInventory) tileEntity;

    	for (int i = 0; i < inventory.getSizeInventory(); i++) {
    		ItemStack item = inventory.getStackInSlot(i);

    		if (item != null && item.stackSize > 0) {
    			inventory.setInventorySlotContents(i, null);
    			dropItem(world, x, y, z, item);
    			item.stackSize = 0;
    		}
    	}
    }

	public static void dropItem(World world, int x, int y, int z, ItemStack item) {
		float rx = rand.nextFloat() * 0.8F + 0.1F;
		float ry = rand.nextFloat() * 0.8F + 0.1F;
		float rz = rand.nextFloat() * 0.8F + 0.1F;

		EntityItem entityItem = new EntityItem(world,
				x + rx, y + ry, z + rz,
				new ItemStack(item.getItem(), item.stackSize, item.getItemDamage()));

		if (item.hasTagCompound()) {
			entityItem.getEntityItem().setTagCompound((NBTTagCompound)item.getTagCompound().copy());
		}

		float factor = 0.05F;
		entityItem.motionX = rand.nextGaussian() * factor;
		entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
		entityItem.motionZ = rand.nextGaussian() * factor;
		world.spawnEntityInWorld(entityItem);
	}
    
}
