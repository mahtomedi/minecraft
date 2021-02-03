package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuriedTreasurePieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class BuriedTreasureFeature extends StructureFeature<ProbabilityFeatureConfiguration> {
    public BuriedTreasureFeature(Codec<ProbabilityFeatureConfiguration> param0) {
        super(param0);
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
        ProbabilityFeatureConfiguration param8,
        LevelHeightAccessor param9
    ) {
        param3.setLargeFeatureWithSalt(param2, param4, param5, 10387320);
        return param3.nextFloat() < param8.probability;
    }

    @Override
    public StructureFeature.StructureStartFactory<ProbabilityFeatureConfiguration> getStartFactory() {
        return BuriedTreasureFeature.BuriedTreasureStart::new;
    }

    public static class BuriedTreasureStart extends StructureStart<ProbabilityFeatureConfiguration> {
        public BuriedTreasureStart(
            StructureFeature<ProbabilityFeatureConfiguration> param0, int param1, int param2, BoundingBox param3, int param4, long param5
        ) {
            super(param0, param1, param2, param3, param4, param5);
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            int param3,
            int param4,
            Biome param5,
            ProbabilityFeatureConfiguration param6,
            LevelHeightAccessor param7
        ) {
            BlockPos var0 = new BlockPos(SectionPos.sectionToBlockCoord(param3, 9), 90, SectionPos.sectionToBlockCoord(param4, 9));
            this.pieces.add(new BuriedTreasurePieces.BuriedTreasurePiece(var0));
            this.calculateBoundingBox();
        }

        @Override
        public BlockPos getLocatePos() {
            return new BlockPos(SectionPos.sectionToBlockCoord(this.getChunkX(), 9), 0, SectionPos.sectionToBlockCoord(this.getChunkZ(), 9));
        }
    }
}
