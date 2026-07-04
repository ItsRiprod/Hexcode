---
title: "Tier 2 Glyphs"
order: 3
published: true
draft: false
---
# Introduction

# Tier 2

Two-shape draws. These are the core toolbox for building useful hexes. Most of these cost very low mana or volatility, serving as a way to prop up the Tier 3 glyphs naturally.

---

### \[◯△\] Projectile

Your cheapest selector. Launches a projectile that triggers child glyphs on collision. Children do not execute until the projectile hits something. You can add Gravity, Bounces, or Speed as configuration options.  

---

### \[□◯\] Beam

Raycasts from an entity in a direction and stores the first thing it hits. Your primary single-target selector. Has a limited range and costs more the longer the range.

---

### \[◯□\] Area

Collects all targets within a radius around a center point. Your area-of-effect selector. For every entity in the area, it triggers downstream glyphs. This can get pricey very quickly and typically eats up all your volatility immediately. If you hit a block, it selects all blocks. If you hit an entity, it selects all entities.  

---

### \[△△\] Debug

The best way to get a peek into what hexcode is doing during execution. Connect to several variables to inspect their values. Sends debug info into the chat to you.

---

### \[□△\] Conjure

Spawns a temporary area zone at a position. The zone can trigger child glyphs on an interval for entities inside it. You can also connect “immediate” to do things like add velocity to the zone and push it along, or warp it, or change its color.

---

### \[◇𝟢\] Interfere

Hijacks active hex constructs or strips hex effects from targets. On persistent hex entities (Propel projectiles, Conjure zones, Arc chains): replaces their glyph chain with yours. On buffed entities: strips the buff. Children of Interfere become the injected payload, not continuations. The primary way to Counterspell.

---

### \[◇◯\] Resonate

Appends a parallel glyph chain to an ally's active hex construct. You pay for your injected glyphs. Multiple casters can Resonate on the same construct. The cooperative counterpart to Interfere.

---

### \[□▽\] Output

Specifies an output location. Notably useful in Interfere to determine the "continuation" point of the existing glyph. 
**Future Version:** Will enable specifying as an "anchor point" for flycasting, making it easier to nest a flycasted glyhp deep inside another glyph. Also enables COLORING that output, making it easier to quickly identify the glyph itself.

---

### \[△◯\] Concentrate

Sustains downstream glyphs while the caster holds the primary interaction. Releasing early cancels the hex. Increases volatility by 50% for longer hexes.

---