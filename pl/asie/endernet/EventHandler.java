package pl.asie.endernet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.gson.Gson;

import pl.asie.endernet.lib.EnderRegistry;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;

public class EventHandler {
	@ForgeSubscribe
	public void worldLoadEvent(WorldEvent.Load event) {
		File registryLocation = new File(DimensionManager.getCurrentSaveRootDirectory(), "enderRegistry.json");
		if(registryLocation.exists()) {
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(registryLocation), "UTF-8")
						);
				Gson gson = new Gson();
				EnderNet.registry = gson.fromJson(reader.readLine(), EnderRegistry.class);
				reader.close();
			} catch(Exception e) {
				e.printStackTrace();
				EnderNet.registry = new EnderRegistry();
			}
		} else EnderNet.registry = new EnderRegistry();
	}
}
