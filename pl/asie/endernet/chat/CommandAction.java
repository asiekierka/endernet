package pl.asie.endernet.chat;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import cpw.mods.fml.common.registry.LanguageRegistry;
import pl.asie.endernet.EnderNet;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;

public class CommandAction extends CommandBase
{
	private String name, action;
	
	public CommandAction(String name, String action) {
		this.name = name;
		this.action = action;
		LanguageRegistry.instance().addStringLocalization("command.action."+this.name+".usage", "/"+this.name+" <player>");
	}
	
    public String getCommandName()
    {
        return this.name;
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    public String getCommandUsage(ICommandSender par1ICommandSender)
    {
        return "commands.action."+this.name+".usage";
    }

    public void processCommand(ICommandSender sender, String[] args)
    {
    	String nickname = "Console";
    	if(sender instanceof EntityPlayer) {
    		nickname = ((EntityPlayer)sender).username;
    	}
        if (args.length >= 1 && args[0].length() > 0) {
        	EntityPlayerMP target = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(args[0]);
        	if(target != null) {
        		CommandMe.sendAction(nickname, this.action.replaceAll("PLAYER", target.username));
        		return;
        	}
        }
        throw new WrongUsageException("commands.hug.usage", new Object[0]);
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : null;
    }
}
