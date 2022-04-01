package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.DesertPyramidPiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class DesertPyramidFeature extends StructureFeature<NoneFeatureConfiguration> {
    public DesertPyramidFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0, PieceGeneratorSupplier.simple(DesertPyramidFeature::checkLocation, DesertPyramidFeature::generatePieces));
    }

    private static <C extends FeatureConfiguration> boolean checkLocation(PieceGeneratorSupplier.Context<C> param0x) {
        if (!param0x.validBiomeOnTop(Heightmap.Types.WORLD_SURFACE_WG)) {
            return false;
        } else {
            return param0x.getLowestY(21, 21) >= param0x.chunkGenerator().getSeaLevel();
        }
    }

    private static void generatePieces(StructurePiecesBuilder param0x, PieceGenerator.Context<NoneFeatureConfiguration> param1) {
        param0x.addPiece(new DesertPyramidPiece(param1.random(), param1.chunkPos().getMinBlockX(), param1.chunkPos().getMinBlockZ()));
    }
}
