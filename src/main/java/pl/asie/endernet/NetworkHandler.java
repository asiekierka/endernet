package pl.asie.endernet;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import pl.asie.endernet.block.ContainerEnderTransmitter;
import pl.asie.endernet.block.GuiEnderTransmitter;
import pl.asie.endernet.block.TileEntityEnder;
import pl.asie.endernet.block.TileEntityEnderTransmitter;
import pl.asie.endernet.lib.EntityCoord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class NetworkHandler implements IGuiHandler {

	/*@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {
			int command = data.readUnsignedByte();
			switch(command) {
				case 1: // SET ADDRESS (C->S)
					EntityCoord ec = EnderNet.registry.getEntityCoord(data.readInt());
					if(ec != null) {
						TileEntity entity = ec.get();
						if(entity == null || !(entity instanceof TileEntityEnder)) return;
						((TileEntityEnder)entity).setAddress(data.readUTF());
					}
					break;
			}
		} catch(Exception e) { e.printStackTrace(); }
	}*/

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if(ID == 1) {
			return new ContainerEnderTransmitter((TileEntityEnderTransmitter)world.getTileEntity(x, y, z),
					player.inventory);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if(ID == 1) {
			return new GuiEnderTransmitter((Container)getServerGuiElement(ID, player, world, x, y, z),
					(TileEntityEnderTransmitter)world.getTileEntity(x, y, z));
		}
		return null;
	}

}
