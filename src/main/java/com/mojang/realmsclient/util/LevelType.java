package com.mojang.realmsclient.util;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum LevelType {
    DEFAULT(0, Component.translatable("generator.default")),
    FLAT(1, Component.translatable("generator.flat")),
    LARGE_BIOMES(2, Component.translatable("generator.large_biomes")),
    AMPLIFIED(3, Component.translatable("generator.amplified"));

    private final int index;
    private final Component name;

    private LevelType(int param0, Component param1) {
        this.index = param0;
        this.name = param1;
    }

    public Component getName() {
        return this.name;
    }

    public int getDtoIndex() {
        return this.index;
    }
}
