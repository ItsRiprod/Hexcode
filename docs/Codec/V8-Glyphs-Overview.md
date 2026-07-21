---
title: "V8 Glyph Index"
order: 1
published: true
draft: false
---
# V8 Glyph Index
> All of the following shapes are from the v0.8.X version of Hexcode for backwards compatibility reasons.

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

# Tier 3

Three-shape draws. These are the Effects, the glyphs that do stuff. Generally, these are a lot more expensive than Tier 2 glyphs. 

---

### \[□◇△\] Scale

Scales an entity momentarily for a duration, reverting their shape once expired.

---

### \[△□◇\] Disguise

Disguises a target creature as a reference creature for a duration, copying its model (and player skin) then reverting once expired. Entity to entity only.

---

### \[◯◇◯\] Gust

Radial force explosion pushing all targets away from a center point. Small fixed concussive damage as a side effect

---

### \[□△□\] Growth\*

Restores the natural state of targets. Heals entities, grows crops, repairs damaged blocks

---

### \[□◯□\] Fortify

Increases resilience of targets for a duration. On entities: flat damage reduction per hit. On blocks: increased hardness.

---

### \[◇𝟢◇\] Erode

Weakens targets for a duration, increasing damage taken from all sources including melee. Does zero damage on its own. Pure setup for follow-up attacks.

---

### \[◇▽𝟢\] Levitate\*

Reverses or nullifies gravity on targets for a duration. Zero intensity means weightless. Positive intensity means active upward pull.

---

### \[◇◯◇\] Ignite

Sets targets on fire. Fire does damage over time and can spread to adjacent flammable blocks. Entities can extinguish by entering water.

---

### \[□◯◇\] Burning Hands\*

Shoots fire from your hands in a cone in front of you, selecting all entities as well in the area. Does some fire damage	

---

### \[◯◯◯\] Interact

On blocks\: triggers block interactions remotely (opens doors, flips levers). The only glyph that can activate block interactions from a distance.

---

### \[◯□◯\] Arc

Chain lightning. Hops from entity to entity, executing one child glyph per hop in order. More children means more hops. Volatility is rechecked each hop, so long chains naturally fizzle out.

---

### \[𝟢𝟢𝟢\] Freeze

Slows down and freezes the target and makes the ground below them ice.

---

### \[𝟢◯𝟢\] Shatter

Launches ice shard projectiles in a cone from a position. Each shard is a mini-projectile dealing ice damage on impact and allowing further execution of the hex.

---

### \[𝟢□𝟢\] Glaciate

Spawns ice blocks above targets that fall with gravity. Deals impact damage based on fall speed.

---

### \[□◇□\] Terraform\*

Moves existing natural blocks (dirt, stone, sand, gravel) from one position to another. Block telekinesis. Only works on natural block types.

---

### \[□▽□\] Ensnare

Disrupts terrain in a radius. Raises spike formations from the earth that damage entities walking over them. Area denial through terrain change. Can execute a glyph for every spike that is triggered.

---

### \[□𝟢□\] Phase

Temporarily removes blocks from reality. Blocks become air for a duration, then snap back. Entities caught inside restoring blocks take crush damage. Children execute after blocks are restored.

---

### \[◯▽◯\] Warp

Teleports targets to a destination position. High flat mana cost. Costs more volatility the further the warp distance.

---

### \[▽◯▽\] Swap

Exchanges positions between two variable arrays element-by-element. Item at index i in A swaps position with item at index i in B. If arrays are different lengths, extra elements are skipped.  

---

### \[□□□\] Domain

Domain Expansion. Decreases volatility cost while within your own domain. Enables triggering a glyph for every entity inside of, or who enters your domain. 

**Clashing**

Clashing occurs when two domains interset. The domain with the higher Energy wins the clash. Energy has no use outside of this.

# Tier 4

Four-shape glyphs make up the elemental tier. Every element ships as a **pair**: a `◯`-leading **offensive** glyph that deals direct elemental damage, and a `□`-leading **defensive** glyph that applies a lingering **status condition** to the target.

All tier 4 glyphs convert Complexity into Damage. Defensive will convert that into duration, offensive will convert that into raw damage output.

---

## Fire

### \[◯◇◇◇\] Scorch

| Element | Type |
| --- | --- |
| Fire | Offensive |

Direct burst of fire damage to the target. Your universal opener: no setup, no conditions, reliable damage on anything. When you just need to do some damage, this is the honest answer.

