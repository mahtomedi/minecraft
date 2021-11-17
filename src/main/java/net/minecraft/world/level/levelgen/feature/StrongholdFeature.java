package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureFeature;
import net.minecraft.world.level.levelgen.structure.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class StrongholdFeature extends NoiseAffectingStructureFeature<NoneFeatureConfiguration> {
    public StrongholdFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0, PieceGeneratorSupplier.simple(StrongholdFeature::checkLocation, StrongholdFeature::generatePieces));
    }

    private static boolean checkLocation(PieceGeneratorSupplier.Context<NoneFeatureConfiguration> param0x) {
        return param0x.chunkGenerator().hasStronghold(param0x.chunkPos());
    }

    private static void generatePieces(StructurePiecesBuilder param0x, PieceGenerator.Context<NoneFeatureConfiguration> param1) {
        int var0 = 0;

        StrongholdPieces.StartPiece var1;
        do {
            param0x.clear();
            param1.random().setLargeFeatureSeed(param1.seed() + (long)(var0++), param1.chunkPos().x, param1.chunkPos().z);
            StrongholdPieces.resetPieces();
            var1 = new StrongholdPieces.StartPiece(param1.random(), param1.chunkPos().getBlockX(2), param1.chunkPos().getBlockZ(2));
            param0x.addPiece(var1);
            var1.addChildren(var1, param0x, param1.random());
            List<StructurePiece> var2 = var1.pendingChildren;

            while(!var2.isEmpty()) {
                int var3 = param1.random().nextInt(var2.size());
                StructurePiece var4 = var2.remove(var3);
                var4.addChildren(var1, param0x, param1.random());
            }

            param0x.moveBelowSeaLevel(param1.chunkGenerator().getSeaLevel(), param1.chunkGenerator().getMinY(), param1.random(), 10);
        } while(param0x.isEmpty() || var1.portalRoomPiece == null);

    }
}
