---
title: "Utility Glyphs"
order: 6
published: true
draft: false
---
# Introduction

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

## Utility

Utility glyphs are generally used to setup and prop up effect glyphs. They take no mana and use minimal vitality.

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