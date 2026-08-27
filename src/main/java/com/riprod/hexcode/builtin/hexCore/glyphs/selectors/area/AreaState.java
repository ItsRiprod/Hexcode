package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.area;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.joml.Vector3d;

import com.riprod.hexcode.core.common.construct.state.ConstructState;

public class AreaState implements ConstructState {

    private Vector3d center;
    private Vector3d halfExtents;
    private AreaShape shape;
    private double blocksPerSecond;
    private double scaleRatePerSecond;
    private double totalBlocks;
    private float costPerBlock;
    private List<String> entityLinks;
    private List<String> blockLinks;

    private double scale;
    private Set<UUID> firedEntities;

    public AreaState() {
        this.center = new Vector3d();
        this.halfExtents = new Vector3d();
        this.shape = AreaShape.ELLIPSOID;
        this.entityLinks = new ArrayList<>();
        this.blockLinks = new ArrayList<>();
        this.firedEntities = new HashSet<>();
    }

    public AreaState(Vector3d center, Vector3d halfExtents, AreaShape shape,
            double blocksPerSecond, double totalBlocks, float costPerBlock,
            List<String> entityLinks, List<String> blockLinks) {
        this.center = new Vector3d(center);
        this.halfExtents = new Vector3d(halfExtents);
        this.shape = shape;
        this.blocksPerSecond = blocksPerSecond;
        this.scaleRatePerSecond = blocksPerSecond
                / Math.max(halfExtents.x, Math.max(halfExtents.y, halfExtents.z));
        this.totalBlocks = totalBlocks;
        this.costPerBlock = costPerBlock;
        this.entityLinks = new ArrayList<>(entityLinks);
        this.blockLinks = new ArrayList<>(blockLinks);
        this.scale = 0.0;
        this.firedEntities = new HashSet<>();
    }

    public Vector3d getCenter() {
        return center;
    }

    public Vector3d getHalfExtents() {
        return halfExtents;
    }

    public AreaShape getShape() {
        return shape;
    }

    public double getBlocksPerSecond() {
        return blocksPerSecond;
    }

    public double getTotalBlocks() {
        return totalBlocks;
    }

    public float getCostPerBlock() {
        return costPerBlock;
    }

    public List<String> getEntityLinks() {
        return entityLinks;
    }

    public List<String> getBlockLinks() {
        return blockLinks;
    }

    public boolean isEntitiesWired() {
        return !entityLinks.isEmpty();
    }

    public boolean isBlocksWired() {
        return !blockLinks.isEmpty();
    }

    public boolean isDisplayOnly() {
        return entityLinks.isEmpty() && blockLinks.isEmpty();
    }

    public double advanceScale(float dt) {
        double previous = scale;
        scale = Math.min(1.0, scale + scaleRatePerSecond * dt);
        return totalBlocks * (scale * scale * scale - previous * previous * previous);
    }

    public boolean isComplete() {
        return scale >= 1.0;
    }

    public double getScale() {
        return scale;
    }

    public Vector3d scaledExtents(double at) {
        return new Vector3d(halfExtents.x * at, halfExtents.y * at, halfExtents.z * at);
    }

    public boolean markFired(UUID entityId) {
        return firedEntities.add(entityId);
    }

    @Override
    public AreaState copy() {
        AreaState c = new AreaState(center, halfExtents, shape, blocksPerSecond,
                totalBlocks, costPerBlock, entityLinks, blockLinks);
        c.scale = this.scale;
        c.firedEntities = new HashSet<>(this.firedEntities);
        return c;
    }
}
