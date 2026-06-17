---
title: "Working With Variables"
order: 2
published: true
draft: false
---

Highly Recommended that you read (getting started)[Getting-Started] and (advanced magic)[Advanced-Magic] before attempting anything shown below.

This is updated as of v0.8.6

# The Gist

The `Variable` glyph is by far the most nuanced variable in Hexcode, but it also opens the door to the wildest of spells

It has three slots
- Next
- Input
- Output

A general concept to get out of the way:
There are two ways to invoke a glyph. First is in the Next branch, the other is in the Slot branch

### Next Branch
The next branch is the execution line through the connected Sphere slots (the 'next' slot most of the time)

This will go through each glyph one by one and run them.

All glyphs (math, effect, etc) when executed on this branch will
1. calculate the inputs
2. do some effect
3. set the variables (self, main, any Outputs)
4. continue

### Slot Branch
The 'slot' branch is the *value calculation* branch, enabling inlined math operations and value derivation.
It is executed through the 'Input' slots (the Squares) off of a glyph.

This will go through each glyph, asking for the *value* of the next connected glyphs until the value is found
**All 'next' slots - Spheres - are IGNORED in this branch**

All the glyphs will go through this path:
1. check if self is defined (return early)
2. calculate the inputs
3. do an operation
4. return

> 'self' is only defined if it is executed in the Next branch BEFORE accessing the value. This lets you precompute stuff and then get a constant value later. For instance, you can run `Beam -> Add ( A = position(2, 2, 2), B = beam ) -> Delay -> Debug ( Add )` and the result will be calculated BEFORE the delay - rather than after. Alternatively doing `Beam -> Delay -> Debug ( Add ( A = position(2, 2, 2), B = beam ) )` will result in the position of where the entity was AFTER the delay


# Storing a value

When the `Variable` glyph is executed in the **next branch** it will...
1. Read the `Input` slot directly
2. Write to `Output`, `Self`, and `Default`
3. Continue execution off `Next` branch

So if you have `Variable ( input = Add(1, 1), output = Number 4 )`, the resulting variables will be 
```
 Default: 2
 Variable (self): 2
 4: 2
```

This opens interesting behavior. When outputting to a NUMBER, remember that it works as an index into storage. This is the __only__ way to WRITE to the storage. You cannot read from the storage from the `Input` slot as it will only read what is returned *directly*

# Accessing a value

When the `Variable` glyph is execued in the `Slot Branch` it will...
1. Read the `Input` slot directly
2. Convert `Input` to a number
3. Read from storage the VALUE at the INDEX of `Input`
4. Return that value

> Note: This does not use `Output` OR `Next` in this case.

So if we execute our prior example and have
```
 Default: 2
 Variable (self): 2
 4: 2
```

and want to read 2 from #4 (# prefixing a number is to indicate it is being used as an index)
We would only have to do
`Debug ( Slot = Variable ( Input = Number 4 ) )`

Output: `2`


# Variables in Forks

You may notice that you can **Fork** a **Next Branch** my connecting multiple glyphs to a single **Next** slot

This is called **Forking** (sometimes Branching in older documentation) and will run both forks in Parallel (mostly)

If you have
```
Delay (
    next = [
            Beam
            Area
        ]
)
```
the result will be shooting a beam from you AND an area from you. This is different from
```
Delay (
    next = Beam (
        next = Area ()
    )
)
```
which will shoot a beam and then have an area spawn where the beam hit.

## Forking (actual) execution flow

If you have
```
Delay (
    next = [
        Beam ( next = Halt ( next = Conjure () ) )
        Projectile ( next = Force ( next = Bolt () ))
    ]
)
```

There will still be an execution order. Remember that glyphs delay **one tick** between every execution. This is important.

The resulting order will be 
```sh
1. Delay
# 1st tick
2. Beam
3. Projectile
# 2nd tick
4. Halt
5. Force
# 3rd tick
6. Conjure
7. Bolt
```

They will both execute *individually* practically speaking. The first branch will Shoot a beam, halt the target, then summon a conjuration at the target position. The second branch will Shoot a projectile, Force the target, then Bolt the target.

This is because each branch will get it's OWN **default variable** to operate on.

## Accessing variables across forks
The interesting part is that **storage is shared** between forks.

```
Delay (
    next = [
        Beam ( next = Variable ( input = beam, output = Number 3 ) )
        Projectile ( next = Delay ( next = Bolt ( target = Variable ( input = Number 3 )) ))
    ]
)
```

this will
```sh
1. Delay
# 1st tick
2. Beam
3. Projectile
# 2nd tick
4. Variable # save the result of Beam to Index 3
5. Delay
# 3rd tick
6. Bolt # Bolts the result of BEAM - NOT projectiel
```

This enables cross-branch conditionals. Use wisely my friend. Be aware of Race Conditions and the Execution Order! 