package space.commandf1.hotdeployment.common.command.commands;

import space.commandf1.hotdeployment.common.command.CommandBase;
import space.commandf1.hotdeployment.common.command.commands.subs.ReloadCommand;
import space.commandf1.hotdeployment.common.command.commands.subs.StatusCommand;

public abstract class HotdeploymentCommand extends CommandBase {
    public HotdeploymentCommand() {
        super("hotdeployment", new String[] {"hotd", "hdeployment"}, "The main command of Hotdeployment", "hotdeployment.command", false);

        this.registerSubCommand(new ReloadCommand());
        this.registerSubCommand(new StatusCommand());
    }
}
