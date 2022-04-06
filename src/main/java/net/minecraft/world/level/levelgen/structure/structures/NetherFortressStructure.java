package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class NetherFortressStructure extends Structure {
    public static final WeightedRandomList<MobSpawnSettings.SpawnerData> FORTRESS_ENEMIES = WeightedRandomList.create(
        new MobSpawnSettings.SpawnerData(EntityType.BLAZE, 10, 2, 3),
        new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 4, 4),
        new MobSpawnSettings.SpawnerData(EntityType.WITHER_SKELETON, 8, 5, 5),
        new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 2, 5, 5),
        new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 3, 4, 4)
    );
    public static final Codec<NetherFortressStructure> CODEC = simpleCodec(NetherFortressStructure::new);

    public NetherFortressStructure(Structure.StructureSettings param0) {
        super(param0);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        ChunkPos var0 = param0.chunkPos();
        BlockPos var1 = new BlockPos(var0.getMinBlockX(), 64, var0.getMinBlockZ());
        return Optional.of(new Structure.GenerationStub(var1, param1 -> generatePieces(param1, param0)));
    }

    private static void generatePieces(StructurePiecesBuilder param0, Structure.GenerationContext param1) {
        NetherFortressPieces.StartPiece var0 = new NetherFortressPieces.StartPiece(
            param1.random(), param1.chunkPos().getBlockX(2), param1.chunkPos().getBlockZ(2)
        );
        param0.addPiece(var0);
        var0.addChildren(var0, param0, param1.random());
        List<StructurePiece> var1 = var0.pendingChildren;

        while(!var1.isEmpty()) {
            int var2 = param1.random().nextInt(var1.size());
            StructurePiece var3 = var1.remove(var2);
            var3.addChildren(var0, param0, param1.random());
        }

        param0.moveInsideHeights(param1.random(), 48, 70);
    }

    @Override
    public StructureType<?> type() {
        return StructureType.FORTRESS;
    }
}
