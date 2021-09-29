package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public interface BlockPredicate extends BiPredicate<WorldGenLevel, BlockPos> {
    Codec<BlockPredicate> CODEC = Registry.BLOCK_PREDICATE_TYPES.dispatch(BlockPredicate::type, BlockPredicateType::codec);

    BlockPredicateType<?> type();

    static BlockPredicate allOf(List<BlockPredicate> param0) {
        return new AllOfPredicate(param0);
    }

    static BlockPredicate allOf(BlockPredicate... param0) {
        return allOf(List.of(param0));
    }

    static BlockPredicate allOf(BlockPredicate param0, BlockPredicate param1) {
        return allOf(List.of(param0, param1));
    }

    static BlockPredicate anyOf(List<BlockPredicate> param0) {
        return new AnyOfPredicate(param0);
    }

    static BlockPredicate anyOf(BlockPredicate... param0) {
        return anyOf(List.of(param0));
    }

    static BlockPredicate anyOf(BlockPredicate param0, BlockPredicate param1) {
        return anyOf(List.of(param0, param1));
    }

    static BlockPredicate matchesBlocks(List<Block> param0, BlockPos param1) {
        return new MatchingBlocksPredicate(param0, param1);
    }

    static BlockPredicate matchesBlock(Block param0, BlockPos param1) {
        return matchesBlocks(List.of(param0), param1);
    }

    static BlockPredicate matchesFluids(List<Fluid> param0, BlockPos param1) {
        return new MatchingFluidsPredicate(param0, param1);
    }

    static BlockPredicate matchesFluid(Fluid param0, BlockPos param1) {
        return matchesFluids(List.of(param0), param1);
    }

    static BlockPredicate not(BlockPredicate param0) {
        return new NotPredicate(param0);
    }

    static BlockPredicate replaceable() {
        return ReplaceablePredicate.INSTANCE;
    }
}