package com.riprod.hexcode.core.common.hexbook.component;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.riprod.hexcode.core.common.hexes.codec.HexFieldCodec;
import com.riprod.hexcode.core.common.hexes.component.Hex;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// legacy decode-only bridge. old items stored their hexes here; the data now
// lives in the imbuement map. kept so CasterInventory can migrate Hexes on read.
public class HexBookComponent {

    public static final BuilderCodec<HexBookComponent> CODEC = BuilderCodec
            .builder(HexBookComponent.class, HexBookComponent::new)
            .append(new KeyedCodec<>("Hexes", new ArrayCodec<>(HexFieldCodec.CODEC, Hex[]::new)),
                    (c, v) -> {
                        if (v != null) {
                            c.hexes = new ArrayList<>(Arrays.asList(v));
                        }
                    },
                    c -> c.hexes.stream()
                            .filter(h -> h != null && !h.getGlyphs().isEmpty())
                            .toArray(Hex[]::new))
            .add()
            .build();

    @Nonnull
    private List<Hex> hexes = new ArrayList<>();

    public HexBookComponent() {
    }

    public List<Hex> getHexes() {
        return hexes;
    }
}
