package pl.asie.endernet.http;

import java.util.Map;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import pl.asie.endernet.EnderNet;
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

	public boolean actuallyServe(IHTTPSession session) {
		Map<String, String> params = session.getParms();
		if(!params.containsKey("object")) {
			EnderNet.log.info("/receive did not contain object!");
			return false;
		}
		if(!params.containsKey("target")) {
			EnderNet.log.info("/receive did not contain target ID!");
			return false;
		}
		Gson gson = new Gson();
		EnderID block = gson.fromJson(params.get("object"), EnderID.class);
		if(!block.isReceiveable()) return false;
		int target = new Integer(params.get("target")).intValue();
		EntityCoord location = EnderNet.registry.getEntityCoord(target);
		if(location == null) return false;
		TileEntity entity = location.get();
		if(entity == null || !(entity instanceof TileEntityEnderReceiver)) return false;
		TileEntityEnderReceiver receiver = (TileEntityEnderReceiver)entity;
		return receiver.receiveItem(block);
	}
	
	@Override
	public Response serve(IHTTPSession session) {
		try {
			session.parseBody(null);
		} catch(Exception e) { e.printStackTrace(); }
		if(actuallyServe(session)) return new Response(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "EEYUP");
		else return new Response(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "NNOPE");
	}
}
