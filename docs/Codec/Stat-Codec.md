---
title: "Stat Index"
order: 1
published: true
draft: false
---
# Introduction

Hexcode has different types of Stats and Values it helps to keep track of

> Note: Docs may be outdated at any time. These are hard to keep up with, but hopefully this helps work as a general overview of the concepts.


## Mana

Mana is calculated per glyph present in the hex upon casting - even if unwired.

This is a one-time payment for casting a spell

## Stamina

Stamina cost is determined by how long you charge up your staff before attacking. Longer charge = more volatility and more stamina

## Volatility

The 'runtime cost' of your spell. Each glyph consumes some amount of volatility (utility costs 0.1 while more powerful glyphs may cost 5+)

Only glyphs connected to a `Circle` slot cost volatility (slots - like "Amount" or "Target" do NOT cost any volatility. Feel free to go crazy here)

Each time a glyph executes, it calculates and consumes volatility. When volatility runs out, you'll get a notification that `<Glyph> Fizzled`

Some slots, like `Magnitude` on Force cost more volatility the larger the input is - so be aware of that.

Increased by
- Better Armor
- Better Staff

## Magic Charges / Spell Slots

This is how many active spells you can have at once.

The limit is calculated at *execution time* - so if you are holding a weak staff with 1 slot, you can cast 1 spell. If you switch to a more powerful staff with 12 slots, you can cast a 2nd spell at the same time just fine.

Increased by
- Better staff

## Efficiency

This is how quickly you draw your glyph. Drawing a faster glyph will contribute *half* to the quality and decrease the initial mana cost of the glyph by up to half.

100% efficiency = 0.5x volatility cost

You can find this stat per glyph by using the Debug Glyph

## Accuracy

This is how accurately you draw your glyph. Drawing more accurately will contibute the other *half* to the quality (the color of the glyph) and decrease the *volatility cost* by up to half. 

100% accuracy = 0.5x volatility cost

You can find this stat per glyph by using the Debug Glyph

## Resource

Resource is the primary way of doing damage. 

Tier 1-3 glyphs will *add* resource when executed. Tier 4 will *consume* resource when executed.

Resource is accumulated *per shape type* in the glyph. Force (circle) will contribute to the `Lightning Resource`
Use the `Debug Glyph` to view your current resource accumulation.


Repeating a glyph will make it contribute less resource per execution. Having 8 Force glyphs in a row will make the 8th glyph only provide up to half of the normal resource amount. Varying up glyphs (i.e. Force -> Interact) will not have a penalty

**Arcane Resource** is a global resource that works for any consumer

## Affinity

Affinity, defined by armor pieces, gives a bonus to certain resource types. Lightning Affinity means all lightning-related spells are increased in effectiveness by that percentage