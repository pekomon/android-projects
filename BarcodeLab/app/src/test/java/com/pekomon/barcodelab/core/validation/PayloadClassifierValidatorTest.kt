package com.pekomon.barcodelab.core.validation

import com.pekomon.barcodelab.domain.model.PayloadKind
import com.pekomon.barcodelab.domain.model.BarcodeFormat
import com.pekomon.barcodelab.domain.model.ValidationStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class PayloadClassifierValidatorTest {
    private val classifier = PayloadClassifier()
    private val validator = PayloadValidator()

    @Test
    fun classifiesAndValidatesUrl() {
        val payload = classifier.classify("https://example.com/ticket/123")

        assertEquals(PayloadKind.Url, payload.kind)
        assertEquals(ValidationStatus.Valid, validator.validate(payload).status)
    }

    @Test
    fun classifiesAndValidatesEan13ProductCode() {
        val payload = classifier.classify(
            rawValue = "6423800161605",
            sourceFormat = BarcodeFormat.Ean13,
        )

        assertEquals(PayloadKind.ProductCode, payload.kind)
        assertEquals(ValidationStatus.Valid, validator.validate(payload).status)
    }

    @Test
    fun rejectsProductCodeWithInvalidCheckDigit() {
        val payload = classifier.classify(
            rawValue = "6423800161600",
            sourceFormat = BarcodeFormat.Ean13,
        )

        assertEquals(PayloadKind.ProductCode, payload.kind)
        assertEquals(ValidationStatus.Invalid, validator.validate(payload).status)
    }

    @Test
    fun classifiesLogisticsCodeAsReadableWarning() {
        val payload = classifier.classify(
            rawValue = "660002597000030000329320260610191817",
            sourceFormat = BarcodeFormat.Code128,
        )

        assertEquals(PayloadKind.LogisticsCode, payload.kind)
        assertEquals(ValidationStatus.Warning, validator.validate(payload).status)
    }

    @Test
    fun rejectsInvalidUrl() {
        val payload = classifier.classify("https://")

        assertEquals(PayloadKind.Url, payload.kind)
        assertEquals(ValidationStatus.Invalid, validator.validate(payload).status)
    }

    @Test
    fun classifiesAndValidatesEmail() {
        val payload = classifier.classify("mailto:person@example.com")

        assertEquals(PayloadKind.Email, payload.kind)
        assertEquals(ValidationStatus.Valid, validator.validate(payload).status)
    }

    @Test
    fun classifiesAndValidatesPhone() {
        val payload = classifier.classify("tel:+358 40 123 4567")

        assertEquals(PayloadKind.Phone, payload.kind)
        assertEquals(ValidationStatus.Valid, validator.validate(payload).status)
    }

    @Test
    fun classifiesAndValidatesSms() {
        val payload = classifier.classify("SMSTO:+358401234567:hello")

        assertEquals(PayloadKind.Sms, payload.kind)
        assertEquals(ValidationStatus.Valid, validator.validate(payload).status)
    }

    @Test
    fun classifiesAndValidatesWifi() {
        val payload = classifier.classify("WIFI:T:WPA;S:DemoNetwork;P:secret;;")

        assertEquals(PayloadKind.Wifi, payload.kind)
        assertEquals(ValidationStatus.Valid, validator.validate(payload).status)
    }

    @Test
    fun classifiesAndValidatesVCard() {
        val payload = classifier.classify("BEGIN:VCARD\nVERSION:3.0\nFN:Ada Lovelace\nEND:VCARD")

        assertEquals(PayloadKind.VCard, payload.kind)
        assertEquals(ValidationStatus.Valid, validator.validate(payload).status)
    }

    @Test
    fun classifiesAndValidatesCalendarEvent() {
        val payload = classifier.classify("BEGIN:VEVENT\nSUMMARY:Demo\nDTSTART:20260907T120000Z\nEND:VEVENT")

        assertEquals(PayloadKind.Calendar, payload.kind)
        assertEquals(ValidationStatus.Valid, validator.validate(payload).status)
    }

    @Test
    fun classifiesAndValidatesGeo() {
        val payload = classifier.classify("geo:60.1699,24.9384")

        assertEquals(PayloadKind.Geo, payload.kind)
        assertEquals(ValidationStatus.Valid, validator.validate(payload).status)
    }

    @Test
    fun plainTextIsReadableButUnstructured() {
        val payload = classifier.classify("Boarding pass desk opens at 12")

        assertEquals(PayloadKind.PlainText, payload.kind)
        assertEquals(ValidationStatus.Warning, validator.validate(payload).status)
    }

    @Test
    fun unknownStructuredPayloadIsUnsupported() {
        val payload = classifier.classify("CUSTOM:abc;VALUE:123")

        assertEquals(PayloadKind.UnknownStructured, payload.kind)
        assertEquals(ValidationStatus.Unsupported, validator.validate(payload).status)
    }
}
