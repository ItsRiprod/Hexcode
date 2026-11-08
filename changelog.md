# [v0.12.0]

## Elementalism Is Here

6 new ways of generating elemental stats

**FIRE**
- Charges over time - extinguishes if charged too long
- Deals
1. Catches on fire
2. Scorches
3. Explodes
**ICE**
- Charges over time, charges faster when moving slower
- Deals
1. A
2. B
3. C
**WATER**
- Charges over time, charges faster when moving down
- Deals
1. A
2. B
3. C
**LIGHTNING**
- Charges over time, charges faster when moving faster
- Deals
1. A
2. B
3. C
**LIFE**
- Charges over time, charges faster when on an entity
- Deals
1. A
2. B
3. C
**VOID**
- Charges over time, charges faster when there are more branches
- Deals
1. A
2. B
3. C

## Bugfixes / changes

Lots of very low technical changes. Things nobody will see nor notice but increases reliability and maintainability.

- Math Variables now use primitives as a transition layer
- Glyphs that required `BlockVar` now accept any variable who's type is `Position` or `Transform`
- GlyphVars now declare their default state for better code management
- Added `TransformVar` in addition to `RotationVar` and `PositionVar` that retains rotation information
- Math Glyphs now behave properly when missing inputs / invalid inputs.
    - x * 0 = 0 // used to be x
    - x ^ 0 = 1 // used to be x
    - 0 ^ x = 0 // used to be x
- Improved memory overhead and reduced vector allocation
- Improved chunk lookup overhead for world-interacting glyphs by using cursors and retaining previously-derived information
- Improved interaction lookup logic to utilize existing block logic
- Removed auto-air routing so glyphs like Erode work consistently (no longer auto-finding solid blocks to erode, now will do nothing if the block is already broken)
- Improved overhead for Growth's bonemeal functionality
- Conjure no longer triggers on intangible entities (decorations)