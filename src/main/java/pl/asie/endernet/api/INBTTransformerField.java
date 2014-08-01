package pl.asie.endernet.api;

import net.minecraft.nbt.NBTTagCompound;

public interface INBTTransformerField {
	public String[] getTransformedFieldNames();
	public boolean toEnder(NBTTagCompound compound);
	public boolean fromEnder(NBTTagCompound compound);
}
