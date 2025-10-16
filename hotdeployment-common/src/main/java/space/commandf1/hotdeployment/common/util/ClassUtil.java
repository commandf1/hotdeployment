package space.commandf1.hotdeployment.common.util;

import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Utility class for class manipulation and inspection.
 *
 * @author commandf1
 */
public class ClassUtil {
    private static final Object CLASS_LOADING_LOCK = new Object();

    /**
     * Gets all loaded classloaders from the given instrumentation.
     *
     * @param instrumentation the instrumentation instance
     * @return a set of all loaded classloaders
     */
    public static @NotNull Set<ClassLoader> getLoadedClassloaders(@NotNull Instrumentation instrumentation) {
        return Arrays.stream(instrumentation.getAllLoadedClasses())
                .map(Class::getClassLoader)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Gets all classes loaded by the specified classloader.
     *
     * @param classLoader     the classloader to filter by
     * @param instrumentation the instrumentation instance
     * @return a map of class names to classes loaded by the specified classloader
     */
    public static @NotNull Map<String, Class<?>> getAllClassesByClassLoader(@NotNull ClassLoader classLoader,
                                                                            @NotNull Instrumentation instrumentation) {
        return Arrays.stream(instrumentation.getAllLoadedClasses())
                .filter(clazz -> classLoader.equals(clazz.getClassLoader()))
                .collect(Collectors.toMap(Class::getName, clazz -> (Class<?>) clazz));
    }

    /**
     * Gets a classloader by its hash code.
     *
     * @param hashCode        the hash code of the classloader
     * @param instrumentation the instrumentation instance
     * @return an optional containing the classloader if found, empty otherwise
     */
    public static @NotNull Optional<ClassLoader> getClassLoaderByHashCode(int hashCode,
                                                                          @NotNull Instrumentation instrumentation) {
        return getLoadedClassloaders(instrumentation)
                .stream()
                .filter(loader -> loader.hashCode() == hashCode)
                .findFirst();
    }

    /**
     * Gets a classloader by its hash code in hexadecimal format.
     *
     * @param hashCodeHax     the hash code in hexadecimal format
     * @param instrumentation the instrumentation instance
     * @return an optional containing the classloader if found, empty otherwise
     */
    public static @NotNull Optional<ClassLoader> getClassLoaderByHashCode(@NotNull String hashCodeHax,
                                                                           @NotNull Instrumentation instrumentation) {
        return getClassLoaderByHashCode(Integer.parseInt(hashCodeHax, 16), instrumentation);
    }

    /**
     * Defines a class in the given classloader.
     *
     * @param loader    the classloader to define the class in
     * @param className the name of the class to define
     * @param bytes     the bytecode of the class
     * @return the defined class
     */
    @SneakyThrows
    public static Class<?> defineClass(ClassLoader loader, String className, byte[] bytes) {
        synchronized (CLASS_LOADING_LOCK) {
            val lookup = MethodHandles.lookup();
            Method defineMethod = ClassLoader.class.getDeclaredMethod(
                    "defineClass", String.class, byte[].class, int.class, int.class
            );

            defineMethod.setAccessible(true);
            return (Class<?>) lookup.unreflect(defineMethod).invoke(loader, className, bytes, 0, bytes.length);
        }
    }

    /**
     * Checks if the given class is a lambda class.
     *
     * @param clazz the class to check
     * @return true if the class is a lambda class, false otherwise
     */
    public static boolean isLambda(Class<?> clazz) {
        return clazz.getName().contains("$$Lambda");
    }

    /**
     * Gets detailed information about a class in a formatted string.
     *
     * @param clazz the class to get information for
     * @return a formatted string containing detailed class information
     */
    public static @NotNull String getClassInfo(@NotNull Class<?> clazz) {
        StringBuilder sb = new StringBuilder();

        appendHeader(sb, "CLASS INFORMATION: " + clazz.getName());

        appendClassBasicInfo(sb, clazz);

        appendAnnotationInfo(sb, clazz);

        appendInterfaceInfo(sb, clazz);

        appendInheritanceInfo(sb, clazz);

        appendFieldInfo(sb, clazz);

        appendConstructorInfo(sb, clazz);

        appendMethodInfo(sb, clazz);

        appendInnerClassInfo(sb, clazz);

        return sb.toString();
    }

    /**
     * Appends a header to the given StringBuilder.
     *
     * @param sb    the StringBuilder to append to
     * @param title the title for the header
     */
    private static void appendHeader(StringBuilder sb, String title) {
        sb.append("╔══════════════════════════════════════════════════════════════╗\n");
        sb.append("  ").append(String.format("%-64s", title)).append("\n");
        sb.append("╚══════════════════════════════════════════════════════════════╝\n\n");
    }

    /**
     * Appends a section header to the given StringBuilder.
     *
     * @param sb    the StringBuilder to append to
     * @param title the title for the section header
     */
    private static void appendSectionHeader(StringBuilder sb, String title) {
        sb.append("┌──────────────────────────────────────────────────────────────┐\n");
        sb.append("│ ").append(String.format("%-60s", title)).append(" │\n");
        sb.append("└──────────────────────────────────────────────────────────────┘\n");
    }

    /**
     * Appends basic class information to the given StringBuilder.
     *
     * @param sb    the StringBuilder to append to
     * @param clazz the class to get information from
     */
    private static void appendClassBasicInfo(StringBuilder sb, Class<?> clazz) {
        appendSectionHeader(sb, "BASIC INFORMATION");

        sb.append(String.format("  %-20s: %s\n", "Class Name", clazz.getName()));
        sb.append(String.format("  %-20s: %s\n", "Simple Name", clazz.getSimpleName()));
        sb.append(String.format("  %-20s: %s\n", "Canonical Name", clazz.getCanonicalName()));
        sb.append(String.format("  %-20s: %s\n", "Modifiers", Modifier.toString(clazz.getModifiers())));
        sb.append(String.format("  %-20s: %s\n", "Package", clazz.getPackage().getName()));
        sb.append(String.format("  %-20s: %s\n", "Class Loader", clazz.getClassLoader() == null ? "null" : clazz.getClassLoader().toString()));
        sb.append(String.format("  %-20s: %s\n", "Is Interface", clazz.isInterface()));
        sb.append(String.format("  %-20s: %s\n", "Is Array", clazz.isArray()));
        sb.append(String.format("  %-20s: %s\n", "Is Enum", clazz.isEnum()));
        sb.append(String.format("  %-20s: %s\n", "Is Annotation", clazz.isAnnotation()));
        sb.append(String.format("  %-20s: %s\n", "Is Primitive", clazz.isPrimitive()));
        sb.append(String.format("  %-20s: %s\n", "Is Synthetic", clazz.isSynthetic()));
        sb.append("\n");
    }

    /**
     * Appends annotation information to the given StringBuilder.
     *
     * @param sb    the StringBuilder to append to
     * @param clazz the class to get information from
     */
    private static void appendAnnotationInfo(StringBuilder sb, Class<?> clazz) {
        Annotation[] annotations = clazz.getDeclaredAnnotations();
        if (annotations.length == 0) {
            return;
        }

        appendSectionHeader(sb, "ANNOTATIONS");

        for (Annotation annotation : annotations) {
            sb.append(String.format("  @%s\n", annotation.annotationType().getName()));


            Method[] annotationMethods = annotation.annotationType().getDeclaredMethods();
            for (Method annotationMethod : annotationMethods) {
                try {
                    Object value = annotationMethod.invoke(annotation);
                    sb.append(String.format("    %-15s: %s\n",
                            annotationMethod.getName(),
                            formatAnnotationValue(value)));
                } catch (Exception e) {
                    sb.append(String.format("    %-15s: [Error: %s]\n",
                            annotationMethod.getName(), e.getMessage()));
                }
            }
            sb.append("\n");
        }
    }

    /**
     * Appends interface information to the given StringBuilder.
     *
     * @param sb    the StringBuilder to append to
     * @param clazz the class to get information from
     */
    private static void appendInterfaceInfo(StringBuilder sb, Class<?> clazz) {
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length == 0) {
            return;
        }

        appendSectionHeader(sb, "IMPLEMENTED INTERFACES");

        for (Class<?> interfaceClass : interfaces) {
            sb.append(String.format("  • %s\n", interfaceClass.getName()));
        }
        sb.append("\n");
    }

