# Memory Game

Memory Game is an earlier Android card-matching project built with Kotlin and Jetpack Compose.

Status: earlier/smaller project. It is kept in the portfolio as a compact example of Compose state, animation, Room persistence, Hilt setup, sound effects, and tests. It has not been polished to the same presentation standard as the featured showcase apps.

## Product Overview

The app presents a shuffled grid of memory cards. The player flips two cards at a time, matched pairs stay revealed, mismatches turn back over after a short delay, and the game tracks moves, score, and best score.

The main loop includes:

- 4-column Compose card grid
- animated card-flip transitions
- score and move counters
- win state with restart action
- persisted best score
- settings dialog for music and effect volumes
- background music plus flip, match, and win sounds

## Technical Highlights

- Kotlin
- Jetpack Compose
- Material 3
- `ViewModel` with `StateFlow`
- Hilt dependency injection
- Room persistence for best score storage
- `SoundPool` and `MediaPlayer` based audio playback
- Compose UI tests and ViewModel unit tests

## Project Structure

```text
memorygame/
|-- app/src/main/java/com/example/pekomon/memorygame/
|   |-- data/
|   |-- di/
|   |-- domain/
|   |-- presentation/
|   `-- util/
|-- app/src/test/
|-- app/src/androidTest/
`-- .github/workflows/memorygame-ci.yml
```

## Build And Test

Run commands from this project directory:

```bash
cd memorygame
./gradlew build
```

The repository also has a path-scoped GitHub Actions workflow for this project at `../.github/workflows/memorygame-ci.yml`.

## Current Limitations

- The project still uses its original `com.example.pekomon.memorygame` namespace.
- The directory name is lowercase while the newer showcase projects use product-style casing.
- There is no screenshot gallery yet.
- Some generated starter tests remain alongside the more meaningful game-state and Compose tests.

Those cleanup items are intentionally deferred so any package or directory rename can be handled as a separate, reviewable polish pass.
