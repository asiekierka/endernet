package pl.asie.endernet.block;

import buildcraft.api.transport.IPipeTile;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import cofh.api.transport.IItemConduit;
import net.minecraftforge.common.ForgeDirection;
import pl.asie.endernet.EnderNet;
import pl.asie.endernet.api.IEnderStringReceiver;
import pl.asie.endernet.lib.EnderID;
import pl.asie.endernet.lib.EnderRedirector;
import pl.asie.endernet.lib.EnderServer;

public class TileEntityEnderModem extends TileEntityEnder implements IEnderStringReceiver {
	public final boolean CAN_RECEIVE_MODEM;
	public final boolean CAN_TRANSMIT_MODEM;
	
	public TileEntityEnderModem(boolean receive, boolean transmit) {
		CAN_RECEIVE_MODEM = receive;
		CAN_TRANSMIT_MODEM = transmit;
	}
	
	public TileEntityEnderModem() {
		this(true, true);
	}
	
	private class StringSendThread extends Thread {
        private String address, text;
        
        StringSendThread(String address, String text) {
            this.address = address;
            this.text = text;
        }

        public void run() {
        	EnderRedirector.sendString(address, text);
        }
    }
	
	public void sendString(String address, String s) {
		StringSendThread sst = new StringSendThread(address, s);
		sst.start();
	}
	
	public void sendString(String s) {
		sendString(address, s);
	}
	
	@Override
	public boolean canTransmit() {
		return CAN_TRANSMIT_MODEM ? super.canTransmit() : false;
	}
	
	@Override
	public boolean canReceive() {
		return CAN_RECEIVE_MODEM ? super.canReceive() : false;
	}
	
	// COMPUTERCRAFT COMPATIBILITY BEGIN
	@Override
	public String[] getMethodNames() {
		String[] names;
		if(CAN_TRANSMIT_MODEM) names = new String[]{ "getAddress", "setAddress", "getID", "canReceive", "canTransmit", "send" };
		else names = new String[]{ "getAddress", "setAddress", "getID", "canReceive", "canTransmit" };
		return names;
	}
	
	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,
			int method, Object[] arguments) throws Exception {
		if(method < 5) return super.callMethod(computer, context, method, arguments);
		else switch(method) {
			case 5: // send
				if(!canTransmit()) return null;
				if(arguments.length == 2) {
					if(!(arguments[0] instanceof String) && !(arguments[1] instanceof String)) return null;
					sendString((String)arguments[0], (String)arguments[1]);
				} else if(arguments.length == 1) {
					if(!(arguments[0] instanceof String)) return null;
					sendString((String)arguments[0]);
				}
				break;
		}
		return null;
	}
	
	@Override
	public boolean receiveString(EnderServer server, String string, String endpoint) {
		if(!canReceive() || super.computers.size() == 0) return false;
		String serverName = (server != null ? server.name : "unknown");
		for(IComputerAccess computer: super.computers) {
			Object[] arguments = new Object[] { serverName, string, computer.getAttachmentName(), endpoint };
			computer.queueEvent("endernet_message", arguments);
		}
		return true;
	}
	
	@Override
	public String getType() {
		return "endernet_modem";
	}
}
