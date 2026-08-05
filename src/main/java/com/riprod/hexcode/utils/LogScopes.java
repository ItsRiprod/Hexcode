package com.riprod.hexcode.utils;

public final class LogScopes {

    public static final String GLYPH = "Hexcode|Glyph";
    public static final String DRAW = "Hexcode|Draw";
    public static final String DIAG = "Hexcode|Diag";
    public static final String CRAFT = "Hexcode|Craft";
    public static final String CAST = "Hexcode|Cast";
    public static final String CMD = "Hexcode|Cmd";
    public static final String ASSETS = "Hexcode|Assets";
    public static final String PROTECT = "Hexcode|Protect";
    public static final String STATE = "Hexcode|State";
    public static final String IMBUE = "Hexcode|Imbue";

    public static final String[] ALL = new String[] {
            GLYPH, DRAW, DIAG, CRAFT, CAST,
            CMD, ASSETS, PROTECT, STATE, IMBUE
    };

    private LogScopes() {
    }
}
