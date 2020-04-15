package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.OceanMonumentPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class OceanMonumentFeature extends StructureFeature<NoneFeatureConfiguration> {
    private static final List<Biome.SpawnerData> MONUMENT_ENEMIES = Lists.newArrayList(new Biome.SpawnerData(EntityType.GUARDIAN, 1, 2, 4));

    public OceanMonumentFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    protected int getSpacing(DimensionType param0, ChunkGeneratorSettings param1) {
        return param1.getMonumentsSpacing();
    }

    @Override
    protected int getSeparation(DimensionType param0, ChunkGeneratorSettings param1) {
        return param1.getMonumentsSeparation();
    }

    @Override
    protected int getRandomSalt(ChunkGeneratorSettings param0) {
        return 10387313;
    }

    @Override
    protected boolean linearSeparation() {
        return false;
    }

    @Override
    protected boolean isFeatureChunk(
        BiomeManager param0, ChunkGenerator<?> param1, WorldgenRandom param2, int param3, int param4, Biome param5, ChunkPos param6
    ) {
        for(Biome var1 : param1.getBiomeSource().getBiomesWithin(param3 * 16 + 9, param1.getSeaLevel(), param4 * 16 + 9, 16)) {
            if (!param1.isBiomeValidStartForStructure(var1, this)) {
                return false;
            }
        }

        for(Biome var3 : param1.getBiomeSource().getBiomesWithin(param3 * 16 + 9, param1.getSeaLevel(), param4 * 16 + 9, 29)) {
            if (var3.getBiomeCategory() != Biome.BiomeCategory.OCEAN && var3.getBiomeCategory() != Biome.BiomeCategory.RIVER) {
                return false;
            }
        }

        return true;
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return OceanMonumentFeature.OceanMonumentStart::new;
    }

    @Override
    public String getFeatureName() {
        return "Monument";
    }

    @Override
    public int getLookupRange() {
        return 8;
    }

    @Override
    public List<Biome.SpawnerData> getSpecialEnemies() {
        return MONUMENT_ENEMIES;
    }

    public static class OceanMonumentStart extends StructureStart {
        private boolean isCreated;

        public OceanMonumentStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> param0, StructureManager param1, int param2, int param3, Biome param4) {
            this.generatePieces(param2, param3);
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
        public void postProcess(
            LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<?> param2, Random param3, BoundingBox param4, ChunkPos param5
        ) {
            if (!this.isCreated) {
                this.pieces.clear();
                this.generatePieces(this.getChunkX(), this.getChunkZ());
            }

            super.postProcess(param0, param1, param2, param3, param4, param5);
        }
    }
}
