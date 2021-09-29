package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;

class MatchingBlocksPredicate implements BlockPredicate {
    private final List<Block> blocks;
    private final BlockPos offset;
    public static final Codec<MatchingBlocksPredicate> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Registry.BLOCK.listOf().fieldOf("blocks").forGetter(param0x -> param0x.blocks),
                    BlockPos.CODEC.fieldOf("offset").forGetter(param0x -> param0x.offset)
                )
                .apply(param0, MatchingBlocksPredicate::new)
    );

    public MatchingBlocksPredicate(List<Block> param0, BlockPos param1) {
        this.blocks = param0;
        this.offset = param1;
    }

    public boolean test(WorldGenLevel param0, BlockPos param1) {
        Block var0 = param0.getBlockState(param1.offset(this.offset)).getBlock();
        return this.blocks.contains(var0);
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.MATCHING_BLOCKS;
    }
}
