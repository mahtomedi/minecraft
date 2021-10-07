package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

class TrueBlockPredicate implements BlockPredicate {
    public static TrueBlockPredicate INSTANCE = new TrueBlockPredicate();
    public static final Codec<TrueBlockPredicate> CODEC = Codec.unit(() -> INSTANCE);

    private TrueBlockPredicate() {
    }

    public boolean test(WorldGenLevel param0, BlockPos param1) {
        return true;
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.TRUE;
    }
}
