package space.commandf1.hotdeployment.common;

import lombok.val;
import net.bytebuddy.agent.ByteBuddyAgent;
import space.commandf1.hotdeployment.common.config.HotDeploymentConfig;
import space.commandf1.hotdeployment.common.deployment.detector.DetectorManager;
import space.commandf1.hotdeployment.common.deployment.detector.detectors.PluginClassDetector;
import space.commandf1.hotdeployment.common.deployment.source.AbstractDeploymentSource;
import space.commandf1.hotdeployment.common.deployment.source.DeploymentSourceType;
import space.commandf1.hotdeployment.common.deployment.source.IDeploymentSource;
import space.commandf1.hotdeployment.common.plugin.CommonPlugin;
import space.commandf1.hotdeployment.common.plugin.HotDeploymentPlugin;
import space.commandf1.hotdeployment.common.plugin.IPluginController;

import java.lang.instrument.Instrumentation;

public record HotDeployment(IPluginController controller) {
    private static HotDeployment instance;

    public static HotDeployment getInstance() {
        return instance;
    }

    public HotDeployment(IPluginController controller) {
        instance = this;
        this.controller = controller;
        CommonPlugin.registerController(this.controller);

        DetectorManager.getInstance().registerDetector(new PluginClassDetector());
    }

    public static void main(String[] args) {
        System.out.println("HotDeployment Powered By commandf1");
    }

    public Instrumentation initForAgent() {
        try {
            return ByteBuddyAgent.install();
        } catch (Exception e) {
            IPluginController.getController()
                    .getLogger()
                    .severe("Failed to initialize HotDeployment for agent.");
            throw new RuntimeException(e);
        }
    }

    public void unloadForAgent() {
        val instrumentation = IPluginController.getController().getInstrumentation();
        val logger = IPluginController.getController().getLogger();

        try {
            instrumentation.retransformClasses(instrumentation.getAllLoadedClasses());
        } catch (Exception e) {
            logger.warning("Failed to reset transformed classes: " + e.getMessage());
        }
    }

    public void processHotDeployment(HotDeploymentConfig config) {
        for (IDeploymentSource source : AbstractDeploymentSource.getSources()) {
            source.destroy();
        }

        for (val hookedPlugin : config.getHookedPlugins()) {
            val plugin = this.controller.getPluginByName(hookedPlugin.getName());

            if (plugin.isEmpty()) {
                continue;
            }

            IDeploymentSource source = null;

            val deploymentSourceType = hookedPlugin.getDeploymentSourceType();
            val sourceFactory = deploymentSourceType.getSourceFactory();
            
            if (sourceFactory != null) {
                val githubSource = hookedPlugin.getGithubDeploymentSource();
                source = (IDeploymentSource) sourceFactory.apply(new DeploymentSourceType.SourceConfig() {
                    @Override
                    public HotDeploymentPlugin<?> getPlugin() {
                        return HotDeploymentPlugin.of(plugin.get());
                    }

                    @Override
                    public int getPort() {
                        return githubSource.getPort();
                    }

                    @Override
                    public String getSecret() {
                        return githubSource.getSecret();
                    }

                    @Override
                    public String getPrefix() {
                        return githubSource.getPrefix();
                    }
                });
            }

            if (source != null) {
                source.execute();
            }
        }
    }
}