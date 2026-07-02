# SnapReceipt

Android showcase app in progress.

## Status

The app now supports the core local-first loop:

- import receipt images from Photo Picker or file picker
- run on-device OCR with ML Kit
- parse merchant, date, total, and currency heuristically
- review and edit parsed fields before save
- save receipts into Room with stored local image copies
- browse saved receipts and open a detail screen
- delete receipts and clean up stored image files
- persist local defaults for fallback currency and JPEG quality with DataStore

Still missing:

- CameraX capture flow
- Compose UI tests
- showcase screenshots and final README assets
- final visual polish and manual QA

## Architecture

- Kotlin + Jetpack Compose
- single-activity navigation with manual `ViewModel` factories
- ML Kit Text Recognition for local OCR
- heuristic parser for receipt fields
- Room for receipt metadata
- app-private file storage for saved images
- DataStore Preferences for settings

## Build

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:compileDebugAndroidTestKotlin
```
