package de.adschmidt.sunrisesunset.model

enum class PreferenceDataType {
    STRING, COLOR, BOOLEAN, LOCATION
}

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class PreferenceMeta(
    val key: String,
    val dataType: PreferenceDataType,
    val categoryKey: String) {
}
