package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.OceanMonumentPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class OceanMonumentFeature extends StructureFeature<NoneFeatureConfiguration> {
    private static final WeightedRandomList<MobSpawnSettings.SpawnerData> MONUMENT_ENEMIES = WeightedRandomList.create(
        new MobSpawnSettings.SpawnerData(EntityType.GUARDIAN, 1, 2, 4)
    );

    public OceanMonumentFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    protected boolean linearSeparation() {
        return false;
    }

    protected boolean isFeatureChunk(
        ChunkGenerator param0,
        BiomeSource param1,
        long param2,
        WorldgenRandom param3,
        ChunkPos param4,
        Biome param5,
        ChunkPos param6,
        NoneFeatureConfiguration param7,
        LevelHeightAccessor param8
    ) {
        int var0 = param4.getBlockX(9);
        int var1 = param4.getBlockZ(9);

        for(Biome var3 : param1.getBiomesWithin(var0, param0.getSeaLevel(), var1, 16)) {
            if (!var3.getGenerationSettings().isValidStart(this)) {
                return false;
            }
        }

        for(Biome var5 : param1.getBiomesWithin(var0, param0.getSeaLevel(), var1, 29)) {
            if (var5.getBiomeCategory() != Biome.BiomeCategory.OCEAN && var5.getBiomeCategory() != Biome.BiomeCategory.RIVER) {
                return false;
            }
        }

        return true;
    }

    @Override
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return OceanMonumentFeature.OceanMonumentStart::new;
    }

    @Override
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
        return MONUMENT_ENEMIES;
    }

    public static class OceanMonumentStart extends StructureStart<NoneFeatureConfiguration> {
        public OceanMonumentStart(StructureFeature<NoneFeatureConfiguration> param0, ChunkPos param1, int param2, long param3) {
            super(param0, param1, param2, param3);
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            ChunkPos param3,
            Biome param4,
            NoneFeatureConfiguration param5,
            LevelHeightAccessor param6
        ) {
            int var0 = param3.getMinBlockX() - 29;
            int var1 = param3.getMinBlockZ() - 29;
            Direction var2 = Direction.Plane.HORIZONTAL.getRandomDirection(this.random);
            this.addPiece(new OceanMonumentPieces.MonumentBuilding(this.random, var0, var1, var2));
        }
    }
}
