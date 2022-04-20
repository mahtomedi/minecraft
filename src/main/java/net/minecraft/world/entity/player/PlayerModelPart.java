package net.minecraft.world.entity.player;

import net.minecraft.network.chat.Component;

public enum PlayerModelPart {
    CAPE(0, "cape"),
    JACKET(1, "jacket"),
    LEFT_SLEEVE(2, "left_sleeve"),
    RIGHT_SLEEVE(3, "right_sleeve"),
    LEFT_PANTS_LEG(4, "left_pants_leg"),
    RIGHT_PANTS_LEG(5, "right_pants_leg"),
    HAT(6, "hat");

    private final int bit;
    private final int mask;
    private final String id;
    private final Component name;

    private PlayerModelPart(int param0, String param1) {
        this.bit = param0;
        this.mask = 1 << param0;
        this.id = param1;
        this.name = Component.translatable("options.modelPart." + param1);
    }

    public int getMask() {
        return this.mask;
    }

    public int getBit() {
        return this.bit;
    }

    public String getId() {
        return this.id;
    }

    public Component getName() {
        return this.name;
    }
}
