package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C0Transformer;

public class BiomeInitLayer implements C0Transformer {
    private static final int[] LEGACY_WARM_BIOMES = new int[]{2, 4, 3, 6, 1, 5};
    private static final int[] WARM_BIOMES = new int[]{2, 2, 2, 35, 35, 1};
    private static final int[] MEDIUM_BIOMES = new int[]{4, 29, 3, 1, 27, 6};
    private static final int[] COLD_BIOMES = new int[]{4, 3, 5, 1};
    private static final int[] ICE_BIOMES = new int[]{12, 12, 12, 30};
    private int[] warmBiomes = WARM_BIOMES;

    public BiomeInitLayer(boolean param0) {
        if (param0) {
            this.warmBiomes = LEGACY_WARM_BIOMES;
        }

    }

    @Override
    public int apply(Context param0, int param1) {
        int var0 = (param1 & 3840) >> 8;
        param1 &= -3841;
        if (!Layers.isOcean(param1) && param1 != 14) {
            switch(param1) {
                case 1:
                    if (var0 > 0) {
                        return param0.nextRandom(3) == 0 ? 39 : 38;
                    }

                    return this.warmBiomes[param0.nextRandom(this.warmBiomes.length)];
                case 2:
                    if (var0 > 0) {
                        return 21;
                    }

                    return MEDIUM_BIOMES[param0.nextRandom(MEDIUM_BIOMES.length)];
                case 3:
                    if (var0 > 0) {
                        return 32;
                    }

                    return COLD_BIOMES[param0.nextRandom(COLD_BIOMES.length)];
                case 4:
                    return ICE_BIOMES[param0.nextRandom(ICE_BIOMES.length)];
                default:
                    return 14;
            }
        } else {
            return param1;
        }
    }
}
