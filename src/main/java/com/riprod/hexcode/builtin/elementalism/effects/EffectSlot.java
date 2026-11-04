package com.riprod.hexcode.builtin.elementalism.effects;

public enum EffectSlot {
    DEFAULT("Default", "Fallback for every field another slot leaves unset"),
    CONSTRUCT("Construct", "Played while the element is charging on a construct"),
    BLOCK("Block", "Played when the element lands on a block"),
    ENTITY("Entity", "Played when the element lands on an entity"),
    IMPACT("Impact", "Played when the element collides with or hits something"),
    LAUNCH("Launch", "Anchored at the source of a launch or shot"),
    AURA("Aura", "Played over time while a status sits on an entity or block");

    private final String key;
    private final String documentation;

    EffectSlot(String key, String documentation) {
        this.key = key;
        this.documentation = documentation;
    }

    public String getKey() {
        return this.key;
    }

    public String getDocumentation() {
        return this.documentation;
    }
}
