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

/**
 * The {@code Relocator} class provides functionality for relocating, remapping,
 * and processing class and package names in JAR files. It leverages the ASM library
 * for manipulation of bytecode and provides tools for transforming classes and
 * resources within JAR files based on configurable mappings.
 */
public class Relocator {
    private final Map<String, String> prefixMappings = new HashMap<>();
    private final Map<String, String> classMappings = new HashMap<>();
    private final List<Map.Entry<String, String>> orderedPrefixMappings = new ArrayList<>();
    private final URLClassLoader toolLoader;

    /**
     * Constructs a {@code Relocator} instance with the specified ASM jar files.
     * The ASM and ASM Commons JAR files are loaded into a {@link URLClassLoader}
     * to facilitate relocation operations.
     *
     * @param asmJar the path to the ASM JAR file used for class manipulation
     * @param asmCommonsJar the path to the ASM Commons JAR file for additional utilities
     * @throws Exception if an error occurs while initializing the class loader
     */
    public Relocator(Path asmJar, Path asmCommonsJar) throws Exception {
        this.toolLoader = new URLClassLoader(new URL[]{asmJar.toUri().toURL(), asmCommonsJar.toUri().toURL()}, null);
    }

    /**
     * Adds a mapping from one package or class name to another. This method processes
     * the provided source and destination names, ensuring that they are properly formatted
     * with forward slashes and trailing slashes as required.
     *
     * @param from the original package or class name, using dot notation (e.g., "com.example")
     * @param to the target package or class name, using dot notation (e.g., "org.example")
     */
    public void addMapping(String from, String to) {
        String f = from.replace('.', '/');
        String t = to.replace('.', '/');
        if (!f.endsWith("/")) f += "/";
        if (!t.endsWith("/")) t += "/";

        prefixMappings.put(f, t);
        rebuildPrefixCache();
    }

    /**
     * Prepares a mapping of class names based on the provided JAR files. This method processes the
     * class entries within the JARs and updates the class mappings to reflect any modifications
     * made during name remapping processes.
     *
     * @param jars a list of paths to JAR files that will be processed for class name mappings
     * @throws Exception if an error occurs during the reading of JAR files or the mapping process
     */
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

    /**
     * Relocates and processes the classes and resources from the input JAR file to the output JAR file
     * by applying remappings and transformations as defined in the class configuration.
     *
     * @param input the path to the input JAR file that contains the classes and resources to be relocated
     * @param output the path to the output JAR file where the relocated and transformed classes and resources will be written
     * @throws Exception if an error occurs during the relocation process, such as IO exceptions,
     *                   reflection issues, or JAR file handling errors
     */
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
                        Object reader = classReaderClass.getConstructor(byte[].class).newInstance((Object) data);

                        Constructor<?> writerCons = classWriterClass.getConstructor(classReaderClass, int.class);
                        Object writer = writerCons.newInstance(reader, 0);
                        Constructor<?> remapConstructor = remapClass.getConstructor(classVisitorClass, remapperClass);
                        Object visitor = remapConstructor.newInstance(writer, remapper);

                        Method accept = classReaderClass.getMethod("accept", classVisitorClass, int.class);
                        accept.invoke(reader, visitor, 0);
                        data = (byte[]) classWriterClass.getMethod("toByteArray").invoke(writer);

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
