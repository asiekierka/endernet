package pl.asie.endernet.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import openmods.Log;

import com.google.gson.Gson;

import pl.asie.endernet.EnderNet;
import pl.asie.endernet.lib.EnderID;

public class HTTPClient {
	public static BufferedReader sendPost(String server, String uri, Map<String, String> params) {
		String parameters = "";
	    Iterator it = params.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        parameters += pairs.getKey() + "=" + URLEncoder.encode((String)pairs.getValue()) + (it.hasNext() ? "&" : "\r\n");
	        it.remove();
	    }
	    EnderNet.log.info(parameters);
	    try {
	    	URL url = new URL("http://" + server + uri); 
	    	HttpURLConnection con = (HttpURLConnection) url.openConnection();    
	    	con.setRequestMethod("POST");       
	    	con.setDoOutput(true);
	    	con.setDoInput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(parameters);
			wr.flush();
			wr.close();
			return new BufferedReader(new InputStreamReader(con.getInputStream()));
	    } catch(Exception e) {
	    	e.printStackTrace();
	    	EnderNet.log.info("POST sending for " + server + uri + " failed!");
	    	return null;
	    }
	}
	
	public static boolean readAnswer(BufferedReader br) {
		if(br == null) return false;
		try {
			String s = br.readLine();
			return s.equals("EEYUP");
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean canReceive(String server, EnderID item) {
		Gson gson = new Gson();
		String itemString = gson.toJson(item);
		// send string to the good ol' friend http server
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("object", itemString);
		return readAnswer(sendPost(server, "/canReceive", params));
	}
	
	public static boolean receive(String server, int enderID, EnderID item) {
		Gson gson = new Gson();
		String itemString = gson.toJson(item);
		// send string to the good ol' friend http server
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("object", itemString);
		params.put("target", enderID+"");
		return readAnswer(sendPost(server, "/receive", params));
	}
}
