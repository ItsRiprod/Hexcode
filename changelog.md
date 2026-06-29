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
- [ ] DebugGlyph `{complexity}` is a `0` placeholder until the Complexity accrual accessor lands
- Decoupled Complexity from Volatility: removed the dead `applyComplexity` seam that derived complexity from volatility spent
- Made Complexity a glyph-authored axis via opt-in `GlyphHandler.addComplexity(ctx, amount)`; no implicit accrual, glyphs are the source of truth
- Wired DebugGlyph `{complexity}` to read the live `HexStats` pool instead of the `0` placeholder
- Reworked glyph resource flow: executor calls `GlyphHandler.execute0` (engine caller) which resolves the asset once, computes `getVolatilityCost`, consumes/gates, accrues complexity via `getComplexity`, then runs `execute`
- Added automatic per-slot volatility cost: default `getVolatilityCost` = base cost x product of every per-slot `Impact` (`GlyphCostUtil`); global `Config.VolatilityImpact` is now override-only
- Added `Glyph.computeBaseCost(GlyphAsset)` overload to drop repeated asset lookups
- Complexity now defaults to 1:1 of computed volatility cost (override `getComplexity` or set `ComplexityImpact`)
- Collapsed Bolt, Force, Area, Erode, Fortify, Phase to declarative per-slot impact (deleted cost overrides; behavior-identical)
- Migrated remaining cost overrides (Swap, Conjure, Warp, Scale, Arc, Domain, Gust) to `getVolatilityCost`; Domain stays an override because its per-slot impacts are mana, not volatility
- Ensnare: removed the damage input/dealing, added a `Trapped` on-target routing slot, radius-only cost
- [ ] Redesign Ensnare to snare-only and execute the `Trapped` glyph per ensnared target (see TODO.md)
- Aligned interaction tree of the experimental staff to be able to draw / cancel draw / cast
- Moved large parts of the code to the proper locations / deprecated or removed many unneeded bits of code
- Various bugfixes
- Added `Exponential`, `Constant`, and `Linear` impact curves
- Made Arc a single per-jump cost (removed the duplicate cast-time charge)
- Migrated Domain mana to impacts: per-slot `Linear` for upfront/drain, global `Constant` trigger via `DomainConfig`
- Dropped the hardcoded construct drain fallback (configured constructs use the asset `DrainPerSecond` default)
- [ ] Verify in-game costs match pre-refactor values (Scale ramp, Arc per-jump, Domain mana)