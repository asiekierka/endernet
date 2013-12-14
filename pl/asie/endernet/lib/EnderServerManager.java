package pl.asie.endernet.lib;

import java.io.File;
import java.util.HashMap;

public class EnderServerManager {
	private HashMap<String, EnderServer> servers = new HashMap<String, EnderServer>();
	
	public EnderServer get(String name) {
		return servers.get(name);
	}
	
	public String getAddress(String name) {
		EnderServer server = get(name);
		if(server != null) return server.address;
		else return null;
	}
	
	public void loadFromJSON(File file) {
		
	}
	
	public void saveToJSON(File file) {
		
	}
}
