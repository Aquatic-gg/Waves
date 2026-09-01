package gg.aquatic.waves.dependency;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import xyz.kyngs.librarian.PaperLibraryManager;

@SuppressWarnings("unused")
public class DependencyLoader implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        try {
            var libraryManager = new PaperLibraryManager();
            libraryManager.configureFromJSON();
            libraryManager.classloader(classpathBuilder);
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void handleError(Exception e) {
        System.err.println("CRITICAL: Dependency loading failed!");
        e.printStackTrace();
        System.exit(1);
    }
}
