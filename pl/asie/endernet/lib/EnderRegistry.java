package pl.asie.endernet.lib;

import java.util.ArrayList;

import pl.asie.endernet.block.TileEntityEnderTransmitter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class EnderRegistry {
	private ArrayList<EntityCoord> entities = new ArrayList<EntityCoord>();
	
	public int getNewEntityID(TileEntity entity) {
		EntityCoord ec = new EntityCoord(entity);
		entities.add(ec);
		return entities.indexOf(ec);
	}
}
