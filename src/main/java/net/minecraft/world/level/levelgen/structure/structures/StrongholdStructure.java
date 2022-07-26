package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class StrongholdStructure extends Structure {
    public static final Codec<StrongholdStructure> CODEC = simpleCodec(StrongholdStructure::new);

    public StrongholdStructure(Structure.StructureSettings param0) {
        super(param0);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        return Optional.of(new Structure.GenerationStub(param0.chunkPos().getWorldPosition(), param1 -> generatePieces(param1, param0)));
    }

    private static void generatePieces(StructurePiecesBuilder param0, Structure.GenerationContext param1) {
        int var0 = 0;

        StrongholdPieces.StartPiece var1;
        do {
            param0.clear();
            param1.random().setLargeFeatureSeed(param1.seed() + (long)(var0++), param1.chunkPos().x, param1.chunkPos().z);
            StrongholdPieces.resetPieces();
            var1 = new StrongholdPieces.StartPiece(param1.random(), param1.chunkPos().getBlockX(2), param1.chunkPos().getBlockZ(2));
            param0.addPiece(var1);
            var1.addChildren(var1, param0, param1.random());
            List<StructurePiece> var2 = var1.pendingChildren;

            while(!var2.isEmpty()) {
                int var3 = param1.random().nextInt(var2.size());
                StructurePiece var4 = var2.remove(var3);
                var4.addChildren(var1, param0, param1.random());
            }

            param0.moveBelowSeaLevel(param1.chunkGenerator().getSeaLevel(), param1.chunkGenerator().getMinY(), param1.random(), 10);
        } while(param0.isEmpty() || var1.portalRoomPiece == null);

    }

    @Override
    public StructureType<?> type() {
        return StructureType.STRONGHOLD;
    }
}
