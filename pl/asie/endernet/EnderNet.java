package pl.asie.endernet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;

import pl.asie.endernet.api.IURIHandler;
import pl.asie.endernet.block.BlockEnderModem;
import pl.asie.endernet.block.BlockEnderReceiver;
import pl.asie.endernet.block.BlockEnderTransmitter;
import pl.asie.endernet.block.TileEntityEnder;
import pl.asie.endernet.block.TileEntityEnderModem;
import pl.asie.endernet.block.TileEntityEnderReceiver;
import pl.asie.endernet.block.TileEntityEnderTransmitter;
import pl.asie.endernet.http.EnderHTTPServer;
import pl.asie.endernet.http.HTTPClient;
import pl.asie.endernet.http.URIHandlerCanReceive;
import pl.asie.endernet.http.URIHandlerPing;
import pl.asie.endernet.http.URIHandlerReceive;
import pl.asie.endernet.http.URIHandlerSendRedstone;
import pl.asie.endernet.http.URIHandlerSendString;
import pl.asie.endernet.integration.WailaIntegration;
import pl.asie.endernet.lib.EnderID;
import pl.asie.endernet.lib.EnderRedirector;
import pl.asie.endernet.lib.EnderRegistry;
import pl.asie.endernet.lib.EnderServer;
import pl.asie.endernet.lib.EnderServerManager;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
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

@Mod(modid="endernet", name="EnderNet", version="0.1.2")
@NetworkMod(channels={"EnderNet"}, clientSideRequired=true, packetHandler=NetworkHandler.class)
public class EnderNet {
	// Dev environment parameter. Remember to remove for release!
	public static boolean DEV = false;
	
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
	public static BlockEnderModem enderModem;
	public static EnderRegistry registry;
	public static EnderServerManager servers;
	
	public static boolean spawnParticles;
	public static boolean onlyAllowDefinedReceive, onlyAllowDefinedTransmit;
	private static boolean treatBlacklistAsWhitelist;
	
	private static ArrayList<Integer> blacklistedItems;
	private static ArrayList<Integer> whitelistedDimensions;
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
		if(Loader.isModLoaded("ComputerCraft") && isBlock("enderModem", 2352)) {
			enderModem = new BlockEnderModem(config.getBlock("enderModem", 2352).getInt());
			GameRegistry.registerBlock(enderModem, "enderModem");
		}
		
		GameRegistry.registerTileEntity(TileEntityEnderTransmitter.class, "enderTransmitter");
		GameRegistry.registerTileEntity(TileEntityEnderReceiver.class, "enderReceiver");
		GameRegistry.registerTileEntity(TileEntityEnderModem.class, "enderModem");
		MinecraftForge.EVENT_BUS.register(new pl.asie.endernet.EventHandler());
		
		spawnParticles = config.get("misc", "spawnTransmitterParticles", true).getBoolean(true);
		onlyAllowDefinedReceive = config.get("comm", "receiveFromDefinedOnly", true).getBoolean(true);
		onlyAllowDefinedTransmit = config.get("comm", "transmitToDefinedOnly", true).getBoolean(true);
		serverFile = new File(event.getModConfigurationDirectory(), "endernet-servers.json");
		
		if(DEV) onlyAllowDefinedTransmit = false; // Testing localhost sending
		
		Property blacklistedItems = config.get("comm", "blacklistedItems", "");
		blacklistedItems.comment = "Comma-separated IDs of blocks and items that should be blacklisted. For example: 42,46";
		parseBlacklistedItems(blacklistedItems.getString());
		
		Property whitelistedDimensions = config.get("comm", "whitelistedDimensions", "");
		whitelistedDimensions.comment = "Comma-separated IDs of whitelisted dimensions. If empty, all dimensions are whitelisted.";
		parseWhitelistedDimensions(whitelistedDimensions.getString());
		
