package pl.asie.endernet.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import pl.asie.endernet.EnderNet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockEnderTransmitter extends BlockEnder {
	private Icon iconTop, iconSide;
	
	public BlockEnderTransmitter(int id) {
		super(id);
		this.setTextureName("endernet:ender_transmitter");
		this.setUnlocalizedName("endernet.enderTransmitter");
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityEnderTransmitter();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if(!world.isRemote && !player.isSneaking())
			player.openGui(EnderNet.instance, 1, world, x, y, z);
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int meta) {
        return (side < 2 ? iconTop : iconSide);
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister reg) {
		iconTop = reg.registerIcon("endernet:ender_transmitter_top");
		iconSide = reg.registerIcon("endernet:ender_transmitter_side");
	}
}
