package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.IglooPieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class IglooFeature extends StructureFeature<NoneFeatureConfiguration> {
    public IglooFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0, PieceGeneratorSupplier.simple(PieceGeneratorSupplier.checkForBiomeOnTop(Heightmap.Types.WORLD_SURFACE_WG), IglooFeature::generatePieces));
    }

    private static void generatePieces(StructurePiecesBuilder param0x, PieceGenerator.Context<NoneFeatureConfiguration> param1) {
        BlockPos var0 = new BlockPos(param1.chunkPos().getMinBlockX(), 90, param1.chunkPos().getMinBlockZ());
        Rotation var1 = Rotation.getRandom(param1.random());
        IglooPieces.addPieces(param1.structureManager(), var0, var1, param0x, param1.random());
    }
}
