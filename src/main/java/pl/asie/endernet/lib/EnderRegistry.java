package pl.asie.endernet.lib;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.google.gson.Gson;

import pl.asie.endernet.EnderNet;
import pl.asie.endernet.block.TileEntityEnder;
import pl.asie.endernet.block.TileEntityEnderTransmitter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class EnderRegistry {
	public ArrayList<EntityCoord> entities = new ArrayList<EntityCoord>();
	
	public EntityCoord getEntityCoord(int id) {
		return entities.size() > id ? entities.get(id) : null;
	}
	
	public TileEntity getTileEntity(int id) {
		EntityCoord location = getEntityCoord(id);
		return (location != null ? location.get() : null);
	}
	
	public int getEntityID(TileEntity entity) {
		EntityCoord ec = new EntityCoord(entity);
		if(!entities.contains(ec)) {
			entities.add(ec);
			saveJSON();
		}
		return entities.indexOf(ec);
	}
	
	public void saveJSON() {
		EnderNet.log.info("Saving JSON");
		Gson gson = new Gson();
		String output = gson.toJson(this);
		File location = new File(DimensionManager.getCurrentSaveRootDirectory(), "enderRegistry.json");
		FileUtils.save(output, location);
	}
	
	public void removeEntity(TileEntity entity) {
		EntityCoord ec = new EntityCoord(entity);
		if(entities.contains(ec)) {
			EnderNet.log.info("Removing entity!");
			entities.set(entities.indexOf(ec), null);
		}
	}

	public int forceEntityID(TileEntityEnder entity, int eid) {
		try {
			if(entities.get(eid) != null) {
				EntityCoord ec = entities.get(eid);
				EnderNet.log.warn("Entity already found for ID " + eid + "! Location: " + entities.get(eid).toString());
				return -1;
			}
			entities.set(eid, new EntityCoord(entity));
			saveJSON();
			return eid;
		} catch(Exception e) { e.printStackTrace(); return -1; }
	}
}
