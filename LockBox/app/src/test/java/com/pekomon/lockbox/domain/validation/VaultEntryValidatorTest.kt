package com.pekomon.lockbox.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VaultEntryValidatorTest {
    @Test
    fun validLoginDraftPasses() {
        val result = VaultEntryValidator.validate(
            VaultEntryDraft.Login(
                title = "GitHub",
                username = "octocat",
                password = "correct horse battery staple",
                url = "https://github.com",
                notes = "Recovery codes stored separately.",
            ),
        )

        assertTrue(result.isValid)
        assertEquals(emptyList<ValidationError>(), result.errors)
    }

    @Test
    fun invalidLoginDraftReportsFieldSpecificErrors() {
        val result = VaultEntryValidator.validate(
            VaultEntryDraft.Login(
                title = " ",
                username = "",
                password = "",
                url = "github.com",
                notes = "",
            ),
        )

        assertFalse(result.isValid)
        assertInvalidFields(
            result,
            ValidationField.Title,
            ValidationField.Username,
            ValidationField.Password,
            ValidationField.Url,
        )
    }

    @Test
    fun validSecureNoteDraftPasses() {
        val result = VaultEntryValidator.validate(
            VaultEntryDraft.SecureNote(
                title = "Passport",
                body = "Stored in the safe.",
            ),
        )

        assertTrue(result.isValid)
    }

    @Test
    fun invalidSecureNoteDraftReportsTitleAndBodyErrors() {
        val result = VaultEntryValidator.validate(
            VaultEntryDraft.SecureNote(
                title = "",
                body = " ",
            ),
        )

        assertFalse(result.isValid)
        assertInvalidFields(
            result,
            ValidationField.Title,
            ValidationField.NoteBody,
        )
    }

    @Test
    fun validCardDraftPasses() {
        val result = VaultEntryValidator.validate(
            VaultEntryDraft.Card(
                title = "Travel card",
                cardholder = "Pekka",
                number = "4111 1111 1111 1111",
                expiry = "09/29",
                securityCode = "123",
                notes = "Personal travel card.",
            ),
        )

        assertTrue(result.isValid)
    }

    @Test
    fun invalidCardDraftReportsFieldSpecificErrors() {
        val result = VaultEntryValidator.validate(
            VaultEntryDraft.Card(
                title = " ",
                cardholder = "",
                number = "123",
                expiry = "9/2029",
                securityCode = "12",
                notes = "",
            ),
        )

        assertFalse(result.isValid)
        assertInvalidFields(
            result,
            ValidationField.Title,
            ValidationField.Cardholder,
            ValidationField.CardNumber,
            ValidationField.CardExpiry,
            ValidationField.CardSecurityCode,
        )
    }

    private fun assertInvalidFields(
        result: ValidationResult,
        vararg fields: ValidationField,
    ) {
        assertEquals(
            fields.toSet(),
            result.errors.mapTo(linkedSetOf()) { it.field },
        )
        fields.forEach { field ->
            assertTrue(result.errorsFor(field).isNotEmpty())
        }
    }
}
