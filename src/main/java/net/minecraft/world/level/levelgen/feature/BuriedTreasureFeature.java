package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BuriedTreasurePieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class BuriedTreasureFeature extends StructureFeature<ProbabilityFeatureConfiguration> {
    private static final int RANDOM_SALT = 10387320;

    public BuriedTreasureFeature(Codec<ProbabilityFeatureConfiguration> param0) {
        super(param0, BuriedTreasureFeature::generatePieces);
    }

    protected boolean isFeatureChunk(
        ChunkGenerator param0,
        BiomeSource param1,
        long param2,
        WorldgenRandom param3,
        ChunkPos param4,
        ChunkPos param5,
        ProbabilityFeatureConfiguration param6,
        LevelHeightAccessor param7
    ) {
        param3.setLargeFeatureWithSalt(param2, param4.x, param4.z, 10387320);
        return param3.nextFloat() < param6.probability;
    }

    private static void generatePieces(StructurePiecesBuilder param0x, ProbabilityFeatureConfiguration param1, PieceGenerator.Context param2) {
        if (param2.validBiomeOnTop(Heightmap.Types.OCEAN_FLOOR_WG)) {
            BlockPos var0 = new BlockPos(param2.chunkPos().getBlockX(9), 90, param2.chunkPos().getBlockZ(9));
            param0x.addPiece(new BuriedTreasurePieces.BuriedTreasurePiece(var0));
        }
    }

    @Override
    public BlockPos getLocatePos(ChunkPos param0) {
        return new BlockPos(param0.getBlockX(9), 0, param0.getBlockZ(9));
    }
}
