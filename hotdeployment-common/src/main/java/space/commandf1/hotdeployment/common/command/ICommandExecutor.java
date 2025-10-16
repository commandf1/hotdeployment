package space.commandf1.hotdeployment.common.command;

import space.commandf1.hotdeployment.common.command.sender.CommonCommandSender;

/**
 * @author commandf1
 */
public interface ICommandExecutor {
    boolean onCommand(CommonCommandSender<?> sender, String[] args);
}
