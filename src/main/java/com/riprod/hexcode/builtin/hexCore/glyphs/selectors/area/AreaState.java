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
    private double totalBlocks;
    private float costPerBlock;
    private List<String> entityLinks;
    private List<String> blockLinks;

    private double sweptBlocks;
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
        this.totalBlocks = totalBlocks;
        this.costPerBlock = costPerBlock;
        this.entityLinks = new ArrayList<>(entityLinks);
        this.blockLinks = new ArrayList<>(blockLinks);
        this.sweptBlocks = 0.0;
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

    public double getSweptBlocks() {
        return sweptBlocks;
    }

    public double advanceSweep(float dt) {
        double previous = sweptBlocks;
        sweptBlocks = Math.min(totalBlocks, sweptBlocks + blocksPerSecond * dt);
        return sweptBlocks - previous;
    }

    public boolean isComplete() {
        return sweptBlocks >= totalBlocks;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
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
        c.sweptBlocks = this.sweptBlocks;
        c.scale = this.scale;
        c.firedEntities = new HashSet<>(this.firedEntities);
        return c;
    }
}
