package pl.asie.endernet.chat;

import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;
import pl.asie.endernet.EnderNet;
import pl.asie.endernet.block.TileEntityEnderChatBox;
import pl.asie.endernet.lib.EntityCoord;

public class ChatHandler {
	public boolean enableChatFeatures, enableShout, enableGreentext;
	public int HEARING_DISTANCE, SPEAKING_DISTANCE, CHAT_RADIUS;
	public String colorAction;
	
	public ChatHandler(Configuration config) {
		HEARING_DISTANCE = config.get("chat", "chatboxHearingDistance", 12).getInt();
		SPEAKING_DISTANCE = config.get("chat", "chatboxSpeakingDistance", 12).getInt();
		CHAT_RADIUS = config.get("chat", "playerRadius", 0).getInt();
		enableShout = config.get("chat", "enableShout", true).getBoolean(true);
		enableChatFeatures = config.get("chat", "enableChatTweaks", true).getBoolean(true);
		config.get("chat", "enableGreentext", false).comment = ">implying anyone will ever turn this on";
		enableGreentext = config.get("chat", "enableGreentext", false).getBoolean(false);
		colorAction = config.get("chat", "colorAction", "5").getString();
	}
	
	public void registerCommands(FMLServerStartingEvent event) {
		if(enableChatFeatures) {
			event.registerServerCommand(new CommandMe());
			event.registerServerCommand(new CommandAction("hug", "hugs PLAYER!"));
			event.registerServerCommand(new CommandAction("slap", "slaps PLAYER around a bit with a large trout"));
		}
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
			
			if(event.message.startsWith("!") && enableShout) {
				chat.addText(EnumChatFormatting.YELLOW + "[Shout] " + EnumChatFormatting.RESET + "<" + username + "> " + message);
				disableRadius = true;
			} else {
				chat.addText("<" + username + "> " + message);
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
