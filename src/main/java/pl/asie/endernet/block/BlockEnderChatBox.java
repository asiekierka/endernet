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
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockEnderChatBox extends BlockEnderModem {
	private Icon iconTop, iconSide;
	
	public BlockEnderChatBox(int id) {
		super(id);
		this.setTextureName("endernet:ender_chatbox");
		this.setUnlocalizedName("endernet.enderChatBox");
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityEnderChatBox();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int meta) {
        return (side < 2 ? iconTop : iconSide);
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister reg) {
		iconTop = reg.registerIcon("endernet:ender_chatbox_top");
		iconSide = reg.registerIcon("endernet:ender_chatbox_side");
	}
}
