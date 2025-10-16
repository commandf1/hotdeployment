package space.commandf1.hotdeployment.common;

/**
 * @author commandf1
 */
public class ItemHolder<T> {
    private final T item;

    public ItemHolder(T item) {
        this.item = item;
    }

    protected final T getItem() {
        return item;
    }
}
