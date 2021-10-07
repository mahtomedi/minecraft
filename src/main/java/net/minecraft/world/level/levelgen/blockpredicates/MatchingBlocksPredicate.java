package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

class MatchingBlocksPredicate extends StateTestingPredicate {
    private final List<Block> blocks;
    public static final Codec<MatchingBlocksPredicate> CODEC = RecordCodecBuilder.create(
        param0 -> stateTestingCodec(param0)
                .and(Registry.BLOCK.listOf().fieldOf("blocks").forGetter(param0x -> param0x.blocks))
                .apply(param0, MatchingBlocksPredicate::new)
    );

    public MatchingBlocksPredicate(BlockPos param0, List<Block> param1) {
        super(param0);
        this.blocks = param1;
    }

    @Override
    protected boolean test(BlockState param0) {
        return this.blocks.contains(param0.getBlock());
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.MATCHING_BLOCKS;
    }
}
