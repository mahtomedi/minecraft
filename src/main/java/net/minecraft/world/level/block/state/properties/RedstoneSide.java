package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum RedstoneSide implements StringRepresentable {
    UP("up"),
    SIDE("side"),
    NONE("none");

    private final String name;

    private RedstoneSide(String param0) {
        this.name = param0;
    }

    @Override
    public String toString() {
        return this.getSerializedName();
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
