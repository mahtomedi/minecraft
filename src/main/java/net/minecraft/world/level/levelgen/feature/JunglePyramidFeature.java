package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.JunglePyramidPiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class JunglePyramidFeature extends StructureFeature<NoneFeatureConfiguration> {
    public JunglePyramidFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0, PieceGeneratorSupplier.simple(JunglePyramidFeature::checkLocation, JunglePyramidFeature::generatePieces));
    }

    private static <C extends FeatureConfiguration> boolean checkLocation(PieceGeneratorSupplier.Context<C> param0x) {
        if (!param0x.validBiomeOnTop(Heightmap.Types.WORLD_SURFACE_WG)) {
            return false;
        } else {
            return param0x.getLowestY(12, 15) >= param0x.chunkGenerator().getSeaLevel();
        }
    }

    private static void generatePieces(StructurePiecesBuilder param0x, PieceGenerator.Context<NoneFeatureConfiguration> param1) {
        param0x.addPiece(new JunglePyramidPiece(param1.random(), param1.chunkPos().getMinBlockX(), param1.chunkPos().getMinBlockZ()));
    }
}
