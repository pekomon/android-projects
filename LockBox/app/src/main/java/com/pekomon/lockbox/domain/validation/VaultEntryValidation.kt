package com.pekomon.lockbox.domain.validation

enum class ValidationField {
    Title,
    Username,
    Password,
    Url,
    NoteBody,
    Cardholder,
    CardNumber,
    CardExpiry,
    CardSecurityCode,
}

data class ValidationError(
    val field: ValidationField,
    val message: String,
)

data class ValidationResult(
    val errors: List<ValidationError>,
) {
    val isValid: Boolean = errors.isEmpty()

    fun errorsFor(field: ValidationField): List<ValidationError> {
        return errors.filter { it.field == field }
    }
}
