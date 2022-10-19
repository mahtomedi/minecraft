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
import net.minecraft.world.level.material.Fluids;

public interface BlockPredicate extends BiPredicate<WorldGenLevel, BlockPos> {
    Codec<BlockPredicate> CODEC = Registry.BLOCK_PREDICATE_TYPES.byNameCodec().dispatch(BlockPredicate::type, BlockPredicateType::codec);
    BlockPredicate ONLY_IN_AIR_PREDICATE = matchesBlocks(Blocks.AIR);
    BlockPredicate ONLY_IN_AIR_OR_WATER_PREDICATE = matchesBlocks(Blocks.AIR, Blocks.WATER);

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

    static BlockPredicate matchesBlocks(Vec3i param0, List<Block> param1) {
        return new MatchingBlocksPredicate(param0, HolderSet.direct(Block::builtInRegistryHolder, param1));
    }

    static BlockPredicate matchesBlocks(List<Block> param0) {
        return matchesBlocks(Vec3i.ZERO, param0);
    }

    static BlockPredicate matchesBlocks(Vec3i param0, Block... param1) {
        return matchesBlocks(param0, List.of(param1));
    }

    static BlockPredicate matchesBlocks(Block... param0) {
        return matchesBlocks(Vec3i.ZERO, param0);
    }

    static BlockPredicate matchesTag(Vec3i param0, TagKey<Block> param1) {
        return new MatchingBlockTagPredicate(param0, param1);
    }

    static BlockPredicate matchesTag(TagKey<Block> param0) {
        return matchesTag(Vec3i.ZERO, param0);
    }

    static BlockPredicate matchesFluids(Vec3i param0, List<Fluid> param1) {
        return new MatchingFluidsPredicate(param0, HolderSet.direct(Fluid::builtInRegistryHolder, param1));
    }

    static BlockPredicate matchesFluids(Vec3i param0, Fluid... param1) {
        return matchesFluids(param0, List.of(param1));
    }

    static BlockPredicate matchesFluids(Fluid... param0) {
        return matchesFluids(Vec3i.ZERO, param0);
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

    static BlockPredicate noFluid() {
        return noFluid(Vec3i.ZERO);
    }

    static BlockPredicate noFluid(Vec3i param0) {
        return matchesFluids(param0, Fluids.EMPTY);
    }

    static BlockPredicate insideWorld(Vec3i param0) {
        return new InsideWorldBoundsPredicate(param0);
    }

    static BlockPredicate alwaysTrue() {
        return TrueBlockPredicate.INSTANCE;
    }
}
