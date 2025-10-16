package space.commandf1.hotdeployment.velocity.plugin;

import com.velocitypowered.api.plugin.PluginContainer;
import space.commandf1.hotdeployment.common.plugin.CommonPlugin;
import space.commandf1.hotdeployment.common.plugin.PluginDescription;
import space.commandf1.hotdeployment.velocity.util.VelocityUtil;

public class VelocityPlugin extends CommonPlugin<PluginContainer> {
    public VelocityPlugin(PluginContainer plugin) {
        super(plugin);
    }

    @Override
    public PluginDescription getDescription() {
        return VelocityUtil.getPluginDescription(this.getPlugin());
    }

    @Override
    public String getName() {
        return this.getPlugin().getDescription().getId();
    }

}
