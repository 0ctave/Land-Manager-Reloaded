package brightspark.landmanager.command.nonop;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.command.LMCommand;
import brightspark.landmanager.message.MessageShowArea;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

//lm showAll
public class CommandShowOff extends LMCommand
{
    @Override
    public String getName()
    {
        return "showoff";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lm.command.showoff.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if(!(sender instanceof EntityPlayer))
        {
            sender.sendMessage(new TextComponentTranslation("lm.command.player"));
            return;
        }

        LandManager.NETWORK.sendTo(new MessageShowArea(""), (EntityPlayerMP) sender);
        sender.sendMessage(new TextComponentTranslation("lm.command.showoff"));
    }
}
