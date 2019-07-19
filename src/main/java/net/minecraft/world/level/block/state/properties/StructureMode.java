package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum StructureMode implements StringRepresentable {
    SAVE("save"),
    LOAD("load"),
    CORNER("corner"),
    DATA("data");

    private final String name;

    private StructureMode(String param0) {
        this.name = param0;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
