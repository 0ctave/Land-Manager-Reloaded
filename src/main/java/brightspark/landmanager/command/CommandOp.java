package brightspark.landmanager.command;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.command.op.*;
import brightspark.landmanager.command.optional.CommandExplosions;
import brightspark.landmanager.command.optional.CommandHostiles;
import brightspark.landmanager.command.optional.CommandInteractions;
import brightspark.landmanager.command.optional.CommandPassives;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;

public class CommandOp extends CommandTreeBase
{
    public CommandOp()
    {
        addSubcommand(new CommandDelete());
        addSubcommand(new CommandAllocate());
        addSubcommand(new CommandClearAllocation());
        addSubcommand(new CommandTool());
        addSubcommand(new CommandRequests());
        addSubcommand(new CommandApprove());
        addSubcommand(new CommandDisapprove());

        if(!LMConfig.permissions.passiveSpawning)
            addSubcommand(new CommandPassives());
        if(!LMConfig.permissions.hostileSpawning)
            addSubcommand(new CommandHostiles());
        if(!LMConfig.permissions.explosions)
            addSubcommand(new CommandExplosions());
        if(!LMConfig.permissions.interactions)
            addSubcommand(new CommandInteractions());

        addSubcommand(new CommandTreeHelp(this));
    }

    @Override
    public String getName()
    {
        return "op";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "lm.command.op.usage";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
}
