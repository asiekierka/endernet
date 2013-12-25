package pl.asie.endernet.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemBlockEnder extends ItemBlock {

	public ItemBlockEnder(int id) {
		super(id);
	}
	
	@Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
		NBTTagCompound data = stack.getTagCompound();
		if(data != null && data.hasKey("eid")) {
			list.add("ID: " + data.getInteger("eid"));
		}
	}

	@Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
		if(super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata)) {
			if(stack.getTagCompound() != null && stack.getTagCompound().hasKey("eid")) {
				TileEntityEnder ender = (TileEntityEnder)(((ITileEntityProvider)Block.blocksList[this.getBlockID()]).createNewTileEntity(world));
				ender.xCoord = x;
				ender.yCoord = y;
				ender.zCoord = z;
				ender.worldObj = world;
				ender.putEnderID(stack.getTagCompound().getInteger("eid"));
		        world.setBlockTileEntity(x, y, z, ender);
			}
			return true;
		} else return false;
    }
}
