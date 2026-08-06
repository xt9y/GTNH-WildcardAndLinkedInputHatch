# xt9y-features

A standalone GTNH addon that re-introduces the experimental XT9Y features on top of the
unmodified GTNH pack (no custom `gregtech` jar required).

## Features

### Wireless Linked Input Hatch
- 4x input hatch (9 slots each) linked wirelessly to a single channel.
- Left/right-click with a data stick to copy/paste the channel + circuit configuration.
- Channels do not cross world boundaries.

### CRIB wildcard pattern expansion
- Adds a wildcard toggle button to the Crafting Input Bus (ME) GUI (bottom-left corner).
- When enabled, patterns with wildcard item entries match against all matching item variants.
- The same wildcard state can also be toggled by sneaking + right-clicking the CRIB with a
  soft mallet.

### CRIB per-pattern circuit configuration
- Circuit configuration is read from the patterns themselves, so a single CRIB can handle
  recipes that require different circuits.

### AE2 Interface wildcard expansion
- Interfaces can be set to wildcard expansion mode so their patterns also match item variants.
- **There is no GUI button.** Toggle it by **sneaking + right-clicking the interface with a
  soft mallet** (same as the CRIB). You will get a chat message confirming the new state.

## Requirements
- GTNH 2.9.0-beta-1 (or compatible)
- gregtech
- appliedenergistics2

## Installation
Drop `xt9yfeatures-vX.Y.Z.jar` into the `mods/` folder. No configuration required.

## Building
```
./gradlew build
```
The resulting jar is in `build/libs/`.
