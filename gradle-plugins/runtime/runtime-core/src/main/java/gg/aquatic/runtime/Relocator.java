package gg.aquatic.runtime;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.*;

public class Relocator {
    private final Map<String, String> prefixMappings = new HashMap<>();
    private final Map<String, String> classMappings = new HashMap<>();
    private final List<Map.Entry<String, String>> orderedPrefixMappings = new ArrayList<>();
    private final URLClassLoader toolLoader;

    public Relocator(Path asmJar, Path asmCommonsJar) throws Exception {
        this.toolLoader = new URLClassLoader(new URL[]{asmJar.toUri().toURL(), asmCommonsJar.toUri().toURL()}, null);
    }

    public void addMapping(String from, String to) {
        String f = from.replace('.', '/');
        String t = to.replace('.', '/');
        if (!f.endsWith("/")) f += "/";
        if (!t.endsWith("/")) t += "/";

        prefixMappings.put(f, t);
        rebuildPrefixCache();
    }

    public void prepareClassMappings(List<Path> jars) throws Exception {
        classMappings.clear();
        for (Path jar : jars) {
            try (JarInputStream jin = new JarInputStream(new BufferedInputStream(Files.newInputStream(jar)))) {
                JarEntry entry;
                while ((entry = jin.getNextJarEntry()) != null) {
                    String name = entry.getName();
                    if (!name.endsWith(".class")) continue;

                    String internalName = name.substring(0, name.length() - 6);
                    if (internalName.startsWith("META-INF/versions/")) {
                        int verEnd = internalName.indexOf('/', 18);
                        if (verEnd != -1) internalName = internalName.substring(verEnd + 1);
                    }

                    String mapped = mapInternalName(internalName);
                    if (!mapped.equals(internalName)) {
                        classMappings.put(internalName, mapped);
                    }
                }
            }
        }
    }

    public void relocate(Path input, Path output) throws Exception {
        Class<?> classReaderClass = toolLoader.loadClass("org.objectweb.asm.ClassReader");
        Class<?> classWriterClass = toolLoader.loadClass("org.objectweb.asm.ClassWriter");
        Class<?> classVisitorClass = toolLoader.loadClass("org.objectweb.asm.ClassVisitor");
        Class<?> remapClass = toolLoader.loadClass("org.objectweb.asm.commons.ClassRemapper");
        Class<?> remapperClass = toolLoader.loadClass("org.objectweb.asm.commons.Remapper");
        Class<?> simpleRemapperClass = toolLoader.loadClass("org.objectweb.asm.commons.SimpleRemapper");

        Object remapper = simpleRemapperClass.getConstructor(Map.class).newInstance(classMappings);
        Method mapMethod = remapperClass.getMethod("map", String.class);

        try (JarInputStream jin = new JarInputStream(new BufferedInputStream(Files.newInputStream(input)))) {
            Manifest manifest = jin.getManifest();
            if (manifest == null) manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().put(new Attributes.Name("Multi-Release"), "true");

            try (JarOutputStream jout = new JarOutputStream(new BufferedOutputStream(Files.newOutputStream(output)), manifest)) {
                JarEntry entry;
                while ((entry = jin.getNextJarEntry()) != null) {
                    String name = entry.getName();
                    if (entry.isDirectory() || name.equalsIgnoreCase("META-INF/MANIFEST.MF") || name.toUpperCase().startsWith("META-INF/SIG-")) continue;

                    byte[] data = jin.readAllBytes();

                    if (name.endsWith(".class")) {
                        // 1. Initialize Reader
                        Object reader = classReaderClass.getConstructor(byte[].class).newInstance((Object) data);

                        // 2. Initialize Writer in "Copying Mode"
                        // Using (reader, 0) is CRITICAL. It prevents ASM from calling getCommonSuperClass,
                        // which avoids the TypeNotPresentException/ClassNotFoundException during relocation.
                        Constructor<?> writerCons = classWriterClass.getConstructor(classReaderClass, int.class);
                        Object writer = writerCons.newInstance(reader, 0);

                        // 3. Setup Remapper
                        Constructor<?> remapConstructor = remapClass.getConstructor(classVisitorClass, remapperClass);
                        Object visitor = remapConstructor.newInstance(writer, remapper);

                        // 4. Transform
                        Method accept = classReaderClass.getMethod("accept", classVisitorClass, int.class);
                        accept.invoke(reader, visitor, 0);
                        data = (byte[]) classWriterClass.getMethod("toByteArray").invoke(writer);

                        // 5. Handle Multi-Release Path mapping
                        String prefix = "";
                        String internalName = name.substring(0, name.length() - 6);
                        if (internalName.startsWith("META-INF/versions/")) {
                            int verEnd = internalName.indexOf('/', 18);
                            if (verEnd != -1) {
                                prefix = name.substring(0, verEnd + 1);
                                internalName = internalName.substring(verEnd + 1);
                            }
                        }

                        String mappedInternalName = (String) mapMethod.invoke(remapper, internalName);
                        name = prefix + (mappedInternalName != null ? mappedInternalName : internalName) + ".class";
                    } else if (name.startsWith("META-INF/services/")) {
                        String content = new String(data, StandardCharsets.UTF_8);
                        for (Map.Entry<String, String> m : orderedPrefixMappings) {
                            String f = m.getKey().replace('/', '.');
                            String t = m.getValue().replace('/', '.');
                            content = content.replace(f, t);
                        }
                        String serviceName = name.substring("META-INF/services/".length());
                        String mappedServiceName = mapDotName(serviceName);
                        if (!mappedServiceName.equals(serviceName)) {
                            name = "META-INF/services/" + mappedServiceName;
                        }
                        data = content.getBytes(StandardCharsets.UTF_8);
                    } else {
                        name = mapResourceName(name);
                    }

                    try {
                        jout.putNextEntry(new JarEntry(name));
                        jout.write(data);
                        jout.closeEntry();
                    } catch (java.util.zip.ZipException ignored) {}
                }
            }
        }
    }

    private String mapInternalName(String internalName) {
        for (Map.Entry<String, String> m : orderedPrefixMappings) {
            String from = m.getKey();
            if (internalName.startsWith(from)) {
                return m.getValue() + internalName.substring(from.length());
            }
        }
        return internalName;
    }

    private String mapDotName(String name) {
        for (Map.Entry<String, String> m : orderedPrefixMappings) {
            String from = m.getKey().replace('/', '.');
            if (name.startsWith(from)) {
                return m.getValue().replace('/', '.') + name.substring(from.length());
            }
        }
        return name;
    }

    private String mapResourceName(String name) {
        for (Map.Entry<String, String> m : orderedPrefixMappings) {
            String from = m.getKey();
            if (name.startsWith(from)) {
                return m.getValue() + name.substring(from.length());
            }
        }
        return name;
    }

    private void rebuildPrefixCache() {
        orderedPrefixMappings.clear();
        orderedPrefixMappings.addAll(prefixMappings.entrySet());
        orderedPrefixMappings.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));
    }
}
