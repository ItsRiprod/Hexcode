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
- Aligned interaction tree of the experimental staff to be able to draw / cancel draw / cast
- Moved large parts of the code to the proper locations / deprecated or removed many unneeded bits of code
- Various bugfixes 