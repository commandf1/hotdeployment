package space.commandf1.hotdeployment.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarDiffUtil {

    @Getter
    @AllArgsConstructor
    public static class JarSnapshot {
        private final Map<String, byte[]> classNameToBytes;
        private final Map<String, byte[]> resourcePathToBytes;
    }

    @Getter
    @AllArgsConstructor
    public static class DiffResult {
        private final Map<String, byte[]> addedOrChangedClasses;
        private final Set<String> removedClasses;
        private final Map<String, byte[]> addedOrChangedResources;
        private final Set<String> removedResources;
    }

    public static @NotNull JarSnapshot readFromFile(@NotNull File file) {
        try (val fis = new FileInputStream(file)) {
            return readFromStream(fis);
        } catch (IOException e) {
            return new JarSnapshot(Collections.emptyMap(), Collections.emptyMap());
        }
    }

    public static @NotNull JarSnapshot readFromBytes(byte[] bytes) {
        return readFromStream(new ByteArrayInputStream(bytes));
    }

    @SneakyThrows
    public static @NotNull JarSnapshot readFromStream(@NotNull InputStream inputStream) {
        Map<String, byte[]> classes = new HashMap<>();
        Map<String, byte[]> resources = new HashMap<>();

        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                val name = entry.getName();
                byte[] data = readAllBytes(zis);

                if (name.endsWith(".class")) {
                    val className = name.substring(0, name.length() - 6).replace('/', '.');
                    classes.put(className, data);
                } else {
                    resources.put(name, data);
                }
            }
        }

        return new JarSnapshot(classes, resources);
    }

    public static @NotNull DiffResult diffAgainst(@NotNull JarSnapshot remote,
                                                  @NotNull Map<String, Class<?>> loadedClasses,
                                                  @NotNull ClassLoader resourceLoader) {
        val changedClasses = new HashMap<String, byte[]>();
        val removedClasses = new HashSet<String>();

        for (val entry : remote.getClassNameToBytes().entrySet()) {
            val className = entry.getKey();
            val newBytes = entry.getValue();
            val existing = loadedClasses.get(className);
            if (existing == null) {
                changedClasses.put(className, newBytes);
            } else {
                val existingBytes = getClassBytes(existing);
                if (!Arrays.equals(hash(newBytes), hash(existingBytes))) {
                    changedClasses.put(className, newBytes);
                }
            }
        }

        for (val existingName : loadedClasses.keySet()) {
            if (!remote.getClassNameToBytes().containsKey(existingName)) {
                removedClasses.add(existingName);
            }
        }

        Map<String, byte[]> changedResources = new HashMap<>();
        Set<String> removedResources = new HashSet<>();

        for (val entry : remote.getResourcePathToBytes().entrySet()) {
            val path = entry.getKey();
            val newBytes = entry.getValue();
            val oldBytes = readResource(resourceLoader, path).orElse(null);
            if (oldBytes == null || !Arrays.equals(hash(newBytes), hash(oldBytes))) {
                changedResources.put(path, newBytes);
            }
        }

        return new DiffResult(changedClasses, removedClasses, changedResources, removedResources);
    }

    private static Optional<byte[]> readResource(ClassLoader loader, String path) {
        try (val is = loader.getResourceAsStream(path)) {
            if (is == null) return Optional.empty();
            return Optional.of(readAllBytes(is));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        val baos = new ByteArrayOutputStream();
        val buffer = new byte[8192];
        int read;
        while ((read = is.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        return baos.toByteArray();
    }

    private static byte[] getClassBytes(Class<?> clazz) {
        try {
            val field = Class.class.getDeclaredField("classData");
            field.setAccessible(true);
            return (byte[]) field.get(clazz);
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private static byte[] hash(byte[] data) {
        try {
            val md = MessageDigest.getInstance("SHA-256");
            return md.digest(data);
        } catch (Exception e) {
            return data;
        }
    }
}


