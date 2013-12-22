package pl.asie.endernet.lib;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import pl.asie.endernet.EnderNet;
import pl.asie.endernet.api.IEnderStringReceiver;
import pl.asie.endernet.block.TileEntityEnderReceiver;
import pl.asie.endernet.http.HTTPClient;
import pl.asie.endernet.http.HTTPResponse;
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
			e.printStackTrace();
			return false;
		}
	}
	
	public static HTTPResponse receive(String address, ItemStack stack) {
		try {
			if(isLocal(getServerName(address))) {
				if(new EnderID(stack).isReceiveable()) {
					TileEntity entity = EnderNet.registry.getTileEntity(getServerEnderID(address));
					if(entity == null || !(entity instanceof TileEntityEnderReceiver)) return new HTTPResponse(false);
					int amountSent = ((TileEntityEnderReceiver)entity).receiveItem(new EnderID(stack));
					return new HTTPResponse(amountSent > 0, amountSent);
				} else return new HTTPResponse(false);
			} else {
				String serverName = getServerName(address);
				String serverAddress = EnderNet.servers.getAddress(serverName);
				if(serverAddress == null) return new HTTPResponse(false);
				return HTTPClient.receive(serverAddress, getServerEnderID(address), new EnderID(stack));
			}
		} catch(Exception e) {
			return new HTTPResponse(false);
		}
	}
	
	public static String getServerName(String server) {
		String[] list = server.split("\\.");
		list = ArrayUtils.remove(list, list.length - 1);
		EnderNet.log.info("Server name ["+server+"] -> " + StringUtils.join(list, "."));
		return StringUtils.join(list, ".");
	}
	
	public static int getServerEnderID(String server) {
		String[] id = server.split("\\.");
		return new Integer(id[id.length - 1]).intValue();
	}
	
	public static boolean isLocal(String name) {
		return (name.equals("local") || name.equals(""));
	}
}
