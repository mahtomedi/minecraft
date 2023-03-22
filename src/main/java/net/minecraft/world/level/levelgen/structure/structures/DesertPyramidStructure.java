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
            }
        }

        ObjectArrayList<BlockPos> var3 = new ObjectArrayList<>(var0.stream().toList());
        Util.shuffle(var3, param3);
        int var4 = Math.min(var0.size(), param3.nextInt(5, 8));

        for(BlockPos var5 : var3) {
            if (var4 > 0) {
                --var4;
                param0.setBlock(var5, Blocks.SUSPICIOUS_SAND.defaultBlockState(), 2);
                param0.getBlockEntity(var5, BlockEntityType.BRUSHABLE_BLOCK)
                    .ifPresent(param1x -> param1x.setLootTable(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY, var5.asLong()));
            } else {
                param0.setBlock(var5, Blocks.SAND.defaultBlockState(), 2);
            }
        }

    }

    @Override
    public StructureType<?> type() {
        return StructureType.DESERT_PYRAMID;
    }
}
