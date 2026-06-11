package com.riprod.hexcode.builtin.glyphs.ensnare.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import java.util.UUID;

import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

class EnsnareComponentTest {

    @Test
    void findNearestSpikeUsesHitRadiusAndVerticalWindow() {
        SpikeEntry near = new SpikeEntry(new Vector3d(1.5, 4.0, 1.5), null);
        SpikeEntry far = new SpikeEntry(new Vector3d(6.5, 4.0, 6.5), null);
        EnsnareComponent ensnare = new EnsnareComponent(
                List.of(near, far), 5f, 4f, 1f, new Vector3d(0, 0, 0), 4.0);

        assertSame(near, ensnare.findNearestSpike(new Vector3d(1.6, 4.5, 1.4), 0.7 * 0.7));
        assertNull(ensnare.findNearestSpike(new Vector3d(1.6, 6.0, 1.4), 0.7 * 0.7));
        assertNull(ensnare.findNearestSpike(new Vector3d(8.0, 4.5, 8.0), 0.7 * 0.7));
    }

    @Test
    void cloneCopiesCenterAndDamageCooldownState() {
        UUID targetId = UUID.randomUUID();
        EnsnareComponent ensnare = new EnsnareComponent(
                List.of(), 5f, 4f, 1f, new Vector3d(1, 2, 3), 4.0);
        ensnare.recordDamage(targetId);

        EnsnareComponent copy = ensnare.clone();

        assertEquals(new Vector3d(1, 2, 3), copy.getCenter());
        assertNotSame(ensnare.getCenter(), copy.getCenter());
        assertEquals(ensnare.canDamageTarget(targetId), copy.canDamageTarget(targetId));
    }
}
