package com.riprod.hexcode.api.execution;

import com.riprod.hexcode.core.common.glyphs.variables.HexVar;

public class IllegalOperationException extends RuntimeException {

    public IllegalOperationException(String message) {
        super(message, null, false, false);
    }

    public static <T extends HexVar> T requireFinite(T value) {
        if (value != null && !value.isFinite()) {
            throw new IllegalOperationException("non-finite result: " + value.describe());
        }
        return value;
    }
}
