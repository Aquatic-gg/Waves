@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Suppress("unused")
annotation class RegisterRequirement(
    val id: String,
    vararg val aliases: String = [],
)