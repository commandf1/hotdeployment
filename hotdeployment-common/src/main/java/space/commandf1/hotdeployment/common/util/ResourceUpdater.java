package space.commandf1.hotdeployment.common.util;

import lombok.RequiredArgsConstructor;
import space.commandf1.hotdeployment.common.plugin.IPluginController;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

@RequiredArgsConstructor
public class ResourceUpdater {
    private final File targetDirectory;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void apply(Map<String, byte[]> resourcePathToBytes) {
        if (resourcePathToBytes.isEmpty()) return;
        for (var entry : resourcePathToBytes.entrySet()) {
            String path = entry.getKey();
            byte[] bytes = entry.getValue();
            File target = new File(targetDirectory, path);
            try {
                File parent = target.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                try (FileOutputStream fos = new FileOutputStream(target)) {
                    fos.write(bytes);
                }
            } catch (Exception e) {
                IPluginController.getController().getLogger()
                        .warning("Resource update failed for " + path + ": " + e.getMessage());
            }
        }
    }
}


