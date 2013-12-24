package pl.asie.endernet.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;
import pl.asie.endernet.EnderNet;
import pl.asie.endernet.lib.EnderServer;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;

public class TileEntityEnderChatBox extends TileEntityEnderModem {
	@Override
	public boolean receiveString(EnderServer server, String string) {
		super.receiveString(server, string);
		//sendChatMessage(string, EnderNet.SPEAKING_DISTANCE);
		return true;
	}
	
	public void receiveChatMessage(String username, String message) {
		if(super.computers.size() > 0) {
			for(IComputerAccess computer: super.computers) {
				Object[] arguments = new Object[3];
				arguments[0] = username;
				arguments[1] = message;
				arguments[2] = computer.getAttachmentName();
				computer.queueEvent("endernet_chat_message", arguments);
			}
		}
	}
	
	public void sendChatMessage(String string, int distance) {
		ChatMessageComponent chat = new ChatMessageComponent();
		chat.setColor(EnumChatFormatting.GRAY);
		chat.setItalic(true);
		chat.addText(EnumChatFormatting.ITALIC + "[ChatBox "+this.enderNetID+"] ");
		chat.addText(EnumChatFormatting.RESET + "" + EnumChatFormatting.GRAY + string);
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
					int distance = 0;
					if(arguments.length >= 2 && (arguments[1] instanceof Integer))
						distance = ((Integer)arguments[1]).intValue();
					if(arguments.length >= 2 && (arguments[1] instanceof Long))
						distance = ((Long)arguments[1]).intValue();
					if(distance == 0) distance = EnderNet.chat.SPEAKING_DISTANCE;
					sendChatMessage((String)arguments[0], distance);
				}
				break;
		}
		return null;
	}
}
