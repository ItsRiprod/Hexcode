package com.riprod.hexcode.core.common.obelisk.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.riprod.hexcode.core.common.obelisk.registry.ObeliskHandlerKeyValidator;
import com.riprod.hexcode.core.common.obelisk.registry.ObeliskHandlerRegistry;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

public class ObeliskBlockComponent implements Component<ChunkStore> {

    public static final BuilderCodec<ObeliskBlockComponent> CODEC =
        BuilderCodec.builder(ObeliskBlockComponent.class, ObeliskBlockComponent::new)
            .append(
                new KeyedCodec<>("Power", Codec.INTEGER),
                (state, power) -> state.power = power,
                state -> state.power
            )
            .add()
            .append(
                new KeyedCodec<>("HandlerId", Codec.STRING),
                (state, v) -> state.handlerId = v,
                state -> state.handlerId
            )
                .metadata(new UIEditor(new UIEditor.Dropdown("HexcodeObeliskHandlers")))
                .addValidatorLate(() -> ObeliskHandlerKeyValidator.INSTANCE.late())
            .add()
            .build();

    private static ComponentType<ChunkStore, ObeliskBlockComponent> componentType;

    public static void setComponentType(ComponentType<ChunkStore, ObeliskBlockComponent> type) {
        componentType = type;
    }

    public static ComponentType<ChunkStore, ObeliskBlockComponent> getComponentType() {
        return componentType;
    }

    private int power = 1;
    private String handlerId = "";
    private Vector3i registeredPedestalLoc = null;

    public int getPower() {
        return power;
    }

    public String getHandlerId() {
        return handlerId;
    }

    public Vector3i getRegisteredPedestalLoc() {
        return registeredPedestalLoc;
    }

    public void setRegisteredPedestalLoc(Vector3i loc) {
        this.registeredPedestalLoc = loc;
    }

    public boolean isRegistered() {
        return registeredPedestalLoc != null;
    }

    public void clearRegistration() {
        this.registeredPedestalLoc = null;
    }

    @Nonnull
    @Override
    public Component<ChunkStore> clone() {
        ObeliskBlockComponent copy = new ObeliskBlockComponent();
        copy.power = this.power;
        copy.handlerId = this.handlerId;
        copy.registeredPedestalLoc = this.registeredPedestalLoc != null ? new Vector3i(this.registeredPedestalLoc) : null;
        return copy;
    }
}
