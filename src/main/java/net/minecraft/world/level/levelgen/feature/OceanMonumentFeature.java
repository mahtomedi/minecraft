package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.OceanMonumentPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class OceanMonumentFeature extends StructureFeature<NoneFeatureConfiguration> {
    private static final List<Biome.SpawnerData> MONUMENT_ENEMIES = Lists.newArrayList(new Biome.SpawnerData(EntityType.GUARDIAN, 1, 2, 4));

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
        int param4,
        int param5,
        Biome param6,
        ChunkPos param7,
        NoneFeatureConfiguration param8
    ) {
        for(Biome var1 : param1.getBiomesWithin(param4 * 16 + 9, param0.getSeaLevel(), param5 * 16 + 9, 16)) {
            if (!var1.isValidStart(this)) {
                return false;
            }
        }

        for(Biome var3 : param1.getBiomesWithin(param4 * 16 + 9, param0.getSeaLevel(), param5 * 16 + 9, 29)) {
            if (var3.getBiomeCategory() != Biome.BiomeCategory.OCEAN && var3.getBiomeCategory() != Biome.BiomeCategory.RIVER) {
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
    public List<Biome.SpawnerData> getSpecialEnemies() {
        return MONUMENT_ENEMIES;
    }

    public static class OceanMonumentStart extends StructureStart<NoneFeatureConfiguration> {
        private boolean isCreated;

        public OceanMonumentStart(StructureFeature<NoneFeatureConfiguration> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        public void generatePieces(
            RegistryAccess param0, ChunkGenerator param1, StructureManager param2, int param3, int param4, Biome param5, NoneFeatureConfiguration param6
        ) {
            this.generatePieces(param3, param4);
        }

        private void generatePieces(int param0, int param1) {
            int var0 = param0 * 16 - 29;
            int var1 = param1 * 16 - 29;
            Direction var2 = Direction.Plane.HORIZONTAL.getRandomDirection(this.random);
            this.pieces.add(new OceanMonumentPieces.MonumentBuilding(this.random, var0, var1, var2));
            this.calculateBoundingBox();
            this.isCreated = true;
        }

        @Override
        public void placeInChunk(
            WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5
        ) {
            if (!this.isCreated) {
                this.pieces.clear();
                this.generatePieces(this.getChunkX(), this.getChunkZ());
            }

            super.placeInChunk(param0, param1, param2, param3, param4, param5);
        }
    }
}