    /**
     * Appends inheritance information to the given StringBuilder.
     *
     * @param sb    the StringBuilder to append to
     * @param clazz the class to get information from
     */
    private static void appendInheritanceInfo(StringBuilder sb, Class<?> clazz) {
        appendSectionHeader(sb, "INHERITANCE HIERARCHY");

        Class<?> currentClass = clazz;
        int level = 0;
        while (currentClass != null) {
            String indent = "  ".repeat(level);
            sb.append(String.format("%s%s└── %s\n",
                    indent, (level > 0 ? " " : ""), currentClass.getName()));
            currentClass = currentClass.getSuperclass();
            level++;
        }
        sb.append("\n");
    }

    /**
     * Appends field information to the given StringBuilder.
     *
     * @param sb    the StringBuilder to append to
     * @param clazz the class to get information from
     */
    private static void appendFieldInfo(StringBuilder sb, Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        if (fields.length == 0) {
            return;
        }

        appendSectionHeader(sb, "FIELDS");

        for (Field field : fields) {
            sb.append(String.format("  %s %s %s\n",
                    Modifier.toString(field.getModifiers()),
                    field.getType().getSimpleName(),
                    field.getName()));


            Annotation[] fieldAnnotations = field.getDeclaredAnnotations();
            if (fieldAnnotations.length > 0) {
                sb.append("    Annotations: ");
                StringJoiner annotationJoiner = new StringJoiner(", ");
                for (Annotation annotation : fieldAnnotations) {
                    annotationJoiner.add("@" + annotation.annotationType().getSimpleName());
                }
                sb.append(annotationJoiner).append("\n");
            }
        }
        sb.append("\n");
    }

