package pl.asie.endernet.http;

import java.util.Map;

import pl.asie.endernet.api.IURIHandler;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;

public class URIHandlerPing implements IURIHandler {
	@Override
	public Object serve(Map<String, String> params) {
		return "Pong!";
	}

	@Override
	public String getPermissionName() {
		return null;
	}
	
	@Override
	public String getURI() {
		return "/ping";
	}

	@Override
	public String[] getRequiredParams() {
		return new String[0];
	}
}
