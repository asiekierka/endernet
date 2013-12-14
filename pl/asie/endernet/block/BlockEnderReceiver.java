package pl.asie.endernet.block;

import pl.asie.endernet.EnderNet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;

public class BlockEnderReceiver extends BlockEnder {
	public BlockEnderReceiver(int id) {
		super(id);
		this.setTextureName("endernet:ender_receiver");
		this.setUnlocalizedName("endernet.enderReceiver");
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityEnderReceiver();
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if(!world.isRemote && !player.isSneaking()) {
			ChatMessageComponent chat = new ChatMessageComponent();
			TileEntityEnder ender = (TileEntityEnder)world.getBlockTileEntity(x, y, z);
			chat.addText("This Receiver's ID is " + ender.enderNetID);
			player.sendChatToPlayer(chat);
		}
		return true;
	}
}
