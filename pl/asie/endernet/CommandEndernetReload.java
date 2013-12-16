package pl.asie.endernet;

import java.util.List;

import pl.asie.endernet.lib.EnderID;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;

public class CommandEndernetReload extends CommandBase
{
	@Override
    public String getCommandName()
    {
        return "enreload";
    }

	@Override
    public String getCommandUsage(ICommandSender par1ICommandSender)
    {
        return "commands.endernet.reload";
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

	@Override
    public void processCommand(ICommandSender par1ICommandSender, String[] par2ArrayOfStr)
    {
		// Reload
		EnderNet.instance.reloadServerFile();
		
		// Give success message
        EntityPlayerMP player = getCommandSenderAsPlayer(par1ICommandSender);
        ChatMessageComponent chat = ChatMessageComponent.createFromTranslationKey("commands.endernet.reload.success");
        player.sendChatToPlayer(chat);
    }
}
