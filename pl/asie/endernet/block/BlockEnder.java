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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

public class BlockEnder extends BlockBase {
	public BlockEnder(int id, boolean connectsToRedstone) {
		super(id, connectsToRedstone);
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
			else chat.addFormatted("info.endernet.id", new Object[]{""+ender.getEnderNetID()});
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
			enderData.setInteger("eid", ender.getEnderNetID());

			this.breakBlock(world, x, y, z, this.blockID, 0);
			world.setBlockToAir(x, y, z);
			item.setTagCompound(enderData);
			MiscUtils.dropItem(world, x, y, z, item);
		}
	}

	@Override
	public void onBlockDestroyed(World world, int x, int y, int z, int meta) {
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if(tileEntity != null) {
			EnderNet.registry.removeEntity(tileEntity);
			tileEntity.invalidate();
		}
	}
}
