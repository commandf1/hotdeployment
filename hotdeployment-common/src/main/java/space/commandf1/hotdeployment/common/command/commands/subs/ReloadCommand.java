package space.commandf1.hotdeployment.common.command.commands.subs;

import org.jetbrains.annotations.NotNull;
import space.commandf1.hotdeployment.common.HotDeployment;
import space.commandf1.hotdeployment.common.command.SubCommand;
import space.commandf1.hotdeployment.common.command.sender.CommonCommandSender;
import space.commandf1.hotdeployment.common.config.HotDeploymentConfig;

public class ReloadCommand extends SubCommand {
    public ReloadCommand() {
        super("reload", null, "Reload hotdeployment config and restart sources", "hotdeployment.command.reload", false);
    }

    @Override
    public void execute(@NotNull CommonCommandSender<?> sender, @NotNull String[] args) {
        var hot = HotDeployment.getInstance();
        if (hot == null) {
            sender.sendMessage("HotDeployment is not initialized.");
            return;
        }
        var config = HotDeploymentConfig.getInstance(hot);
        hot.processHotDeployment(config);
        sender.sendMessage("HotDeployment reloaded.");
    }
}