    /**
     * Appends constructor information to the given StringBuilder.
     *
     * @param sb    the StringBuilder to append to
     * @param clazz the class to get information from
     */
    private static void appendConstructorInfo(StringBuilder sb, Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 0) {
            return;
        }

        appendSectionHeader(sb, "CONSTRUCTORS");

        for (Constructor<?> constructor : constructors) {
            sb.append(String.format("  %s %s(",
                    Modifier.toString(constructor.getModifiers()),
                    clazz.getSimpleName()));


            Parameter[] parameters = constructor.getParameters();
            StringJoiner paramJoiner = new StringJoiner(", ");
            for (Parameter parameter : parameters) {
                paramJoiner.add(parameter.getType().getSimpleName() + " " + parameter.getName());
            }
            sb.append(paramJoiner).append(")");


            Class<?>[] exceptionTypes = constructor.getExceptionTypes();
            if (exceptionTypes.length > 0) {
                sb.append(" throws ");
                StringJoiner exceptionJoiner = new StringJoiner(", ");
                for (Class<?> exceptionType : exceptionTypes) {
                    exceptionJoiner.add(exceptionType.getSimpleName());
                }
                sb.append(exceptionJoiner);
            }
            sb.append("\n");


            Annotation[] constructorAnnotations = constructor.getDeclaredAnnotations();
            if (constructorAnnotations.length > 0) {
                sb.append("    Annotations: ");
                StringJoiner annotationJoiner = new StringJoiner(", ");
                for (Annotation annotation : constructorAnnotations) {
                    annotationJoiner.add("@" + annotation.annotationType().getSimpleName());
                }
                sb.append(annotationJoiner).append("\n");
            }
        }
        sb.append("\n");
    }

    /**
     * Appends method information to the given StringBuilder.
     *
     * @param sb    the StringBuilder to append to
     * @param clazz the class to get information from
     */
    private static void appendMethodInfo(StringBuilder sb, Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        if (methods.length == 0) {
            return;
        }

        appendSectionHeader(sb, "METHODS");

        for (Method method : methods) {
            sb.append(String.format("  %s %s %s(",
                    Modifier.toString(method.getModifiers()),
                    method.getReturnType().getSimpleName(),
                    method.getName()));


            Parameter[] parameters = method.getParameters();
            StringJoiner paramJoiner = new StringJoiner(", ");
            for (Parameter parameter : parameters) {
                paramJoiner.add(parameter.getType().getSimpleName() + " " + parameter.getName());
            }
            sb.append(paramJoiner).append(")");


            Class<?>[] exceptionTypes = method.getExceptionTypes();
            if (exceptionTypes.length > 0) {
                sb.append(" throws ");
                StringJoiner exceptionJoiner = new StringJoiner(", ");
                for (Class<?> exceptionType : exceptionTypes) {
                    exceptionJoiner.add(exceptionType.getSimpleName());
                }
                sb.append(exceptionJoiner);
            }
            sb.append("\n");


            Annotation[] methodAnnotations = method.getDeclaredAnnotations();
            if (methodAnnotations.length > 0) {
                sb.append("    Annotations: ");
                StringJoiner annotationJoiner = new StringJoiner(", ");
                for (Annotation annotation : methodAnnotations) {
                    annotationJoiner.add("@" + annotation.annotationType().getSimpleName());
                }
                sb.append(annotationJoiner).append("\n");
            }
        }
        sb.append("\n");
    }

    /**
     * Appends inner class information to the given StringBuilder.
     *
     * @param sb    the StringBuilder to append to
     * @param clazz the class to get information from
     */
    private static void appendInnerClassInfo(StringBuilder sb, Class<?> clazz) {
        Class<?>[] innerClasses = clazz.getDeclaredClasses();
        if (innerClasses.length == 0) {
            return;
        }

        appendSectionHeader(sb, "INNER CLASSES");

        for (Class<?> innerClass : innerClasses) {
            sb.append(String.format("  %s %s\n",
                    Modifier.toString(innerClass.getModifiers()),
                    innerClass.getSimpleName()));
        }
        sb.append("\n");
    }

    /**
     * Formats an annotation value for display.
     *
     * @param value the value to format
     * @return a formatted string representation of the value
     */
    private static String formatAnnotationValue(Object value) {
        if (value instanceof Object[]) {
            return Arrays.stream((Object[]) value)
                    .map(Object::toString)
                    .collect(Collectors.joining(", ", "[", "]"));
        }
        if (value instanceof Class) {
            return ((Class<?>) value).getSimpleName() + ".class";
        }
        return value.toString();
    }
}
