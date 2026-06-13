package com.riprod.hexcode.builtin.obelisks.importexport;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.joml.Vector3d;
import org.joml.Vector3f;
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
import com.riprod.hexcode.core.common.hexes.codec.DecodeIssue;
import com.riprod.hexcode.core.common.hexes.codec.DecodeResult;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;
import com.riprod.hexcode.core.state.crafting.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.state.crafting.session.SessionUtils;
import com.riprod.hexcode.utils.VfxUtil;

public class ImportExportPage extends InteractiveCustomUIPage<ImportExportPage.PageEventData> {

    private static final int MAX_ISSUE_LINE = 80;

    private String fieldValue = "";

    private Vector3i obeliskPosition;

    public ImportExportPage(@Nonnull PlayerRef playerRef, Vector3i obeliskPosition) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, PageEventData.CODEC);
        this.obeliskPosition = obeliskPosition;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {

        cmd.append("Hexcode/Obelisks/ImportExport.ui");
        cmd.set("#StatusLabel.Text", initialStatus(ref, store));
        cmd.set("#DataField.Value", fieldValue);

        events.addEventBinding(
                CustomUIEventBindingType.ValueChanged, "#DataField",
                EventData.of("Action", "sync").append("@Data", "#DataField.Value"),
                false);

        events.addEventBinding(
                CustomUIEventBindingType.Activating, "#ExportButton",
                EventData.of("Action", "export"));

        events.addEventBinding(
                CustomUIEventBindingType.Activating, "#ImportButton",
                EventData.of("Action", "import"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
            @Nonnull PageEventData data) {

        if ("sync".equals(data.action)) {
            // pasted codes pick up trailing/wrapping whitespace that breaks base64 decode; valid codes never contain interior whitespace
            fieldValue = data.data != null ? data.data.replaceAll("\\s+", "") : "";
        } else if ("export".equals(data.action)) {
            handleExport(ref, store);
        } else if ("import".equals(data.action)) {
            handleImport(ref, store);
        }
    }

    private String initialStatus(Ref<EntityStore> ref, Store<EntityStore> store) {
        HexcodeSessionComponent session = SessionUtils.resolveSessionByPlayer(ref, store);
        if (session == null) {
            return "No active crafting session.";
        }
        String slotKey = session.getActiveSlotKey();
        if (slotKey == null) {
            return "Select a slot, then Export to copy its hex or paste data and Import to write it.";
        }
        return "Slot " + slotKey + " active.";
    }

    private void handleExport(Ref<EntityStore> ref, Store<EntityStore> store) {
        HexcodeSessionComponent session = SessionUtils.resolveSessionByPlayer(ref, store);
        if (session == null) {
            updateStatus("No active crafting session.");
            return;
        }

        Ref<EntityStore> ownerRef = session.getOwnerRef();
        if (ownerRef == null || !ownerRef.isValid() || !ownerRef.equals(ref)) {
            updateStatus("You don't have permission to export from this pedestal.");
            return;
        }

        String slotKey = session.getActiveSlotKey();
        if (slotKey == null) {
            updateStatus("Select a slot first.");
            return;
        }

        Hex hex = null;
        Ref<EntityStore> activeHexRef = SessionUtils.findPreviewForSlot(session, slotKey);
        if (activeHexRef != null && activeHexRef.isValid()) {
            HexComponent hexComp = store.getComponent(activeHexRef, HexComponent.getComponentType());
            if (hexComp != null) {
                hex = hexComp.getHex();
            }
        }
        if (hex == null || hex.getGlyphs().isEmpty()) {
            hex = session.getHexAt(slotKey, store);
        }
        if (hex == null || hex.getGlyphs().isEmpty()) {
            updateStatus("Slot " + slotKey + " is empty — nothing to export.");
            return;
        }

        String encoded;
        try {
            encoded = HexUtils.serialize(hex);
        } catch (Exception e) {
            updateStatus("Failed to serialize hex: " + truncate(String.valueOf(e.getMessage()), MAX_ISSUE_LINE));
            return;
        }
        if (encoded == null) {
            updateStatus("Failed to serialize hex.");
            return;
        }

        UICommandBuilder cmd = new UICommandBuilder();
        cmd.set("#DataField.Value", encoded);
        cmd.set("#StatusLabel.Text", "Exported hex from slot " + slotKey + ".");
        sendUpdate(cmd);
        SpawnParticleEffect(new Vector3d(obeliskPosition), store);
    }

    private void handleImport(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (fieldValue.isEmpty()) {
            updateStatus("Paste hex data first.");
            return;
        }

        long t0 = System.nanoTime();
        DecodeResult result = HexUtils.deserializeWithResult(fieldValue);
        if (!result.isOk() || result.getHex() == null) {
            updateStatus("Import failed:\n" + formatIssues(result.getIssues(), 4));
            return;
        }
        Hex hex = result.getHex();
        HexUtils.validate(hex);
        long elapsedMs = (System.nanoTime() - t0) / 1_000_000L;

        HexcodeSessionComponent importSession = SessionUtils.resolveSessionByPlayer(ref, store);
        if (importSession == null) {
            updateStatus("Not in a crafting session.");
            return;
        }

        Ref<EntityStore> ownerRef2 = importSession.getOwnerRef();
        if (ownerRef2 == null || !ownerRef2.isValid() || !ownerRef2.equals(ref)) {
            updateStatus("You don't own this pedestal.");
            return;
        }

        String slotKey = importSession.getActiveSlotKey();
        if (slotKey == null) {
            updateStatus("Select a slot first.");
            return;
        }

        importSession.setPendingImportHex(hex);

        StringBuilder msg = new StringBuilder();
        msg.append("Imported into ").append(slotKey).append(" in ").append(elapsedMs).append("ms.");
        String warnings = formatIssues(filterNonInfo(result.getIssues()), 3);
        if (!warnings.isEmpty()) {
            msg.append('\n').append(warnings);
        }
        updateStatus(msg.toString());

        SpawnParticleEffect(new Vector3d(obeliskPosition), store);
    }

    private void updateStatus(String message) {
        UICommandBuilder cmd = new UICommandBuilder();
        cmd.set("#StatusLabel.Text", message);
        sendUpdate(cmd);
    }

    private static List<DecodeIssue> filterNonInfo(List<DecodeIssue> issues) {
        List<DecodeIssue> out = new ArrayList<>();
        if (issues == null)
            return out;
        for (DecodeIssue issue : issues) {
            if (issue.getSeverity() != DecodeIssue.Severity.INFO) {
                out.add(issue);
            }
        }
        return out;
    }

    private static String formatIssues(List<DecodeIssue> issues, int maxLines) {
        if (issues == null || issues.isEmpty())
            return "";
        Set<String> seen = new LinkedHashSet<>();
        for (DecodeIssue issue : issues) {
            seen.add(truncate(issue.getMessage(), MAX_ISSUE_LINE));
        }
        StringBuilder out = new StringBuilder();
        int rendered = 0;
        for (String line : seen) {
            if (rendered >= maxLines)
                break;
            if (rendered > 0)
                out.append('\n');
            out.append("• ").append(line);
            rendered++;
        }
        int overflow = seen.size() - rendered;
        if (overflow > 0) {
            out.append("\n… (").append(overflow).append(" more)");
        }
        return out.toString();
    }

    private static String truncate(String s, int max) {
        if (s == null)
            return "";
        if (s.length() <= max)
            return s;
        return s.substring(0, Math.max(0, max - 1)) + "…";
    }

    private static void SpawnParticleEffect(Vector3d position, Store<EntityStore> store) {
        ParticleUtil.spawnParticleEffect("ForgottenTemple_Beam", position.add(0.5, 0.5, 0.5), 0, 0, (float) Math.toRadians(-90), 1, 100,
                store);
    }

    public static class PageEventData {
        public String action;
        public String data;

        public static final BuilderCodec<PageEventData> CODEC = BuilderCodec
                .builder(PageEventData.class, PageEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING),
                        (d, v) -> d.action = v, d -> d.action)
                .add()
                .append(new KeyedCodec<>("@Data", Codec.STRING),
                        (d, v) -> d.data = v, d -> d.data)
                .add()
                .build();
    }
}
