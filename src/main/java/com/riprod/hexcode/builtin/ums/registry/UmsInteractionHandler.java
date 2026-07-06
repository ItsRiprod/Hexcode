package com.riprod.hexcode.builtin.ums.registry;

public interface UmsInteractionHandler {
    String getId();

    void handle(UmsReactionContext ctx);
}
