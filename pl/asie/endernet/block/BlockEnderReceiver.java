package pl.asie.endernet.block;

import pl.asie.endernet.EnderNet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockEnderReceiver extends BlockEnder {
	public BlockEnderReceiver(int id) {
		super(id);
		this.setUnlocalizedName("endernet.enderReceiver");
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityEnderReceiver();
	}
}
