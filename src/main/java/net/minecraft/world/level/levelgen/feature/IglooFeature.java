package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.IglooPieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class IglooFeature extends StructureFeature<NoneFeatureConfiguration> {
    public IglooFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0, IglooFeature::generatePieces);
    }

    private static void generatePieces(StructurePiecesBuilder param0x, NoneFeatureConfiguration param1, PieceGenerator.Context param2) {
        if (param2.validBiomeOnTop(Heightmap.Types.WORLD_SURFACE_WG)) {
            BlockPos var0 = new BlockPos(param2.chunkPos().getMinBlockX(), 90, param2.chunkPos().getMinBlockZ());
            Rotation var1 = Rotation.getRandom(param2.random());
            IglooPieces.addPieces(param2.structureManager(), var0, var1, param0x, param2.random());
        }
    }
}
