package com.riprod.hexcode.builtin.hexCore.obelisks.seeker;

import javax.annotation.Nonnull;

import org.joml.Vector3d;
import org.joml.Vector3i;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;

public class HexNamePage extends InteractiveCustomUIPage<HexNamePage.PageEventData> {

    private static final int MAX_NAME_LENGTH = 32;

    private final Vector3i obeliskPosition;

    public HexNamePage(@Nonnull PlayerRef playerRef, Vector3i obeliskPosition) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, PageEventData.CODEC);
        this.obeliskPosition = obeliskPosition;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {

        cmd.append("Hexcode/Obelisks/HexName.ui");

        Hex hex = resolveActiveHex(ref, store);
        String current = hex != null && hex.getDisplayName() != null ? hex.getDisplayName() : "";
        cmd.set("#NameInput.Value", current);
        cmd.set("#StatusLabel.Text", initialStatus(ref, store));

        events.addEventBinding(
                CustomUIEventBindingType.Activating, "#ConfirmButton",
                EventData.of("@Name", "#NameInput.Value"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
            @Nonnull PageEventData data) {

        HexcodeSessionComponent session = SessionUtils.resolveSessionByPlayer(ref, store);
        if (session == null) {
            updateStatus("No active crafting session.");
            return;
        }

        Ref<EntityStore> ownerRef = session.getOwnerRef();
        if (ownerRef == null || !ownerRef.isValid() || !ownerRef.equals(ref)) {
            updateStatus("You don't own this pedestal.");
            return;
        }

        String slotKey = session.getActiveSlotKey();
        if (slotKey == null) {
            updateStatus("Select a slot first.");
            return;
        }

        Hex hex = resolveActiveHex(ref, store);
        if (hex == null) {
            updateStatus("No hex is being crafted.");
            return;
        }

        String name = data.name != null ? data.name.trim() : "";
        if (name.length() > MAX_NAME_LENGTH) {
            name = name.substring(0, MAX_NAME_LENGTH);
        }

        if (name.isEmpty()) {
            hex.setDisplayName(null);
            updateStatus("Name cleared.");
        } else {
            hex.setDisplayName(name);
            updateStatus("Named \"" + name + "\".");
        }

        UICommandBuilder cmd = new UICommandBuilder();
        cmd.set("#NameInput.Value", name);
        sendUpdate(cmd);

        spawnParticleEffect(new Vector3d(obeliskPosition), store);
    }

    private String initialStatus(Ref<EntityStore> ref, Store<EntityStore> store) {
        HexcodeSessionComponent session = SessionUtils.resolveSessionByPlayer(ref, store);
        if (session == null) {
            return "No active crafting session.";
        }
        String slotKey = session.getActiveSlotKey();
        if (slotKey == null) {
            return "Select a slot first.";
        }
        return "Naming the hex in slot " + slotKey + ".";
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

    private void updateStatus(String message) {
        UICommandBuilder cmd = new UICommandBuilder();
        cmd.set("#StatusLabel.Text", message);
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
