package pl.asie.endernet.http;

import java.util.Map;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import pl.asie.endernet.EnderNet;
import pl.asie.endernet.api.IURIHandler;
import pl.asie.endernet.block.TileEntityEnderReceiver;
import pl.asie.endernet.lib.EnderID;
import pl.asie.endernet.lib.EnderRedirector;
import pl.asie.endernet.lib.EntityCoord;

import com.google.gson.Gson;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;

public class URIHandlerReceive implements IURIHandler {
	@Override
	public Object serve(Map<String, String> params) {
		String fail = new HTTPResponse(false).toJson();
		Gson gson = new Gson();
		EnderID block = gson.fromJson(params.get("object"), EnderID.class);
		if(!block.isReceiveable()) return fail;
		int target = new Integer(params.get("target")).intValue();
		TileEntity entity = EnderNet.registry.getTileEntity(target);
		if(entity == null || !(entity instanceof TileEntityEnderReceiver)) return fail;
		TileEntityEnderReceiver receiver = (TileEntityEnderReceiver)entity;
		int amountSent = receiver.receiveItem(block);
		return new HTTPResponse(amountSent > 0, amountSent).toJson();
	}
	
	@Override
	public String getPermissionName() {
		return "item";
	}
	
	@Override
	public String getURI() {
		return "/receive";
	}
	
	@Override
	public String[] getRequiredParams() {
		return new String[]{"object", "target"};
	}
}
