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

### Generic CRIB non-consumable pattern inputs
- In an AE2 Pattern Terminal, switch to a processing pattern and **Shift + right-click** any
  item input to toggle it as non-consumable.
- Non-consumable inputs show a small **NC** marker in the top-left of their pattern slot.
- The NC state is stored directly on the encoded pattern, so it survives moves, copies and
  restarts.
- NC inputs are generic: any item can be marked. There is no mold/circuit/item whitelist.
- AE2 does not multiply NC inputs by the requested craft count. The CRIB borrows only the
  encoded amount from ME storage (for example one mold for 2,000 recipe operations), keeps
  that amount reserved while the consumable inputs are processed, then returns it to ME.
- The NC item must already exist in ME storage when the CRIB starts the processing pattern;
  it is borrowed as a catalyst and is not recursively autocrafted as part of the parent job.
- Borrowed NC reservations are persisted in the CRIB NBT so a server restart cannot strand
  them.
- Only mark an input NC when the GregTech recipe itself treats that input as non-consumable.
  The flag changes AE/CRIB transport semantics; it does not change the underlying GregTech
  recipe's consumption rules.

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
