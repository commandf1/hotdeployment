package space.commandf1.hotdeployment.bungee.sender;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import space.commandf1.hotdeployment.common.command.sender.CommonCommandSender;

public class BungeeCommandSender extends CommonCommandSender<CommandSender> {
    public BungeeCommandSender(CommandSender item) {
        super(item);
    }

    @Override
    public void sendMessage(String message) {
        this.getCommandSender().sendMessage(TextComponent.fromLegacy(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.getCommandSender().hasPermission(permission);
    }

    @Override
    public boolean isPlayer() {
        return this.getCommandSender() instanceof ProxiedPlayer;
    }
}
