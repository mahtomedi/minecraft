package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class WoodlandMansionStructure extends Structure {
    public static final Codec<WoodlandMansionStructure> CODEC = RecordCodecBuilder.create(param0 -> codec(param0).apply(param0, WoodlandMansionStructure::new));

    public WoodlandMansionStructure(HolderSet<Biome> param0, Map<MobCategory, StructureSpawnOverride> param1, GenerationStep.Decoration param2, boolean param3) {
        super(param0, param1, param2, param3);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        Rotation var0 = Rotation.getRandom(param0.random());
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

        int var3 = param0.chunkPos().getBlockX(7);
        int var4 = param0.chunkPos().getBlockZ(7);
        int[] var5 = getCornerHeights(param0, var3, var1, var4, var2);
        int var6 = Math.min(Math.min(var5[0], var5[1]), Math.min(var5[2], var5[3]));
        if (var6 < 60) {
            return Optional.empty();
        } else {
            BlockPos var7 = new BlockPos(var3, var6, var4);
            return Optional.of(new Structure.GenerationStub(var7, param3 -> this.generatePieces(param3, param0, var7, var0)));
        }
    }

    private void generatePieces(StructurePiecesBuilder param0, Structure.GenerationContext param1, BlockPos param2, Rotation param3) {
        ChunkPos var0 = param1.chunkPos();
        BlockPos var1 = new BlockPos(var0.getMiddleBlockX(), param2.getY() + 1, var0.getMiddleBlockZ());
        List<WoodlandMansionPieces.WoodlandMansionPiece> var2 = Lists.newLinkedList();
        WoodlandMansionPieces.generateMansion(param1.structureTemplateManager(), var1, param3, var2, param1.random());
        var2.forEach(param0::addPiece);
    }

    @Override
    public void afterPlace(
        WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5, PiecesContainer param6
    ) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        int var1 = param0.getMinBuildHeight();
        BoundingBox var2 = param6.calculateBoundingBox();
        int var3 = var2.minY();

        for(int var4 = param4.minX(); var4 <= param4.maxX(); ++var4) {
            for(int var5 = param4.minZ(); var5 <= param4.maxZ(); ++var5) {
                var0.set(var4, var3, var5);
                if (!param0.isEmptyBlock(var0) && var2.isInside(var0) && param6.isInsidePiece(var0)) {
                    for(int var6 = var3 - 1; var6 > var1; --var6) {
                        var0.setY(var6);
                        if (!param0.isEmptyBlock(var0) && !param0.getBlockState(var0).getMaterial().isLiquid()) {
                            break;
                        }

                        param0.setBlock(var0, Blocks.COBBLESTONE.defaultBlockState(), 2);
                    }
                }
            }
        }

    }

    @Override
    public StructureType<?> type() {
        return StructureType.WOODLAND_MANSION;
    }
}
