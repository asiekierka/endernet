package pl.asie.endernet.api;

import pl.asie.endernet.lib.EnderServer;

public interface IEnderStringReceiver {
	public boolean receiveString(EnderServer server, String string, String endpoint);
}
