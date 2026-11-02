package com.riprod.hexcode.builtin.hexCore.glyphs.elements.snap;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.api.event.GlyphFizzleEvent;
import com.riprod.hexcode.api.execution.HexExecuter;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementGlyphAsset;
import com.riprod.hexcode.builtin.hexCore.glyphs.elements.ElementSupport;
import com.riprod.hexcode.core.common.execution.context.HexContext;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;

public class SnapGlyph extends ElementGlyphAsset {

    public static final String TYPE = "Snap";

    private String damageCause = "Ice";
    private String onHitEffect = "Ice";
    private float onHitDuration = 1.5f;

    public static final BuilderCodec<SnapGlyph> CODEC = BuilderCodec
            .builder(SnapGlyph.class, SnapGlyph::new, ElementGlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("DamageCause", Codec.STRING, true),
                    (c, v) -> c.damageCause = v, c -> c.damageCause)
            .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("OnHitEffect", Codec.STRING, true),
                    (c, v) -> c.onHitEffect = v, c -> c.onHitEffect)
            .addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late())
            .add()
            .append(new KeyedCodec<>("OnHitDuration", Codec.FLOAT, true),
                    (c, v) -> c.onHitDuration = v, c -> c.onHitDuration)
            .add()
            .build();

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        Ref<EntityStore> target = ElementSupport.resolveTarget(glyph, hexContext);
        if (target == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Snap must target an entity");
            return;
        }

        float affinity = ElementSupport.affinityFactor(hexContext, affinityStat, affinityScale);
        float limit = ElementSupport.resourceLimit(glyph, this, hexContext);
        float amount = ElementSupport.consumeResource(hexContext, glyph, resource, limit)
                * efficiency * affinity;

        DamageCause cause = DamageCause.getAssetMap().getAsset(damageCause);
        if (cause != null) {
            Ref<EntityStore> caster = hexContext.getRootRef();
            Damage.Source source = caster != null && caster.isValid()
                    ? new Damage.EntitySource(caster)
                    : new Damage.EnvironmentSource("Magic");
            DamageSystems.executeDamage(target, hexContext.getAccessor(),
                    new Damage(source, cause, amount));
        }

        if (onHitEffect != null && !onHitEffect.isEmpty()) {
            ElementSupport.applyStatus(hexContext, target, glyph,
                    onHitEffect, onHitDuration);
        }

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
