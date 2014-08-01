package pl.asie.endernet.chat;

import java.util.Date;
import java.util.HashMap;

import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;
import pl.asie.endernet.EnderNet;
import pl.asie.endernet.block.TileEntityEnderChatBox;
import pl.asie.endernet.lib.EntityCoord;

public class ChatHandler {
	private HashMap<String, String> actions = new HashMap<String, String>();
	public boolean enableChatFeatures, enableShout, enableGreentext;
	public int HEARING_DISTANCE, SPEAKING_DISTANCE, CHAT_RADIUS;
	public String colorAction, messageFormat;
	
	public ChatHandler(Configuration config) {
		HEARING_DISTANCE = config.get("chat", "chatboxHearingDistance", 12).getInt();
		SPEAKING_DISTANCE = config.get("chat", "chatboxSpeakingDistance", 12).getInt();
		CHAT_RADIUS = config.get("chat", "playerRadius", 0).getInt();
		enableShout = config.get("chat", "enableShout", true).getBoolean(true);
		enableChatFeatures = config.get("chat", "enableChatTweaks", true).getBoolean(true);
		config.get("chat", "enableGreentext", false).comment = ">implying anyone will ever turn this on";
		enableGreentext = config.get("chat", "enableGreentext", false).getBoolean(false);
		colorAction = config.get("chat", "colorAction", "5").getString();
		messageFormat = config.get("chat", "messageFormat", "<%u> %m").getString();
		
		ConfigCategory chatActions = config.getCategory("chatactions");
		chatActions.setComment("Define chat actions here. S:commandName=action. Only works when enableChatFeatures is true. If empty, will add in the default ones.");
		if(chatActions.isEmpty()) {
			config.get("chatactions", "hug", "hugs PLAYER!");
			config.get("chatactions", "slap", "slaps PLAYER around a bit with a large trout");
		}
		for(String key: chatActions.keySet()) {
			actions.put(key, chatActions.get(key).getString());
		}
	}
	
	public void registerCommands(FMLServerStartingEvent event) {
		if(enableChatFeatures) {
			event.registerServerCommand(new CommandMe());
			for(String key: actions.keySet()) {
				event.registerServerCommand(new CommandAction(key, actions.get(key)));
			}
		}
	}
	
	private static String pad(int t) {
		if(t < 10) return "0"+t;
		else return ""+t;
	}
	
	@ForgeSubscribe
	public void chatEvent(ServerChatEvent event) {
		if(CHAT_RADIUS < 0 && enableChatFeatures) { // Chat disabled altogether
			event.setCanceled(true);
			return;
		}
		
		ChatMessageComponent chat = new ChatMessageComponent();
		boolean disableRadius = false;
		String username = event.username;
		String message = event.message;
		int dim = event.player.worldObj.provider.dimensionId;
		
		if(enableChatFeatures) {
			if(event.message.startsWith("!")) message = message.substring(1);
			
			if(enableGreentext && message.startsWith(">")) {
				message = EnumChatFormatting.GREEN + message;
			}
			
			Date now = new Date();
			String formattedMessage = EnumChatFormatting.RESET + messageFormat;
			try {
				formattedMessage = formattedMessage.replaceAll("%u", username)
					.replaceAll("%m", message)
					.replaceAll("%w", event.player.worldObj.provider.getDimensionName())
					.replaceAll("%H", pad(now.getHours()))
					.replaceAll("%M", pad(now.getMinutes()))
					.replaceAll("%S", pad(now.getSeconds()));
			} catch(Exception e) { e.printStackTrace(); formattedMessage = EnumChatFormatting.RESET + "<" + username + "" + EnumChatFormatting.RESET + "> " + message; }
			try {
				formattedMessage = formattedMessage.replaceAll("&", "\u00a7");
			} catch(Exception e) { e.printStackTrace(); }
			if(event.message.startsWith("!") && enableShout) {
				chat.addText(EnumChatFormatting.YELLOW + "[Shout] " + formattedMessage);
				disableRadius = true;
			} else {
				chat.addText(formattedMessage);
			}
		}
		
		if(HEARING_DISTANCE > 0) {
			for(EntityCoord ec: EnderNet.registry.entities) {
				if(ec == null || dim != ec.dimensionID) continue;
				int distance = (int)Math.round(event.player.getDistance(ec.x, ec.y, ec.z));
				if(distance > HEARING_DISTANCE) continue;
				TileEntity te = event.player.worldObj.getBlockTileEntity(ec.x, ec.y, ec.z);
				if(!(te instanceof TileEntityEnderChatBox)) continue;
				((TileEntityEnderChatBox)te).receiveChatMessage(username, message);
			}
		}
		
		if(CHAT_RADIUS > 0 && !disableRadius) {
			event.setCanceled(true); // Override regular sending
			if(MinecraftServer.getServer() == null) return;
			for(WorldServer ws: MinecraftServer.getServer().worldServers) {
				if(ws.provider.dimensionId != dim) continue;
				for(Object o: ws.playerEntities) {
					if(o instanceof EntityPlayer) {
						EntityPlayer target = (EntityPlayer)o;
						if(event.player == target || event.player.getDistanceToEntity(target) <= CHAT_RADIUS) {
							target.sendChatToPlayer(chat);
						}
					}
				}
			}
		} else if(enableChatFeatures) {
			event.component = chat;
		}
	}
}
