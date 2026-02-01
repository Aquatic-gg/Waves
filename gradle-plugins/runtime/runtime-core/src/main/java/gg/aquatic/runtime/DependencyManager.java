package gg.aquatic.runtime;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class DependencyManager {
    private final InternalResolver resolver;
    private final Path relocatedDir;

    private DependencyManager(Path baseDir) throws Exception {
        this.relocatedDir = baseDir.resolve("relocated");
        Files.createDirectories(relocatedDir);
        this.resolver = new InternalResolver(baseDir);
    }

    public static DependencyManager create(Path baseDir) throws Exception {
        return new DependencyManager(baseDir);
    }

    public DependencyManager loadSecrets(Path envPath) {
        resolver.loadSecrets(envPath);
        return this;
    }

    public void process(InputStream manifestStream, Consumer<Path> jarConsumer) throws Exception {
        String manifest = new String(manifestStream.readAllBytes(), StandardCharsets.UTF_8);

        Path asm = resolver.downloadTool("org.ow2.asm", "asm", "9.7");
        Path asmCommons = resolver.downloadTool("org.ow2.asm", "asm-commons", "9.7");
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
            if (!Files.exists(output)) {
                relocator.relocate(jar, output);
            }
            jarConsumer.accept(output);
        }
    }
}