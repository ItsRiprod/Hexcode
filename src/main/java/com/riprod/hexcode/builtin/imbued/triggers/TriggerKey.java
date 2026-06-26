package com.riprod.hexcode.builtin.imbued.triggers;

public final class TriggerKey {

    public static final String PRIMARY = "Primary";
    public static final String SECONDARY = "Secondary";
    public static final String USE = "Use";

    public static final String DEATH = "Death";

    public static final String CAST = "Cast";

    public static final String SLEEP = "Sleep";

    public static final String[] ALL = new String[] {
            PRIMARY, SECONDARY, USE,
            DEATH, CAST, SLEEP
    };

    private TriggerKey() {
    }
}
