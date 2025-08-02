package gg.aquatic.waves.util.reflection

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.net.URLClassLoader

object AnnotationLookup {

    inline fun <reified T : Annotation> lookup(plugin: Any, pckg: String): Set<Class<*>> {
        val classLoader = URLClassLoader(ClasspathHelper.forPackage(pckg,plugin.javaClass.classLoader).toTypedArray(), plugin.javaClass.classLoader)
        val reflections = Reflections(
            ConfigurationBuilder()
                .setUrls(
                    ClasspathHelper.forPackage(pckg, classLoader)
                ).setScanners(
                    Scanners.TypesAnnotated, Scanners.SubTypes
                ).setClassLoaders(arrayOf(classLoader))
        )
        val annotatedClasses = reflections.getTypesAnnotatedWith(T::class.java)
        classLoader.close()
        return annotatedClasses
    }

}