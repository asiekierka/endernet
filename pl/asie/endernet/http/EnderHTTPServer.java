package pl.asie.endernet.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pl.asie.endernet.EnderNet;
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
	
	public void registerHandler(String uri, IURIHandler handler) {
		handlers.put(uri, handler);
	}
	
	@Override
    public Response serve(IHTTPSession session) {
		if(handlers.containsKey(session.getUri())) {
			return handlers.get(session.getUri()).serve(session);
		}
        return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "URI " + session.getUri() + " not found!");
	}
}
