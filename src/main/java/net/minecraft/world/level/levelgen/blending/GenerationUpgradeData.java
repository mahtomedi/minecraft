package net.minecraft.world.level.levelgen.blending;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;

public final class GenerationUpgradeData {
    private final boolean oldBiome;
    private final boolean oldNoise;

    public GenerationUpgradeData(boolean param0, boolean param1) {
        this.oldBiome = param0;
        this.oldNoise = param1;
    }

    @Nullable
    public static GenerationUpgradeData read(CompoundTag param0) {
        return param0.isEmpty() ? null : new GenerationUpgradeData(param0.getBoolean("old_biome"), param0.getBoolean("old_noise"));
    }

    public CompoundTag write() {
        CompoundTag var0 = new CompoundTag();
        var0.putBoolean("old_biome", this.oldBiome);
        var0.putBoolean("old_noise", this.oldNoise);
        return var0;
    }

    public boolean oldBiome() {
        return this.oldBiome;
    }

    public boolean oldNoise() {
        return this.oldNoise;
    }
}
