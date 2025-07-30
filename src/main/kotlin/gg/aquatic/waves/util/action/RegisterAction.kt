package gg.aquatic.waves.util.action


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RegisterAction(
    val id: String
)