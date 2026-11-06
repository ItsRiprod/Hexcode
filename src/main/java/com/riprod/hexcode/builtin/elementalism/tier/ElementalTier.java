package com.riprod.hexcode.builtin.elementalism.tier;

import javax.annotation.Nullable;

import org.joml.Vector3i;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.effects.registry.GlyphEffects;
import com.riprod.hexcode.core.common.execution.context.HexContext;

public abstract class ElementalTier {
    public static final CodecMapCodec<ElementalTier> CODEC = new CodecMapCodec<>("Type");
    public static final BuilderCodec<ElementalTier> BASE_CODEC;

    protected String id;
    protected String name;
    protected String description;
    protected String lore;
    protected String effectsId;

    public String getId() {
        return this.id;
    }

    @Nullable
    public String getName() {
        return this.name;
    }

    @Nullable
    public String getDescription() {
        return this.description;
    }

    @Nullable
    public String getLore() {
        return this.lore;
    }

    @Nullable
    public GlyphEffects getEffects() {
        return this.effectsId != null ? GlyphEffects.getAssetMap().getAsset(this.effectsId) : null;
    }

    public boolean supportsEntity() {
        return false;
    }

    public boolean supportsBlock() {
        return false;
    }

    public boolean applyEntity(Ref<EntityStore> target, HexContext ctx, CommandBuffer<EntityStore> buffer) {
        return false;
    }

    public boolean applyBlock(Vector3i block, HexContext ctx, CommandBuffer<EntityStore> buffer) {
        return false;
    }

    protected static Damage.Source damageSource(HexContext ctx) {
        var caster = ctx.getRootRef();
        return caster.isValid() ? new Damage.EntitySource(caster) : Damage.NULL_SOURCE;
    }

    static {
        BASE_CODEC = BuilderCodec.abstractBuilder(ElementalTier.class)
                .append(new KeyedCodec<>("Id", Codec.STRING),
                        (t, v) -> t.id = v, t -> t.id)
                .addValidator(Validators.nonNull())
                .addValidator(Validators.nonEmptyString())
                .documentation("Stable id composites reference, unique across every element")
                .add()
                .append(new KeyedCodec<>("Name", Codec.STRING),
                        (t, v) -> t.name = v, t -> t.name)
                .documentation("Translation key for the tier name")
                .add()
                .append(new KeyedCodec<>("Description", Codec.STRING),
                        (t, v) -> t.description = v, t -> t.description)
                .documentation("Translation key for the tier description")
                .add()
                .append(new KeyedCodec<>("Lore", Codec.STRING),
                        (t, v) -> t.lore = v, t -> t.lore)
                .documentation("Translation key for the in-world lore of the tier")
                .add()
                .append(new KeyedCodec<>("Effects", GlyphEffects.CHILD_ASSET_CODEC),
                        (t, v) -> t.effectsId = v, t -> t.effectsId)
                .addValidatorLate(() -> GlyphEffects.VALIDATOR_CACHE.getValidator().late())
                .documentation("Effects composed over the element while this tier is reached; Hit is the landing at discharge")
                .add()
                .build();
    }
}
