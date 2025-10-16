package space.commandf1.hotdeployment.common.config;

import com.google.gson.Gson;
import lombok.Data;
import lombok.val;
import space.commandf1.hotdeployment.common.HotDeployment;
import space.commandf1.hotdeployment.common.deployment.source.DeploymentSourceType;
import space.commandf1.hotdeployment.common.plugin.IPluginController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

@Data
public class HotDeploymentConfig {
    private static final Gson GSON = new Gson();

    private final boolean debug = false;

    private final Set<HookedPluginInfo> hookedPlugins = Set.of();

    @Data
    public static class HookedPluginInfo {
        private final String name;
        private final DeploymentSourceType deploymentSourceType = DeploymentSourceType.GITHUB;

        private final GithubDeploymentSourceConfig githubDeploymentSource = new GithubDeploymentSourceConfig();
    }

    @Data
    public static class GithubDeploymentSourceConfig {
        private final String prefix = "webhook";
        private final int port = 14514;
        private final String secret = null;
    }

    public static HotDeploymentConfig defaultConfig() {
        return new HotDeploymentConfig();
    }
    
    public static HotDeploymentConfig getInstance(HotDeployment hotDeployment) {
        val dataFolder = hotDeployment.controller().getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new RuntimeException("Failed to create data folder.");
        }

        val configFile = new File(dataFolder, "config.json");

        try {
            if (!configFile.exists()) {
                val hotDeploymentConfig = defaultConfig();
                hotDeploymentConfig.save();
                return hotDeploymentConfig;
            }
            return GSON.fromJson(Files.readString(configFile.toPath()), HotDeploymentConfig.class);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return defaultConfig();
        }
    }

    public void save() {
        val dataFolder = IPluginController.getController().getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new RuntimeException("Failed to create data folder.");
        }

        val configFile = new File(dataFolder, "config.json");
        val json = GSON.toJson(this);
        
        try {
            Files.writeString(configFile.toPath(), json);
        } catch (IOException e) {
            if (this.debug) {
                e.printStackTrace(System.err);
            }
        }
    }
}
