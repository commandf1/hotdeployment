package space.commandf1.hotdeployment.bungee.plugin;

import lombok.val;
import net.md_5.bungee.api.plugin.Plugin;
import space.commandf1.hotdeployment.common.plugin.CommonPlugin;
import space.commandf1.hotdeployment.common.plugin.PluginDescription;

public class BungeePlugin extends CommonPlugin<Plugin> {
    public BungeePlugin(Plugin plugin) {
        super(plugin);
    }

    @Override
    public PluginDescription getDescription() {
        val description = this.getPlugin().getDescription();
        return new PluginDescription(description.getName(),
                description.getMain(),
                description.getVersion()
        );
    }

    @Override
    public String getName() {
        return this.getPlugin().getDescription().getName();
    }
}
