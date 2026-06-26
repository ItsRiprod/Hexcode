package com.riprod.hexcode.builtin.imbued.triggers;

import com.riprod.hexcode.core.common.triggers.registry.DefaultVariableKind;
import com.riprod.hexcode.core.common.triggers.registry.Trigger;

public final class Ability3Trigger implements Trigger {
    public static final String ID = "Ability3";

    @Override public String getId() { return ID; }
    @Override public Source getSource() { return Source.ITEM_HELD; }
    @Override public DefaultVariableKind getDefaultVariable() { return DefaultVariableKind.SELF; }
}
