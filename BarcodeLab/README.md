# Barcode Lab

Barcode Lab is an Android barcode-scanning showcase app.

The MVP focuses on the core mobile work behind the role requirements:

- CameraX live preview
- continuous `ImageAnalysis`
- ML Kit Barcode Scanning
- QR Code, PDF417, Aztec, and Data Matrix recognition
- pause/resume behavior after detection
- deterministic local payload classification and validation
- offline-first behavior with no backend dependency

## Technical Direction

Barcode Lab uses CameraX instead of Camera2 directly. CameraX keeps lifecycle binding, preview, image analysis, rotation, and device differences manageable while still exposing the control needed for real-time scanning.

ML Kit Barcode Scanning is used directly instead of Google Code Scanner because this app needs a custom camera UI and continuous analysis.

The first backend-related scope is intentionally architectural: scanner output is mapped into app-owned models, then payload validation and future verification happen outside the camera and ML Kit layers.

## Build

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

## Full Local Verification

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:compileDebugAndroidTestKotlin
```

## Current Scope

Implemented:

- app shell under `com.pekomon.barcodelab`
- camera permission flow
- CameraX preview and real-time `ImageAnalysis`
- ML Kit barcode analyzer with explicit 2D formats
- result sheet with copy and resume actions
- payload classifier and validator
- unit tests for validation and scanner state
- CI workflow

Planned:

- richer low-light/focus behavior
- stronger duplicate suppression and performance notes
- fake verifier interface
- optional backend adapter demo
- screenshots and portfolio polish
