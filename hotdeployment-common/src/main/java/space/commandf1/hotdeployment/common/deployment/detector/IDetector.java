package space.commandf1.hotdeployment.common.deployment.detector;

import space.commandf1.hotdeployment.common.deployment.provider.IProvider;
import space.commandf1.hotdeployment.common.plugin.HotDeploymentPlugin;

public interface IDetector<T> {
    void detect(IProvider<T, HotDeploymentPlugin<?>> provider);
}
