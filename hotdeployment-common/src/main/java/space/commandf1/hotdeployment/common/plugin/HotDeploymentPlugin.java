package space.commandf1.hotdeployment.common.plugin;

public abstract class HotDeploymentPlugin<T> extends CommonPlugin<T> {
    public HotDeploymentPlugin(T plugin) {
        super(plugin);
    }

    public static <E> HotDeploymentPlugin<E> of(CommonPlugin<E> common) {
        return new HotDeploymentPlugin<>(common.getPlugin()) {
            @Override
            public PluginDescription getDescription() {
                return common.getDescription();
            }

            @Override
            public String getName() {
                return common.getName();
            }
        };
    }
}
