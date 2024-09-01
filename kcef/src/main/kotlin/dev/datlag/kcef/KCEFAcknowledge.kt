package dev.datlag.kcef

@RequiresOptIn(message = "This API may be library and application breaking. Only use it if you fully understand what you are doing.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class KCEFAcknowledge
