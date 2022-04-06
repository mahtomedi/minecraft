package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class MangroveRootPlacer extends RootPlacer {
    public static final int ROOT_WIDTH_LIMIT = 8;
    public static final int ROOT_LENGTH_LIMIT = 15;
    public static final Codec<MangroveRootPlacer> CODEC = RecordCodecBuilder.create(
        param0 -> rootPlacerParts(param0)
                .and(
                    param0.group(
                        RegistryCodecs.homogeneousList(Registry.BLOCK_REGISTRY).fieldOf("can_grow_through").forGetter(param0x -> param0x.canGrowThrough),
                        RegistryCodecs.homogeneousList(Registry.BLOCK_REGISTRY).fieldOf("muddy_roots_in").forGetter(param0x -> param0x.muddyRootsIn),
                        BlockStateProvider.CODEC.fieldOf("muddy_roots_provider").forGetter(param0x -> param0x.muddyRootsProvider),
                        Codec.intRange(1, 12).fieldOf("max_root_width").forGetter(param0x -> param0x.maxRootWidth),
                        Codec.intRange(1, 64).fieldOf("max_root_length").forGetter(param0x -> param0x.maxRootLength),
                        IntProvider.CODEC.fieldOf("y_offset").forGetter(param0x -> param0x.yOffset),
                        Codec.floatRange(0.0F, 1.0F).fieldOf("random_skew_chance").forGetter(param0x -> param0x.randomSkewChance)
                    )
                )
                .apply(param0, MangroveRootPlacer::new)
    );
    private final HolderSet<Block> canGrowThrough;
    private final HolderSet<Block> muddyRootsIn;
    private final BlockStateProvider muddyRootsProvider;
    private final int maxRootWidth;
    private final int maxRootLength;
    private final IntProvider yOffset;
    private final float randomSkewChance;

    public MangroveRootPlacer(
        BlockStateProvider param0,
        HolderSet<Block> param1,
        HolderSet<Block> param2,
        BlockStateProvider param3,
        int param4,
        int param5,
        IntProvider param6,
        float param7
    ) {
        super(param0);
        this.canGrowThrough = param1;
        this.muddyRootsIn = param2;
        this.muddyRootsProvider = param3;
        this.maxRootWidth = param4;
        this.maxRootLength = param5;
        this.yOffset = param6;
        this.randomSkewChance = param7;
    }

    @Override
    public Optional<BlockPos> placeRoots(
        LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, RandomSource param2, BlockPos param3, TreeConfiguration param4
    ) {
        BlockPos var0 = param3.offset(0, this.yOffset.sample(param2), 0);
        List<BlockPos> var1 = Lists.newArrayList();
        if (!this.canPlaceRoot(param0, var0)) {
            return Optional.empty();
        } else {
            var1.add(var0.below());

            for(Direction var2 : Direction.Plane.HORIZONTAL) {
                BlockPos var3 = var0.relative(var2);
                List<BlockPos> var4 = Lists.newArrayList();
                if (!this.simulateRoots(param0, param2, var3, var2, var0, var4, 0)) {
                    return Optional.empty();
                }

                var1.addAll(var4);
                var1.add(var0.relative(var2));
            }

            for(BlockPos var5 : var1) {
                this.placeRoot(param0, param1, param2, var5, param4);
            }

            return Optional.of(var0);
        }
    }

    private boolean simulateRoots(
        LevelSimulatedReader param0, RandomSource param1, BlockPos param2, Direction param3, BlockPos param4, List<BlockPos> param5, int param6
    ) {
        if (param6 != this.maxRootLength && param5.size() <= this.maxRootLength) {
            for(BlockPos var1 : this.potentialRootPositions(param2, param3, param1, param4)) {
                if (this.canPlaceRoot(param0, var1)) {
                    param5.add(var1);
                    if (!this.simulateRoots(param0, param1, var1, param3, param4, param5, param6 + 1)) {
                        return false;
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    protected List<BlockPos> potentialRootPositions(BlockPos param0, Direction param1, RandomSource param2, BlockPos param3) {
        BlockPos var0 = param0.below();
        BlockPos var1 = param0.relative(param1);
        int var2 = param0.distManhattan(param3);
        if (var2 > this.maxRootWidth - 3 && var2 <= this.maxRootWidth) {
            return param2.nextFloat() < this.randomSkewChance ? List.of(var0, var1.below()) : List.of(var0);
        } else if (var2 > this.maxRootWidth) {
            return List.of(var0);
        } else if (param2.nextFloat() < this.randomSkewChance) {
            return List.of(var0);
        } else {
            return param2.nextBoolean() ? List.of(var1) : List.of(var0);
        }
    }

    protected boolean canPlaceRoot(LevelSimulatedReader param0, BlockPos param1) {
        return TreeFeature.validTreePos(param0, param1) || param0.isStateAtPosition(param1, param0x -> param0x.is(this.canGrowThrough));
    }

    @Override
    protected void placeRoot(
        LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, RandomSource param2, BlockPos param3, TreeConfiguration param4
    ) {
        if (param0.isStateAtPosition(param3, param0x -> param0x.is(this.muddyRootsIn))) {
            BlockState var0 = this.muddyRootsProvider.getState(param2, param3);
            param1.accept(param3, this.getPotentiallyWaterloggedState(param0, param3, var0));
        } else {
            super.placeRoot(param0, param1, param2, param3, param4);
        }

    }

    @Override
    protected RootPlacerType<?> type() {
        return RootPlacerType.MANGROVE_ROOT_PLACER;
    }
}
