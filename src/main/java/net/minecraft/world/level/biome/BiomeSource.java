package net.minecraft.world.level.biome;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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

    @Nullable
    public BlockPos findBiomeHorizontal(int param0, int param1, int param2, int param3, List<Biome> param4, Random param5) {
        int var0 = param0 - param3 >> 2;
        int var1 = param2 - param3 >> 2;
        int var2 = param0 + param3 >> 2;
        int var3 = param2 + param3 >> 2;
        int var4 = var2 - var0 + 1;
        int var5 = var3 - var1 + 1;
        int var6 = param1 >> 2;
        BlockPos var7 = null;
        int var8 = 0;

        for(int var9 = 0; var9 < var5; ++var9) {
            for(int var10 = 0; var10 < var4; ++var10) {
                int var11 = var0 + var10;
                int var12 = var1 + var9;
                if (param4.contains(this.getNoiseBiome(var11, var6, var12))) {
                    if (var7 == null || param5.nextInt(var8 + 1) == 0) {
                        var7 = new BlockPos(var11 << 2, param1, var12 << 2);
                    }

                    ++var8;
                }
            }
        }

        return var7;
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
}
