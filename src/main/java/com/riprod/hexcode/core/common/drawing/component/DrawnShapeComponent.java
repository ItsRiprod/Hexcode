package com.riprod.hexcode.core.common.drawing.component;

import java.util.List;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.Color;
import com.riprod.hexcode.core.common.drawing.registry.ShapeAsset;

public class DrawnShapeComponent {

    public static final BuilderCodec<DrawnShapeComponent> CODEC = BuilderCodec
            .builder(DrawnShapeComponent.class, DrawnShapeComponent::new)
            .append(new KeyedCodec<>("ShapeId", Codec.STRING),
                    (a, v) -> a.shapeId = v, a -> a.shapeId)
            .addValidatorLate(() -> ShapeAsset.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("Size", Codec.FLOAT),
                    (a, v) -> a.relativeSize = v, a -> a.relativeSize)
            .addValidator(Validators.range(0.0f, 1.0f))
            .add()
            .build();

    private String shapeId;

    private float volatility;
    private float efficiency;

    private float size;
    private float relativeSize;
    private List<Vector3d> points;
    private Color color;
    private transient ShapeAsset shapeAsset;

    public DrawnShapeComponent(String shapeId, float size, float relativeSize, float volatility) {
        this.shapeId = shapeId;
        this.size = size;
        this.relativeSize = relativeSize;
        this.volatility = volatility;
    }

    public DrawnShapeComponent(String glyphId, float accuracy, ShapeAsset asset) {
        this(glyphId, 1.0f, 1.0f, accuracy);
        this.shapeAsset = asset;
    }

    private DrawnShapeComponent() {
    }

    public List<Vector3d> getPoints() {
        return points;
    }

    public Color getColor() {
        return color;
    }

    public void setPoints(List<Vector3d> points) {
        this.points = points;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getShapeId() {
        return shapeId;
    }

    public float getSize() {
        return size;
    }

    public float getVolatility() {
        return volatility;
    }

    public float getRelativeSize() {
        return relativeSize;
    }

    public void setShapeId(String glyphId) {
        this.shapeId = glyphId;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public void setVolatility(float accuracy) {
        this.volatility = accuracy;
    }

    public void setRelativeSize(float relativeSize) {
        this.relativeSize = relativeSize;
    }

    public float getEfficiency() {
        return efficiency;
    }

    public void setSpeed(long speed) {
        if (shapeAsset == null || shapeAsset.getExpectedSpeed() <= 0 || speed <= 0) {
            this.efficiency = 0.8f;
            return;
        }
        float ratio = (float) shapeAsset.getExpectedSpeed() / (float) speed;
        this.efficiency = Math.min(ratio, 1.0f);
    }
}
