package pl.asie.endernet;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;

import pl.asie.endernet.block.BlockEnderReceiver;
import pl.asie.endernet.block.BlockEnderTransmitter;
import pl.asie.endernet.block.TileEntityEnderTransmitter;
import pl.asie.endernet.http.EnderHTTPServer;
import pl.asie.endernet.http.HTTPClient;
import pl.asie.endernet.http.URIHandlerCanReceive;
import pl.asie.endernet.http.URIHandlerPing;
import pl.asie.endernet.http.URIHandlerReceive;
import pl.asie.endernet.lib.EnderID;
import pl.asie.endernet.lib.EnderRedirector;
import pl.asie.endernet.lib.EnderRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid="endernet", name="EnderNet", version="0.0.3")
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
	
	public static BlockEnderTransmitter enderTransmitter;
	public static BlockEnderReceiver enderReceiver;
	public static EnderRegistry registry;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {		
		log = Logger.getLogger("endernet");
		log.setParent(FMLLog.getLogger());
		
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		if(System.getProperty("user.dir").indexOf(".asielauncher") >= 0) {
			log.info("Hey, you! Yes, you! Thanks for using AsieLauncher! ~asie");
		}
		
		if(isBlock("enderTransmitter", 2350)) {
			enderTransmitter = new BlockEnderTransmitter(config.getBlock("enderTransmitter", 2350).getInt());
			GameRegistry.registerBlock(enderTransmitter, "enderTransmitter");
		}
		if(isBlock("enderReceiver", 2351)) {
			enderReceiver = new BlockEnderReceiver(config.getBlock("enderReceiver", 2351).getInt());
			GameRegistry.registerBlock(enderReceiver, "enderReceiver");
		}
		
		GameRegistry.registerTileEntity(TileEntityEnderTransmitter.class, "enderTransmitter");
		MinecraftForge.EVENT_BUS.register(new pl.asie.endernet.EventHandler());
	}
	
	@EventHandler
	public void serverLoad(FMLServerStartingEvent event)
	{
		if(config.get("misc", "enableDevCommands", true).getBoolean(true))
			event.registerServerCommand(new CommandEndernetInfo());
	}
	 
	@EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.instance().registerGuiHandler(this, new NetworkHandler());
		
		ConfigCategory servers = config.getCategory("servers");
		servers.setComment("List servers in S:name=ip form");
		for(String name: servers.keySet()) {
			EnderRedirector.serverIPs.put(name, servers.get(name).getString());
		}
		
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
		httpServer.registerHandler("/canReceive", new URIHandlerCanReceive());
		httpServer.registerHandler("/receive", new URIHandlerReceive());
		
		LanguageRegistry.instance().addStringLocalization("tile.endernet.enderTransmitter.name", "Ender Transmitter");
		LanguageRegistry.instance().addStringLocalization("tile.endernet.enderReceiver.name", "Ender Receiver");
		LanguageRegistry.instance().addStringLocalization("command.endernet.info", "Gives you dev information on the currently held inventory item.");
		// End
		config.save();
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
	
	@EventHandler
	public void receiveMessages(IMCEvent event) {
		ImmutableList<IMCMessage> messages = event.getMessages();
		for(IMCMessage msg : messages) {
			if(msg.key.equals("WhitelistItemNBT")) {
				EnderID.whitelistedNBTItems.add(EnderID.getItemIdentifierFor(msg.getItemStackValue()));
			}
			if(msg.key.equals("BlacklistItem")) {
				EnderID.blacklistedItems.add(EnderID.getItemIdentifierFor(msg.getItemStackValue()));
			}
		}
	}
}
