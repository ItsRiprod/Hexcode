package com.riprod.hexcode.builtin.hexCore.obelisks.importexport;

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
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.builtin.hexCore.obelisks.ObeliskStatus;
import com.riprod.hexcode.core.common.hexes.codec.DecodeIssue;
import com.riprod.hexcode.core.common.hexes.codec.DecodeResult;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.hexes.component.HexComponent;
import com.riprod.hexcode.core.common.hexes.utils.HexUtils;
import com.riprod.hexcode.core.common.pedestal.session.HexcodeSessionComponent;
import com.riprod.hexcode.core.common.pedestal.session.SessionUtils;
import com.riprod.hexcode.utils.VfxUtil;

public class ImportExportPage extends InteractiveCustomUIPage<ImportExportPage.PageEventData> {

    private static final int MAX_ISSUE_LINE = 80;

    private String exportedValue = "";
    private ObeliskStatus statusTone;
    private Message statusMessage;

    private Vector3i obeliskPosition;

    public ImportExportPage(@Nonnull PlayerRef playerRef, Vector3i obeliskPosition) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, PageEventData.CODEC);
        this.obeliskPosition = obeliskPosition;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {

        cmd.append("Hexcode/Obelisks/ImportExport.ui");
        cmd.set("#DataField.Value", exportedValue);
        if (statusMessage != null) {
            statusTone.write(cmd, statusMessage);
        } else {
            writeInitialStatus(cmd, ref, store);
        }

        events.addEventBinding(
                CustomUIEventBindingType.Activating, "#ExportButton",
                EventData.of("Action", "export"));

        events.addEventBinding(
                CustomUIEventBindingType.Activating, "#ImportButton",
                EventData.of("Action", "import").append("@Data", "#DataField.Value"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
            @Nonnull PageEventData data) {

        if ("export".equals(data.action)) {
            handleExport(ref, store);
        } else if ("import".equals(data.action)) {
            handleImport(ref, store, data.data);
        }
    }

    private void writeInitialStatus(UICommandBuilder cmd, Ref<EntityStore> ref, Store<EntityStore> store) {
        HexcodeSessionComponent session = SessionUtils.resolveSessionByPlayer(ref, store);
        if (session == null) {
            ObeliskStatus.ERROR.write(cmd, Message.translation("hexcode.pages.obelisk.not_crafting"));
            return;
        }
        String slotKey = session.getActiveSlotKey();
        if (slotKey == null) {
            ObeliskStatus.NORMAL.write(cmd, Message.translation("hexcode.obelisks.transfer.hint"));
            return;
        }
        ObeliskStatus.NORMAL.write(cmd,
                Message.translation("hexcode.obelisks.transfer.slot_active").param("slot", slotKey));
    }

    private void handleExport(Ref<EntityStore> ref, Store<EntityStore> store) {
        HexcodeSessionComponent session = SessionUtils.resolveSessionByPlayer(ref, store);
        if (session == null) {
            showStatus(ObeliskStatus.ERROR, Message.translation("hexcode.pages.obelisk.not_crafting"));
            return;
        }

        Ref<EntityStore> ownerRef = session.getOwnerRef();
        if (ownerRef == null || !ownerRef.isValid() || !ownerRef.equals(ref)) {
            showStatus(ObeliskStatus.ERROR, Message.translation("hexcode.pages.obelisk.not_owner"));
            return;
        }

        String slotKey = session.getActiveSlotKey();
        if (slotKey == null) {
            showStatus(ObeliskStatus.ERROR, Message.translation("hexcode.pages.obelisk.no_slot"));
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
            showStatus(ObeliskStatus.ERROR,
                    Message.translation("hexcode.obelisks.transfer.export_empty").param("slot", slotKey));
            return;
        }

        String encoded;
        try {
            encoded = HexUtils.serialize(hex);
        } catch (Exception e) {
            showStatus(ObeliskStatus.ERROR, Message.join(
                    Message.translation("hexcode.obelisks.transfer.export_failed"),
                    Message.raw("\n"),
                    issueLine(String.valueOf(e.getMessage()))));
            return;
        }
        if (encoded == null) {
            showStatus(ObeliskStatus.ERROR, Message.translation("hexcode.obelisks.transfer.export_failed"));
            return;
        }

        exportedValue = encoded;
        statusTone = ObeliskStatus.NORMAL;
        statusMessage = Message.translation("hexcode.obelisks.transfer.exported").param("slot", slotKey);
        rebuild();
        SpawnParticleEffect(new Vector3d(obeliskPosition), store);
    }

    private void handleImport(Ref<EntityStore> ref, Store<EntityStore> store, String rawData) {
        // pasted codes pick up trailing/wrapping whitespace that breaks base64 decode; valid codes never contain interior whitespace
        String fieldValue = rawData != null ? rawData.replaceAll("\\s+", "") : "";
        if (fieldValue.isEmpty()) {
            showStatus(ObeliskStatus.ERROR, Message.translation("hexcode.obelisks.transfer.import_empty"));
            return;
        }

        long t0 = System.nanoTime();
        DecodeResult result = HexUtils.deserializeWithResult(fieldValue);
        if (!result.isOk() || result.getHex() == null) {
            showStatus(ObeliskStatus.ERROR, withIssues(
                    Message.translation("hexcode.obelisks.transfer.import_failed"), result.getIssues(), 4));
            return;
        }
        Hex hex = result.getHex();
        HexUtils.validate(hex);
        long elapsedMs = (System.nanoTime() - t0) / 1_000_000L;

        HexcodeSessionComponent importSession = SessionUtils.resolveSessionByPlayer(ref, store);
        if (importSession == null) {
            showStatus(ObeliskStatus.ERROR, Message.translation("hexcode.pages.obelisk.not_crafting"));
            return;
        }

        Ref<EntityStore> ownerRef2 = importSession.getOwnerRef();
        if (ownerRef2 == null || !ownerRef2.isValid() || !ownerRef2.equals(ref)) {
            showStatus(ObeliskStatus.ERROR, Message.translation("hexcode.pages.obelisk.not_owner"));
            return;
        }

        String slotKey = importSession.getActiveSlotKey();
        if (slotKey == null) {
            showStatus(ObeliskStatus.ERROR, Message.translation("hexcode.pages.obelisk.no_slot"));
            return;
        }

        importSession.setPendingImportHex(hex);

        var warnings = filterNonInfo(result.getIssues());
        var imported = Message.translation("hexcode.obelisks.transfer.imported")
                .param("slot", slotKey)
                .param("ms", elapsedMs);
        showStatus(warnings.isEmpty() ? ObeliskStatus.NORMAL : ObeliskStatus.WARNING,
                withIssues(imported, warnings, 3));

        SpawnParticleEffect(new Vector3d(obeliskPosition), store);
    }

    private void showStatus(ObeliskStatus tone, Message message) {
        statusTone = tone;
        statusMessage = message;
        var cmd = new UICommandBuilder();
        tone.write(cmd, message);
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

    private static Message withIssues(Message header, List<DecodeIssue> issues, int maxLines) {
        if (issues == null || issues.isEmpty())
            return header;
        Set<String> seen = new LinkedHashSet<>();
        for (DecodeIssue issue : issues) {
            seen.add(truncate(issue.getMessage(), MAX_ISSUE_LINE));
        }
        var parts = new ArrayList<Message>();
        parts.add(header);
        int rendered = 0;
        for (String line : seen) {
            if (rendered >= maxLines)
                break;
            parts.add(Message.raw("\n"));
            parts.add(issueLine(line));
            rendered++;
        }
        int overflow = seen.size() - rendered;
        if (overflow > 0) {
            parts.add(Message.raw("\n"));
            parts.add(Message.translation("hexcode.obelisks.transfer.more_issues").param("count", overflow));
        }
        return Message.join(parts.toArray(Message[]::new));
    }

    private static Message issueLine(String issue) {
        return Message.translation("hexcode.obelisks.transfer.issue").param("issue", issue);
    }

    private static String truncate(String s, int max) {
        if (s == null)
            return "";
        if (s.length() <= max)
            return s;
        return s.substring(0, Math.max(0, max - 3)) + "...";
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
