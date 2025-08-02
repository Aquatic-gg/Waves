package gg.aquatic.waves.util.action


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Suppress("unused")
annotation class RegisterAction(
    val id: String,
    vararg val aliases: String = []
)