package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum ComparatorMode implements StringRepresentable {
    COMPARE("compare"),
    SUBTRACT("subtract");

    private final String name;

    private ComparatorMode(String param0) {
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
