## v0.8.5

- Migrated to new gradle properties
- Fixed ping comp issue
- Updated to a new patchly version
- General code cleanup
- Made glyphs defer-tick during execution
- Made branches share context for cross-branch refs
- Groundwork for client-driven casting/drawing

## v0.9.0

- Added `Complexity` stat (not yet incremented)
- Added Config on glyphs (so glyphs can have unique config values)
- Added `Impact` on glyph asset configs to remove magic numbers
- Added codec-registered Impact curves: PowerLaw, SphereVolume, RatioToDefault, Threshold (will add more as needed - again, removing magic numbers)
- Added per-slot `Impact` for multi-input cost scaling (Ensnare)
- Removed `AreaTax`/`computeAreaScale` - cost scaling now derived per-glyph via Impact
- Moved base cost compute onto `Glyph.computeBaseCost()` - `HexStats` only holds/tracks
- Migrated Force, Bolt, Gust, Area, Domain, Warp, Swap, Conjure, Phase, Erode, Fortify, Ensnare, Scale, Arc to Impact-based cost
- Reworked DebugGlyph output: wired slot shows linked glyphs (value, accuracy, speed, type); unwired shows full GIS variable map
- Made DebugGlyph translation-backed via `hexcode/debugGlyph.lang` templates with inline color/markup (chat `markupEnabled` enabled per node)
- Decoupled Complexity from Volatility: removed the dead `applyComplexity` seam that derived complexity from volatility spent
- Made Complexity a glyph-authored axis via opt-in `GlyphHandler.addComplexity(ctx, amount)`; no implicit accrual, glyphs are the source of truth
- Wired DebugGlyph `{complexity}` to read the live `HexStats` pool instead of the `0` placeholder
- Reworked glyph resource flow to simplify resource consumption
- Added automatic per-slot volatility cost: default `getVolatilityCost` = base cost x product of every per-slot `Impact` - global `Config.VolatilityImpact` is now override-only
- Added `Glyph.computeBaseCost(GlyphAsset)` overload to drop repeated asset lookups
- Ensnare: removed the damage input/dealing
- Aligned interaction tree of the experimental staff to be able to draw / cancel draw / cast
- mig: Moved large parts of the code to the proper locations / deprecated or removed many unneeded bits of code
- feat: Added `Exponential`, `Constant`, and `Linear` impact curves
- bug: Dropped the hardcoded construct drain fallback (configured constructs use the asset `DrainPerSecond` default)
- bug: Added `HexVar.copy()` and clone-on-write in `HexContext.setVariable` / `setDefaultVariable` so no two slots can alias the same mutable var
- Various bugfixes
- feat: Fleshed out the six element styles
- style: Centralized glyph style resolution in `VfxUtil.resolveModelId` / `resolvePrimaryColor` / `resolvePrimaryColorRaw` (context override then glyph asset); removed the duplicated per-glyph `resolveColor`/`resolveModelId` helpers and all eleven `DEFAULT_COLOR` constants
- style: Made `primaryModel` an overridable `HexStyleAsset` field (now carried by `applyOverride`) so context models win over the glyph asset model
- style: Routed Delay/Ensnare/Projectile/Shatter model spawns through style resolution; Glaciate and Conjure keep hardcoded structural models on purpose
- style: Defined `PrimaryColor`/`SecondaryColor` on every visual glyph asset (parenting `Essence_*`/`Self_Growth`/`Chaos_Void` where thematic), added `PrimaryModel` to Ensnare/Shatter
- style: Whitened every color-injected glyph particle spawner so the runtime-injected primary/secondary color is the sole color source (model trails, slot/effect, and pedestal particles left untouched)
- [ ] Domain: `renderDespawn`/`renderContested` still call `VfxUtil.effect` with hardcoded particle+sound ids (bypasses asset sound slot and color injection); Domain has 5 effects but only 4 style slots, so slot-ifying needs a design decision
- [ ] `Ignite_Fire` references base-game `Explosion_Big_*` spawners which cannot be whitened; injected color will multiply against their baked colors
