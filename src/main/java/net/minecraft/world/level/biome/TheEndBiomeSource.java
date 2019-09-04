package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class TheEndBiomeSource extends BiomeSource {
    private final SimplexNoise islandNoise;
    private final WorldgenRandom random;
    private static final Set<Biome> POSSIBLE_BIOMES = ImmutableSet.of(
        Biomes.THE_END, Biomes.END_HIGHLANDS, Biomes.END_MIDLANDS, Biomes.SMALL_END_ISLANDS, Biomes.END_BARRENS
    );

    public TheEndBiomeSource(TheEndBiomeSourceSettings param0) {
        super(POSSIBLE_BIOMES);
        this.random = new WorldgenRandom(param0.getSeed());
        this.random.consumeCount(17292);
        this.islandNoise = new SimplexNoise(this.random);
    }

    @Override
    public Biome getNoiseBiome(int param0, int param1, int param2) {
        int var0 = param0 >> 2;
        int var1 = param2 >> 2;
        if ((long)var0 * (long)var0 + (long)var1 * (long)var1 <= 4096L) {
            return Biomes.THE_END;
        } else {
            float var2 = this.getHeightValue(var0 * 2 + 1, var1 * 2 + 1);
            if (var2 > 40.0F) {
                return Biomes.END_HIGHLANDS;
            } else if (var2 >= 0.0F) {
                return Biomes.END_MIDLANDS;
            } else {
                return var2 < -20.0F ? Biomes.SMALL_END_ISLANDS : Biomes.END_BARRENS;
            }
        }
    }

    @Override
    public float getHeightValue(int param0, int param1) {
        int var0 = param0 / 2;
        int var1 = param1 / 2;
        int var2 = param0 % 2;
        int var3 = param1 % 2;
        float var4 = 100.0F - Mth.sqrt((float)(param0 * param0 + param1 * param1)) * 8.0F;
        var4 = Mth.clamp(var4, -100.0F, 80.0F);

        for(int var5 = -12; var5 <= 12; ++var5) {
            for(int var6 = -12; var6 <= 12; ++var6) {
                long var7 = (long)(var0 + var5);
                long var8 = (long)(var1 + var6);
                if (var7 * var7 + var8 * var8 > 4096L && this.islandNoise.getValue((double)var7, (double)var8) < -0.9F) {
                    float var9 = (Mth.abs((float)var7) * 3439.0F + Mth.abs((float)var8) * 147.0F) % 13.0F + 9.0F;
                    float var10 = (float)(var2 - var5 * 2);
                    float var11 = (float)(var3 - var6 * 2);
                    float var12 = 100.0F - Mth.sqrt(var10 * var10 + var11 * var11) * var9;
                    var12 = Mth.clamp(var12, -100.0F, 80.0F);
                    var4 = Math.max(var4, var12);
                }
            }
        }

        return var4;
    }
}
