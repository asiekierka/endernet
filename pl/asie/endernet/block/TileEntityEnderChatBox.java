package pl.asie.endernet.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;
import pl.asie.endernet.lib.EnderServer;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;

public class TileEntityEnderChatBox extends TileEntityEnderModem {
	private static final int DISTANCE = 16;
	
	@Override
	public boolean receiveString(EnderServer server, String string) {
		super.receiveString(server, string);
		sendChatMessage(string, DISTANCE);
		return true;
	}
	
	public void sendChatMessage(String string, int distance) {
		ChatMessageComponent chat = new ChatMessageComponent();
		chat.setColor(EnumChatFormatting.GRAY);
		chat.setItalic(true);
		chat.addText("[ChatBox "+this.enderNetID+"] ");
		chat.setItalic(false);
		chat.addText(string);
		for(Object o: this.worldObj.playerEntities) {
			if(!(o instanceof EntityPlayer)) continue;
			EntityPlayer player = (EntityPlayer)o;
			if(player.getDistance(this.xCoord, this.yCoord, this.zCoord) < distance) {
				player.sendChatToPlayer(chat);
			}
		}
	}
	
	@Override
	public String[] getMethodNames() {
		String[] names = new String[]{"getAddress", "setAddress", "getID", "canReceive", "canTransmit", "send", "say"};
		return names;
	}
	
	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,
			int method, Object[] arguments) throws Exception {
		if(method < 6) return super.callMethod(computer, context, method, arguments);
		else switch(method) {
			case 6: // say
				if(arguments.length >= 1 && (arguments[0] instanceof String)) {
					sendChatMessage((String)arguments[0], DISTANCE);
				}
				break;
		}
		return null;
	}
}
