package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

public class BlockPredicateFilter extends PlacementFilter {
    public static final Codec<BlockPredicateFilter> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(BlockPredicate.CODEC.fieldOf("predicate").forGetter(param0x -> param0x.predicate)).apply(param0, BlockPredicateFilter::new)
    );
    private final BlockPredicate predicate;

    private BlockPredicateFilter(BlockPredicate param0) {
        this.predicate = param0;
    }

    public static BlockPredicateFilter forPredicate(BlockPredicate param0) {
        return new BlockPredicateFilter(param0);
    }

    @Override
    protected boolean shouldPlace(PlacementContext param0, Random param1, BlockPos param2) {
        return this.predicate.test(param0.getLevel(), param2);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.BLOCK_PREDICATE_FILTER;
    }
}
