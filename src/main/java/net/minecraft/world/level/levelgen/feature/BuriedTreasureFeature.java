package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BuriedTreasurePieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class BuriedTreasureFeature extends StructureFeature<ProbabilityFeatureConfiguration> {
    private static final int RANDOM_SALT = 10387320;

    public BuriedTreasureFeature(Codec<ProbabilityFeatureConfiguration> param0) {
        super(param0, PieceGeneratorSupplier.simple(BuriedTreasureFeature::checkLocation, BuriedTreasureFeature::generatePieces));
    }

    private static boolean checkLocation(PieceGeneratorSupplier.Context<ProbabilityFeatureConfiguration> param0x) {
        WorldgenRandom var0 = new WorldgenRandom(new LegacyRandomSource(0L));
        var0.setLargeFeatureWithSalt(param0x.seed(), param0x.chunkPos().x, param0x.chunkPos().z, 10387320);
        return var0.nextFloat() < param0x.config().probability && param0x.validBiomeOnTop(Heightmap.Types.OCEAN_FLOOR_WG);
    }

    private static void generatePieces(StructurePiecesBuilder param0x, PieceGenerator.Context<ProbabilityFeatureConfiguration> param1) {
        BlockPos var0 = new BlockPos(param1.chunkPos().getBlockX(9), 90, param1.chunkPos().getBlockZ(9));
        param0x.addPiece(new BuriedTreasurePieces.BuriedTreasurePiece(var0));
    }
}
