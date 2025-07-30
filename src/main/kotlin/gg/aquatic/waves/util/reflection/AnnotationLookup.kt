package gg.aquatic.waves.util.reflection

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder

object AnnotationLookup {

    inline fun <reified T: Annotation> lookup(pckg: String): Set<Class<*>> {
        val reflections = Reflections(
            ConfigurationBuilder()
                .setUrls(
                    ClasspathHelper.forPackage(pckg)
                ).setScanners(
                    Scanners.TypesAnnotated, Scanners.SubTypes
                )
        )
        val annotatedClasses = reflections.getTypesAnnotatedWith(T::class.java)
        return annotatedClasses
    }

}