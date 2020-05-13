package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PillagerOutpostPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class PillagerOutpostFeature extends RandomScatteredFeature<NoneFeatureConfiguration> {
    private static final List<Biome.SpawnerData> OUTPOST_ENEMIES = Lists.newArrayList(new Biome.SpawnerData(EntityType.PILLAGER, 1, 1, 1));

    public PillagerOutpostFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public String getFeatureName() {
        return "Pillager_Outpost";
    }

    @Override
    public int getLookupRange() {
        return 3;
    }

    @Override
    public List<Biome.SpawnerData> getSpecialEnemies() {
        return OUTPOST_ENEMIES;
    }

    @Override
    protected boolean isFeatureChunk(
        BiomeManager param0, ChunkGenerator param1, long param2, WorldgenRandom param3, int param4, int param5, Biome param6, ChunkPos param7
    ) {
        int var0 = param4 >> 4;
        int var1 = param5 >> 4;
        param3.setSeed((long)(var0 ^ var1 << 4) ^ param2);
        param3.nextInt();
        if (param3.nextInt(5) != 0) {
            return false;
        } else {
            for(int var2 = param4 - 10; var2 <= param4 + 10; ++var2) {
                for(int var3 = param5 - 10; var3 <= param5 + 10; ++var3) {
                    Biome var4 = param0.getBiome(new BlockPos((var2 << 4) + 9, 0, (var3 << 4) + 9));
                    if (Feature.VILLAGE.featureChunk(param0, param1, param2, param3, var2, var3, var4)) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return PillagerOutpostFeature.FeatureStart::new;
    }

    @Override
    protected int getRandomSalt(ChunkGeneratorSettings param0) {
        return 165745296;
    }

    public static class FeatureStart extends BeardedStructureStart {
        public FeatureStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator param0, StructureManager param1, int param2, int param3, Biome param4) {
            BlockPos var0 = new BlockPos(param2 * 16, 0, param3 * 16);
            PillagerOutpostPieces.addPieces(param0, param1, var0, this.pieces, this.random);
            this.calculateBoundingBox();
        }
    }
}
