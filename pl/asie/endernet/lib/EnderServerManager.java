package pl.asie.endernet.lib;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class EnderServerManager {
	private HashMap<String, EnderServer> servers = new HashMap<String, EnderServer>();
	private HashMap<EnderServer, File> saveLocation = new HashMap<EnderServer, File>();
	
	public EnderServer get(String name) {
		return servers.get(name);
	}
	
	public String getAddress(String name) {
		EnderServer server = get(name);
		if(server != null) return server.address;
		else return null;
	}
	
	public void loadFromJSON(File file) {
		Gson gson = new Gson();
		String data = FileUtils.load(file);
		if(data == null) return; // No data
		
		Type arrayType = new TypeToken<ArrayList<EnderServer>>(){}.getType();
		ArrayList<EnderServer> serverList = gson.fromJson(data, arrayType);
		for(EnderServer es: serverList) {
			servers.put(es.name, es);
			saveLocation.put(es, file);
		}
	}
	
	public void saveToJSON(File file) {
		
	}
}
