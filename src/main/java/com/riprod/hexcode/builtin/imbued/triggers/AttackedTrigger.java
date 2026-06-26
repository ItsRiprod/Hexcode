package com.riprod.hexcode.builtin.imbued.triggers;

import com.riprod.hexcode.core.common.triggers.registry.DefaultVariableKind;
import com.riprod.hexcode.core.common.triggers.registry.Trigger;

public final class AttackedTrigger implements Trigger {
    public static final String ID = "Attacked";

    @Override public String getId() { return ID; }
    @Override public Source getSource() { return Source.ITEM_EQUIPPED_ARMOR; }
    @Override public DefaultVariableKind getDefaultVariable() { return DefaultVariableKind.ATTACKER; }
}
