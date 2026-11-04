package com.riprod.hexcode.builtin.elementalism.tier;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.hexcode.builtin.elementalism.status.StatusAsset;

public abstract class EffectElement extends ElementalTier {
    public static final BuilderCodec<EffectElement> BASE_CODEC;

    protected String status;

    public String getStatusId() {
        return this.status;
    }

    private static final class VisibleStatusValidator implements Validator<String> {
        @Override
        public void accept(String statusId, ValidationResults results) {
            if (statusId == null) {
                return;
            }
            var status = StatusAsset.getAssetMap().getAsset(statusId);
            if (status != null && status.isHidden()) {
                results.fail("Status '" + statusId + "' is Hidden and can only be applied by a composite");
            }
        }

        @Override
        public void updateSchema(SchemaContext context, Schema target) {
        }
    }

    static {
        BASE_CODEC = BuilderCodec.abstractBuilder(EffectElement.class, ElementalTier.BASE_CODEC)
                .append(new KeyedCodec<>("Status", Codec.STRING),
                        (t, v) -> t.status = v, t -> t.status)
                .addValidator(Validators.nonNull())
                .addValidator(StatusAsset.VALIDATOR_CACHE.getValidator())
                .addValidator(new VisibleStatusValidator())
                .documentation("Status applied by this tier. Hidden statuses are rejected.")
                .add()
                .build();
    }
}
