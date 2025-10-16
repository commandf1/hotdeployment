package space.commandf1.hotdeployment.common.deployment.provider;

import java.util.Optional;

public interface IProvider<T, E> {
    Optional<T> getProvided();

    E getProvider();
}
