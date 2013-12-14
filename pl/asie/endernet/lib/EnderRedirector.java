package pl.asie.endernet.lib;

import java.util.HashMap;

import pl.asie.endernet.http.HTTPClient;
import net.minecraft.item.ItemStack;

public class EnderRedirector {
	public static HashMap<String, String> serverIPs = new HashMap<String, String>();
	
	public static boolean canReceive(String address, ItemStack stack) {
		try {
			if(isLocal(getServerName(address))) {
				return new EnderID(stack).isReceiveable();
			} else {
				String serverName = getServerName(address);
				if(!serverIPs.containsKey(serverName)) return false;
				return HTTPClient.canReceive(serverIPs.get(serverName), new EnderID(stack));
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
