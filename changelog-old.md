# v0.10.1

- fix: Erode actually breaks blocks again
- fix: Arbitrary spell duration limit removed

# v0.10.0
- feat: Updated to Hytale Update 6
- feat: Conjure can be rotated!
- feat: Conjure hitbox now stops projectiles and entities
- feat: Area can now be a Sphere, Box, or Cylindar (trilean)
- feat: Area now "grows" at a set rate of blocks/s until scale is achieved.
- feat: Area can be a non-uniform shape (Corner A/B defined now)
- 
- fix: Updated Cast logic to be more aligned with an expandable ECS system
- fix: Updated Block Accessing logic
- fix: Updated Block Rotation logic

# v0.9.0-Patch-1
*The patch has to be like this till Update 6 releases*

\+ Updated hexcode to `v0.9.0-Patch-1`
- Per-spell limit of 128 glyphs in a single tick
- Global limit of 512 glyphs in a single tick
- Glyphs can now bypass NoPVP if configured to do so
- Glyph Shapes now updated correctly
- Beam properly costs more the longer it goes
- Debug has a Trilean slot to change debug mode
  - You can now send notifications
- Messages were fixed to a queue and max at 10/tick to prevent a client crash
- Logger was updated for better fine-tuning and control
- Performance updates across the board with crafting/casting/flycasting/etc
- Various other bugfixes I found
\+ Added Configly 


# v0.9.0 - The Everything Update

## Headline Features

### ~21 New Glyphs
*Effects*
- Disguise
- Illuminate
- Interact
- Interaction
- Invisibility
- MageArmor
- Ward

*Elements*
- Drown
- Electrocute
- HealthSurge
- Rebreathing
- Scorch
- Snap
- Freeze
- Bolt

*Utilities*
- Color
- Compare
- Identify
- Modulo
- Query
- Shape
- Sound

*Rebranded / Reworked:*
- Rotation
Can rotate blocks/entities now
- Arc
Targets nearby entities on an interval
- Conjure
Can make the hitbox solid/transparent/soft 
- Projectile
Resolves on blocks properly + some config options
- Shatter
Shattered glyphs can be overridden
- Glaciate
Damage curve tweaked
- Beam
Resolves on blocks properly
- Erode
Permission gated
- Force
You can read the value of it
- Gust
Negative values pull to the center
- Levitate
Actually works now properly
- Phase
Does more damage the longer it is phased for
- Debug
Full overhaul on design
- AOE
Properly executes per item in the area
- Concentration
You can now accumulate a resource while concentrating or convert mana/stamina into volatility

*Glyph-Wide Changes:*
- Glyphs are implied models - single source of truth for shape
- All magic numbers are now on the glyph asset
- Glyphs are all permission-gated and work with world config values + claim plugins
- Glyphs can all have their volume muted, particles disabled or recolored, or main bits recolored
- Glyphs are now standardized on accessing 
- Shape Scale matters when drawing a glyph

### Added Tier-4 Glyphs
Tier 4 brings ELEMENTAL glyphs!

As you build your hex, you will accumulate Elemental Resource
This will be spent on a Tier 4 glyph doing more of what it does

Each SHAPE in a glyph adds to that resepctive element (by default, 8 per shape). Each time a glyph repeats, it contributes less (so looping one glyph will NOT give infinite resource)

### Pages

There are now pages - spell snippets - that spawn in various loot chests. These were all made by the community. They're still in-beta and may behave a bit strangely at time. 

You can decipher the page into a Hexbook (or weapon imbuement) and look at the original spell that was encoded.

You can also encode a glyph into a page. 

Use the **Life Obelisk** to do this.

Pages enable all sorts of behaviors - but most notably enables letting you re-organize your spellbooks! They are limited-use though, but you can refresh them at a life obelisk and be on your way again.

### Naming Hexes
You can now name your hexes using the Seeker Obelisk! They will show up while flycasting, letting you quickly see what spell you are going to cast.


Note: This is a stop-gap feature until Noesis is out and I can enable this in a proper book. For now thoguh, it is a great quality-add.

## Other Features

### Hexcode is Client-Driven

200 ping? No problem
Hexcode was rebuilt to be interactions-first and client-first on every user-facing aspect. 

This makes things feel buttery smooth and more accurate.

Attacks now charge. Your staff will shoot at 0.8x on a short click up to 1.2x on a full charge. Larger charges consume for stamina

### ECS By Design

Hexcode is fully ECS-Driven.

I won't go too in-depth just yet, but this means that integrating other mods into hexcode could literally never be easier. 

### Modular - and the cost

Hytale v5 has a bug that prevents me, with subplugins, from incrementing a PATCH version type - so until v6 releases, I will be stuck on v0.9.0 until i release v0.10.0

However, hexcode is now split into sub-modules. If you don't like a part of hexcode (say, rituals) you can do `/plugins manage` (the hytale command) and uncheck Hexcode:Ritualistic

### Permission Gating 

Hexcode has gained a ton of admin-level configurations. You can pause all hexcode activity for a world, for a player, or just put it on timeout. Hexcode effects now respect claims and factions as well as world-level restrictions.


## Rest of Changelog:

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
- Fix: Rebalanced Hexcode stats and damage outputs
    - Staffs now give a base Arcane elemental value (converts to damage)
    - Glyphs give 8 of a stat per shape present relative to the respective element
        - Circle = Lightning
        - Square = Life
        - Triangle = Water
        - Oval = Freeze 
        - Diamond = Fire
        - Upside-Down Triangle = Void
    - Each time a glyph is repeated, it will provide less down to 50% of the original contribution (1 instead of 2)
    - `DebugGlyph` will properly show the current stats available