### \[□◇◇◇\] Ignite

| Element | Type | Weak | Resistant |
| --- | --- | --- | --- |
| Fire | Defensive | Water | Fire, Life, Ice | 

Applies the **Burning** condition. Fire damage over time that ticks for a duration and spreads to adjacent flammable blocks. Deals nothing upfront: the fire does the work. Targets extinguish by entering water.

---

## Water

### \[◯△△△\] Drown

| Element | Type |
| --- | --- |
| Water | Offensive |

A crushing surge of water damage. A solid mid-weight strike with a heavy, deliberate cadence. No conditions to satisfy, just weight behind the hit.

### \[□△△△\] Drench

| Element | Type | Weak | Resistant |
| --- | --- | --- | --- |
| Water | Defensive | Lightning, Life | Fire, Water, Ice |

Applies the **Drenched** condition. Soaks the target, leaving them dripping and conductive for a duration. Near-zero damage on its own: most of its Complexity buys the condition, not a health bar.

---

## Lightning

### \[◯◯◯◯\] Bolt

| Element | Type |
| --- | --- |
| Lightning | Offensive |

Instant high-voltage discharge. The burst option: front-loaded damage with no travel time and no wind-up. Some of its Complexity pays for that instantaneous, unavoidable delivery.

### \[□◯◯◯\] Shocking

| Element | Type | Weak | Resistant |
| --- | --- | --- | --- |
| Lightning | Defensive | N/A | Lightning |

Applies the **Shocked** condition. Static charge that interrupts the target's actions and briefly staggers them, jittering their inputs. A control condition first, damage second.

---

## Life

### \[◯□□□\] Health Surge

| Element | Type |
| --- | --- |
| Life | Offensive |

Heals the target for the provided Complexity. 

### \[□□□□\] Mage Armor

| Element | Type | Weak | Resistant |
| --- | --- | --- | --- |
| Life | Defensive | N/A | Water, Lightning, Life |

Applies the **Mage Armor** condition. Gives temporary hitpoints equal to the provided complexity and lasts for the duration of the complexity. Received damage will reduce the mage armor instead of the caster's HP. Once the mage armor runs out, the status effect ends.

---

## Ice

### \[◯𝟢𝟢𝟢\] Snap

| Element | Type |
| --- | --- |
| Ice | Offensive |

A brittle burst of ice damage. Lighter on raw damage than the other offensive glyphs; its Complexity leans into reach and the cold bite rather than pure impact.

### \[□𝟢𝟢𝟢\] Freeze

| Element | Type | Weak | Resistant |
| --- | --- | --- | --- |
| Ice | Defensive | Fire | Ice | 

Applies the **Chilled** condition. Slows movement and attack speed, stacking toward a full Freeze. Control, not damage: kite and peel while the target seizes up.

---

## Void

### \[◯▽▽▽\] Extinguish

| Element | Type |
| --- | --- |
| Void | Offensive |

???

### \[□▽▽▽\] Decimate

| Element | Type | Weak | Resistant |
| --- | --- | --- | --- |
| Void | Defensive | ?? | Void |

???


# Utility Glyphs
Glyphs to get you from concept to number.
Utility glyphs are generally used to setup and prop up effect glyphs. They take no mana and use minimal vitality.

---

### \[△\] Delay

Delays execution of child glyphs. Everything after this glyph in the chain waits the specified time before continuing.

---

### \[□\] Self

Returns a reference to the caster entity. The starting point for any self-targeting hex. As a value, it provides the caster reference directly. As an effect, stores the caster in a variable slot.

---

### \[◇\] Chaos

Randomly generates a number between the Min (default 0\) and Max (default 1\) values.

---

### \[\>\] Greater

Branches execution based on comparison. If A is greater than B, the first child executes. Otherwise, the second child executes. Costs no mana.

---

### \[\<\] Less

Branches execution based on comparison. If A is less than B, the first child executes. Otherwise, the second child executes. Costs no mana.

---

### \[|―\] Add

Adds two values together. Works on numbers, positions, rotations. First number type is authoritative. If the first number is a Vector (rotation or position), the result will also be a Vector

---

### \[―\] Subtract

Subtracts one value from another. For entity and block lists, removes matching elements. If only A/B is filled with a rot/pos, the value is inverted. Rot(1, 2, 3\) becomes Rot(-1, \-2, \-3). If the first number is a Vector (rotation or position), the result will also be a Vector

---

### \[ \\ / \] Multiply

