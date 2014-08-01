package pl.asie.endernet;

import java.util.List;

import pl.asie.endernet.lib.EnderID;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;

public class CommandEndernetInfo extends CommandBase
{
	@Override
    public String getCommandName()
    {
        return "eninfo";
    }

	@Override
    public String getCommandUsage(ICommandSender par1ICommandSender)
    {
        return "commands.endernet.info.usage";
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

	@Override
    public void processCommand(ICommandSender par1ICommandSender, String[] par2ArrayOfStr)
    {
        EntityPlayerMP player = getCommandSenderAsPlayer(par1ICommandSender);
        ItemStack stack = player.inventory.getStackInSlot(player.inventory.currentItem);
        ChatMessageComponent chat = new ChatMessageComponent();
        chat.addText("UID: " + EnderID.getItemIdentifierFor(stack)
        		+ " | NBT: " + (stack.hasTagCompound() ? "Yes" : "No"));
        player.sendChatToPlayer(chat);
    }
}
