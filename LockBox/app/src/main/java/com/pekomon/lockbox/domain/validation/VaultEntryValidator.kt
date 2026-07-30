package com.pekomon.lockbox.domain.validation

object VaultEntryValidator {
    fun validate(draft: VaultEntryDraft): ValidationResult {
        val errors = buildList {
            if (draft.title.isBlank()) {
                add(ValidationError(ValidationField.Title, "Title is required."))
            }

            when (draft) {
                is VaultEntryDraft.Login -> validateLogin(draft)
                is VaultEntryDraft.SecureNote -> validateSecureNote(draft)
                is VaultEntryDraft.Card -> validateCard(draft)
            }.also(::addAll)
        }

        return ValidationResult(errors)
    }

    private fun validateLogin(draft: VaultEntryDraft.Login): List<ValidationError> {
        return buildList {
            if (draft.username.isBlank()) {
                add(ValidationError(ValidationField.Username, "Username is required."))
            }
            if (draft.password.isBlank()) {
                add(ValidationError(ValidationField.Password, "Password is required."))
            }
            if (draft.url.isNotBlank() && !draft.url.isWebUrl()) {
                add(ValidationError(ValidationField.Url, "URL must start with http:// or https://."))
            }
        }
    }

    private fun validateSecureNote(draft: VaultEntryDraft.SecureNote): List<ValidationError> {
        return buildList {
            if (draft.body.isBlank()) {
                add(ValidationError(ValidationField.NoteBody, "Note body is required."))
            }
        }
    }

    private fun validateCard(draft: VaultEntryDraft.Card): List<ValidationError> {
        return buildList {
            if (draft.cardholder.isBlank()) {
                add(ValidationError(ValidationField.Cardholder, "Cardholder is required."))
            }
            if (!draft.number.onlyDigits().hasLengthIn(12..19)) {
                add(ValidationError(ValidationField.CardNumber, "Card number must contain 12 to 19 digits."))
            }
            if (!draft.expiry.matches(Regex("""\d{2}/\d{2}"""))) {
                add(ValidationError(ValidationField.CardExpiry, "Expiry must use MM/YY."))
            }
            if (!draft.securityCode.onlyDigits().hasLengthIn(3..4)) {
                add(ValidationError(ValidationField.CardSecurityCode, "Security code must contain 3 or 4 digits."))
            }
        }
    }

    private fun String.isWebUrl(): Boolean {
        return startsWith("https://") || startsWith("http://")
    }

    private fun String.onlyDigits(): String {
        return filter { it.isDigit() }
    }

    private fun String.hasLengthIn(range: IntRange): Boolean {
        return length in range
    }
}
