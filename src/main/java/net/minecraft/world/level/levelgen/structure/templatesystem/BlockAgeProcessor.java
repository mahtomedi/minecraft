package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

public class BlockAgeProcessor extends StructureProcessor {
    public static final Codec<BlockAgeProcessor> CODEC = Codec.FLOAT.fieldOf("mossiness").xmap(BlockAgeProcessor::new, param0 -> param0.mossiness).codec();
    private static final float PROBABILITY_OF_REPLACING_FULL_BLOCK = 0.5F;
    private static final float PROBABILITY_OF_REPLACING_STAIRS = 0.5F;
    private static final float PROBABILITY_OF_REPLACING_OBSIDIAN = 0.15F;
    private static final BlockState[] NON_MOSSY_REPLACEMENTS = new BlockState[]{
        Blocks.STONE_SLAB.defaultBlockState(), Blocks.STONE_BRICK_SLAB.defaultBlockState()
    };
    private final float mossiness;

    public BlockAgeProcessor(float param0) {
        this.mossiness = param0;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
        LevelReader param0,
        BlockPos param1,
        BlockPos param2,
        StructureTemplate.StructureBlockInfo param3,
        StructureTemplate.StructureBlockInfo param4,
        StructurePlaceSettings param5
    ) {
        RandomSource var0 = param5.getRandom(param4.pos());
        BlockState var1 = param4.state();
        BlockPos var2 = param4.pos();
        BlockState var3 = null;
        if (var1.is(Blocks.STONE_BRICKS) || var1.is(Blocks.STONE) || var1.is(Blocks.CHISELED_STONE_BRICKS)) {
            var3 = this.maybeReplaceFullStoneBlock(var0);
        } else if (var1.is(BlockTags.STAIRS)) {
            var3 = this.maybeReplaceStairs(var0, param4.state());
        } else if (var1.is(BlockTags.SLABS)) {
            var3 = this.maybeReplaceSlab(var0);
        } else if (var1.is(BlockTags.WALLS)) {
            var3 = this.maybeReplaceWall(var0);
        } else if (var1.is(Blocks.OBSIDIAN)) {
            var3 = this.maybeReplaceObsidian(var0);
        }

        return var3 != null ? new StructureTemplate.StructureBlockInfo(var2, var3, param4.nbt()) : param4;
    }

    @Nullable
    private BlockState maybeReplaceFullStoneBlock(RandomSource param0) {
        if (param0.nextFloat() >= 0.5F) {
            return null;
        } else {
            BlockState[] var0 = new BlockState[]{Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), getRandomFacingStairs(param0, Blocks.STONE_BRICK_STAIRS)};
            BlockState[] var1 = new BlockState[]{Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), getRandomFacingStairs(param0, Blocks.MOSSY_STONE_BRICK_STAIRS)};
            return this.getRandomBlock(param0, var0, var1);
        }
    }

    @Nullable
    private BlockState maybeReplaceStairs(RandomSource param0, BlockState param1) {
        Direction var0 = param1.getValue(StairBlock.FACING);
        Half var1 = param1.getValue(StairBlock.HALF);
        if (param0.nextFloat() >= 0.5F) {
            return null;
        } else {
            BlockState[] var2 = new BlockState[]{
                Blocks.MOSSY_STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, var0).setValue(StairBlock.HALF, var1),
                Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState()
            };
            return this.getRandomBlock(param0, NON_MOSSY_REPLACEMENTS, var2);
        }
    }

    @Nullable
    private BlockState maybeReplaceSlab(RandomSource param0) {
        return param0.nextFloat() < this.mossiness ? Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState() : null;
    }

    @Nullable
    private BlockState maybeReplaceWall(RandomSource param0) {
        return param0.nextFloat() < this.mossiness ? Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState() : null;
    }

    @Nullable
    private BlockState maybeReplaceObsidian(RandomSource param0) {
        return param0.nextFloat() < 0.15F ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : null;
    }

    private static BlockState getRandomFacingStairs(RandomSource param0, Block param1) {
        return param1.defaultBlockState()
            .setValue(StairBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(param0))
            .setValue(StairBlock.HALF, Util.getRandom(Half.values(), param0));
    }

    private BlockState getRandomBlock(RandomSource param0, BlockState[] param1, BlockState[] param2) {
        return param0.nextFloat() < this.mossiness ? getRandomBlock(param0, param2) : getRandomBlock(param0, param1);
    }

    private static BlockState getRandomBlock(RandomSource param0, BlockState[] param1) {
        return param1[param0.nextInt(param1.length)];
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.BLOCK_AGE;
    }
}
