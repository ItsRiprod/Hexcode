package com.riprod.hexcode.core.common.hexes.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class EncodingStroke {

    private String shapeId;
    private float relativeSize;

    public EncodingStroke() {
    }

    public EncodingStroke(String shapeId, float relativeSize) {
        this.shapeId = shapeId;
        this.relativeSize = relativeSize;
    }

    public String getShapeId() {
        return shapeId;
    }

    public float getRelativeSize() {
        return relativeSize;
    }

    public EncodingStroke copy() {
        return new EncodingStroke(shapeId, relativeSize);
    }

    public static final BuilderCodec<EncodingStroke> CODEC = BuilderCodec
            .builder(EncodingStroke.class, EncodingStroke::new)
            .append(new KeyedCodec<>("ShapeId", Codec.STRING),
                    (c, v) -> c.shapeId = v,
                    c -> c.shapeId)
            .add()
            .append(new KeyedCodec<>("Size", Codec.FLOAT),
                    (c, v) -> c.relativeSize = v,
                    c -> c.relativeSize)
            .add()
            .build();
}
