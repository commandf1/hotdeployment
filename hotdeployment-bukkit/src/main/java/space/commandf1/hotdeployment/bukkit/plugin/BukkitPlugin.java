package space.commandf1.hotdeployment.bukkit.plugin;

import lombok.val;
import org.bukkit.plugin.Plugin;
import space.commandf1.hotdeployment.common.plugin.CommonPlugin;
import space.commandf1.hotdeployment.common.plugin.PluginDescription;

public class BukkitPlugin extends CommonPlugin<Plugin> {

    public BukkitPlugin(Plugin plugin) {
        super(plugin);
    }

    @Override
    public PluginDescription getDescription() {
        val description = this.getPlugin().getDescription();
        return new PluginDescription(description.getName(), description.getMain(), description.getVersion());
    }

    @Override
    public String getName() {
        return this.getPlugin().getName();
    }
}
