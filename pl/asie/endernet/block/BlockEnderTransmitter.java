package pl.asie.endernet.block;

import pl.asie.endernet.EnderNet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockEnderTransmitter extends BlockEnder {
	public BlockEnderTransmitter(int id) {
		super(id);
		this.setUnlocalizedName("endernet.enderTransmitter");
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityEnderTransmitter();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if(!world.isRemote || player.isSneaking())
			player.openGui(EnderNet.instance, 1, world, x, y, z);
		return true;
	}
}
