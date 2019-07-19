package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum SlabType implements StringRepresentable {
    TOP("top"),
    BOTTOM("bottom"),
    DOUBLE("double");

    private final String name;

    private SlabType(String param0) {
        this.name = param0;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
