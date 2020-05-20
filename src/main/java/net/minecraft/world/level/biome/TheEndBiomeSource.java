package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TheEndBiomeSource extends BiomeSource {
    public static final Codec<TheEndBiomeSource> CODEC = Codec.LONG.fieldOf("seed").xmap(TheEndBiomeSource::new, param0 -> param0.seed).stable().codec();
    private final SimplexNoise islandNoise;
    private static final List<Biome> POSSIBLE_BIOMES = ImmutableList.of(
        Biomes.THE_END, Biomes.END_HIGHLANDS, Biomes.END_MIDLANDS, Biomes.SMALL_END_ISLANDS, Biomes.END_BARRENS
    );
    private final long seed;

    public TheEndBiomeSource(long param0) {
        super(POSSIBLE_BIOMES);
        this.seed = param0;
        WorldgenRandom var0 = new WorldgenRandom(param0);
        var0.consumeCount(17292);
        this.islandNoise = new SimplexNoise(var0);
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BiomeSource withSeed(long param0) {
        return new TheEndBiomeSource(param0);
    }

    @Override
    public Biome getNoiseBiome(int param0, int param1, int param2) {
        int var0 = param0 >> 2;
        int var1 = param2 >> 2;
        if ((long)var0 * (long)var0 + (long)var1 * (long)var1 <= 4096L) {
            return Biomes.THE_END;
        } else {
            float var2 = getHeightValue(this.islandNoise, var0 * 2 + 1, var1 * 2 + 1);
            if (var2 > 40.0F) {
                return Biomes.END_HIGHLANDS;
            } else if (var2 >= 0.0F) {
                return Biomes.END_MIDLANDS;
            } else {
                return var2 < -20.0F ? Biomes.SMALL_END_ISLANDS : Biomes.END_BARRENS;
            }
        }
    }

    public boolean stable(long param0) {
        return this.seed == param0;
    }

    public static float getHeightValue(SimplexNoise param0, int param1, int param2) {
        int var0 = param1 / 2;
        int var1 = param2 / 2;
        int var2 = param1 % 2;
        int var3 = param2 % 2;
        float var4 = 100.0F - Mth.sqrt((float)(param1 * param1 + param2 * param2)) * 8.0F;
        var4 = Mth.clamp(var4, -100.0F, 80.0F);

        for(int var5 = -12; var5 <= 12; ++var5) {
            for(int var6 = -12; var6 <= 12; ++var6) {
                long var7 = (long)(var0 + var5);
                long var8 = (long)(var1 + var6);
                if (var7 * var7 + var8 * var8 > 4096L && param0.getValue((double)var7, (double)var8) < -0.9F) {
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
