package pl.asie.endernet;

import java.io.IOException;
import java.util.logging.Logger;

import pl.asie.endernet.http.EnderHTTPServer;
import pl.asie.endernet.http.URIHandlerPing;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.Configuration;

@Mod(modid="endernet", name="EnderNet", version="0.0.1")
@NetworkMod(channels={"EnderNet"}, clientSideRequired=true, packetHandler=NetworkHandler.class)
public class EnderNet {
	public Configuration config;
	public static Logger log;
	
	@Instance(value="endernet")
	public static EnderNet instance;
	
	public static EnderHTTPServer httpServer;
	
	public boolean isBlock(String name, int defaultID) {
		int blockID = config.getBlock(name, defaultID).getInt(); 
		return blockID > 0 && blockID < 4096;
	}
	
	public boolean isItem(String name, int defaultID) {
		int itemID = config.getItem(name, defaultID).getInt(); 
		return itemID > 0 && itemID < 65536;
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		log = Logger.getLogger("endernet");
		log.setParent(FMLLog.getLogger());
		
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		if(System.getProperty("user.dir").indexOf(".asielauncher") >= 0) {
			log.info("Hey, you! Yes, you! Thanks for using AsieLauncher! ~asie");
		}
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		httpServer = new EnderHTTPServer(config.get("comm", "httpServerPort", 21500).getInt());
		try {
			if(config.get("comm", "httpServerEnabled", true).getBoolean(true)) httpServer.start();
		} catch(IOException e) {
			e.printStackTrace();
		}
		if(httpServer.wasStarted()) {
			log.info("HTTP server ready!");
		} else {
			log.warning("HTTP server not initialized; EnderNet will transmit *ONLY*!");
		}
		httpServer.registerHandler("/ping", new URIHandlerPing());
		
		// End
		config.save();
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}