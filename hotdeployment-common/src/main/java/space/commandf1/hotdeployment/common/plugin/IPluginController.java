package space.commandf1.hotdeployment.common.plugin;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author commandf1
 */
public interface IPluginController {
    CommonPlugin<?> currentPlugin();

    List<? extends CommonPlugin<?>> getPlugins();

    Optional<? extends CommonPlugin<?>> getPluginByName(String name);

    Logger getLogger();
    
    Instrumentation getInstrumentation();

    File getDataFolder();

    static IPluginController getController() {
        return CommonPlugin.controller;
    }
}
