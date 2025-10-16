package space.commandf1.hotdeployment.bungee;

import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import space.commandf1.hotdeployment.bungee.command.BungeeHotDeploymentCommand;
import space.commandf1.hotdeployment.bungee.plugin.BungeePlugin;
import space.commandf1.hotdeployment.common.HotDeployment;
import space.commandf1.hotdeployment.common.command.CommandManager;
import space.commandf1.hotdeployment.common.config.HotDeploymentConfig;
import space.commandf1.hotdeployment.common.plugin.CommonPlugin;
import space.commandf1.hotdeployment.common.plugin.IPluginController;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Optional;

public class HotDeploymentBungeePlugin extends Plugin implements IPluginController {
    @Getter
    private static HotDeploymentBungeePlugin instance;

    private Instrumentation instrumentation;

    @Getter
    private HotDeploymentConfig hotDeploymentConfig;

    @Getter
    private HotDeployment hotDeployment;

    @Override
    public void onLoad() {
        instance = this;

        this.hotDeployment = new HotDeployment(this);
        this.instrumentation = hotDeployment.initForAgent();
        this.hotDeploymentConfig = HotDeploymentConfig.getInstance(hotDeployment);
    }

    @Override
    public void onEnable() {
        this.getHotDeployment().processHotDeployment(this.getHotDeploymentConfig());
        CommandManager.getManager().registerCommands(new BungeeHotDeploymentCommand());
    }

    @Override
    public void onDisable() {
        instance = null;

        this.getHotDeployment().unloadForAgent();
    }

    @Override
    public CommonPlugin<?> currentPlugin() {
        return new BungeePlugin(this);
    }

    @Override
    public List<? extends CommonPlugin<?>> getPlugins() {
        return ProxyServer.getInstance().getPluginManager().getPlugins().stream().map(BungeePlugin::new).toList();
    }

    @Override
    public Optional<? extends CommonPlugin<?>> getPluginByName(String name) {
        return this.getPlugins().stream().filter(plugin -> plugin.getName().equals(name)).findFirst();
    }

    @Override
    public Instrumentation getInstrumentation() {
        return this.instrumentation;
    }
}
