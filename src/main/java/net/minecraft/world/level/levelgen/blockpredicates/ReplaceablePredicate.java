package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

class ReplaceablePredicate extends StateTestingPredicate {
    public static final Codec<ReplaceablePredicate> CODEC = RecordCodecBuilder.create(
        param0 -> stateTestingCodec(param0).apply(param0, ReplaceablePredicate::new)
    );

    public ReplaceablePredicate(BlockPos param0) {
        super(param0);
    }

    @Override
    protected boolean test(BlockState param0) {
        return param0.getMaterial().isReplaceable();
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.REPLACEABLE;
    }
}
