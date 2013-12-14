package pl.asie.endernet;

import pl.asie.endernet.block.ContainerEnderTransmitter;
import pl.asie.endernet.block.GuiEnderTransmitter;
import pl.asie.endernet.block.TileEntityEnderTransmitter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class NetworkHandler implements IPacketHandler, IGuiHandler {

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if(ID == 1) {
			return new ContainerEnderTransmitter((TileEntityEnderTransmitter)world.getBlockTileEntity(x, y, z),
					player.inventory);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if(ID == 1) {
			return new GuiEnderTransmitter((Container)getServerGuiElement(ID, player, world, x, y, z),
					(TileEntityEnderTransmitter)world.getBlockTileEntity(x, y, z));
		}
		return null;
	}

}
