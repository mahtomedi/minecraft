package net.minecraft.world.level.biome;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class TheEndBiomeSource extends BiomeSource {
    private final SimplexNoise islandNoise;
    private final WorldgenRandom random;
    private final Biome[] possibleBiomes = new Biome[]{Biomes.THE_END, Biomes.END_HIGHLANDS, Biomes.END_MIDLANDS, Biomes.SMALL_END_ISLANDS, Biomes.END_BARRENS};

    public TheEndBiomeSource(TheEndBiomeSourceSettings param0) {
        this.random = new WorldgenRandom(param0.getSeed());
        this.random.consumeCount(17292);
        this.islandNoise = new SimplexNoise(this.random);
    }

    @Override
    public Biome getBiome(int param0, int param1) {
        int var0 = param0 >> 4;
        int var1 = param1 >> 4;
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
    public Biome[] getBiomeBlock(int param0, int param1, int param2, int param3, boolean param4) {
        Biome[] var0 = new Biome[param2 * param3];
        Long2ObjectMap<Biome> var1 = new Long2ObjectOpenHashMap<>();

        for(int var2 = 0; var2 < param2; ++var2) {
            for(int var3 = 0; var3 < param3; ++var3) {
                int var4 = var2 + param0;
                int var5 = var3 + param1;
                long var6 = ChunkPos.asLong(var4, var5);
                Biome var7 = var1.get(var6);
                if (var7 == null) {
                    var7 = this.getBiome(var4, var5);
                    var1.put(var6, var7);
                }

                var0[var2 + var3 * param2] = var7;
            }
        }

        return var0;
    }

    @Override
    public Set<Biome> getBiomesWithin(int param0, int param1, int param2) {
        int var0 = param0 - param2 >> 2;
        int var1 = param1 - param2 >> 2;
        int var2 = param0 + param2 >> 2;
        int var3 = param1 + param2 >> 2;
        int var4 = var2 - var0 + 1;
        int var5 = var3 - var1 + 1;
        return Sets.newHashSet(this.getBiomeBlock(var0, var1, var4, var5));
    }

    @Nullable
    @Override
    public BlockPos findBiome(int param0, int param1, int param2, List<Biome> param3, Random param4) {
        int var0 = param0 - param2 >> 2;
        int var1 = param1 - param2 >> 2;
        int var2 = param0 + param2 >> 2;
        int var3 = param1 + param2 >> 2;
        int var4 = var2 - var0 + 1;
        int var5 = var3 - var1 + 1;
        Biome[] var6 = this.getBiomeBlock(var0, var1, var4, var5);
        BlockPos var7 = null;
        int var8 = 0;

        for(int var9 = 0; var9 < var4 * var5; ++var9) {
            int var10 = var0 + var9 % var4 << 2;
            int var11 = var1 + var9 / var4 << 2;
            if (param3.contains(var6[var9])) {
                if (var7 == null || param4.nextInt(var8 + 1) == 0) {
                    var7 = new BlockPos(var10, 0, var11);
                }

                ++var8;
            }
        }

        return var7;
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

    @Override
    public boolean canGenerateStructure(StructureFeature<?> param0) {
        return this.supportedStructures.computeIfAbsent(param0, param0x -> {
            for(Biome var0 : this.possibleBiomes) {
                if (var0.isValidStart(param0x)) {
                    return true;
                }
            }

            return false;
        });
    }

    @Override
    public Set<BlockState> getSurfaceBlocks() {
        if (this.surfaceBlocks.isEmpty()) {
            for(Biome var0 : this.possibleBiomes) {
                this.surfaceBlocks.add(var0.getSurfaceBuilderConfig().getTopMaterial());
            }
        }

        return this.surfaceBlocks;
    }
}
