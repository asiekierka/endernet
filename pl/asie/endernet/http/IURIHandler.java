package pl.asie.endernet.http;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public interface IURIHandler {
	public Response serve(IHTTPSession session);
}
