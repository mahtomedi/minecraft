package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum WallSide implements StringRepresentable {
    NONE("none"),
    LOW("low"),
    TALL("tall");

    private final String name;

    private WallSide(String param0) {
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
