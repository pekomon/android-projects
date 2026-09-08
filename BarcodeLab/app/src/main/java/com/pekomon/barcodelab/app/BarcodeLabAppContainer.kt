package com.pekomon.barcodelab.app

import com.pekomon.barcodelab.core.validation.PayloadClassifier
import com.pekomon.barcodelab.core.validation.PayloadValidator

class BarcodeLabAppContainer {
    val payloadClassifier = PayloadClassifier()
    val payloadValidator = PayloadValidator()
}
