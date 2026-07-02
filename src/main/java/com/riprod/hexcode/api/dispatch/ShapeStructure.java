package com.riprod.hexcode.api.dispatch;

import java.util.List;

import com.riprod.hexcode.core.common.drawing.component.DrawnShapeComponent;

public class ShapeStructure {

    private final List<DrawnShapeComponent> shapes;
    private final float efficiency;
    private final float volatility;

    public ShapeStructure(List<DrawnShapeComponent> shapes, float efficiency, float volatility) {
        this.shapes = shapes;
        this.efficiency = efficiency;
        this.volatility = volatility;
    }

    public List<DrawnShapeComponent> getShapes() {
        return shapes;
    }

    public float getEfficiency() {
        return efficiency;
    }

    public float getVolatility() {
        return volatility;
    }
}
