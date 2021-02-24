package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
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
        ChunkPos param4,
        Biome param5,
        ChunkPos param6,
        ProbabilityFeatureConfiguration param7,
        LevelHeightAccessor param8
    ) {
        param3.setLargeFeatureWithSalt(param2, param4.x, param4.z, 10387320);
        return param3.nextFloat() < param7.probability;
    }

    @Override
    public StructureFeature.StructureStartFactory<ProbabilityFeatureConfiguration> getStartFactory() {
        return BuriedTreasureFeature.BuriedTreasureStart::new;
    }

    public static class BuriedTreasureStart extends StructureStart<ProbabilityFeatureConfiguration> {
        public BuriedTreasureStart(StructureFeature<ProbabilityFeatureConfiguration> param0, ChunkPos param1, BoundingBox param2, int param3, long param4) {
            super(param0, param1, param2, param3, param4);
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            ChunkPos param3,
            Biome param4,
            ProbabilityFeatureConfiguration param5,
            LevelHeightAccessor param6
        ) {
            BlockPos var0 = new BlockPos(param3.getBlockX(9), 90, param3.getBlockZ(9));
            this.pieces.add(new BuriedTreasurePieces.BuriedTreasurePiece(var0));
            this.calculateBoundingBox();
        }

        @Override
        public BlockPos getLocatePos() {
            ChunkPos var0 = this.getChunkPos();
            return new BlockPos(var0.getBlockX(9), 0, var0.getBlockZ(9));
        }
    }
}
