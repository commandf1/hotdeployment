package space.commandf1.hotdeployment.velocity;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.val;
import space.commandf1.hotdeployment.common.HotDeployment;
import space.commandf1.hotdeployment.common.command.CommandManager;
import space.commandf1.hotdeployment.common.config.HotDeploymentConfig;
import space.commandf1.hotdeployment.common.plugin.CommonPlugin;
import space.commandf1.hotdeployment.common.plugin.IPluginController;
import space.commandf1.hotdeployment.velocity.command.VelocityHotDeploymentCommand;
import space.commandf1.hotdeployment.velocity.plugin.VelocityPlugin;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Plugin(id = "hotdeployment", authors = "commandf1")
@Getter
public class HotDeploymentVelocityPlugin implements IPluginController {

    @Getter
    private static HotDeploymentVelocityPlugin instance;

    private final ProxyServer server;
    private final Logger logger;
    private final Path folder;

    private final Instrumentation instrumentation;

    private final HotDeployment hotDeployment;

    private final HotDeploymentConfig hotDeploymentConfig;

    @Inject
    public HotDeploymentVelocityPlugin(ProxyServer server,
                                       Logger logger,
                                       @DataDirectory final Path folder) {
        instance = this;
        this.server = server;
        this.logger = logger;
        this.folder = folder;

        this.hotDeployment = new HotDeployment(this);
        this.instrumentation = this.hotDeployment.initForAgent();
        this.hotDeploymentConfig = HotDeploymentConfig.getInstance(this.hotDeployment);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CommandManager.getManager().registerCommands(new VelocityHotDeploymentCommand());
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (instrumentation != null) {
            this.hotDeployment.unloadForAgent();
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public CommonPlugin<?> currentPlugin() {
        return new VelocityPlugin(this.getServer()
                .getPluginManager()
                .fromInstance(this)
                .get()
        );
    }

    @Override
    public List<? extends CommonPlugin<?>> getPlugins() {
        return this.getServer()
                .getPluginManager()
                .getPlugins()
                .stream()
                .map(plugin -> (CommonPlugin<?>) new VelocityPlugin(plugin))
                .toList();
    }

    @Override
    public Optional<? extends CommonPlugin<?>> getPluginByName(String name) {
        val plugin = this.getServer()
                .getPluginManager()
                .getPlugin(name);
        if (plugin.isPresent()) {
            return Optional.of(new VelocityPlugin(plugin.get()));
        }

        return Optional.empty();
    }

    @Override
    public File getDataFolder() {
        return this.getFolder().toFile();
    }
}
