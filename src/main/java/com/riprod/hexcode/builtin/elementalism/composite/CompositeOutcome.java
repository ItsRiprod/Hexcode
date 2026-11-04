package com.riprod.hexcode.builtin.elementalism.composite;

import java.util.List;

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
import com.riprod.hexcode.builtin.elementalism.effects.ElementEffects;

public final class CompositeOutcome {
    private static final Codec<Retention> RETENTION_CODEC = new EnumCodec<>(Retention.class);

    public static final BuilderCodec<CompositeOutcome> CODEC;
    public static final Validator<CompositeOutcome> ENTITY_VALIDATOR = new TargetValidator(true);
    public static final Validator<CompositeOutcome> BLOCK_VALIDATOR = new TargetValidator(false);

    private Retention existing = Retention.KEEP;
    private Retention incoming = Retention.KEEP;
    private ElementEffects effects = new ElementEffects();
    private List<CompositeAction> actions = List.of();

    private CompositeOutcome() {
    }

    public Retention getExisting() {
        return this.existing;
    }

    public Retention getIncoming() {
        return this.incoming;
    }

    public ElementEffects getEffects() {
        return this.effects;
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
                .append(new KeyedCodec<>("Effects", ElementEffects.CODEC),
                        (o, v) -> o.effects = v, o -> o.effects)
                .documentation("Overrides for the incoming tier's effects, only the slots set here")
                .add()
                .append(new KeyedCodec<>("Actions", new ArrayCodec<>(CompositeActions.CODEC, CompositeAction[]::new)),
                        (o, v) -> o.actions = List.of(v), o -> o.actions.toArray(CompositeAction[]::new))
                .documentation("Actions run when the composite fires, before the existing status is resolved")
                .add()
                .build();
    }
}