- Feat: Getting the value of `Force` will give you the current velocity of the entity
- Feat: Modulo glyph added to get remainders
- Feat: Disguise Glyph added to disguise one glyph as another
    - Note: I really did try and make this work with blocks but hit atlas issues. Restricting it to only entities was FAR less cursed
- Feat: Added permission boundaries. Now hexcode will respect Simple Claims and other claims boundaries and prevent griefing
- Feat: Added operator commands
    - Added /hexcode stop to stop hexcode from functioning (panic mode)
    - Added /hexcode resume to resume hexcode
    - Added /hexcode stop --player=<player\> or --world=<world\> (or both) for targeted timeouts
    - Added /hexcode timeout --player=<player\> and/or --world=<world\> --duration=<time\> for targeted timeouts
- Feat: Fixed a few UI / interaction bugs pertaining to selecting
- Feat: Fixed hexcode hitting itself
- Fix: Logging levels are now `fine` instead of `info` by default for runtime logs
- Fix: Levitate now attempts to normalize levitation amount
- Fix: Force now properly applies to Conjured entities
- Fix: Projectile and Beam now resolve on the block correctly
- Fix: Scale Glyph properly removes itself if the server crashes
- Feat: Aligned Nodes and Slots to be centrally Fixed around a single pipeline (architectural cleanup - enables more Nodes and SlotTypes in the future)
- Feat: Added support for arbitrary slot metadata in the codecs 
- Fix: Block and Item imbuements work again
- Fix: when out of range of a pedestal in selection mode, you properly get removed
- Feat: added `Target` slot to every elemental effect
- Fix: updated `Levitate` to work on entities
- Fix: updated branch `Ids` to properly track active branches to better guess when a spell has ended naturally 
- Feat: Reworked `Arc` into a Tier 2 chaining selector (shape `◯□◯` -> `◯◇`)
    - Target can now be an entity, block, or position (block/position spawn a marker entity to host the chain)
    - Replaced the child-per-hop model + `Shock` effect with `Iterations`/`Interval`/`Range` slots: fires the wired output on the nearest unvisited entity each iteration, Fixed to the original target
    - Per-arc volatility now scales quadratically with jump distance (`SphereVolume` impact); a longer `Interval` makes each arc cheaper
    - `PrimarySound` plays on the initial cast, `SecondarySound` on each arc
    - Repeats over the in-range entities: hits each once, then cycles the list again until iterations run out
    - Added custom sounds: `SFX_Arc_Impact` (cast) and `SFX_Arc_Shock` (per-arc, 4 randomized variants on one event)
- Feat: Added Ward (drains volatility, has the Ward ability of concentrate)
- Feat: Removed Ward ability from concentrate 
- Feat: Made concentrate passively generate volatility 
- Feat: Made it so you can generate MORE volatility with concentrate if you drain another resource (i.e. mana) in the process
- Feat: Made Arc have repeat-damage. Scales volatility cost depending on how far away the target is. Longer intervals make that cost less expensive
- Feat: Spell Pages can be found in more chests  (spell selection is still quite limited and underwhelming)
- Feat: Spell Pages can be refilled from the table (like while flycasting) as well as saved/loaded to any slot (makes re-organizing spells WAY easier and is a budget void obelisk)
- Feat: Spell Page Rarity pertains to Base Volatility of that spell page (higher rarity = less casts though)
- Feat: **ADDED NAMING GLYPHS** 
- Feat: Named Hexes show up while Flycasting and hovering
- Feat: Named hexes show up as the Name of a page
- Feat: Named hexes show up when hovering over a slot above the slot name itself 
- Feat: Named hexes can be added/changed directly from the Seeker Obelisk
- Feat: Hex Names will persist across sharing with other people (encoded)
- Feat: Swapped Mage Armor and Fortify
- Feat: Fortify now lasts as long as there is Life Resource to run it (can't chain off itself) 
- Feat: Mage Armor now takes a duration and consumed Volatility on hit until depleted 
- Fix: Held State now properly removes across warping
- Fix: Delay now has physics
- Fix: Projectile can be disguised
- Fix: Illuminate now defaults to Glow
- Fix: Illuminate TV hitbox now properly covers block
- Fix: Ability2 now properly early-ends the commit
- Fix: Arc/Sphere now properly displays 
- Fix: Most math glyphs properly resolve to ZERO instead of DEFAULT VARIABLE
- Increased Resource to 8 per shape (was 2) 
- Decreased Base Resource on staffs
- State is now properly cleaned on disconnect (no more "still holding" concentration after reconnect)
- `Hexcode Reset` got updated to clean up a few more odd resources
- Rotation now queues the teleport and doesn't overflow the `byte` pending teleport id (currently hytale casts an incremented `int` to a `byte` without range-checking - so the client overflows `-128` and sends an invalid teleportation id. Low and behold: crash)
- Rotation now resets Roll after 30s (i may mess with this timing a bit)
- Concentration now has `manaPerSecond` `hpPerSecond` and `staminaPerSecond` fields
- Concentration now properly gives 3 volatility/s by default and does not stack
- Concentration fields are lossy after 5/s
- Staff interactions take less stamina on-use (now up to 25% of your stamina rather than 50%)
- Arc properly works now
- Trigger Glyphs properly remove their component ( @airun2518 the OnUse bug you found - it wasn't concentrate, concentrate just showed it better)
- Fixed icon pathing for some status effects
- A few other small bugfixes
- Rotation is now properly applied to blocks
- Rotation is properly read from blocks
- Query Glyph was added and allows a way to read stats from entities, the player, the glyph, blocks, etc
- HexStats were moved to a Component
- HexContext is now psudo-ECS by passing a stable ID instead of object reference
- AOE threads are isolated with a thread-local impl that enables reliably reading from other variables in the thread
- Probably much more. I tried keeping a running note this time