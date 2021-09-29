package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

class NotPredicate implements BlockPredicate {
    public static final Codec<NotPredicate> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(BlockPredicate.CODEC.fieldOf("predicate").forGetter(param0x -> param0x.predicate)).apply(param0, NotPredicate::new)
    );
    private final BlockPredicate predicate;

    public NotPredicate(BlockPredicate param0) {
        this.predicate = param0;
    }

    public boolean test(WorldGenLevel param0, BlockPos param1) {
        return !this.predicate.test(param0, param1);
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.NOT;
    }
}
