package net.minecraft.world.level.biome;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.newbiome.layer.Layer;
import net.minecraft.world.level.newbiome.layer.Layers;
import net.minecraft.world.level.storage.LevelData;

public class OverworldBiomeSource extends BiomeSource {
    private final Layer noiseBiomeLayer;
    private final Layer blockBiomeLayer;
    private final Biome[] possibleBiomes = new Biome[]{
        Biomes.OCEAN,
        Biomes.PLAINS,
        Biomes.DESERT,
        Biomes.MOUNTAINS,
        Biomes.FOREST,
        Biomes.TAIGA,
        Biomes.SWAMP,
        Biomes.RIVER,
        Biomes.FROZEN_OCEAN,
        Biomes.FROZEN_RIVER,
        Biomes.SNOWY_TUNDRA,
        Biomes.SNOWY_MOUNTAINS,
        Biomes.MUSHROOM_FIELDS,
        Biomes.MUSHROOM_FIELD_SHORE,
        Biomes.BEACH,
        Biomes.DESERT_HILLS,
        Biomes.WOODED_HILLS,
        Biomes.TAIGA_HILLS,
        Biomes.MOUNTAIN_EDGE,
        Biomes.JUNGLE,
        Biomes.JUNGLE_HILLS,
        Biomes.JUNGLE_EDGE,
        Biomes.DEEP_OCEAN,
        Biomes.STONE_SHORE,
        Biomes.SNOWY_BEACH,
        Biomes.BIRCH_FOREST,
        Biomes.BIRCH_FOREST_HILLS,
        Biomes.DARK_FOREST,
        Biomes.SNOWY_TAIGA,
        Biomes.SNOWY_TAIGA_HILLS,
        Biomes.GIANT_TREE_TAIGA,
        Biomes.GIANT_TREE_TAIGA_HILLS,
        Biomes.WOODED_MOUNTAINS,
        Biomes.SAVANNA,
        Biomes.SAVANNA_PLATEAU,
        Biomes.BADLANDS,
        Biomes.WOODED_BADLANDS_PLATEAU,
        Biomes.BADLANDS_PLATEAU,
        Biomes.WARM_OCEAN,
        Biomes.LUKEWARM_OCEAN,
        Biomes.COLD_OCEAN,
        Biomes.DEEP_WARM_OCEAN,
        Biomes.DEEP_LUKEWARM_OCEAN,
        Biomes.DEEP_COLD_OCEAN,
        Biomes.DEEP_FROZEN_OCEAN,
        Biomes.SUNFLOWER_PLAINS,
        Biomes.DESERT_LAKES,
        Biomes.GRAVELLY_MOUNTAINS,
        Biomes.FLOWER_FOREST,
        Biomes.TAIGA_MOUNTAINS,
        Biomes.SWAMP_HILLS,
        Biomes.ICE_SPIKES,
        Biomes.MODIFIED_JUNGLE,
        Biomes.MODIFIED_JUNGLE_EDGE,
        Biomes.TALL_BIRCH_FOREST,
        Biomes.TALL_BIRCH_HILLS,
        Biomes.DARK_FOREST_HILLS,
        Biomes.SNOWY_TAIGA_MOUNTAINS,
        Biomes.GIANT_SPRUCE_TAIGA,
        Biomes.GIANT_SPRUCE_TAIGA_HILLS,
        Biomes.MODIFIED_GRAVELLY_MOUNTAINS,
        Biomes.SHATTERED_SAVANNA,
        Biomes.SHATTERED_SAVANNA_PLATEAU,
        Biomes.ERODED_BADLANDS,
        Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU,
        Biomes.MODIFIED_BADLANDS_PLATEAU
    };

    public OverworldBiomeSource(OverworldBiomeSourceSettings param0) {
        LevelData var0 = param0.getLevelData();
        OverworldGeneratorSettings var1 = param0.getGeneratorSettings();
        Layer[] var2 = Layers.getDefaultLayers(var0.getSeed(), var0.getGeneratorType(), var1);
        this.noiseBiomeLayer = var2[0];
        this.blockBiomeLayer = var2[1];
    }

    @Override
    public Biome getBiome(int param0, int param1) {
        return this.blockBiomeLayer.get(param0, param1);
    }

    @Override
    public Biome getNoiseBiome(int param0, int param1) {
        return this.noiseBiomeLayer.get(param0, param1);
    }

    @Override
    public Biome[] getBiomeBlock(int param0, int param1, int param2, int param3, boolean param4) {
        return this.blockBiomeLayer.getArea(param0, param1, param2, param3);
    }

    @Override
    public Set<Biome> getBiomesWithin(int param0, int param1, int param2) {
        int var0 = param0 - param2 >> 2;
        int var1 = param1 - param2 >> 2;
        int var2 = param0 + param2 >> 2;
        int var3 = param1 + param2 >> 2;
        int var4 = var2 - var0 + 1;
        int var5 = var3 - var1 + 1;
        Set<Biome> var6 = Sets.newHashSet();
        Collections.addAll(var6, this.noiseBiomeLayer.getArea(var0, var1, var4, var5));
        return var6;
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
        Biome[] var6 = this.noiseBiomeLayer.getArea(var0, var1, var4, var5);
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
