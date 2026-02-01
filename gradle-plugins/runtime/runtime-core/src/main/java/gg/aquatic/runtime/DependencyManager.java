package gg.aquatic.runtime;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * DependencyManager is a utility class designed to handle the resolution and relocation
 * of dependencies based on a manifest file. It provides methods for managing secrets,
 * processing dependencies, and applying relocation mappings to JAR files. This class
 * operates primarily within a specified base directory and ensures that the necessary
 * operations are performed, including the downloading of tools and the relocation of JARs.
 *
 * The class offers a builder-style approach for loading secrets and provides functionality
 * to resolve dependencies and apply relocations defined in a manifest file, ultimately
 * processing each relocated JAR file using a provided consumer.
 */
@SuppressWarnings("unused")
public class DependencyManager {
    private static final String DEFAULT_ASM_VERSION = "9.9.1";
    private final InternalResolver resolver;
    private final Path relocatedDir;

    private DependencyManager(Path baseDir) throws Exception {
        this.relocatedDir = baseDir.resolve("relocated");
        Files.createDirectories(relocatedDir);
        this.resolver = new InternalResolver(baseDir);
    }

    /**
     * Creates and initializes a new instance of {@code DependencyManager} with the specified base directory.
     *
     * @param baseDir the base directory to be used for dependency management. This path is utilized for
     *                resolving and storing dependencies and related operations.
     * @return a new instance of {@code DependencyManager} configured with the specified base directory.
     * @throws Exception if an error occurs while initializing the {@code DependencyManager}, including
     *                   issues related to invalid paths or directory creation failures.
     */
    public static DependencyManager create(Path baseDir) throws Exception {
        return new DependencyManager(baseDir);
    }

    /**
     * Loads secrets from the specified file and makes them available for internal use.
     * The secrets are key-value pairs defined in the file, with each pair delimited by an '=' character.
     * Lines starting with '#' or empty lines are ignored.
     *
     * @param envPath the path to the file containing the secrets. If the file does not exist, this method does nothing.
     * @return the current instance of {@code DependencyManager}.
     */
    public DependencyManager loadSecrets(Path envPath) {
        resolver.loadSecrets(envPath);
        return this;
    }

    /**
     * Processes the provided manifest stream to resolve dependencies, apply relocations,
     * and invoke the given consumer for each resulting relocated JAR file.
     *
     * @param manifestStream the input stream containing the manifest data; it is assumed
     *                       to be in UTF-8 encoding and contains definitions for dependencies
     *                       and relocation rules.
     * @param jarConsumer    a consumer that is called with the path of each relocated
     *                       JAR file after processing is complete.
     * @throws Exception if an error occurs during processing, such as issues with downloading
     *                   tools, resolving dependencies, reading the manifest, creating
     *                   directories, or applying relocations.
     */
    public void process(InputStream manifestStream, Consumer<Path> jarConsumer) throws Exception {
        String manifest = new String(manifestStream.readAllBytes(), StandardCharsets.UTF_8);

        String asmVersion = resolveAsmVersion();
        Path asm = resolver.downloadTool("org.ow2.asm", "asm", asmVersion);
        Path asmCommons = resolver.downloadTool("org.ow2.asm", "asm-commons", asmVersion);
        Relocator relocator = new Relocator(asm, asmCommons);

        resolver.extractList(manifest, "relocations").forEach(relocJson -> {
            String from = resolver.extractValue(relocJson, "from");
            String to = resolver.extractValue(relocJson, "to");
            if (!from.isEmpty()) relocator.addMapping(from, to);
        });

        List<Path> downloaded = resolver.resolve(manifest);
        relocator.prepareClassMappings(downloaded);

        for (Path jar : downloaded) {
            Path output = relocatedDir.resolve("relocated-" + jar.getFileName());
            Files.deleteIfExists(output);
            relocator.relocate(jar, output);
            jarConsumer.accept(output);
        }
    }

    private String resolveAsmVersion() {
        String prop = System.getProperty("runtime.asm.version");
        if (prop != null && !prop.isBlank()) {
            return prop.trim();
        }

        String env = System.getenv("RUNTIME_ASM_VERSION");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }

        return DEFAULT_ASM_VERSION;
    }
}