Multiplies two values together. First number type is authoritative. If the first number is a Vector (rotation or position), the result will also be a Vector

---

### \[/\] Divide

Divides one value by another. Division by zero returns the original value.

---

### \[◯/◯\] Modulo

Returns the remainder of one value divided by another.

---

### \[― ―\] Equal

Two modes. With both inputs wired: branches execution (first child if equal, second child if not). With only A/B wired: assigns A/B's value to the output slot.

---

### \[\<\>\] Position

Constructs a position from X, Y, Z components. Wire number glyphs into each component. Wiring a Variable or an Entity (i.e. Self) will extract the coordinate from that entity. If an Entity is connected to Slot X, the X coordinate of the entity will be inputted there.

---

### \[ΛV\] Rotation

Constructs a rotation from pitch, yaw, roll components. Wire number glyphs into each component. Wiring a Variable or an Entity (i.e. Self) will extract the coordinate from that entity. If an Entity is connected to Slot Pitch, the pitch of the entity will be inputted there

---

### \[\<□\>\] Style

Sets the color of the execution at this point. Returns a 4 param vector (R, G, B, A) if value extracted

---

### \[Λ\] Power

Calculates A to the power of B

---

### \[―Λ\] Root

Calculates A to the root of B

---

### \[△\<\] Sin

Calculates the sin of A

---

### \[△\>\] Cos

Calculates the cos of A

---

### \[△Λ\] Tan

Calculates the tan of A

---

### \[△V\] PI

Returns the value of PI

---

### \[||\] Abs

Returns the absolute value of an input. Converts vector -> magnitude as well.

---

### \[|V|\] Floor

Rounds A down to the nearest whole number. Preserves type: Positions and Rotations are floored component-wise. Entities, Blocks and Colors pass through unchanged.

---

### \[|Λ|\] Ceiling

Rounds A up to the nearest whole number. Preserves type: Positions and Rotations are ceiled component-wise. Entities, Blocks and Colors pass through unchanged.

---

### \[|◯|\] Round

Rounds A to the nearest whole number. Preserves type: Positions and Rotations are rounded component-wise. Entities, Blocks and Colors pass through unchanged.

---

### \[<\>◯\] Dot Product

Derives the dot product of two vectors. Will convert a number to a vector (i.e. 1 = (1, 1, 1)) or a rotation to a vector with a magnitude of 1. 

---

### \[<\>\\ / \] Cross Product

Derives the cross product of two vectors. Will convert a number to a vector (i.e. 1 = (1, 1, 1)) or a rotation to a vector with a magnitude of 1.

---

### \[V\] Variable

Reads an input slot and saves it to it’s own reference, allowing for creation of a “snapshot” in time. Also enables outputting to another variable to overwrite it.
Sets the read value as the "default variable" too.

If it has not been run inside the glyph execution (i.e. linked to a Slot of another glyph) it will take the "Value Of" whatever the input is. 

---

### \[Λ◯\] Number 1

### \[Λ|\] Number 2

### \[Λ△\] Number 3

### \[Λ□\] Number 4

### \[\>◯\] Number 5

### \[\>|\] Number 6

### \[\>△\] Number 7

### \[\>□\] Number 8

### \[V◯\] Number 9

### \[V|\] Number 10

### \[V△\] Number 11

### \[V□\] Number 12

### \[\<◯\] Number 13

### \[\<|\] Number 14

### \[\<△\] Number 15

### \[\<□\] Number 16

---
# Unimplemented/Planned/Partially Working Glyphs

## Boolean Values \-1 (false) 0 (equal) \+1 (true)

### \[―◯\] IsEntity\*

\-1 not, 0, is entity, 1 is player

### \[―□\] IsBlock\*

\-1 not a block, 0 air block, 1 solid block

### \[―V\] IsNumber\*

\-1 not a number, 0 is zero, 1 is a number

### \[―\>\<\] IsVector\*

\-1 not a vector, 0 rotation, 1 position

---

## Conditions

### \[|△\] OnEating\*

### \[|◯\] OnAttack\*

### \[|𝟢\] OnAttacked\*

### \[|\>\<\] OnMove\*

### \[|\>\] OnRightClick\*

### \[|\<\] OnLeftClick\*

### \[|ΛV\] OnRotate\*

### \[|◇\] OnDeath\*

### \[|□\] OnCast\*

### \[|▽\] OnSleep\*

\*Glyphs are not fully implemented or are a bit buggy in the current version of Hexcode.