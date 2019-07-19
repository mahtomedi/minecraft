package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum ChestType implements StringRepresentable {
    SINGLE("single", 0),
    LEFT("left", 2),
    RIGHT("right", 1);

    public static final ChestType[] BY_ID = values();
    private final String name;
    private final int opposite;

    private ChestType(String param0, int param1) {
        this.name = param0;
        this.opposite = param1;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public ChestType getOpposite() {
        return BY_ID[this.opposite];
    }
}
