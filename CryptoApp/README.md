# CryptoApp

`CryptoApp` is a polished Android showcase app for tracking a focused crypto watchlist, monitoring favorites, and managing a local portfolio with transaction-backed holdings.

It is intentionally product-sized rather than exchange-sized: the app prioritizes fast scanning, reliable state handling, and clear portfolio feedback over advanced trading or charting features.

## Product Summary

The app is built around four core screens:

- `Watchlist` for the selected assets you want to monitor every day
- `Favorites` for a tighter quick-monitoring subset
- `Portfolio` for local holdings, cost basis, allocation, and return
- `Settings` for currency, shared sorting, and watchlist selection

The result is a compact crypto companion app that demonstrates:

- single-activity Jetpack Compose app structure
- repository-backed data loading with explicit market error modeling
- local persistence with DataStore
- transaction-derived portfolio state instead of naive balance storage
- Compose UI testing around critical screen states

## Highlights

- Watchlist search, shared sorting, and pull-to-refresh
- Favorites list with quick add into portfolio flows
- Portfolio metrics for value, invested amount, return, allocation, and transaction history
- Explicit handling for stale prices, partial market data, and rate limiting
- Persistent settings for selected currency, favorites, watchlist, and holdings
- Custom launcher icon and non-default visual styling

## Screens

### Watchlist

- Shows the selected set of tracked assets
- Supports searching by asset name or symbol
- Surfaces last updated time, stale pricing, and retry actions
- Allows quick-add into portfolio directly from the list

### Favorites

- Keeps a smaller subset for repeat monitoring
- Reuses the same sorting model as Watchlist
- Preserves pull-to-refresh and explicit market status messaging

### Portfolio

- Uses transaction-backed holdings as the source of truth
- Calculates invested amount, current value, P/L, and allocation per asset
- Supports editing holdings, removing holdings, and inspecting transaction history
- Avoids misleading totals when live prices are missing

### Settings

- Switches between `EUR` and `USD`
- Controls shared sort order for Watchlist and Favorites
- Manages the selected watchlist asset set
- Surfaces whether asset metadata came from the live API, local cache, or built-in defaults

## Architecture Notes

The project is intentionally organized into clearer layers instead of keeping everything inside one broad UI module:

- `data/`
  Market loading, persistence, DTO mapping, and API integration
- `domain/`
  Market sorting, selection sanitization, portfolio calculations, validation, and models
- `ui/`
  Screen composables, state coordinators, app shell, dialogs, and shared components
- `core/`
  Formatting helpers and logging utilities

Notable implementation choices:

- Remote market failures are mapped into typed domain errors instead of collapsing into silent empty states.
- Portfolio holdings are derived from transactions, which makes add/edit/remove behavior more defensible than storing only a mutable balance.
- Shared UI state is shaped through focused collaborators such as bootstrap, market-refresh, and feature state owners instead of pushing every workflow into one screen composable.

## Reliability and UX Decisions

- Market refresh preserves last known prices when only part of the response fails.
- Rate limiting, offline failures, and stale data are surfaced explicitly in the UI.
- Asset selection is sanitized against the available asset catalog so invalid saved state does not leak into the main screens.
- Portfolio summaries exclude unpriced assets from misleading live-value totals and explain why.

## Tech Stack

- Kotlin
- Jetpack Compose
- Navigation Compose
- Material 3
- AndroidX DataStore
- Retrofit
- Kotlinx Serialization
- CoinGecko market data API

## Build

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:assembleDebug
```

### Optional CoinGecko Demo API Key

CryptoApp can call CoinGecko without a key, but keyless requests are more likely to be rate limited. To use a free CoinGecko Demo API key, add this to `local.properties`:

```properties
COINGECKO_DEMO_API_KEY=your_key_here
```

`local.properties` is ignored by git, so API keys should never be committed.

## Test

```bash
./gradlew :app:testDebugUnitTest
```

Compose screen-state coverage lives under `app/src/androidTest` and focuses on the primary empty, content, error, and destructive-action flows.

## Scope

This app is intentionally a local companion app. It does not include:

- exchange integration
- authentication
- server sync
- tax reporting
- advanced charting

## Tradeoffs

- The app uses CoinGecko market data instead of broker-specific integrations, which keeps setup simple but limits portfolio realism.
- Without a configured CoinGecko Demo API key, public unauthenticated requests may be rate limited more often.
- The app is optimized for a clean local showcase flow rather than full investment-account reconciliation.
