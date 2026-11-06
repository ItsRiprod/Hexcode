package com.riprod.hexcode.builtin.hexCore.obelisks.seeker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Vector3d;
import org.joml.Vector3i;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.riprod.hexcode.builtin.hexCore.contexts.crafting.nodes.slot.named.NamedSlot;
import com.riprod.hexcode.builtin.hexCore.obelisks.ObeliskStatus;
import com.riprod.hexcode.builtin.hexCore.glyphs.utilities.slot.SlotGlyphSlots;
import com.riprod.hexcode.core.common.glyphs.component.Glyph;
import com.riprod.hexcode.core.common.glyphs.component.Slot;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;

public class HexNamePage extends InteractiveCustomUIPage<HexNamePage.PageEventData> {

    private static final int MAX_NAME_LENGTH = 32;

    private final Vector3i obeliskPosition;
    private final String targetGlyphId;
    private final Ref<EntityStore> targetNodeRef;

    public HexNamePage(@Nonnull PlayerRef playerRef, Vector3i obeliskPosition) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, PageEventData.CODEC);
        this.obeliskPosition = obeliskPosition;
        this.targetGlyphId = null;
        this.targetNodeRef = null;
    }

    private HexNamePage(@Nonnull PlayerRef playerRef, String targetGlyphId,
            @Nullable Ref<EntityStore> targetNodeRef) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, PageEventData.CODEC);
        this.obeliskPosition = null;
        this.targetGlyphId = targetGlyphId;
        this.targetNodeRef = targetNodeRef;
    }

    public static HexNamePage forSlotGlyph(@Nonnull PlayerRef playerRef, String targetGlyphId,
            @Nullable Ref<EntityStore> targetNodeRef) {
        return new HexNamePage(playerRef, targetGlyphId, targetNodeRef);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {

        cmd.append("Hexcode/Obelisks/HexName.ui");

        String current;
        if (targetGlyphId != null) {
            NamedSlot named = resolveNameSlot(ref, store);
            current = named != null && named.getValue() != null ? named.getValue() : "";
        } else {
            Hex hex = resolveActiveHex(ref, store);
            current = hex != null && hex.getDisplayName() != null ? hex.getDisplayName() : "";
        }
        cmd.set("#NameInput.Value", current);
        cmd.set("#NameInput.MaxLength", MAX_NAME_LENGTH);
        writeInitialStatus(cmd, ref, store);

        var submit = EventData.of("@Name", "#NameInput.Value");
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ConfirmButton", submit);
        events.addEventBinding(CustomUIEventBindingType.Validating, "#NameInput", submit);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
            @Nonnull PageEventData data) {

        HexcodeSessionComponent session = SessionUtils.resolveSessionByPlayer(ref, store);
        if (session == null) {
            showError("hexcode.pages.obelisk.not_crafting");
            return;
        }

        Ref<EntityStore> ownerRef = session.getOwnerRef();
        if (ownerRef == null || !ownerRef.isValid() || !ownerRef.equals(ref)) {
            showError("hexcode.pages.obelisk.not_owner");
            return;
        }

        String slotKey = session.getActiveSlotKey();
        if (slotKey == null) {
            showError("hexcode.pages.obelisk.no_slot");
            return;
        }

        Hex hex = resolveActiveHex(ref, store);
        if (hex == null) {
            showError("hexcode.obelisks.name.no_hex");
            return;
        }

        String name = data.name != null ? data.name.trim() : "";
        if (name.length() > MAX_NAME_LENGTH) {
            name = name.substring(0, MAX_NAME_LENGTH);
        }

        if (targetGlyphId != null) {
            NamedSlot named = resolveNameSlot(ref, store);
            if (named == null) {
                showError("hexcode.obelisks.name.slot_gone");
                return;
            }
            named.setValue(name.isEmpty() ? null : name);
            refreshNodeLabel(store, named);
        } else {
            hex.setDisplayName(name.isEmpty() ? null : name);
        }

        if (obeliskPosition != null) {
            spawnParticleEffect(new Vector3d(obeliskPosition), store);
        }
        close();
    }

    private void writeInitialStatus(UICommandBuilder cmd, Ref<EntityStore> ref, Store<EntityStore> store) {
        HexcodeSessionComponent session = SessionUtils.resolveSessionByPlayer(ref, store);
        if (session == null) {
            ObeliskStatus.ERROR.write(cmd, Message.translation("hexcode.pages.obelisk.not_crafting"));
            return;
        }
        String slotKey = session.getActiveSlotKey();
        if (slotKey == null) {
            ObeliskStatus.ERROR.write(cmd, Message.translation("hexcode.pages.obelisk.no_slot"));
            return;
        }
        var message = targetGlyphId != null
                ? Message.translation("hexcode.obelisks.name.status_port")
                : Message.translation("hexcode.obelisks.name.status_hex").param("slot", slotKey);
        ObeliskStatus.NORMAL.write(cmd, message);
    }

    private NamedSlot resolveNameSlot(Ref<EntityStore> ref, Store<EntityStore> store) {
        Hex hex = resolveActiveHex(ref, store);
        Glyph target = hex != null && targetGlyphId != null ? hex.get(targetGlyphId) : null;
        Slot slot = target != null ? target.getOrCreateSlot(SlotGlyphSlots.NAME) : null;
        return slot instanceof NamedSlot named ? named : null;
    }

    private void refreshNodeLabel(Store<EntityStore> store, NamedSlot named) {
        Ref<EntityStore> node = targetNodeRef;
        if (node == null) return;
        store.getExternalData().getWorld().execute(() -> {
            if (!node.isValid()) return;
            store.putComponent(node, DisplayNameComponent.getComponentType(),
                    new DisplayNameComponent(named.displayMessage()));
        });
    }

    private Hex resolveActiveHex(Ref<EntityStore> ref, Store<EntityStore> store) {
        HexcodeSessionComponent session = SessionUtils.resolveSessionByPlayer(ref, store);
        if (session == null) {
            return null;
        }
        Ref<EntityStore> activeHexRef = session.getActiveContainerRef();
        if (activeHexRef == null || !activeHexRef.isValid()) {
            return null;
        }
        HexComponent hexComp = store.getComponent(activeHexRef, HexComponent.getComponentType());
        return hexComp != null ? hexComp.getHex() : null;
    }

    private void showError(String translationKey) {
        var cmd = new UICommandBuilder();
        ObeliskStatus.ERROR.write(cmd, Message.translation(translationKey));
        sendUpdate(cmd);
    }

    private static void spawnParticleEffect(Vector3d position, Store<EntityStore> store) {
        ParticleUtil.spawnParticleEffect("ForgottenTemple_Beam", position.add(0.5, 0.5, 0.5), 0, 0,
                (float) Math.toRadians(-90), 1, 100, store);
    }

    public static class PageEventData {
        public String name;

        public static final BuilderCodec<PageEventData> CODEC = BuilderCodec
                .builder(PageEventData.class, PageEventData::new)
                .append(new KeyedCodec<>("@Name", Codec.STRING),
                        (d, v) -> d.name = v, d -> d.name)
                .add()
                .build();
    }
}
