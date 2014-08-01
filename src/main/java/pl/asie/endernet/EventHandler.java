package pl.asie.endernet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.gson.Gson;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import pl.asie.endernet.block.TileEntityEnder;
import pl.asie.endernet.lib.EnderRegistry;
import pl.asie.endernet.lib.EntityCoord;
import pl.asie.endernet.lib.FileUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

public class EventHandler {
	@SubscribeEvent
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
	
	@SubscribeEvent
	public void chunkLoadEvent(ChunkEvent.Load event) {
		for(Object o: event.getChunk().chunkTileEntityMap.values()) {
			if(o instanceof TileEntityEnder) {
				TileEntityEnder e = (TileEntityEnder)o;
				System.out.println("Marking block for update");
				event.world.markBlockForUpdate(e.xCoord, e.yCoord, e.zCoord);
			}
		}
	}
}
