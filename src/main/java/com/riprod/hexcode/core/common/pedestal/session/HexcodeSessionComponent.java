package com.riprod.hexcode.core.common.pedestal.session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3iUtil;

import org.joml.Vector3f;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.hexcode.core.common.hexes.component.HexColors;
import com.riprod.hexcode.core.common.hexbook.component.HexBookAsset;
import com.riprod.hexcode.core.common.hexcaster.utils.CasterInventory;
import com.riprod.hexcode.core.common.hexes.component.Hex;
import com.riprod.hexcode.core.common.imbuement.asset.ImbuementProfileAsset;
import com.riprod.hexcode.core.common.imbuement.component.ImbuementData;
import com.riprod.hexcode.core.common.imbuement.utils.ImbuementUtils;
import com.riprod.hexcode.core.common.pedestal.constants.CraftingColors;
import com.riprod.hexcode.core.common.pedestal.constants.PedestalState;
import com.riprod.hexcode.utils.HexSlot;

public class HexcodeSessionComponent implements Component<EntityStore> {

    public static final BuilderCodec<HexcodeSessionComponent> CODEC = BuilderCodec
            .builder(HexcodeSessionComponent.class, HexcodeSessionComponent::new)
            .append(new KeyedCodec<>("StoredItem", ItemStack.CODEC),
                    (c, v) -> c.storedItem = v,
                    c -> c.storedItem)
            .add()
            .append(new KeyedCodec<>("SourceSlot", new EnumCodec<>(HexSlot.class)),
                    (c, v) -> c.sourceSlot = v,
                    c -> c.sourceSlot)
            .add()
            .append(new KeyedCodec<>("PedestalLocation", Vector3iUtil.CODEC),
                    (c, v) -> c.pedestalLocation = v,
                    c -> c.pedestalLocation)
            .add()
            .append(new KeyedCodec<>("ProfileId", Codec.STRING),
                    (c, v) -> c.profileId = v,
                    c -> c.profileId)
            .add()
            .build();

    private static ComponentType<EntityStore, HexcodeSessionComponent> componentType;

    public static void setComponentType(ComponentType<EntityStore, HexcodeSessionComponent> type) {
        componentType = type;
    }

    public static ComponentType<EntityStore, HexcodeSessionComponent> getComponentType() {
        return componentType;
    }

    private UUID sessionId = UUID.randomUUID();
    private Vector3i pedestalLocation;
    private boolean isOpen = true;
    private Ref<EntityStore> ownerRef;
    private Set<Ref<EntityStore>> participantRefs = new HashSet<>();

    private ItemStack storedItem = ItemStack.EMPTY;
    private HexSlot sourceSlot = HexSlot.MainHand;
    private String profileId = null;

    private Vector3f cachedGlyphColor = null;
    private Color cachedGlyphProtocolColor = null;

    private PedestalState blockState = PedestalState.IDLE;
    private Ref<EntityStore> anchorRef = null;
    private Ref<EntityStore> imbuedItemDisplayRef;
    private List<Ref<EntityStore>> hexPreviewRefs = new ArrayList<>();
    private List<Ref<EntityStore>> slotNodeRefs = new ArrayList<>();
    private Ref<EntityStore> anchorNodeRef;
    private Ref<EntityStore> activeContainerRef;

    private String activeSlotKey = null;
    private Hex pendingImportHex = null;
    private ItemStack pendingExportPage = null;
    private String pendingReenterSlotKey = null;

    public HexcodeSessionComponent() {
    }

    public HexcodeSessionComponent(Vector3i pedestalLocation, Ref<EntityStore> ownerRef, boolean isOpen) {
        this.pedestalLocation = pedestalLocation;
        this.ownerRef = ownerRef;
        this.isOpen = isOpen;
        if (ownerRef != null) {
            this.participantRefs.add(ownerRef);
        }
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        this.isOpen = open;
    }

    public Vector3i getPedestalLocation() {
        return pedestalLocation;
    }

