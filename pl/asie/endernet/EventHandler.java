package pl.asie.endernet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.gson.Gson;

import pl.asie.endernet.block.TileEntityEnder;
import pl.asie.endernet.block.TileEntityEnderChatBox;
import pl.asie.endernet.lib.EnderRegistry;
import pl.asie.endernet.lib.EntityCoord;
import pl.asie.endernet.lib.FileUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

public class EventHandler {
	@ForgeSubscribe
	public void worldLoadEvent(WorldEvent.Load event) {
		File registryLocation = new File(DimensionManager.getCurrentSaveRootDirectory(), "enderRegistry.json");
		if(registryLocation.exists()) {
			String jsonString = FileUtils.load(registryLocation);
			if(jsonString != null) {
				Gson gson = new Gson();
				EnderNet.registry = gson.fromJson(jsonString, EnderRegistry.class);
			}
			else EnderNet.registry = new EnderRegistry();
		} else EnderNet.registry = new EnderRegistry();
		for(Object o: event.world.loadedTileEntityList) {
			if(o instanceof TileEntityEnder) {
				TileEntityEnder e = (TileEntityEnder)o;
				System.out.println("Marking block for update");
				event.world.markBlockForUpdate(e.xCoord, e.yCoord, e.zCoord);
			}
		}
	}
	
	@ForgeSubscribe
	public void chunkLoadEvent(ChunkEvent.Load event) {
		for(Object o: event.getChunk().chunkTileEntityMap.values()) {
			if(o instanceof TileEntityEnder) {
				TileEntityEnder e = (TileEntityEnder)o;
				System.out.println("Marking block for update");
				event.world.markBlockForUpdate(e.xCoord, e.yCoord, e.zCoord);
			}
		}
	}
	
	@ForgeSubscribe
	public void chatEvent(ServerChatEvent event) {
		if(EnderNet.CHAT_RADIUS < 0) { // Chat disabled altogether
			event.setCanceled(true);
			return;
		}
		if(EnderNet.HEARING_DISTANCE == 0) return;
		int dim = event.player.worldObj.provider.dimensionId;
		for(EntityCoord ec: EnderNet.registry.entities) {
			if(dim != ec.dimensionID) continue;
			int distance = (int)Math.round(event.player.getDistance(ec.x, ec.y, ec.z));
			if(distance > EnderNet.HEARING_DISTANCE) continue;
			TileEntity te = event.player.worldObj.getBlockTileEntity(ec.x, ec.y, ec.z);
			if(!(te instanceof TileEntityEnderChatBox)) continue;
			((TileEntityEnderChatBox)te).receiveChatMessage(event.username, event.message);
		}
		if(EnderNet.CHAT_RADIUS > 0) { // Chat radius
			event.setCanceled(true);
			ChatMessageComponent chat = new ChatMessageComponent();
			if(event.message.startsWith("!") && EnderNet.shoutEnabled) {
				chat.addText(EnumChatFormatting.YELLOW + "[Shout] " + EnumChatFormatting.RESET + "<" + event.username +
						"> " + event.message.substring(1));
				for(WorldServer ws: MinecraftServer.getServer().worldServers) {
					for(Object o: ws.playerEntities) {
						if(o instanceof EntityPlayer)
							((EntityPlayer)o).sendChatToPlayer(chat);
					}
				}
			} else {
				chat.addText("<" + event.username + "> " + event.message);
				for(WorldServer ws: MinecraftServer.getServer().worldServers) {
					if(ws.provider.dimensionId != dim) continue;
					for(Object o: ws.playerEntities) {
						if(o instanceof EntityPlayer) {
							EntityPlayer target = (EntityPlayer)o;
							if(event.player == target || event.player.getDistanceToEntity(target) <= EnderNet.CHAT_RADIUS) {
								target.sendChatToPlayer(chat);
							}
						}
					}
				}
			}
		}
	}
}
