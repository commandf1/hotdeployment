package space.commandf1.hotdeployment.bukkit;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import space.commandf1.hotdeployment.bukkit.command.BukkitHotDeploymentCommand;
import space.commandf1.hotdeployment.bukkit.plugin.BukkitPlugin;
import space.commandf1.hotdeployment.common.HotDeployment;
import space.commandf1.hotdeployment.common.command.CommandManager;
import space.commandf1.hotdeployment.common.config.HotDeploymentConfig;
import space.commandf1.hotdeployment.common.plugin.CommonPlugin;
import space.commandf1.hotdeployment.common.plugin.IPluginController;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HotDeploymentBukkitPlugin extends JavaPlugin
        implements IPluginController {
    @Getter
    private static HotDeploymentBukkitPlugin instance;

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
        CommandManager.getManager().registerCommands(new BukkitHotDeploymentCommand());
    }

    @Override
    public void onDisable() {
        instance = null;

        this.getHotDeployment().unloadForAgent();
    }

    @Override
    public CommonPlugin<?> currentPlugin() {
        return new BukkitPlugin(this);
    }

    @Override
    public List<? extends CommonPlugin<?>> getPlugins() {
        return Arrays.stream(Bukkit.getPluginManager().getPlugins())
                .map(BukkitPlugin::new)
                .toList();
    }

    @Override
    public Optional<? extends CommonPlugin<?>> getPluginByName(String name) {
        return this.getPlugins().stream()
                .filter(plugin -> plugin.getName().equals(name))
                .findFirst();
    }

    @Override
    public Instrumentation getInstrumentation() {
        return instrumentation;
    }
}
