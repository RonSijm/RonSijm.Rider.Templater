package ronsijm.templater.handlers


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RegisterHandler(
    val module: String,
    val description: String = "",
    val example: String = "",
    val pure: Boolean = false,
    val barrier: Boolean = false
)
