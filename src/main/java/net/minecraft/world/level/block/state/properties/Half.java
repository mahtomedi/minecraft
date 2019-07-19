package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum Half implements StringRepresentable {
    TOP("top"),
    BOTTOM("bottom");

    private final String name;

    private Half(String param0) {
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
