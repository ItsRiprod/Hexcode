---
title: "Server Owners"
order: 4
published: true
draft: false
---
# Server Owners

Hello server owners and those trying to integrate Hexcode! There are a few things to know: 

1) Vanilla hexcode __WILL__ enable griefing if you are not careful
2) Vanilla hexcode enables a LOT of configuration options if you are willing to dig into it
3) Vanilla hexcode (as of v0.9.0) WILL work properly with claims mods and instance configs as well as PVP flags

## Asset Editor
If you haven't already, I recommend looking through Hexcode in the Asset Editor. You can
- Disable specific glyphs
- Change costs of glyphs
- Change the cost curve of glyphs (i.e. make high-magnitude force glyphs more expensive)
- Disable certain aspects of glyphs (support varies - if you have suggestions, let me know at `Riprod`)
- Tweak/modify visuals/sfx/models/etc

> *To edit a value, find it in the Riprod:Hexcode pack and hit "Override Asset" at the top. It will prompt you for a pack to save it in or to make a new pack. Choose your server pack, or make a new one, then change the values you'd like. Do not rename the asset*

In addition to this, **categories** of hexcode are split into individual mods. This means you can disable/enable entire categories of the mod. This will be more important later.

- **Riprod:HexCore** // The base hexcode glyphs + styles + assets
- **Riprod:Statly** // The unified magic system - elemental damage causes, statuses, and interaction reactions
- **Riprod:Imbued** // Imbuements / Block Imbuements / Item Imbuements
- **Riprod:Ritualistic** // The ritual-casting plugin
- **Riprod:Hexability** // The ultimate compatibility pack between hexcode and every-other plugin in existence
- **Riprod:Hextreme** // The survival plugin with loot and structures
- **Riprod:Hexomation** // Automation and synthesis functionality
- **Riprod:Counterspell** // The debug code
- **Riprod:Hextras** // Extra glyphs that are for testing



# Glyphs to be aware of for...
> Note: C = Configurable / disablable on the glyph

### World Impact

The following glyphs have the ability to impact the world
1. **Swap** *swaps two block positions*
2. **Warp** *moves a block*
3. **Erode** *breaks a block*
4. **Interact (low risk)** *interacts with a block*
5. **Terraform** *not implemented yet - planned impact*
6. **Phase** *temporarily removes a block*

### Entity Impact

1. **Force** *pushes an entity*
2. **Drain** *lowers entity stats*
3. **Phase** *damages entity when block returns*
4. **Scale** *scales an entity*
5. **Disguise** *temporarily disguises an entity as another*
6. **Swap** *swaps two entities*
7. **Warp** *moves an entity*
8. (C) **Rotate** *rotates an entity*
9. **Gust** *pushes nearby entities*
10. **Halt** *stops an entity from falling*
11. **Levitate** *gently pushes up an entity*

**(Damage / Heath)**
1. **Bolt**
2. **Drown**
3. **Electrocute**
4. **Freeze**
5. **HealthSurge**
6. **Ignite**
7. **MageArmor** *deflects a certain amount of damage received*
8. **Rebreathing**
9. **Scorch**
10. **Snap**
11. **Fortify** *increases resistance*
12. **Erode** *decreases resistance*

### Entity Spawning Impact
*All of these create/spawn an entity*

1. **Conjure**
2. **Projectile**
3. **Shatter**
4. **Ensnare**
5. **Concentration**
6. **Delay**

## Stats, Dials, and Balancing

Hexcode primarily uses two stats
- **Mana** // how many spells you can cast in a row
- **Volatility** // how big the spells can be

Secondary stats include
- **Stability** (staff) // how fast your spell decays
- **Slots** (book) // how many slots there are 
- **Accuracy** (drawing) // how accurate your glyphs are
- **Affinity_\<Element\>** // the affinity to a specific element you have. Gives bonus to that element

# ⚠️ This page is in progress ⚠️
Expect more to come. Hexcode is still in `Beta` and stats are still being worked out

If there are things missing here, please be sure to let me know on discord or through CurseForge messages! I would love to provide more insight into how the system works to allow you to better integrate hexcode into your systems.