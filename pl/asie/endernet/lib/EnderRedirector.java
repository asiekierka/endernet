package pl.asie.endernet.lib;

import java.util.HashMap;

import pl.asie.endernet.EnderNet;
import pl.asie.endernet.api.IEnderStringReceiver;
import pl.asie.endernet.block.TileEntityEnderReceiver;
import pl.asie.endernet.http.HTTPClient;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class EnderRedirector {
	
	public static boolean sendString(String address, String string) {
		try {
			if(isLocal(getServerName(address))) {
				TileEntity entity = EnderNet.registry.getTileEntity(getServerEnderID(address));
				if(entity == null || !(entity instanceof IEnderStringReceiver)) return false;
				return ((IEnderStringReceiver)entity).receiveString(new EnderServer("local", "127.0.0.1"), string);
			} else {
				String serverName = getServerName(address);
				String serverAddress = EnderNet.servers.getAddress(serverName);
				if(serverAddress == null) return false;
				return HTTPClient.sendString(serverAddress, getServerEnderID(address), string);
			}
		} catch(Exception e) {
			return false;
		}
	}
	public static boolean canReceive(String address, ItemStack stack) {
		try {
			if(isLocal(getServerName(address))) {
				return new EnderID(stack).isReceiveable();
			} else {
				String serverName = getServerName(address);
				String serverAddress = EnderNet.servers.getAddress(serverName);
				if(serverAddress == null) return false;
				return HTTPClient.canReceive(serverAddress, new EnderID(stack));
			}
		} catch(Exception e) {
			return false;
		}
	}
	
	public static boolean receive(String address, ItemStack stack) {
		try {
			if(isLocal(getServerName(address))) {
				if(new EnderID(stack).isReceiveable()) {
					TileEntity entity = EnderNet.registry.getTileEntity(getServerEnderID(address));
					if(entity == null || !(entity instanceof TileEntityEnderReceiver)) return false;
					return ((TileEntityEnderReceiver)entity).receiveItem(new EnderID(stack));
				} else return false;
			} else {
				String serverName = getServerName(address);
				String serverAddress = EnderNet.servers.getAddress(serverName);
				if(serverAddress == null) return false;
				return HTTPClient.receive(serverAddress, getServerEnderID(address), new EnderID(stack));
			}
		} catch(Exception e) {
			return false;
		}
	}
	
	public static String getServerName(String server) {
		return server.split("\\.")[0];
	}
	
	public static int getServerEnderID(String server) {
		return new Integer(server.split("\\.")[1]).intValue();
	}
	
	public static boolean isLocal(String name) {
		return (name.equals("0") || name.equals("local") || name.equals("localhost") || name.equals(""));
	}
}
