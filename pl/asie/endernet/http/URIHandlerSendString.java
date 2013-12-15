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
	public boolean actuallyServe(IHTTPSession session) {
		Map<String, String> params = session.getParms();
		if(!params.containsKey("string")) {
			EnderNet.log.info("/sendString did not contain string!");
			return false;
		}
		if(!params.containsKey("target")) {
			EnderNet.log.info("/sendString did not contain target ID!");
			return false;
		}
		int target = new Integer(params.get("target")).intValue();
		TileEntity entity = EnderNet.registry.getTileEntity(target);
		if(entity == null || !(entity instanceof IEnderStringReceiver)) return false;
		IEnderStringReceiver receiver = (IEnderStringReceiver)entity;
		EnderServer server = EnderNet.servers.findByAddress(session.getHeaders().get("remote-addr"));
		return receiver.receiveString(server, params.get("string"));
	}
	
	@Override
	public Response serve(IHTTPSession session) {
		try {
			session.parseBody(null);
		} catch(Exception e) { e.printStackTrace(); }
		if(actuallyServe(session)) return new Response(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "EEYUP");
		else return new Response(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "NNOPE");
	}

	@Override
	public String getPermissionName() {
		return "message";
	}
	
	@Override
	public String getURI() {
		return "/sendString";
	}
}
