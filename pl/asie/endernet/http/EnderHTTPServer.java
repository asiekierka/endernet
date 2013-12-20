package pl.asie.endernet.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import pl.asie.endernet.EnderNet;
import pl.asie.endernet.api.IURIHandler;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.ResponseException;

public class EnderHTTPServer extends NanoHTTPD {
	private final HashMap<String, IURIHandler> handlers = new HashMap<String, IURIHandler>();
	
	public EnderHTTPServer(int port) {
		super(port);
	}
	
	public void registerHandler(IURIHandler handler) {
		handlers.put(handler.getURI(), handler);
	}
	
	@Override
    public Response serve(IHTTPSession session) {
		String address = session.getHeaders().get("remote-addr");
		if(handlers.containsKey(session.getUri())) {
			IURIHandler handler = handlers.get(session.getUri());
			if(!EnderNet.servers.canReceiveFrom(address, handler.getPermissionName())) {
				return new Response(Response.Status.FORBIDDEN, MIME_PLAINTEXT, "Not whitelisted!");
			} else {
				Map<String, String> params = session.getParms();
				String[] requiredParams = handler.getRequiredParams();
				if(requiredParams != null) for(String param: requiredParams) {
					if(!params.containsKey(param)) {
						return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Key " + param + " missing!");
					}
				}
				Gson gson = new Gson();
				if(EnderNet.servers.findByAddress(address) != null)
					params.put("remoteServer", EnderNet.servers.findByAddress(address).name);
				else
					params.put("remoteServer", "unknown");
				return new Response(Response.Status.OK, MIME_PLAINTEXT, gson.toJson(handler.serve(params)));
			}
		}
        return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "URI " + session.getUri() + " not found!");
	}
}
