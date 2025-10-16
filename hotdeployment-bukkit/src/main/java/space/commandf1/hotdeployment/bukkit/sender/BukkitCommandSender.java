package space.commandf1.hotdeployment.bukkit.sender;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import space.commandf1.hotdeployment.common.command.sender.CommonCommandSender;

public class BukkitCommandSender extends CommonCommandSender<CommandSender> {
    public BukkitCommandSender(CommandSender item) {
        super(item);
    }

    @Override
    public void sendMessage(String message) {
        this.getCommandSender().sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.getCommandSender().hasPermission(permission);
    }

    @Override
    public boolean isPlayer() {
        return this.getCommandSender() instanceof Player;
    }
}
