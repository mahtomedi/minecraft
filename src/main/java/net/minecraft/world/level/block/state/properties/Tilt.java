package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum Tilt implements StringRepresentable {
    NONE("none", true),
    UNSTABLE("unstable", false),
    PARTIAL("partial", true),
    FULL("full", true);

    private final String name;
    private final boolean causesVibration;

    private Tilt(String param0, boolean param1) {
        this.name = param0;
        this.causesVibration = param1;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public boolean causesVibration() {
        return this.causesVibration;
    }
}
