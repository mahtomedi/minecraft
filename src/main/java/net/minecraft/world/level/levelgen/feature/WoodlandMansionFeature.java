package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class WoodlandMansionFeature extends StructureFeature<NoneFeatureConfiguration> {
    public WoodlandMansionFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0, WoodlandMansionFeature::generatePieces, WoodlandMansionFeature::afterPlace);
    }

    @Override
    protected boolean linearSeparation() {
        return false;
    }

    private static void generatePieces(StructurePiecesBuilder param0x, NoneFeatureConfiguration param1, PieceGenerator.Context param2) {
        Rotation var0 = Rotation.getRandom(param2.random());
        int var1 = 5;
        int var2 = 5;
        if (var0 == Rotation.CLOCKWISE_90) {
            var1 = -5;
        } else if (var0 == Rotation.CLOCKWISE_180) {
            var1 = -5;
            var2 = -5;
        } else if (var0 == Rotation.COUNTERCLOCKWISE_90) {
            var2 = -5;
        }

        int var3 = param2.chunkPos().getBlockX(7);
        int var4 = param2.chunkPos().getBlockZ(7);
        int[] var5 = param2.getCornerHeights(var3, var1, var4, var2);
        int var6 = Math.min(Math.min(var5[0], var5[1]), Math.min(var5[2], var5[3]));
        if (var6 >= 60) {
            if (param2.validBiome()
                .test(param2.chunkGenerator().getNoiseBiome(QuartPos.fromBlock(var3), QuartPos.fromBlock(var5[0]), QuartPos.fromBlock(var4)))) {
                BlockPos var7 = new BlockPos(param2.chunkPos().getMiddleBlockX(), var6 + 1, param2.chunkPos().getMiddleBlockZ());
                List<WoodlandMansionPieces.WoodlandMansionPiece> var8 = Lists.newLinkedList();
                WoodlandMansionPieces.generateMansion(param2.structureManager(), var7, var0, var8, param2.random());
                var8.forEach(param0x::addPiece);
            }
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
