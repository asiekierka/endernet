package pl.asie.endernet.http;

import java.util.Map;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import pl.asie.endernet.EnderNet;
import pl.asie.endernet.api.IEnderRedstone;
import pl.asie.endernet.api.IEnderStringReceiver;
import pl.asie.endernet.api.IURIHandler;
import pl.asie.endernet.block.TileEntityEnderReceiver;
import pl.asie.endernet.lib.EnderID;
import pl.asie.endernet.lib.EnderRedirector;
import pl.asie.endernet.lib.EnderServer;
import pl.asie.endernet.lib.EntityCoord;

import com.google.gson.Gson;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;

public class URIHandlerSendRedstone implements IURIHandler {
	public Object serve(Map<String, String> params) {
		int target = new Integer(params.get("target")).intValue();
		TileEntity entity = EnderNet.registry.getTileEntity(target);
		if(entity == null || !(entity instanceof IEnderRedstone)) return false;
		IEnderRedstone receiver = (IEnderRedstone)entity;
		return new HTTPResponse(receiver.setRedstone(new Integer(params.get("value")).intValue()));
	}
	
	@Override
	public String getPermissionName() {
		return "redstone";
	}
	
	@Override
	public String getURI() {
		return "/sendRedstone";
	}
	
	@Override
	public String[] getRequiredParams() {
		return new String[]{"value", "target"};
	}
	
	@Override
	public Class getOutputType() {
		return HTTPResponse.class;
	}
}
