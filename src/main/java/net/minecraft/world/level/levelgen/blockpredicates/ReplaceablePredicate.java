package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

class ReplaceablePredicate implements BlockPredicate {
    public static final ReplaceablePredicate INSTANCE = new ReplaceablePredicate();
    public static final Codec<ReplaceablePredicate> CODEC = Codec.unit(() -> INSTANCE);

    private ReplaceablePredicate() {
    }

    public boolean test(WorldGenLevel param0, BlockPos param1) {
        return param0.getBlockState(param1).getMaterial().isReplaceable();
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.REPLACEABLE;
    }
}
