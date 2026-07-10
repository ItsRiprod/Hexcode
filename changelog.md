## v0.8.5

- Migrated to new gradle properties
- Fixed ping comp issue
- Updated to a new patchly version
- General code cleanup
- Made glyphs defer-tick during execution
- Made branches share context for cross-branch refs
- Groundwork for client-driven casting/drawing

## v0.9.0

- Added `Complexity` stat
- Added Config on glyphs (so glyphs can have unique config values)
- Added `Impact` on glyph asset configs to remove magic numbers
- Added codec-registered Impact curves: PowerLaw, SphereVolume, RatioToDefault, Threshold (will add more as needed - again, removing magic numbers)
- Added per-slot `Impact` for multi-input cost scaling (Ensnare)
- Removed `AreaTax`/`computeAreaScale` - cost scaling now derived per-glyph via Impact
- Moved base cost compute onto `Glyph.computeBaseCost()` - `HexStats` only holds/tracks
- Reworked DebugGlyph output: wired slot shows linked glyphs (value, accuracy, speed, type); unwired shows full GIS variable map
- Made DebugGlyph translation-backed via `hexcode/debugGlyph.lang` templates with inline color/markup
- Reworked glyph resource flow to simplify resource consumption
- Added automatic per-slot volatility cost: default `getVolatilityCost` = base cost x product of every per-slot `Impact` - global `Config.VolatilityImpact` is now override-only
- Ensnare: removed the damage input/dealing
- Aligned interaction tree of the experimental staff to be able to draw / cancel draw / cast
- mig: Moved large parts of the code to the proper locations / deprecated or removed many unneeded bits of code
- feat: Added `Exponential`, `Constant`, and `Linear` impact curves
- bug: Dropped the hardcoded construct drain fallback (configured constructs use the asset `DrainPerSecond` default)
- bug: Added `HexVar.copy()` and clone-on-write in `HexContext.setVariable` / `setDefaultVariable` so no two slots can alias the same mutable var
- feat: Fleshed out the six element styles
- style: Centralized glyph style resolution in `VfxUtil.resolveModelId` / `resolvePrimaryColor` / `resolvePrimaryColorRaw` (context override then glyph asset); removed the duplicated per-glyph `resolveColor`/`resolveModelId` helpers and all eleven `DEFAULT_COLOR` constants
- style: Made `primaryModel` an overridable `HexStyleAsset` field (now carried by `applyOverride`) so context models win over the glyph asset model
- style: Routed Delay/Ensnare/Projectile/Shatter model spawns through style resolution; Glaciate and Conjure keep hardcoded structural models on purpose
- style: Defined `PrimaryColor`/`SecondaryColor` on every visual glyph asset (parenting `Essence_*`/`Self_Growth`/`Chaos_Void` where thematic), added `PrimaryModel` to Ensnare/Shatter
- Feat: Added ECS event transparency logging to the counterspell diagnostics sub-plugin
- Feat: Added several new elemental glyphs
- Fix: rebalanced Bolt/Erode/Fortify/Drain/Concentrate
- Feat: Finalized Complexity balancing
- Probably much more. I tried keeping a running note this time