package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureFeature;
import net.minecraft.world.level.levelgen.structure.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class StrongholdFeature extends NoiseAffectingStructureFeature<NoneFeatureConfiguration> {
    public StrongholdFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0, StrongholdFeature::generatePieces);
    }

    protected boolean isFeatureChunk(
        ChunkGenerator param0,
        BiomeSource param1,
        long param2,
        WorldgenRandom param3,
        ChunkPos param4,
        ChunkPos param5,
        NoneFeatureConfiguration param6,
        LevelHeightAccessor param7
    ) {
        return param0.hasStronghold(param4);
    }

    private static void generatePieces(StructurePiecesBuilder param0x, NoneFeatureConfiguration param1, PieceGenerator.Context param2) {
        int var0 = 0;

        StrongholdPieces.StartPiece var1;
        do {
            param0x.clear();
            param2.random().setLargeFeatureSeed(param2.seed() + (long)(var0++), param2.chunkPos().x, param2.chunkPos().z);
            StrongholdPieces.resetPieces();
            var1 = new StrongholdPieces.StartPiece(param2.random(), param2.chunkPos().getBlockX(2), param2.chunkPos().getBlockZ(2));
            param0x.addPiece(var1);
            var1.addChildren(var1, param0x, param2.random());
            List<StructurePiece> var2 = var1.pendingChildren;

            while(!var2.isEmpty()) {
                int var3 = param2.random().nextInt(var2.size());
                StructurePiece var4 = var2.remove(var3);
                var4.addChildren(var1, param0x, param2.random());
            }

            param0x.moveBelowSeaLevel(param2.chunkGenerator().getSeaLevel(), param2.chunkGenerator().getMinY(), param2.random(), 10);
        } while(param0x.isEmpty() || var1.portalRoomPiece == null);

    }
}
