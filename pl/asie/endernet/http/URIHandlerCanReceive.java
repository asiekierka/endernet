package pl.asie.endernet.http;

import java.util.Map;

import pl.asie.endernet.EnderNet;
import pl.asie.endernet.api.IURIHandler;
import pl.asie.endernet.lib.EnderID;

import com.google.gson.Gson;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;

public class URIHandlerCanReceive implements IURIHandler {

	public Object serve(Map<String, String> params) {
		Gson gson = new Gson();
		EnderID block = gson.fromJson(params.get("object"), EnderID.class);
		return new HTTPResponse(block.isReceiveable());
	}

	@Override
	public String getPermissionName() {
		return null;
	}
	
	@Override
	public String getURI() {
		return "/canReceive";
	}
	
	@Override
	public String[] getRequiredParams() {
		return new String[]{"object"};
	}
}
