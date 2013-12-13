package pl.asie.endernet.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockEnderTransmitter extends BlockContainer {
	public BlockEnderTransmitter(int id) {
		super(id, Material.iron);
		this.setUnlocalizedName("endernet.enderTransmitter");
		this.setCreativeTab(CreativeTabs.tabMisc);
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityEnderTransmitter();
	}

}
