# SnapReceipt

Android showcase app in progress.

## Status

Scaffold created. Product shell replaces the default Android Studio template. Core receipt capture, OCR, parsing, and persistence flows are still to be implemented.

## Planned Scope

- import or capture receipt images
- run on-device OCR
- review and correct parsed merchant, date, total, and currency
- save receipts locally with image previews
- browse saved receipts and inspect details

## Build

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```
