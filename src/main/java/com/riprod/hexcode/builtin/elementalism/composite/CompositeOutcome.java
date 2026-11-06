package com.riprod.hexcode.builtin.elementalism.composite;

import java.util.List;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.hexcode.builtin.elementalism.composite.action.CompositeAction;
import com.riprod.hexcode.builtin.elementalism.composite.action.CompositeActions;
import com.riprod.hexcode.core.common.effects.registry.GlyphEffects;

public final class CompositeOutcome {
    private static final Codec<Retention> RETENTION_CODEC = new EnumCodec<>(Retention.class);

    public static final BuilderCodec<CompositeOutcome> CODEC;
    public static final Validator<CompositeOutcome> ENTITY_VALIDATOR = new TargetValidator(true);
    public static final Validator<CompositeOutcome> BLOCK_VALIDATOR = new TargetValidator(false);

    private Retention existing = Retention.KEEP;
    private Retention incoming = Retention.KEEP;
    private String effectsId;
    private List<CompositeAction> actions = List.of();

    private CompositeOutcome() {
    }

    public Retention getExisting() {
        return this.existing;
    }

    public Retention getIncoming() {
        return this.incoming;
    }

    @Nullable
    public GlyphEffects getEffects() {
        return this.effectsId != null ? GlyphEffects.getAssetMap().getAsset(this.effectsId) : null;
    }

    public List<CompositeAction> getActions() {
        return this.actions;
    }

    private record TargetValidator(boolean entity) implements Validator<CompositeOutcome> {
        @Override
        public void accept(CompositeOutcome outcome, ValidationResults results) {
            if (outcome == null) {
                return;
            }
            for (var action : outcome.actions) {
                if (this.entity ? !action.supportsEntity() : !action.supportsBlock()) {
                    results.fail("Action '" + CompositeActions.CODEC.getIdFor(action.getClass())
                            + "' does not support " + (this.entity ? "entities" : "blocks"));
                }
            }
        }

        @Override
        public void updateSchema(SchemaContext context, Schema target) {
        }
    }

    static {
        CODEC = BuilderCodec.builder(CompositeOutcome.class, CompositeOutcome::new)
                .append(new KeyedCodec<>("Existing", RETENTION_CODEC),
                        (o, v) -> o.existing = v, o -> o.existing)
                .addValidator(Validators.nonNull())
                .documentation("Whether the status already on the host stays or is removed")
                .add()
                .append(new KeyedCodec<>("Incoming", RETENTION_CODEC),
                        (o, v) -> o.incoming = v, o -> o.incoming)
                .addValidator(Validators.nonNull())
                .documentation("Whether the incoming tier still applies or is consumed by the composite")
                .add()
                .append(new KeyedCodec<>("Effects", GlyphEffects.CHILD_ASSET_CODEC),
                        (o, v) -> o.effectsId = v, o -> o.effectsId)
                .addValidatorLate(() -> GlyphEffects.VALIDATOR_CACHE.getValidator().late())
                .documentation("Effects composed over the incoming tier's landing")
                .add()
                .append(new KeyedCodec<>("Actions", new ArrayCodec<>(CompositeActions.CODEC, CompositeAction[]::new)),
                        (o, v) -> o.actions = List.of(v), o -> o.actions.toArray(CompositeAction[]::new))
                .documentation("Actions run when the composite fires, before the existing status is resolved")
                .add()
                .build();
    }
}
