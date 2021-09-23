package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.JunglePyramidPiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class JunglePyramidFeature extends StructureFeature<NoneFeatureConfiguration> {
    public JunglePyramidFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0, JunglePyramidFeature::generatePieces);
    }

    private static void generatePieces(StructurePiecesBuilder param0x, NoneFeatureConfiguration param1, PieceGenerator.Context param2) {
        if (param2.validBiomeOnTop(Heightmap.Types.WORLD_SURFACE_WG)) {
            if (param2.getLowestY(12, 15) >= param2.chunkGenerator().getSeaLevel()) {
                param0x.addPiece(new JunglePyramidPiece(param2.random(), param2.chunkPos().getMinBlockX(), param2.chunkPos().getMinBlockZ()));
            }
        }
    }
}