    public void setPedestalLocation(Vector3i pedestalLocation) {
        this.pedestalLocation = pedestalLocation;
    }

    @Nullable
    public Ref<EntityStore> getOwnerRef() {
        return ownerRef;
    }

    public void setOwnerRef(@Nullable Ref<EntityStore> ownerRef) {
        this.ownerRef = ownerRef;
    }

    public boolean isOwner(@Nullable Ref<EntityStore> playerRef) {
        return ownerRef != null && ownerRef.equals(playerRef);
    }

    public Set<Ref<EntityStore>> getParticipantRefs() {
        return participantRefs;
    }

    public boolean addParticipant(@Nullable Ref<EntityStore> playerRef) {
        if (playerRef == null) return false;
        return participantRefs.add(playerRef);
    }

    public boolean removeParticipant(@Nullable Ref<EntityStore> playerRef) {
        if (playerRef == null) return false;
        return participantRefs.remove(playerRef);
    }

    public boolean isParticipant(@Nullable Ref<EntityStore> playerRef) {
        return playerRef != null && participantRefs.contains(playerRef);
    }

    public ItemStack getStoredItem() {
        return storedItem;
    }

    public void setStoredItem(ItemStack storedItem) {
        this.storedItem = storedItem;
        this.cachedGlyphColor = null;
        this.cachedGlyphProtocolColor = null;
        HexBookAsset bookAsset = CasterInventory.getHexBookAsset(storedItem);
        if (bookAsset != null && bookAsset.getColors() != null
                && bookAsset.getColors().getPrimaryColor() != null) {
            this.cachedGlyphProtocolColor = bookAsset.getColors().getPrimaryColor();
            this.cachedGlyphColor = HexColors.toVector3f(this.cachedGlyphProtocolColor);
        }
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    @Nullable
    public ImbuementProfileAsset getProfile() {
        if (profileId == null) return null;
        return ImbuementProfileAsset.getAssetMap().getAsset(profileId);
    }

    public Map<String, ImbuementData> getImbuements() {
        return ImbuementUtils.readAll(storedItem);
    }

    @Nullable
    public Hex getHexAt(String slotKey, ComponentAccessor<EntityStore> accessor) {
        ImbuementProfileAsset profile = getProfile();
        return profile != null ? profile.readHex(storedItem, slotKey, accessor) : null;
    }

    public int getSlotCount() {
        ImbuementProfileAsset profile = getProfile();
        return profile != null ? profile.resolveSlots(storedItem).size() : 0;
    }

    public HexSlot getSourceSlot() {
        return sourceSlot;
    }

    public void setSourceSlot(HexSlot sourceSlot) {
        this.sourceSlot = sourceSlot;
    }

    public Vector3f getGlyphColor() {
        return cachedGlyphColor != null ? cachedGlyphColor : CraftingColors.GLYPH_LINK;
    }

    public Color getGlyphProtocolColor() {
        return cachedGlyphProtocolColor;
    }

    public PedestalState getState() {
        return blockState;
    }

    public void setState(PedestalState state) {
        this.blockState = state;
    }

    public Ref<EntityStore> getAnchorRef() {
        return anchorRef;
    }

    public void setAnchorEntityRef(Ref<EntityStore> anchorEntityRef) {
        this.anchorRef = anchorEntityRef;
    }

    public Ref<EntityStore> getImbuedItemDisplayRef() {
        return imbuedItemDisplayRef;
    }

    public void setImbuedItemDisplayRef(Ref<EntityStore> imbuedItemDisplayRef) {
        this.imbuedItemDisplayRef = imbuedItemDisplayRef;
    }

    public List<Ref<EntityStore>> getHexPreviewRefs() {
        return hexPreviewRefs;
    }

    public void setHexPreviewRefs(List<Ref<EntityStore>> refs) {
        this.hexPreviewRefs = refs;
    }

    public void clearHexPreviewRefs() {
        this.hexPreviewRefs = new ArrayList<>();
    }

    public List<Ref<EntityStore>> getSlotNodeRefs() {
        return slotNodeRefs;
    }

    public void setSlotNodeRefs(List<Ref<EntityStore>> slotNodeRefs) {
        this.slotNodeRefs = slotNodeRefs;
    }

    public Ref<EntityStore> getAnchorNodeRef() {
        return anchorNodeRef;
    }

    public void setAnchorNodeRef(Ref<EntityStore> anchorNodeRef) {
        this.anchorNodeRef = anchorNodeRef;
    }

    @Nullable
    public Ref<EntityStore> getActiveContainerRef() {
        return activeContainerRef;
    }

    public void setActiveContainerRef(@Nullable Ref<EntityStore> activeContainerRef) {
        this.activeContainerRef = activeContainerRef;
    }

    @Nullable
    public String getActiveSlotKey() {
        return activeSlotKey;
    }

    public void setActiveSlotKey(@Nullable String activeSlotKey) {
        this.activeSlotKey = activeSlotKey;
    }

    public Hex getPendingImportHex() {
        return pendingImportHex;
    }

    public void setPendingImportHex(Hex hex) {
        this.pendingImportHex = hex;
    }

    @Nullable
    public ItemStack getPendingExportPage() {
        return pendingExportPage;
    }

    public void setPendingExportPage(@Nullable ItemStack page) {
        this.pendingExportPage = page;
    }

    @Nullable
    public String getPendingReenterSlotKey() {
        return pendingReenterSlotKey;
    }

    public void setPendingReenterSlotKey(@Nullable String slotKey) {
        this.pendingReenterSlotKey = slotKey;
    }

    public List<Ref<EntityStore>> getAllRefs() {
        List<Ref<EntityStore>> all = new ArrayList<>();
        if (imbuedItemDisplayRef != null && imbuedItemDisplayRef.isValid()) all.add(imbuedItemDisplayRef);
        if (hexPreviewRefs != null) all.addAll(hexPreviewRefs);
        if (anchorNodeRef != null && anchorNodeRef.isValid()) all.add(anchorNodeRef);
        if (slotNodeRefs != null) all.addAll(slotNodeRefs);
        return all;
    }

    @Nonnull
    @Override
    public HexcodeSessionComponent clone() {
        HexcodeSessionComponent copy = new HexcodeSessionComponent();
        copy.sessionId = this.sessionId;
        copy.pedestalLocation = this.pedestalLocation != null ? new Vector3i(this.pedestalLocation) : null;
        copy.isOpen = this.isOpen;
        copy.ownerRef = this.ownerRef;
        copy.participantRefs = new HashSet<>(this.participantRefs);
        copy.storedItem = this.storedItem;
        copy.sourceSlot = this.sourceSlot;
        copy.profileId = this.profileId;
        copy.cachedGlyphColor = this.cachedGlyphColor != null ? new Vector3f(this.cachedGlyphColor) : null;
        copy.cachedGlyphProtocolColor = this.cachedGlyphProtocolColor;
        copy.blockState = this.blockState;
        copy.anchorRef = this.anchorRef;
        copy.imbuedItemDisplayRef = this.imbuedItemDisplayRef;
        copy.hexPreviewRefs = this.hexPreviewRefs != null ? new ArrayList<>(this.hexPreviewRefs) : new ArrayList<>();
        copy.slotNodeRefs = this.slotNodeRefs != null ? new ArrayList<>(this.slotNodeRefs) : new ArrayList<>();
        copy.anchorNodeRef = this.anchorNodeRef;
        copy.activeContainerRef = this.activeContainerRef;
        copy.activeSlotKey = this.activeSlotKey;
        copy.pendingImportHex = this.pendingImportHex;
        copy.pendingExportPage = this.pendingExportPage;
        copy.pendingReenterSlotKey = this.pendingReenterSlotKey;
        return copy;
    }
}
