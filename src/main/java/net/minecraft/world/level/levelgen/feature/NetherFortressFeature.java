package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.QuartPos;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.NetherBridgePieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class NetherFortressFeature extends StructureFeature<NoneFeatureConfiguration> {
    public static final WeightedRandomList<MobSpawnSettings.SpawnerData> FORTRESS_ENEMIES = WeightedRandomList.create(
        new MobSpawnSettings.SpawnerData(EntityType.BLAZE, 10, 2, 3),
        new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 4, 4),
        new MobSpawnSettings.SpawnerData(EntityType.WITHER_SKELETON, 8, 5, 5),
        new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 2, 5, 5),
        new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 3, 4, 4)
    );

    public NetherFortressFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0, NetherFortressFeature::generatePieces);
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
        return param3.nextInt(5) < 2;
    }

    private static void generatePieces(StructurePiecesBuilder param0x, NoneFeatureConfiguration param1, PieceGenerator.Context param2) {
        if (param2.validBiome()
            .test(
                param2.chunkGenerator()
                    .getNoiseBiome(
                        QuartPos.fromBlock(param2.chunkPos().getMiddleBlockX()),
                        QuartPos.fromBlock(64),
                        QuartPos.fromBlock(param2.chunkPos().getMiddleBlockZ())
                    )
            )) {
            NetherBridgePieces.StartPiece var0 = new NetherBridgePieces.StartPiece(
                param2.random(), param2.chunkPos().getBlockX(2), param2.chunkPos().getBlockZ(2)
            );
            param0x.addPiece(var0);
            var0.addChildren(var0, param0x, param2.random());
            List<StructurePiece> var1 = var0.pendingChildren;

            while(!var1.isEmpty()) {
                int var2 = param2.random().nextInt(var1.size());
                StructurePiece var3 = var1.remove(var2);
                var3.addChildren(var0, param0x, param2.random());
            }

            param0x.moveInsideHeights(param2.random(), 48, 70);
        }
    }
}
