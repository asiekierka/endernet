package pl.asie.endernet.block;

import java.util.ArrayList;
import java.util.Random;

import buildcraft.api.tools.IToolWrench;
import pl.asie.endernet.EnderNet;
import pl.asie.endernet.lib.MiscUtils;
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
import net.minecraftforge.event.ForgeEventFactory;

public class BlockEnder extends BlockContainer implements IConnectableRedNet {
	public BlockEnder(int id) {
		super(id, Material.iron);
		this.setCreativeTab(CreativeTabs.tabMisc);
		this.setHardness(1.5F);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return null;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if(world.isRemote) return true;
		if(player.isSneaking()) {
			tryRemovingBlock(world, x, y, z, player);
		} else { // Not sneaking
			ChatMessageComponent chat = new ChatMessageComponent();
			TileEntityEnder ender = (TileEntityEnder)world.getBlockTileEntity(x, y, z);
			if(!ender.canReceive()) chat.addKey("error.endernet.dimension");
			else chat.addFormatted("info.endernet.id", new Object[]{""+ender.enderNetID});
			player.sendChatToPlayer(chat);
		}
		return true;
	}

	public void tryRemovingBlock(World world, int x, int y, int z, EntityPlayer player) {
		ItemStack held = player.inventory.getCurrentItem();
		boolean doDrop = false;
		if(held.getItem() instanceof IToolWrench && ((IToolWrench)held.getItem()).canWrench(player, x, y, z)) {
			doDrop = true;
			((IToolWrench)held.getItem()).wrenchUsed(player, x, y, z);
		}
		if(doDrop) {
			TileEntityEnder ender = (TileEntityEnder)world.getBlockTileEntity(x, y, z);
			ItemStack item = new ItemStack(this, 1);
			NBTTagCompound enderData = item.getTagCompound();
			if(enderData == null) enderData = new NBTTagCompound();
			enderData.setInteger("eid", ender.enderNetID);

			this.breakBlock(world, x, y, z, this.blockID, 0);
			world.setBlockToAir(x, y, z);
			item.setTagCompound(enderData);
			MiscUtils.dropItem(world, x, y, z, item);
		}
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
    	super.breakBlock(world, x, y, z, id, meta);
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
