package space.commandf1.hotdeployment.common.deployment.source;

import lombok.Getter;
import lombok.val;
import space.commandf1.hotdeployment.common.deployment.source.sources.GithubDeploymentSource;
import space.commandf1.hotdeployment.common.plugin.HotDeploymentPlugin;

import java.util.Optional;
import java.util.function.Function;

@Getter
public enum DeploymentSourceType {
    GITHUB("github", GithubDeploymentSource::new);

    private final String name;
    private final Function<SourceConfig, ?> sourceFactory;

    DeploymentSourceType(String name, Function<SourceConfig, ?> sourceFactory) {
        this.name = name;
        this.sourceFactory = sourceFactory;
    }

    public static Optional<DeploymentSourceType> getByName(String name) {
        for (val value : values()) {
            if (value.getName().equals(name)) {
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }

    public interface SourceConfig {
        HotDeploymentPlugin<?> getPlugin();
        int getPort();
        String getSecret();
        String getPrefix();
    }
}
