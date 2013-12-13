package pl.asie.endernet.http;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;

public class URIHandlerPing implements IURIHandler {

	@Override
	public Response serve(IHTTPSession session) {
		return new Response(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "PONG");
	}
}
