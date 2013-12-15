package pl.asie.endernet.api;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public interface IURIHandler {
	public Response serve(IHTTPSession session);

	public String getPermission();
}
