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

## Test

```bash
./gradlew :app:testDebugUnitTest
```

## Notes

Market data comes from CoinGecko's public API, so rate limits or network failures can affect refresh behavior.
