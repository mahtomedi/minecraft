package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class WoodlandMansionStructure extends Structure {
    public static final Codec<WoodlandMansionStructure> CODEC = simpleCodec(WoodlandMansionStructure::new);

    public WoodlandMansionStructure(Structure.StructureSettings param0) {
        super(param0);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        Rotation var0 = Rotation.getRandom(param0.random());
        BlockPos var1 = this.getLowestYIn5by5BoxOffset7Blocks(param0, var0);
        return var1.getY() < 60 ? Optional.empty() : Optional.of(new Structure.GenerationStub(var1, param3 -> this.generatePieces(param3, param0, var1, var0)));
    }

    private void generatePieces(StructurePiecesBuilder param0, Structure.GenerationContext param1, BlockPos param2, Rotation param3) {
        List<WoodlandMansionPieces.WoodlandMansionPiece> var0 = Lists.newLinkedList();
        WoodlandMansionPieces.generateMansion(param1.structureTemplateManager(), param2, param3, var0, param1.random());
        var0.forEach(param0::addPiece);
    }

    @Override
    public void afterPlace(
        WorldGenLevel param0, StructureManager param1, ChunkGenerator param2, RandomSource param3, BoundingBox param4, ChunkPos param5, PiecesContainer param6
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
                        if (!param0.isEmptyBlock(var0) && !param0.getBlockState(var0).liquid()) {
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
