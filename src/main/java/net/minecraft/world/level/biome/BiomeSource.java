package net.minecraft.world.level.biome;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public abstract class BiomeSource implements BiomeManager.NoiseBiomeSource {
    private static final List<Biome> PLAYER_SPAWN_BIOMES = Lists.newArrayList(
        Biomes.FOREST, Biomes.PLAINS, Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.WOODED_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS
    );
    protected final Map<StructureFeature<?>, Boolean> supportedStructures = Maps.newHashMap();
    protected final Set<BlockState> surfaceBlocks = Sets.newHashSet();
    protected final Set<Biome> possibleBiomes;

    protected BiomeSource(Set<Biome> param0) {
        this.possibleBiomes = param0;
    }

    public List<Biome> getPlayerSpawnBiomes() {
        return PLAYER_SPAWN_BIOMES;
    }

    public Set<Biome> getBiomesWithin(int param0, int param1, int param2, int param3) {
        int var0 = param0 - param3 >> 2;
        int var1 = param1 - param3 >> 2;
        int var2 = param2 - param3 >> 2;
        int var3 = param0 + param3 >> 2;
        int var4 = param1 + param3 >> 2;
        int var5 = param2 + param3 >> 2;
        int var6 = var3 - var0 + 1;
        int var7 = var4 - var1 + 1;
        int var8 = var5 - var2 + 1;
        Set<Biome> var9 = Sets.newHashSet();

        for(int var10 = 0; var10 < var8; ++var10) {
            for(int var11 = 0; var11 < var6; ++var11) {
                for(int var12 = 0; var12 < var7; ++var12) {
                    int var13 = var0 + var11;
                    int var14 = var1 + var12;
                    int var15 = var2 + var10;
                    var9.add(this.getNoiseBiome(var13, var14, var15));
                }
            }
        }

        return var9;
    }

    public BlockPos findBiomeHorizontal(int param0, int param1, int param2, int param3, List<Biome> param4, Random param5) {
        return this.findBiomeHorizontal(param0, param1, param2, param3, 1, param4, param5, false);
    }

    @Nullable
    public BlockPos findBiomeHorizontal(int param0, int param1, int param2, int param3, int param4, List<Biome> param5, Random param6, boolean param7) {
        int var0 = param0 >> 2;
        int var1 = param2 >> 2;
        int var2 = param3 >> 2;
        int var3 = param1 >> 2;
        BlockPos var4 = null;
        int var5 = 0;
        int var6 = param7 ? 0 : var2;

        for(int var7 = var6; var7 <= var2; var7 += param4) {
            for(int var8 = -var7; var8 <= var7; var8 += param4) {
                boolean var9 = Math.abs(var8) == var7;

                for(int var10 = -var7; var10 <= var7; var10 += param4) {
                    if (param7) {
                        boolean var11 = Math.abs(var10) == var7;
                        if (!var11 && !var9) {
                            continue;
                        }
                    }

                    int var12 = var0 + var10;
                    int var13 = var1 + var8;
                    if (param5.contains(this.getNoiseBiome(var12, var3, var13))) {
                        if (var4 == null || param6.nextInt(var5 + 1) == 0) {
                            var4 = new BlockPos(var12 << 2, param1, var13 << 2);
                            if (param7) {
                                return var4;
                            }
                        }

                        ++var5;
                    }
                }
            }
        }

        return var4;
    }

    public float getHeightValue(int param0, int param1) {
        return 0.0F;
    }

    public boolean canGenerateStructure(StructureFeature<?> param0) {
        return this.supportedStructures.computeIfAbsent(param0, param0x -> this.possibleBiomes.stream().anyMatch(param1 -> param1.isValidStart(param0x)));
    }

    public Set<BlockState> getSurfaceBlocks() {
        if (this.surfaceBlocks.isEmpty()) {
            for(Biome var0 : this.possibleBiomes) {
                this.surfaceBlocks.add(var0.getSurfaceBuilderConfig().getTopMaterial());
            }
        }

        return this.surfaceBlocks;
    }

    public abstract BiomeSourceType<?, ?> getType();

    public abstract <T> Dynamic<T> serialize(DynamicOps<T> var1);

    public Stream<Biome> getKnownBiomes() {
        return this.possibleBiomes.stream();
    }
}
