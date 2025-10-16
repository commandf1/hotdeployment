package space.commandf1.hotdeployment.common.deployment.source.sources;

import com.sun.net.httpserver.HttpHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import space.commandf1.hotdeployment.common.deployment.source.AbstractDeploymentSource;
import space.commandf1.hotdeployment.common.deployment.source.DeploymentSourceType;
import space.commandf1.hotdeployment.common.plugin.HotDeploymentPlugin;
import space.commandf1.hotdeployment.common.webhook.WebhookServerManager;


public abstract class AbstractGitDeploymentSource extends AbstractDeploymentSource
        implements HttpHandler {
    private final String prefix;

    @Getter
    private final String secret;

    private final int port;

    @SneakyThrows
    public AbstractGitDeploymentSource(HotDeploymentPlugin<?> plugin,
                                       int port,
                                       String secret,
                                       String prefix) {
        super(plugin);
        this.secret = secret;
        this.prefix = prefix;
        this.port = port;
    }

    public AbstractGitDeploymentSource(DeploymentSourceType.SourceConfig config) {
        this(config.getPlugin(), config.getPort(), config.getSecret(), config.getPrefix());
    }

    @Override
    public void destroy() {
        WebhookServerManager.getInstance().unregisterHandler(this.port, "/" + this.prefix);
    }

    @Override
    public final void execute() {
        try {
            this.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void init() {
        WebhookServerManager.getInstance().registerHandler(this.port, "/" + this.prefix, this);
    }
}
