package space.commandf1.hotdeployment.common.deployment.detector.detectors;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import space.commandf1.hotdeployment.common.deployment.detector.IDetector;
import space.commandf1.hotdeployment.common.deployment.provider.IProvider;
import space.commandf1.hotdeployment.common.plugin.HotDeploymentPlugin;
import space.commandf1.hotdeployment.common.plugin.IPluginController;
import space.commandf1.hotdeployment.common.util.*;
import space.commandf1.hotdeployment.common.util.processor.processors.ClassByteProcessor;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class PluginClassDetector implements IDetector<byte[]> {
    @Override
    public void detect(IProvider<byte[], HotDeploymentPlugin<?>> provider) {
        val provided = provider.getProvided();
        val providerObject = provider.getProvider();

        if (provided.isEmpty() || providerObject == null) {
            return;
        }

        val bytes = provided.get();
        Logger logger = IPluginController.getController().getLogger();
        try {
            val snapshot = JarDiffUtil.readFromBytes(bytes);
            val instrumentation = IPluginController.getController().getInstrumentation();
            val loaded = ClassUtil.getAllClassesByClassLoader(providerObject.getPluginClassLoader(), instrumentation);
            val diff = JarDiffUtil.diffAgainst(snapshot, loaded, providerObject.getPluginClassLoader());

            redefine(diff.getAddedOrChangedClasses(), providerObject.getPluginClassLoader(), instrumentation);

            new ResourceUpdater(IPluginController.getController().getDataFolder()).apply(diff.getAddedOrChangedResources());

            if (!diff.getAddedOrChangedClasses().isEmpty() || !diff.getAddedOrChangedResources().isEmpty()) {
                logger.info("HotDeployment: classes updated=" + diff.getAddedOrChangedClasses().size() +
                        ", resources changed=" + diff.getAddedOrChangedResources().size());

                UpdateHistory.getInstance().add(providerObject.getName(),
                        diff.getAddedOrChangedClasses().size(),
                        diff.getAddedOrChangedResources().size(),
                        "github release");
            } else {
                logger.info("HotDeployment: No changes detected");
            }
        } catch (Exception e) {
            logger.warning("HotDeployment detection failed: " + e.getMessage());
        }
    }

    private static void redefine(@NotNull Map<String, byte[]> classNameToBytes,
                                 @NotNull ClassLoader targetLoader,
                                 @NotNull Instrumentation instrumentation) {
        if (classNameToBytes.isEmpty()) {
            return;
        }

        Set<Class<?>> targets = new HashSet<>();
        for (Class<?> clazz : instrumentation.getAllLoadedClasses()) {
            if (clazz.getClassLoader() == targetLoader && classNameToBytes.containsKey(clazz.getName())) {
                targets.add(clazz);
            }
        }

        new ClassByteProcessor(targets, instrumentation)
                .transformer(new ClassFileTransformer() {
                    @Override
                    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                        if (loader != targetLoader || classBeingRedefined == null) {
                            return null;
                        }

                        return classNameToBytes.get(classBeingRedefined.getName());
                    }
                })
                .process();
    }
}
