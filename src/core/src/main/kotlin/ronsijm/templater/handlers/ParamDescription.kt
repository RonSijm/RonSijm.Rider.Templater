package ronsijm.templater.handlers


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ParamDescription(val value: String)
