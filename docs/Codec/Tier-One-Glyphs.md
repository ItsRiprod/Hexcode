---
title: "Tier One Glyphs"
order: 2
published: true
draft: false
---

# Tier 1

These are the foundational glyphs. Single-shape draws.

---

### \[◯\] Force

Applies directional force to the target. Does not deal damage directly. Direction, magnitude, and target can all be specified via the slots.

---

### \[△\] Delay

Delays execution of child glyphs. Everything after this glyph in the chain waits the specified time before continuing.

---

### \[▽\] Halt

Instantly zeros all velocity on targets. Things stop moving. Useful for freezing mid-air after a Force launch, or stopping a Propel projectile. It can either be Immediate or over time \- duration scales harshly against volatility cost.

---

### \[□\] Self

Returns a reference to the caster entity. The starting point for any self-targeting hex. As a value, it provides the caster reference directly. As an effect, stores the caster in a variable slot.

---

### \[◇\] Chaos

Randomly generates a number between the Min (default 0\) and Max (default 1\) values.

---

### \[𝟢\] Drain

Used to modify Entity Stats like mana / stamina / health  
Can be used to either gain knowledge of the target’s current stats OR to transfer from one stat to another (stamina \-\> mana or mana \-\> hp \- etc)  

---