package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

public class WoodlandMansionFeature extends StructureFeature<NoneFeatureConfiguration> {
    public WoodlandMansionFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0, WoodlandMansionFeature::pieceGeneratorSupplier, WoodlandMansionFeature::afterPlace);
    }

    private static Optional<PieceGenerator<NoneFeatureConfiguration>> pieceGeneratorSupplier(PieceGeneratorSupplier.Context<NoneFeatureConfiguration> param0x) {
        WorldgenRandom var0 = new WorldgenRandom(new LegacyRandomSource(0L));
        var0.setLargeFeatureSeed(param0x.seed(), param0x.chunkPos().x, param0x.chunkPos().z);
        Rotation var1 = Rotation.getRandom(var0);
        int var2 = 5;
        int var3 = 5;
        if (var1 == Rotation.CLOCKWISE_90) {
            var2 = -5;
        } else if (var1 == Rotation.CLOCKWISE_180) {
            var2 = -5;
            var3 = -5;
        } else if (var1 == Rotation.COUNTERCLOCKWISE_90) {
            var3 = -5;
        }

        int var4 = param0x.chunkPos().getBlockX(7);
        int var5 = param0x.chunkPos().getBlockZ(7);
        int[] var6 = param0x.getCornerHeights(var4, var2, var5, var3);
        int var7 = Math.min(Math.min(var6[0], var6[1]), Math.min(var6[2], var6[3]));
        if (var7 < 60) {
            return Optional.empty();
        } else if (!param0x.validBiome()
            .test(param0x.chunkGenerator().getNoiseBiome(QuartPos.fromBlock(var4), QuartPos.fromBlock(var6[0]), QuartPos.fromBlock(var5)))) {
            return Optional.empty();
        } else {
            BlockPos var8 = new BlockPos(param0x.chunkPos().getMiddleBlockX(), var7 + 1, param0x.chunkPos().getMiddleBlockZ());
            return Optional.of((param3, param4) -> {
                List<WoodlandMansionPieces.WoodlandMansionPiece> var0x = Lists.newLinkedList();
                WoodlandMansionPieces.generateMansion(param4.structureManager(), var8, var1, var0x, var0);
                var0x.forEach(param3::addPiece);
            });
        }
    }

    private static void afterPlace(
        WorldGenLevel param0x,
        StructureFeatureManager param1,
        ChunkGenerator param2,
        Random param3,
        BoundingBox param4,
        ChunkPos param5,
        PiecesContainer param6
    ) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        int var1 = param0x.getMinBuildHeight();
        BoundingBox var2 = param6.calculateBoundingBox();
        int var3 = var2.minY();

        for(int var4 = param4.minX(); var4 <= param4.maxX(); ++var4) {
            for(int var5 = param4.minZ(); var5 <= param4.maxZ(); ++var5) {
                var0.set(var4, var3, var5);
                if (!param0x.isEmptyBlock(var0) && var2.isInside(var0) && param6.isInsidePiece(var0)) {
                    for(int var6 = var3 - 1; var6 > var1; --var6) {
                        var0.setY(var6);
                        if (!param0x.isEmptyBlock(var0) && !param0x.getBlockState(var0).getMaterial().isLiquid()) {
                            break;
                        }

                        param0x.setBlock(var0, Blocks.COBBLESTONE.defaultBlockState(), 2);
                    }
                }
            }
        }

    }
}
