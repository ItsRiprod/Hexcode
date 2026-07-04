---
title: "Tier One Glyphs"
order: 2
published: true
draft: false
---
# Introduction

Hexcode is built upon Three Basic Shapes. That is…

---

#### 1- □ **Square**

*It means Divinity, the idea of Self or Creation.*

#### 2- ◯ **Circle**

*It means Energy. The presence of force or heat.*

#### 3- △ **Triangle**

*It means Time. The passage of, or the duration through.*

Beyond these basic three types, there exists three Anti-Shapes. These provide the Inverse Effect of the base three shapes


#### 4- ◇ **Diamond** (Anti-Square)

*Means Death, Chaos, and Destruction. The absence of order.*

#### 5- 𝟢 **Oval** (Anti-Circle)

*It means the absence of Energy. To be Void or Cold.*

#### 6- ▽ **Upside-Down Triangle** (Anti-Triangle)

*It means Immediate, Instant, or sometimes even Negative passage of time.*


The combination of these six shapes are what form the backbone of every single glyph in Hexcode. The combination of ◯△ means Energy over Time. This manifests as what appears to be a Projectile \- the physical creation of energy passing through time and space. All of hexcode follows this logic.

Refer to the **Creating Glyphs** section of the main document for how to create your glyphs. The following is every glyph in Hexcode in order of Tier and Type

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