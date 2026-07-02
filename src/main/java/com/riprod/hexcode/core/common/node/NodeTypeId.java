package com.riprod.hexcode.core.common.node;

import java.util.Objects;

public final class NodeTypeId {

    public static final NodeTypeId ANCHOR = new NodeTypeId("anchor");
    public static final NodeTypeId CONTAINER = new NodeTypeId("container");
    public static final NodeTypeId GLYPH = new NodeTypeId("glyph");
    public static final NodeTypeId SLOT_STANDARD = new NodeTypeId("slot.standard");

    private final String id;

    public NodeTypeId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("NodeTypeId id must be non-null and non-empty");
        }
        this.id = id;
    }

    public String value() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof NodeTypeId)) return false;
        return id.equals(((NodeTypeId) other).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
