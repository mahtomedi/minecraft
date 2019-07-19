package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.AreaTransformer0;

public enum OceanLayer implements AreaTransformer0 {
    INSTANCE;

    @Override
    public int applyPixel(Context param0, int param1, int param2) {
        ImprovedNoise var0 = param0.getBiomeNoise();
        double var1 = var0.noise((double)param1 / 8.0, (double)param2 / 8.0, 0.0, 0.0, 0.0);
        if (var1 > 0.4) {
            return Layers.WARM_OCEAN;
        } else if (var1 > 0.2) {
            return Layers.LUKEWARM_OCEAN;
        } else if (var1 < -0.4) {
            return Layers.FROZEN_OCEAN;
        } else {
            return var1 < -0.2 ? Layers.COLD_OCEAN : Layers.OCEAN;
        }
    }
}
