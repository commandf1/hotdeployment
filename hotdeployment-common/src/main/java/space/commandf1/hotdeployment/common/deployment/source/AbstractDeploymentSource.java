package space.commandf1.hotdeployment.common.deployment.source;

import lombok.Getter;
import space.commandf1.hotdeployment.common.plugin.HotDeploymentPlugin;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractDeploymentSource implements IDeploymentSource {
    @Getter
    private final HotDeploymentPlugin<?> plugin;

    @Getter
    private static final Set<IDeploymentSource> sources = new HashSet<>();

    public <T> AbstractDeploymentSource(HotDeploymentPlugin<T> plugin) {
        this.plugin = plugin;
        sources.add(this);
    }
}
