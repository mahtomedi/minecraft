package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class DesertPyramidStructure extends SinglePieceStructure {
    public static final Codec<DesertPyramidStructure> CODEC = simpleCodec(DesertPyramidStructure::new);

    public DesertPyramidStructure(Structure.StructureSettings param0) {
        super(DesertPyramidPiece::new, 21, 21, param0);
    }

    @Override
    public void afterPlace(
        WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, PiecesContainer param6
    ) {
        Set<BlockPos> var0 = SortedArraySet.create(Vec3i::compareTo);

        for(StructurePiece var1 : param6.pieces()) {
            if (var1 instanceof DesertPyramidPiece var2) {
                var0.addAll(var2.getPotentialSuspiciousSandWorldPositions());
                placeSuspiciousSand(param4, param0, var2.getRandomCollapsedRoofPos());
            }
        }

        ObjectArrayList<BlockPos> var3 = new ObjectArrayList<>(var0.stream().toList());
        RandomSource var4 = RandomSource.create(param0.getSeed()).forkPositional().at(param6.calculateBoundingBox().getCenter());
        Util.shuffle(var3, var4);
        int var5 = Math.min(var0.size(), var4.nextInt(5, 8));

        for(BlockPos var6 : var3) {
            if (var5 > 0) {
                --var5;
                placeSuspiciousSand(param4, param0, var6);
            } else if (param4.isInside(var6)) {
                param0.setBlock(var6, Blocks.SAND.defaultBlockState(), 2);
            }
        }

    }

    private static void placeSuspiciousSand(BoundingBox param0, WorldGenLevel param1, BlockPos param2) {
        if (param0.isInside(param2)) {
            param1.setBlock(param2, Blocks.SUSPICIOUS_SAND.defaultBlockState(), 2);
            param1.getBlockEntity(param2, BlockEntityType.BRUSHABLE_BLOCK)
                .ifPresent(param1x -> param1x.setLootTable(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY, param2.asLong()));
        }

    }

    @Override
    public StructureType<?> type() {
        return StructureType.DESERT_PYRAMID;
    }
}
