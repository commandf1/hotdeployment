package space.commandf1.hotdeployment.bungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import space.commandf1.hotdeployment.bungee.HotDeploymentBungeePlugin;
import space.commandf1.hotdeployment.bungee.sender.BungeeCommandSender;
import space.commandf1.hotdeployment.common.command.commands.HotdeploymentCommand;

public class BungeeHotDeploymentCommand extends HotdeploymentCommand {
    @Override
    public void register() {
        ProxyServer.getInstance().getPluginManager().registerCommand(
                HotDeploymentBungeePlugin.getInstance(),
                new BungeeCommand(this)
        );
    }

    private static class BungeeCommand extends Command implements TabExecutor {
        private final BungeeHotDeploymentCommand command;

        public BungeeCommand(BungeeHotDeploymentCommand command) {
            super(command.getName(), command.getPermission(), command.getAliases());
            this.command = command;
            this.setPermissionMessage(this.command.getNoPermissionMessage());
        }

        @Override
        public void execute(CommandSender commandSender, String[] strings) {
            this.command.onCommand(new BungeeCommandSender(commandSender), strings);
        }

        @Override
        public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
            return this.command.onTabComplete(new BungeeCommandSender(sender), args);
        }
    }
}
