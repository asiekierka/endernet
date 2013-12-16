package pl.asie.endernet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;

import pl.asie.endernet.block.BlockEnderReceiver;
import pl.asie.endernet.block.BlockEnderTransmitter;
import pl.asie.endernet.block.TileEntityEnder;
import pl.asie.endernet.block.TileEntityEnderReceiver;
import pl.asie.endernet.block.TileEntityEnderTransmitter;
import pl.asie.endernet.http.EnderHTTPServer;
import pl.asie.endernet.http.HTTPClient;
import pl.asie.endernet.http.URIHandlerCanReceive;
import pl.asie.endernet.http.URIHandlerPing;
import pl.asie.endernet.http.URIHandlerReceive;
import pl.asie.endernet.http.URIHandlerSendString;
import pl.asie.endernet.integration.WailaIntegration;
import pl.asie.endernet.lib.EnderID;
import pl.asie.endernet.lib.EnderRedirector;
import pl.asie.endernet.lib.EnderRegistry;
import pl.asie.endernet.lib.EnderServer;
import pl.asie.endernet.lib.EnderServerManager;
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
import mcp.mobius.waila.api.IWailaRegistrar;
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
import net.minecraftforge.common.Property;

@Mod(modid="endernet", name="EnderNet", version="0.0.12")
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
		return itemID > 256 && itemID < 32000;
	}
	
	public static BlockEnderTransmitter enderTransmitter;
	public static BlockEnderReceiver enderReceiver;
	public static EnderRegistry registry;
	public static EnderServerManager servers;
	
	public static boolean spawnParticles;
	public static boolean onlyAllowDefined;
	private static boolean treatBlacklistAsWhitelist;
	
	private static ArrayList<Integer> blacklistedItems;
	
	private File serverFile;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {		
		log = Logger.getLogger("endernet");
		log.setParent(FMLLog.getLogger());
		
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		if(System.getProperty("user.dir").indexOf(".asielauncher") >= 0)
			log.info("Thanks for using AsieLauncher!");
		
		if(isBlock("enderTransmitter", 2350)) {
			enderTransmitter = new BlockEnderTransmitter(config.getBlock("enderTransmitter", 2350).getInt());
			GameRegistry.registerBlock(enderTransmitter, "enderTransmitter");
		}
		if(isBlock("enderReceiver", 2351)) {
			enderReceiver = new BlockEnderReceiver(config.getBlock("enderReceiver", 2351).getInt());
			GameRegistry.registerBlock(enderReceiver, "enderReceiver");
		}
		
		GameRegistry.registerTileEntity(TileEntityEnderTransmitter.class, "enderTransmitter");
		GameRegistry.registerTileEntity(TileEntityEnderReceiver.class, "enderReceiver");
		MinecraftForge.EVENT_BUS.register(new pl.asie.endernet.EventHandler());
		
		spawnParticles = config.get("misc", "spawnTransmitterParticles", true).getBoolean(true);
		onlyAllowDefined = config.get("comm", "receiveFromDefinedOnly", true).getBoolean(true);
		serverFile = new File(event.getModConfigurationDirectory(), "endernet-servers.json");
		
		Property blacklistedItems = config.get("comm", "blacklistedItems", "");
		blacklistedItems.comment = "Comma-separated IDs of blocks and items that should be blacklisted. For example: 42,46";
		parseBlacklistedItems(blacklistedItems.getString());
		
		treatBlacklistAsWhitelist = config.get("comm", "useBlacklistAsWhitelist", false).getBoolean(false);
	}
	
	public static boolean isItemBlacklisted(int id) {
		boolean contained = blacklistedItems.contains(id);
		return treatBlacklistAsWhitelist ? !contained : contained;
	}
	
	private void parseBlacklistedItems(String s) {
		blacklistedItems = new ArrayList<Integer>();
		String[] items = s.split(",");
		for(String itemString: items) {
			try {
				int itemID = new Integer(itemString.trim()).intValue();
				if(itemID > 0 && itemID < 32000) {
					blacklistedItems.add(itemID);
				}
			} catch(NumberFormatException e) {
				if(itemString.contains("|")) {
					EnderID id = new EnderID(itemString.split("|")[0], itemString.split("|")[1]);
					if(id != null) {
						ItemStack stack = id.createItemStack();
						if(stack != null) {
							blacklistedItems.add(stack.getItem().itemID);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event)
	{
		if(config.get("misc", "enableDevCommands", true).getBoolean(true))
			event.registerServerCommand(new CommandEndernetInfo());
		event.registerServerCommand(new CommandEndernetReload());
	}
	 
	@EventHandler
	public void init(FMLInitializationEvent event) {
		FMLInterModComms.sendMessage("Waila", "register", "pl.asie.endernet.EnderNet.registerWaila");
		
		NetworkRegistry.instance().registerGuiHandler(this, new NetworkHandler());
		
		startServerManager();
		startHTTPServer();
		httpServer.registerHandler(new URIHandlerPing());
		httpServer.registerHandler(new URIHandlerCanReceive());
		httpServer.registerHandler(new URIHandlerReceive());
		httpServer.registerHandler(new URIHandlerSendString());
		
		LanguageRegistry.instance().addStringLocalization("tile.endernet.enderTransmitter.name", "Ender Transmitter");
		LanguageRegistry.instance().addStringLocalization("tile.endernet.enderReceiver.name", "Ender Receiver");
		LanguageRegistry.instance().addStringLocalization("command.endernet.info", "Gives you dev information on the currently held inventory item.");
		LanguageRegistry.instance().addStringLocalization("command.endernet.reload", "Reloads the endernet-servers.json file.");
		LanguageRegistry.instance().addStringLocalization("command.endernet.reload.success", "Successfully reloaded!");

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
	
	private void startServerManager() {
		servers = new EnderServerManager();
		reloadServerFile();
		
		ConfigCategory serverC = config.getCategory("servers");
        serverC.setComment("List servers in S:name=ip form, see endernet-servers.json for more detailed config");
        for(String name: serverC.keySet()) {
            	if(servers.get(name) != null) continue;
                EnderServer es = new EnderServer(name, serverC.get(name).getString());
                servers.add(es);
        }
        
        saveServerFile();
	}
	
	public void reloadServerFile() {
		if(serverFile.exists()) {
			servers.clear();
			servers.loadFromJSON(serverFile);
		} else {
			servers.saveToJSON(serverFile);
		}
	}
	
	public void saveServerFile() {
		servers.saveToJSON(serverFile);
	}
	
	private void startHTTPServer() {
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
	}
	
	public static void registerWaila(IWailaRegistrar reg) {
		reg.registerBodyProvider(new WailaIntegration(), TileEntityEnder.class);
	}
}
