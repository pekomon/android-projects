# CryptoApp

CryptoApp is a small Android showcase app for tracking a crypto watchlist, favorites, and a local portfolio. It uses CoinGecko market data, Jetpack Compose, DataStore persistence, and a simple transaction-backed portfolio model.

## Features

- Watchlist of selected crypto assets
- Favorites for quick monitoring
- Portfolio holdings with transaction history
- EUR/USD display currency
- Shared sorting across Watchlist and Favorites
- Local persistence for settings, watchlist, favorites, and holdings

## Scope

This app is intentionally a local companion app. It does not include exchange integration, authentication, server sync, tax reporting, or advanced charting.

## Build

```bash
./gradlew :app:compileDebugKotlin
```

### Optional CoinGecko Demo API Key

CryptoApp can call CoinGecko without a key, but keyless requests are more likely to be rate limited. To use a free CoinGecko Demo API key, add this to `local.properties`:

```properties
COINGECKO_DEMO_API_KEY=your_key_here
```

`local.properties` is ignored by git, so do not commit API keys.

## Test

```bash
./gradlew :app:testDebugUnitTest
```

## Notes

Market data comes from CoinGecko. If no Demo API key is configured, the app uses keyless public requests and may be rate limited more often.
