package pl.asie.endernet.lib;

import java.util.ArrayList;

import pl.asie.endernet.block.TileEntityEnderTransmitter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class EnderRegistry {
	private ArrayList<EntityCoord> entities = new ArrayList<EntityCoord>();
	
	public int getEntityID(TileEntity entity) {
		EntityCoord ec = new EntityCoord(entity);
		if(!entities.contains(ec)) entities.add(ec);
		return entities.indexOf(ec);
	}
	
	public void removeEntity(TileEntity entity) {
		EntityCoord ec = new EntityCoord(entity);
		if(entities.contains(ec)) {
			entities.set(entities.indexOf(ec), null);
		}
	}
}
