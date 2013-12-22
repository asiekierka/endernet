package pl.asie.endernet.api;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public interface IURIHandler {
	public Object serve(Map<String, String> params);

	public String[] getRequiredParams();
	
	public String getPermissionName();
	
	public String getURI();
	
	public Class getOutputType();
}
