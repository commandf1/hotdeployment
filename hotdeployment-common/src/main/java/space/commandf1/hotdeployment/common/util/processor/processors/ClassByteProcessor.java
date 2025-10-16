package space.commandf1.hotdeployment.common.util.processor.processors;


import space.commandf1.hotdeployment.common.util.InterceptionUtil;
import space.commandf1.hotdeployment.common.util.processor.IProcessor;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * @author commandf1
 */
public class ClassByteProcessor implements IProcessor<Class<?>, byte[]>, ClassFileTransformer {
    private final Set<BiConsumer<Class<?>, byte[]>> actions = new HashSet<>();

    private final Instrumentation instrumentation;

    private final Set<Class<?>> targets;

    private ClassFileTransformer transformer;

    public ClassByteProcessor(Set<Class<?>> targets, Instrumentation instrumentation) {
        this.targets = targets;
        this.instrumentation = instrumentation;
    }

    @Override
    public IProcessor<Class<?>, byte[]> after(BiConsumer<Class<?>, byte[]> action) {
        this.actions.add(action);
        return this;
    }

    public IProcessor<Class<?>, byte[]> transformer(ClassFileTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    @Override
    public void process() {
        InterceptionUtil.retransformClasses(this.targets, this.transformer == null ? this : this.transformer, this.instrumentation);
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        if (this.targets.contains(classBeingRedefined)) {
            for (BiConsumer<Class<?>, byte[]> action : this.actions) {
                action.accept(classBeingRedefined, classfileBuffer);
            }
        }

        return null;
    }
}