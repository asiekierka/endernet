package pl.asie.endernet.integration;

import java.util.List;

import pl.asie.endernet.block.TileEntityEnder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;

public class WailaIntegration implements IWailaDataProvider {

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		return null;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack,
			List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		return currenttip;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack,
			List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		NBTTagCompound data = accessor.getNBTData();
		currenttip.add("" + EnumChatFormatting.RESET + EnumChatFormatting.ITALIC + "ID: " + data.getInteger("eid"));
		return currenttip;
	}

}
