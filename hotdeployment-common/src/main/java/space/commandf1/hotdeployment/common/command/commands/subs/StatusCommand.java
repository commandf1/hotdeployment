package space.commandf1.hotdeployment.common.command.commands.subs;

import org.jetbrains.annotations.NotNull;
import space.commandf1.hotdeployment.common.command.SubCommand;
import space.commandf1.hotdeployment.common.command.sender.CommonCommandSender;
import space.commandf1.hotdeployment.common.plugin.IPluginController;
import space.commandf1.hotdeployment.common.util.UpdateHistory;

public class StatusCommand extends SubCommand {
    public StatusCommand() {
        super("status", null, "Show deployment sources and server bindings", null, false);
    }

    @Override
    public void execute(@NotNull CommonCommandSender<?> sender, @NotNull String[] args) {
        boolean agent = IPluginController.getController().getInstrumentation() != null;
        sender.sendMessage("Agent initialized: " + agent);

        // webhook status (ports only)
        sender.sendMessage("Webhook servers: single-port with per-plugin prefix (see config.json)");

        // recent update history
        var history = UpdateHistory.getInstance().listRecent(10);
        sender.sendMessage("Recent updates (" + history.size() + "):");
        if (history.isEmpty()) {
            sender.sendMessage("  <none>");
        } else {
            for (var r : history) {
                sender.sendMessage("  " + r.format());
            }
        }
    }
}
