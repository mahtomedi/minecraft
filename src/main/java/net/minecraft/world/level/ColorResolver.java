package net.minecraft.world.level;

import net.minecraft.world.level.biome.Biome;

public interface ColorResolver {
    int getColor(Biome var1, double var2, double var4);
}
