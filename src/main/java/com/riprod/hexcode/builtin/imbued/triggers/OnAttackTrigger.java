package com.riprod.hexcode.builtin.imbued.triggers;

import com.riprod.hexcode.core.common.triggers.registry.DefaultVariableKind;
import com.riprod.hexcode.core.common.triggers.registry.Trigger;

public final class OnAttackTrigger implements Trigger {
    public static final String ID = "OnAttack";

    @Override public String getId() { return ID; }
    @Override public Source getSource() { return Source.ITEM_HELD; }
    @Override public DefaultVariableKind getDefaultVariable() { return DefaultVariableKind.TARGET_ENTITY; }
}
