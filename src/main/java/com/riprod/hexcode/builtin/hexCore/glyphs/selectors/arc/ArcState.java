package com.riprod.hexcode.builtin.hexCore.glyphs.selectors.arc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.riprod.hexcode.core.common.construct.state.ConstructState;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;

public class ArcState implements ConstructState {

    private Glyph arcGlyph;
    private List<String> outputLinks;
    private Set<UUID> exclusions;
    private Set<UUID> visited;
    private float range;
    private float interval;
    private int remainingIterations;
    private float elapsedSeconds;
    private boolean spawnedHost;

    public ArcState() {
        this.outputLinks = new ArrayList<>();
        this.exclusions = new HashSet<>();
        this.visited = new HashSet<>();
    }

    public ArcState(Glyph arcGlyph, List<String> outputLinks, Set<UUID> exclusions,
            float range, float interval, int remainingIterations, boolean spawnedHost) {
        this.arcGlyph = arcGlyph;
        this.outputLinks = outputLinks;
        this.exclusions = new HashSet<>(exclusions);
        this.visited = new HashSet<>(exclusions);
        this.range = range;
        this.interval = interval;
        this.remainingIterations = remainingIterations;
        this.elapsedSeconds = 0f;
        this.spawnedHost = spawnedHost;
    }

    public Glyph getArcGlyph() {
        return arcGlyph;
    }

    public List<String> getOutputLinks() {
        return outputLinks;
    }

    public Set<UUID> getVisited() {
        return visited;
    }

    public boolean hasHitThisCycle() {
        return visited.size() > exclusions.size();
    }

    public void resetCycle() {
        visited = new HashSet<>(exclusions);
    }

    public float getRange() {
        return range;
    }

    public float getInterval() {
        return interval;
    }

    public int getRemainingIterations() {
        return remainingIterations;
    }

    public void consumeIteration() {
        remainingIterations--;
    }

    public boolean isSpawnedHost() {
        return spawnedHost;
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

    @Override
    public ArcState copy() {
        ArcState c = new ArcState(arcGlyph, new ArrayList<>(outputLinks),
                exclusions, range, interval, remainingIterations, spawnedHost);
        c.visited = new HashSet<>(this.visited);
        c.elapsedSeconds = this.elapsedSeconds;
        return c;
    }
}
