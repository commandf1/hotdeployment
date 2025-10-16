package space.commandf1.hotdeployment.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import lombok.val;
import space.commandf1.hotdeployment.common.command.commands.HotdeploymentCommand;
import space.commandf1.hotdeployment.velocity.HotDeploymentVelocityPlugin;
import space.commandf1.hotdeployment.velocity.sender.VelocityCommandSender;

import java.util.List;

public class VelocityHotDeploymentCommand extends HotdeploymentCommand
        implements SimpleCommand {
    @Override
    public void register() {
        val commandManager = HotDeploymentVelocityPlugin.getInstance().getServer().getCommandManager();
        val meta = commandManager
                .metaBuilder(this.getName())
                .aliases(this.getAliases() == null ? new String[0] : this.getAliases())
                .build();
        commandManager.register(meta, this);
    }

    @Override
    public void execute(Invocation invocation) {
        super.onCommand(new VelocityCommandSender(invocation.source()), invocation.arguments());
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return super.onTabComplete(new VelocityCommandSender(invocation.source()), invocation.arguments());
    }
}
