package com.riprod.hexcode.builtin.hexCore.glyphs.arc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.riprod.hexcode.core.common.construct.state.ConstructState;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;

public class ArcState implements ConstructState {

    private Glyph arcGlyph;
    private List<String> branches;
    private int branchIndex;
    private Set<UUID> visited;
    private float maxJumpDistance;
    private float delay;
    private float elapsedSeconds;
    private boolean hasFired;

    public ArcState() {
        this.branches = new ArrayList<>();
        this.visited = new HashSet<>();
    }

    public ArcState(Glyph arcGlyph, List<String> branches, Set<UUID> visited,
            float maxJumpDistance, float delay) {
        this.arcGlyph = arcGlyph;
        this.branches = branches;
        this.branchIndex = 0;
        this.visited = visited;
        this.maxJumpDistance = maxJumpDistance;
        this.delay = delay;
        this.elapsedSeconds = 0f;
        this.hasFired = false;
    }

    public Glyph getArcGlyph() {
        return arcGlyph;
    }

    public List<String> getBranches() {
        return branches;
    }

    public int getBranchIndex() {
        return branchIndex;
    }

    public String getCurrentBranch() {
        if (branchIndex >= branches.size()) return null;
        return branches.get(branchIndex);
    }

    public void advanceBranch() {
        branchIndex++;
    }

    public boolean hasMoreBranches() {
        return branchIndex < branches.size();
    }

    public Set<UUID> getVisited() {
        return visited;
    }

    public float getMaxJumpDistance() {
        return maxJumpDistance;
    }

    public float getDelay() {
        return delay;
    }

    public float getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void tick(float dt) {
        elapsedSeconds += dt;
    }

    public void resetTimer() {
        elapsedSeconds = 0f;
    }

    public boolean hasFired() {
        return hasFired;
    }

    public void setHasFired(boolean hasFired) {
        this.hasFired = hasFired;
    }

    @Override
    public ArcState copy() {
        ArcState c = new ArcState(arcGlyph, new ArrayList<>(branches),
                new HashSet<>(visited), maxJumpDistance, delay);
        c.branchIndex = this.branchIndex;
        c.elapsedSeconds = this.elapsedSeconds;
        c.hasFired = this.hasFired;
        return c;
    }
}
