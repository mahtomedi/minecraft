package net.minecraft.data.models.model;

import javax.annotation.Nullable;

public enum TextureSlot {
    ALL("all"),
    TEXTURE("texture", ALL),
    PARTICLE("particle", TEXTURE),
    END("end", ALL),
    BOTTOM("bottom", END),
    TOP("top", END),
    FRONT("front", ALL),
    BACK("back", ALL),
    SIDE("side", ALL),
    NORTH("north", SIDE),
    SOUTH("south", SIDE),
    EAST("east", SIDE),
    WEST("west", SIDE),
    UP("up"),
    DOWN("down"),
    CROSS("cross"),
    PLANT("plant"),
    WALL("wall", ALL),
    RAIL("rail"),
    WOOL("wool"),
    PATTERN("pattern"),
    PANE("pane"),
    EDGE("edge"),
    FAN("fan"),
    STEM("stem"),
    UPPER_STEM("upperstem"),
    CROP("crop"),
    DIRT("dirt"),
    FIRE("fire"),
    LANTERN("lantern"),
    PLATFORM("platform"),
    UNSTICKY("unsticky"),
    TORCH("torch"),
    LAYER0("layer0");

    private final String id;
    @Nullable
    private final TextureSlot parent;

    private TextureSlot(String param0) {
        this.id = param0;
        this.parent = null;
    }

    private TextureSlot(String param0, TextureSlot param1) {
        this.id = param0;
        this.parent = param1;
    }

    public String getId() {
        return this.id;
    }

    @Nullable
    public TextureSlot getParent() {
        return this.parent;
    }
}
