package space.commandf1.hotdeployment.common.deployment.detector;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DetectorManager {
    private static final DetectorManager INSTANCE = new DetectorManager();

    public static DetectorManager getInstance() {
        return INSTANCE;
    }

    private final Set<IDetector<?>> detectors = new HashSet<>();

    public <T> void registerDetector(IDetector<T> detector) {
        this.detectors.add(detector);
    }

    public <T, E extends IDetector<T>> Optional<E> getDetector(Class<E> clazz) {
        return this.detectors.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .findFirst();
    }

    public IDetector<?>[] getDetectors() {
        return this.detectors.toArray(IDetector[]::new);
    }
}
