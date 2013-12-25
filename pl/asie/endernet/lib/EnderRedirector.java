package pl.asie.endernet.lib;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

import pl.asie.endernet.EnderNet;
import pl.asie.endernet.api.IEnderStringReceiver;
import pl.asie.endernet.block.TileEntityEnderReceiver;
import pl.asie.endernet.http.HTTPClient;
import pl.asie.endernet.http.HTTPResponse;
import pl.asie.endernet.http.URIHandlerReceive;
import pl.asie.endernet.http.URIHandlerSendRedstone;
import pl.asie.endernet.http.URIHandlerSendString;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class EnderRedirector {
	private static final Gson gson = new Gson();
	
	public static String getRemoteAddress(String address) {
		return EnderNet.servers.getAddress(getServerName(address));
	}
	
	public static boolean sendString(String address, String string) {
		try {
			HashMap<String, String> params = hashMapFromAddress(address);		
			params.put("string", string);
			if(isLocal(getServerName(address))) {
				URIHandlerSendString handler = new URIHandlerSendString();
				if(!EnderNet.servers.canLocal(handler.getPermissionName())) return false;
				return ((HTTPResponse)handler.serve(params)).success;
			} else {
				return HTTPClient.readHTTPResponse(HTTPClient.sendPost(getRemoteAddress(address), "/sendString", params)).success;
			}
		} catch(Exception e) {
			return false;
		}
	}

	public static boolean sendRedstone(String address, int value) {
		try {
			HashMap<String, String> params = hashMapFromAddress(address);
			params.put("value", ""+value);
			if(isLocal(getServerName(address))) {
				URIHandlerSendRedstone handler = new URIHandlerSendRedstone();
				if(!EnderNet.servers.canLocal(handler.getPermissionName())) return false;
				return ((HTTPResponse)handler.serve(params)).success;
			} else {
				return HTTPClient.readHTTPResponse(HTTPClient.sendPost(getRemoteAddress(address), "/sendRedstone", params)).success;
			}
		} catch(Exception e) {
			return false;
		}
	}
	
	public static boolean canReceive(String address, ItemStack stack) {
		try {
			if(isLocal(getServerName(address))) {
				return new EnderID(stack).isReceiveable();
			} else {
				HashMap<String, String> params = hashMapFromAddress(address);
				params.put("object", gson.toJson(new EnderID(stack)));
				return HTTPClient.readHTTPResponse(HTTPClient.sendPost(getRemoteAddress(address), "/canReceive", params)).success;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static HTTPResponse receive(String address, ItemStack stack) {
		try {
			HashMap<String, String> params = hashMapFromAddress(address);
			params.put("object", gson.toJson(new EnderID(stack)));
			if(isLocal(getServerName(address))) {
				URIHandlerReceive handler = new URIHandlerReceive();
				if(!EnderNet.servers.canLocal(handler.getPermissionName())) return new HTTPResponse(false);
				return (HTTPResponse)handler.serve(params);
			} else {
				return HTTPClient.readHTTPResponse(HTTPClient.sendPost(getRemoteAddress(address), "/canReceive", params));
			}
		} catch(Exception e) {
			return new HTTPResponse(false);
		}
	}
	
	public static String getServerName(String server) {
		String[] list = removeEndpoint(server).split("\\.");
		list = ArrayUtils.remove(list, list.length - 1);
		return StringUtils.join(list, ".");
	}
	
	public static int getServerEnderID(String server) {
		String[] id = removeEndpoint(server).split("\\.");
		return new Integer(id[id.length - 1]).intValue();
	}
	
	public static String removeEndpoint(String server) {
		return server.split("\\/")[0];
	}
	
	public static String getEndpoint(String server) {
		String[] endpoints = server.split("\\/");
		if(endpoints.length < 2) return "";
		else return endpoints[endpoints.length - 1];
	}
	
	public static boolean isLocal(String name) {
		return (name.equals("local") || name.equals(""));
	}
	
	private static HashMap<String, String> hashMapFromAddress(String address) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("target", ""+getServerEnderID(address));
		params.put("endpoint", getEndpoint(address));
		return params;
	}
}
