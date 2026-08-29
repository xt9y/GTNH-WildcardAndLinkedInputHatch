# xt9y-features

## Linked Input Hatch

### Feature
- Adds a LuV **Linked Input Hatch** with **4 fluid tanks**, each holding **64,000 L**.
- Hatches in the same world with the same channel share the same four fluid tanks.
- Channels can be public or private. Private channels are isolated per owner.
- Each hatch has an integrated-circuit slot; the circuit is local configuration while the fluids are shared.
- A shared channel is locked while one linked hatch is actively supplying a recipe, preventing another hatch on that channel from consuming the same fluids at the same time.
- Configuration can be copied between hatches with a Data Stick.

### Use
1. Open the hatch GUI.
2. Enter the same channel name on every hatch that should share fluids.
3. Enable **Private** if the channel should only link hatches owned by you.
4. Put fluids into any linked hatch; all hatches on that channel see the same four tanks.
5. Put an Integrated Circuit in the circuit slot when the connected machine needs one.
6. **Left-click with a Data Stick** to copy channel/private/circuit configuration.
7. **Right-click with that Data Stick** on another Linked Input Hatch to paste it.
8. Normal screwdriver right-click toggles fluid filtering; sneaking screwdriver right-click toggles fluid sorting.

## CRIB Wildcard Patterns

### Feature
- Adds wildcard material expansion to the **Crafting Input Bus (ME) / CRIB**.
- When enabled, a pattern containing material-based GregTech ore-prefix items is expanded across all valid unifiable materials while keeping the same prefixes and stack sizes.
- All expandable material-based inputs and outputs in one pattern must originate from the same material.
- The wildcard state is saved on the CRIB.

### Use
1. Put the pattern in the CRIB.
2. Enable wildcard mode with the wildcard button in the CRIB GUI, or **sneak + right-click the CRIB with a Soft Mallet**.
3. AE2 will then see the generated material variants as crafting options.

## AE2 Interface Wildcard Patterns

### Feature
- Adds the same material wildcard expansion to normal **AE2 Interfaces**.
- The wildcard state is saved on the Interface.

### Use
1. Put the pattern in the Interface.
2. **Sneak + right-click the Interface with a Soft Mallet** to toggle wildcard expansion.
3. A chat message confirms whether wildcard mode is on or off.

## CRIB Non-Consumable Pattern Inputs

### Feature
- Processing-pattern item inputs can be marked **NC** and used as reusable catalysts.
- NC state is stored directly on the encoded pattern and survives moving the pattern and restarting the game.
- Any item input can be marked NC; matching uses the exact item, metadata and NBT.
- AE2 does not multiply NC inputs by the requested craft count and does not recursively autocraft them for the parent craft.
- The CRIB requires the encoded NC amount to already exist in ME storage before accepting the pattern push.
- The required NC item is reserved from ME once per active CRIB pattern slot.
- The reserved real item never enters the CRIB consumable inventory. GregTech receives disposable synthetic copies for recipe validation and parallel calculation, so the real catalyst cannot be consumed by the machine.
- NC inputs do not limit machine parallelism.
- The reservation is returned to ME after the real consumable items and fluids for that CRIB slot are gone.
- Reservations are saved in CRIB NBT so they survive a restart.
- NC applies only to **processing-pattern item inputs**.
- Wildcard expansion and NC can be used together; wildcard expansion happens first and the NC marking is preserved on the resulting CRIB crafting options.

### Use
1. Open an AE2 Pattern Terminal and switch to a **Processing Pattern**.
2. Place the catalyst in an input slot with the amount that must be reserved from ME.
3. **Shift + right-click that input slot**.
4. The slot shows **NC**.
5. Encode the pattern and put it in a CRIB.
6. Keep the required NC item available in ME storage.
7. Start the craft normally. The CRIB reserves the catalyst, uses it for recipe validation without consuming it, and returns it when the slot finishes.
