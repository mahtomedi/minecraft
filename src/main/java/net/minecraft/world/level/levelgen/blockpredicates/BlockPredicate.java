package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public interface BlockPredicate extends BiPredicate<WorldGenLevel, BlockPos> {
    Codec<BlockPredicate> CODEC = Registry.BLOCK_PREDICATE_TYPES.byNameCodec().dispatch(BlockPredicate::type, BlockPredicateType::codec);
    BlockPredicate ONLY_IN_AIR_PREDICATE = matchesBlock(Blocks.AIR, BlockPos.ZERO);
    BlockPredicate ONLY_IN_AIR_OR_WATER_PREDICATE = matchesBlocks(List.of(Blocks.AIR, Blocks.WATER), BlockPos.ZERO);

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

    static BlockPredicate matchesBlocks(List<Block> param0, Vec3i param1) {
        return new MatchingBlocksPredicate(param1, HolderSet.direct(Block::builtInRegistryHolder, param0));
    }

    static BlockPredicate matchesBlocks(List<Block> param0) {
        return matchesBlocks(param0, Vec3i.ZERO);
    }

    static BlockPredicate matchesBlock(Block param0, Vec3i param1) {
        return matchesBlocks(List.of(param0), param1);
    }

    static BlockPredicate matchesTag(TagKey<Block> param0, Vec3i param1) {
        return new MatchingBlockTagPredicate(param1, param0);
    }

    static BlockPredicate matchesTag(TagKey<Block> param0) {
        return matchesTag(param0, Vec3i.ZERO);
    }

    static BlockPredicate matchesFluids(List<Fluid> param0, Vec3i param1) {
        return new MatchingFluidsPredicate(param1, HolderSet.direct(Fluid::builtInRegistryHolder, param0));
    }

    static BlockPredicate matchesFluid(Fluid param0, Vec3i param1) {
        return matchesFluids(List.of(param0), param1);
    }

    static BlockPredicate not(BlockPredicate param0) {
        return new NotPredicate(param0);
    }

    static BlockPredicate replaceable(Vec3i param0) {
        return new ReplaceablePredicate(param0);
    }

    static BlockPredicate replaceable() {
        return replaceable(Vec3i.ZERO);
    }

    static BlockPredicate wouldSurvive(BlockState param0, Vec3i param1) {
        return new WouldSurvivePredicate(param1, param0);
    }

    static BlockPredicate hasSturdyFace(Vec3i param0, Direction param1) {
        return new HasSturdyFacePredicate(param0, param1);
    }

    static BlockPredicate hasSturdyFace(Direction param0) {
        return hasSturdyFace(Vec3i.ZERO, param0);
    }

    static BlockPredicate solid(Vec3i param0) {
        return new SolidPredicate(param0);
    }

    static BlockPredicate solid() {
        return solid(Vec3i.ZERO);
    }

    static BlockPredicate insideWorld(Vec3i param0) {
        return new InsideWorldBoundsPredicate(param0);
    }

    static BlockPredicate alwaysTrue() {
        return TrueBlockPredicate.INSTANCE;
    }
}
