package com.riprod.hexcode.builtin.hexCore.glyphs.elements.bolt;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
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

public class BoltGlyph extends ElementGlyphAsset {

    public static final String TYPE = "Bolt";

    private String damageCause = "Lightning";

    public static final BuilderCodec<BoltGlyph> CODEC = BuilderCodec
            .builder(BoltGlyph.class, BoltGlyph::new, ElementGlyphAsset.ABSTRACT_CODEC)
            .append(new KeyedCodec<>("DamageCause", Codec.STRING, true),
                    (c, v) -> c.damageCause = v, c -> c.damageCause)
            .addValidatorLate(() -> DamageCause.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    @Override
    public void execute(Glyph glyph, HexContext hexContext) {
        Ref<EntityStore> target = ElementSupport.resolveTarget(glyph, hexContext);
        if (target == null) {
            HexExecuter.fail(glyph, hexContext, GlyphFizzleEvent.Reason.HANDLER_FAILED,
                    "Bolt must target an entity");
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

        HexExecuter.continueFromSlot(glyph, Glyph.NEXT_SLOT, hexContext);
    }
}
