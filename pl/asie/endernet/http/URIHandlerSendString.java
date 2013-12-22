package pl.asie.endernet.http;

import java.util.Map;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import pl.asie.endernet.EnderNet;
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

public class URIHandlerSendString implements IURIHandler {
	public Object serve(Map<String, String> params) {
		int target = new Integer(params.get("target")).intValue();
		TileEntity entity = EnderNet.registry.getTileEntity(target);
		if(entity == null || !(entity instanceof IEnderStringReceiver)) return false;
		IEnderStringReceiver receiver = (IEnderStringReceiver)entity;
		EnderServer server = EnderNet.servers.get(params.get("remoteServer"));
		return new HTTPResponse(receiver.receiveString(server, params.get("string")));
	}
	
	@Override
	public String getPermissionName() {
		return "message";
	}
	
	@Override
	public String getURI() {
		return "/sendString";
	}
	
	@Override
	public String[] getRequiredParams() {
		return new String[]{"string", "target"};
	}
	
	@Override
	public Class getOutputType() {
		return HTTPResponse.class;
	}
}
