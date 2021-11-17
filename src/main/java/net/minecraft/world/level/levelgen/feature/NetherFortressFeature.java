package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.QuartPos;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.NetherBridgePieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
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
        super(param0, PieceGeneratorSupplier.simple(NetherFortressFeature::checkLocation, NetherFortressFeature::generatePieces));
    }

    private static boolean checkLocation(PieceGeneratorSupplier.Context<NoneFeatureConfiguration> param0x) {
        WorldgenRandom var0 = new WorldgenRandom(new LegacyRandomSource(0L));
        var0.setLargeFeatureSeed(param0x.seed(), param0x.chunkPos().x, param0x.chunkPos().z);
        return var0.nextInt(5) >= 2
            ? false
            : param0x.validBiome()
                .test(
                    param0x.chunkGenerator()
                        .getNoiseBiome(
                            QuartPos.fromBlock(param0x.chunkPos().getMiddleBlockX()),
                            QuartPos.fromBlock(64),
                            QuartPos.fromBlock(param0x.chunkPos().getMiddleBlockZ())
                        )
                );
    }

    private static void generatePieces(StructurePiecesBuilder param0x, PieceGenerator.Context<NoneFeatureConfiguration> param1) {
        NetherBridgePieces.StartPiece var0 = new NetherBridgePieces.StartPiece(param1.random(), param1.chunkPos().getBlockX(2), param1.chunkPos().getBlockZ(2));
        param0x.addPiece(var0);
        var0.addChildren(var0, param0x, param1.random());
        List<StructurePiece> var1 = var0.pendingChildren;

        while(!var1.isEmpty()) {
            int var2 = param1.random().nextInt(var1.size());
            StructurePiece var3 = var1.remove(var2);
            var3.addChildren(var0, param0x, param1.random());
        }

        param0x.moveInsideHeights(param1.random(), 48, 70);
    }
}
