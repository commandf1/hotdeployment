package space.commandf1.hotdeployment.common.command.sender;

import space.commandf1.hotdeployment.common.ItemHolder;

/**
 * @author commandf1
 */
public abstract class CommonCommandSender<T> extends ItemHolder<T> {
    public CommonCommandSender(T item) {
        super(item);
    }

    public final T getCommandSender() {
        return this.getItem();
    }

    public abstract void sendMessage(String message);

    public abstract boolean hasPermission(String permission);

    public abstract boolean isPlayer();
}
