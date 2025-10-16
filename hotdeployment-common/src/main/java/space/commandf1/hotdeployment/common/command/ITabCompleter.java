package space.commandf1.hotdeployment.common.command;

import space.commandf1.hotdeployment.common.command.sender.CommonCommandSender;

import java.util.List;

/**
 * @author commandf1
 */
public interface ITabCompleter {
    List<String> onTabComplete(CommonCommandSender<?> sender, String[] args);
}
