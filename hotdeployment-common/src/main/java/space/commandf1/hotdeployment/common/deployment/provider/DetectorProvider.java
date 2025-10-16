package space.commandf1.hotdeployment.common.deployment.provider;

import lombok.AllArgsConstructor;
import space.commandf1.hotdeployment.common.plugin.HotDeploymentPlugin;

import java.util.Optional;

@AllArgsConstructor
public class DetectorProvider<T> implements IProvider<T, HotDeploymentPlugin<?>> {
    private final T provided;
    private final HotDeploymentPlugin<?> provider;

    @Override
    public Optional<T> getProvided() {
        return Optional.ofNullable(this.provided);
    }

    @Override
    public HotDeploymentPlugin<?> getProvider() {
        return this.provider;
    }
}
