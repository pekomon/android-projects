# LockBox

`LockBox` is a local-first Android vault showcase app.

The implementation is intentionally zero-network. V1 will use Android biometric
authentication for the app session and Android Keystore-backed AES-GCM
encryption for persisted secret payloads.

## Build

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:compileDebugAndroidTestKotlin
```

Run device/emulator Compose coverage with:

```bash
./gradlew :app:connectedDebugAndroidTest
```

## Manual QA

Use an emulator or device with a screen lock configured.

1. Cold start the app and confirm the lock screen appears before vault content.
2. Tap Unlock and complete the system prompt; the app should show the empty vault.
3. Relaunch the app and cancel the system prompt; the app should stay locked and show a canceled message.
4. Unlock, background the app, then reopen it; the app should return to the locked screen.
5. Test on a profile without an enrolled credential; the unlock button should be disabled with the unavailable explanation.

## Security Notes

- The app must always cold-start locked.
- Backgrounding the process relocks the in-memory app session.
- V1 uses `BIOMETRIC_STRONG | DEVICE_CREDENTIAL` for the unlock prompt.
- The Android manifest intentionally declares no Internet permission.
- Android backup and device-transfer extraction are disabled for vault data.
- This is a showcase local vault, not a certified password manager.