		treatBlacklistAsWhitelist = config.get("comm", "blacklistedItemsAsWhiteList", false).getBoolean(false);
	}
	
	public static boolean isItemBlacklisted(int id) {
		boolean contained = blacklistedItems.contains(id);
		return treatBlacklistAsWhitelist ? !contained : contained;
	}
	
	public static boolean isDimensionBlacklisted(int id) {
		if(whitelistedDimensions.size() == 0) return false;
		else return !whitelistedDimensions.contains(id);
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

	private void parseWhitelistedDimensions(String s) {
		whitelistedDimensions = new ArrayList<Integer>();
		String[] dims = s.split(",");
		for(String dim: dims) {
			try {
				whitelistedDimensions.add(new Integer(dim.trim()).intValue());
			} catch(NumberFormatException e) {
				
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
		httpServer.registerHandler(new URIHandlerSendRedstone());
		
		LanguageRegistry.instance().addStringLocalization("tile.endernet.enderTransmitter.name", "Ender Transmitter");
		LanguageRegistry.instance().addStringLocalization("tile.endernet.enderReceiver.name", "Ender Receiver");
		LanguageRegistry.instance().addStringLocalization("tile.endernet.enderModem.name", "Ender Modem");
		LanguageRegistry.instance().addStringLocalization("command.endernet.info", "Gives you dev information on the currently held inventory item.");
		LanguageRegistry.instance().addStringLocalization("command.endernet.reload", "Reloads the endernet-servers.json file.");
		LanguageRegistry.instance().addStringLocalization("command.endernet.reload.success", "Successfully reloaded!");
		LanguageRegistry.instance().addStringLocalization("command.endernet.reload.failure", "Failed to reload!");
		
		if(!config.get("misc", "disableCraftingRecipes", false).getBoolean(false)) {
			GameRegistry.addRecipe(new ItemStack(enderTransmitter, 1), "odo", "ded", "odo", 'd', new ItemStack(Item.diamond, 1), 'e', new ItemStack(Item.enderPearl, 1), 'o', new ItemStack(Item.redstone, 1));
			GameRegistry.addRecipe(new ItemStack(enderTransmitter, 1), "dod", "oeo", "dod", 'd', new ItemStack(Item.diamond, 1), 'e', new ItemStack(Item.enderPearl, 1), 'o', new ItemStack(Item.redstone, 1));
			GameRegistry.addRecipe(new ItemStack(enderReceiver, 1), "odo", "ded", "odo", 'd', new ItemStack(Item.diamond, 1), 'e', new ItemStack(Item.enderPearl, 1), 'o', new ItemStack(Item.dyePowder, 1, 4));
			GameRegistry.addRecipe(new ItemStack(enderReceiver, 1), "dod", "oeo", "dod", 'd', new ItemStack(Item.diamond, 1), 'e', new ItemStack(Item.enderPearl, 1), 'o', new ItemStack(Item.dyePowder, 1, 4));
			if(enderModem != null) {
				GameRegistry.addRecipe(new ItemStack(enderModem, 1), "gdg", "geg", "ggg", 'd', new ItemStack(Item.diamond, 1), 'e', new ItemStack(Item.enderPearl, 1), 'g', new ItemStack(Item.dyePowder, 1, 2));
			}
		}
		
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
			if(msg.key.equals("RegisterURIHandler")) {
				try {
					Class handlerClass = this.getClass().getClassLoader().loadClass(msg.getStringValue());
					httpServer.registerHandler((IURIHandler)handlerClass.newInstance());
				} catch(Exception e) {
					e.printStackTrace();
					log.severe("Could not load handler " + msg.getStringValue() + "!");
				}
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
	
	public boolean reloadServerFile() {
		if(serverFile.exists()) {
			if(!servers.loadFromJSON(serverFile, true)) return false;
			servers.clear();
			return servers.loadFromJSON(serverFile);
		} else {
			servers.saveToJSON(serverFile);
			return true;
		}
	}
	
	public void saveServerFile() {
		servers.saveToJSON(serverFile);
	}
	
	private Random rand = new Random();
	
	private void startHTTPServer() {
		int port = DEV ? rand.nextInt(20000)+10000 : config.get("comm", "httpServerPort", 21500).getInt();
		httpServer = new EnderHTTPServer(port);
		try {
			if(config.get("comm", "httpServerEnabled", true).getBoolean(true)) httpServer.start();
		} catch(IOException e) {
			e.printStackTrace();
		}
		if(httpServer.wasStarted()) {
			log.info("HTTP server ready on port " + port + "!");
		} else {
			log.warning("HTTP server not initialized; EnderNet will transmit *ONLY*!");
		}
	}
	
	public static void registerWaila(IWailaRegistrar reg) {
		reg.registerBodyProvider(new WailaIntegration(), TileEntityEnder.class);
	}
}
