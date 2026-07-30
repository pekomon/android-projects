# LockBox

`LockBox` is a local-first Android vault showcase app.

The implementation is intentionally zero-network. V1 will use Android biometric
authentication for the app session and Android Keystore-backed AES-GCM
encryption for persisted secret payloads.

## Build

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:compileDebugAndroidTestKotlin
```

## Security Notes

- The app must always cold-start locked.
- The Android manifest intentionally declares no Internet permission.
- Android backup and device-transfer extraction are disabled for vault data.
- This is a showcase local vault, not a certified password manager.
