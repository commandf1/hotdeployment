package space.commandf1.hotdeployment.bukkit.command;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import space.commandf1.hotdeployment.bukkit.sender.BukkitCommandSender;
import space.commandf1.hotdeployment.common.command.commands.HotdeploymentCommand;

import java.util.Arrays;
import java.util.List;

public class BukkitHotDeploymentCommand extends HotdeploymentCommand
        implements CommandExecutor, TabCompleter {
    @Override
    public void register() {
        val command = Bukkit.getPluginCommand(this.getName());
        command.setExecutor(this);
        command.setTabCompleter(this);
        command.setAliases(Arrays.stream(this.getAliases()).toList());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return super.onCommand(new BukkitCommandSender(sender), args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return super.onTabComplete(new BukkitCommandSender(sender), args);
    }
}
