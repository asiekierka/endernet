package pl.asie.endernet.block;

import java.util.Random;

import pl.asie.endernet.EnderNet;
import powercrystals.minefactoryreloaded.api.rednet.IConnectableRedNet;
import powercrystals.minefactoryreloaded.api.rednet.RedNetConnectionType;
import mods.immibis.redlogic.api.wiring.IConnectable;
import mods.immibis.redlogic.api.wiring.IWire;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockEnder extends BlockContainer implements IConnectableRedNet {
	public BlockEnder(int id) {
		super(id, Material.iron);
		this.setCreativeTab(CreativeTabs.tabMisc);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return null;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if(!world.isRemote && !player.isSneaking()) {
			ChatMessageComponent chat = new ChatMessageComponent();
			TileEntityEnder ender = (TileEntityEnder)world.getBlockTileEntity(x, y, z);
			if(!ender.canReceive()) chat.addKey("error.endernet.dimension");
			else chat.addFormatted("info.endernet.id", new Object[]{""+ender.enderNetID});
			player.sendChatToPlayer(chat);
		}
		return true;
	}

	public void onBlockDestroyed(World world, int x, int y, int z, int meta) {
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if(tileEntity != null) {
			EnderNet.registry.removeEntity(tileEntity);
			tileEntity.invalidate();
		}
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int meta) {
		super.onBlockDestroyedByPlayer(world, x, y, z, meta);
		this.onBlockDestroyed(world, x, y, z, meta);
	}

	@Override
	public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion explosion) {
		super.onBlockDestroyedByExplosion(world, x, y, z, explosion);
		this.onBlockDestroyed(world, x, y, z, 0);
	}
	
    @Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta) {
    	this.onBlockDestroyed(world, x, y, z, meta);
    	this.dropItems(world, x, y, z);
    	super.breakBlock(world, x, y, z, id, meta);
	}
    
    public static void dropItems(World world, int x, int y, int z) {
    	Random rand = new Random();
    	TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    	if (tileEntity == null || !(tileEntity instanceof IInventory)) {
    		return;
    	}
    	IInventory inventory = (IInventory) tileEntity;

    	for (int i = 0; i < inventory.getSizeInventory(); i++) {
    		ItemStack item = inventory.getStackInSlot(i);

    		if (item != null && item.stackSize > 0) {
    			inventory.setInventorySlotContents(i, null);
    			float rx = rand.nextFloat() * 0.8F + 0.1F;
    			float ry = rand.nextFloat() * 0.8F + 0.1F;
    			float rz = rand.nextFloat() * 0.8F + 0.1F;

    			EntityItem entityItem = new EntityItem(world,
    					x + rx, y + ry, z + rz,
    					new ItemStack(item.itemID, item.stackSize, item.getItemDamage()));

    			if (item.hasTagCompound()) {
    				entityItem.getEntityItem().setTagCompound((NBTTagCompound)item.getTagCompound().copy());
    			}

    			float factor = 0.05F;
    			entityItem.motionX = rand.nextGaussian() * factor;
    			entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
    			entityItem.motionZ = rand.nextGaussian() * factor;
    			world.spawnEntityInWorld(entityItem);
    			item.stackSize = 0;
    		}
    	}
    }
    
    public int getRedstoneValue(World world, int x, int y, int z) {
    	return (world.isBlockIndirectlyGettingPowered(x, y, z)  ? 1 : 0);
    }

	@Override
	public RedNetConnectionType getConnectionType(World world, int x, int y,
			int z, ForgeDirection side) {
		return RedNetConnectionType.None;
	}

	@Override
	public int[] getOutputValues(World world, int x, int y, int z,
			ForgeDirection side) {
		return null;
	}

	@Override
	public int getOutputValue(World world, int x, int y, int z,
			ForgeDirection side, int subnet) {
		return 0;
	}

	@Override
	public void onInputsChanged(World world, int x, int y, int z,
			ForgeDirection side, int[] inputValues) {
	}

	@Override
	public void onInputChanged(World world, int x, int y, int z,
			ForgeDirection side, int inputValue) {
	}
}
