package pl.asie.endernet.http;

import com.google.gson.Gson;

public class HTTPResponse {
	public boolean success;
	public int amountSent;
	
	public HTTPResponse(boolean success) {
		this(success, 0);
	}
	
	public HTTPResponse(boolean success, int amountSent) {
		this.success = success;
		this.amountSent = amountSent;
	}
	
	@Deprecated
	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}