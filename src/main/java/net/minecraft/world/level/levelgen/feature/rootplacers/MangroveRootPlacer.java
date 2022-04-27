package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class MangroveRootPlacer extends RootPlacer {
    public static final int ROOT_WIDTH_LIMIT = 8;
    public static final int ROOT_LENGTH_LIMIT = 15;
    public static final Codec<MangroveRootPlacer> CODEC = RecordCodecBuilder.create(
        param0 -> rootPlacerParts(param0)
                .and(MangroveRootPlacement.CODEC.fieldOf("mangrove_root_placement").forGetter(param0x -> param0x.mangroveRootPlacement))
                .apply(param0, MangroveRootPlacer::new)
    );
    private final MangroveRootPlacement mangroveRootPlacement;

    public MangroveRootPlacer(IntProvider param0, BlockStateProvider param1, Optional<AboveRootPlacement> param2, MangroveRootPlacement param3) {
        super(param0, param1, param2);
        this.mangroveRootPlacement = param3;
    }

    @Override
    public boolean placeRoots(
        LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, RandomSource param2, BlockPos param3, BlockPos param4, TreeConfiguration param5
    ) {
        List<BlockPos> var0 = Lists.newArrayList();
        BlockPos.MutableBlockPos var1 = param3.mutable();

        while(var1.getY() < param4.getY()) {
            if (!this.canPlaceRoot(param0, var1)) {
                return false;
            }

            var1.move(Direction.UP);
        }

        var0.add(param4.below());

        for(Direction var2 : Direction.Plane.HORIZONTAL) {
            BlockPos var3 = param4.relative(var2);
            List<BlockPos> var4 = Lists.newArrayList();
            if (!this.simulateRoots(param0, param2, var3, var2, param4, var4, 0)) {
                return false;
            }

            var0.addAll(var4);
            var0.add(param4.relative(var2));
        }

        for(BlockPos var5 : var0) {
            this.placeRoot(param0, param1, param2, var5, param5);
        }

        return true;
    }

    private boolean simulateRoots(
        LevelSimulatedReader param0, RandomSource param1, BlockPos param2, Direction param3, BlockPos param4, List<BlockPos> param5, int param6
    ) {
        int var0 = this.mangroveRootPlacement.maxRootLength();
        if (param6 != var0 && param5.size() <= var0) {
            for(BlockPos var2 : this.potentialRootPositions(param2, param3, param1, param4)) {
                if (this.canPlaceRoot(param0, var2)) {
                    param5.add(var2);
                    if (!this.simulateRoots(param0, param1, var2, param3, param4, param5, param6 + 1)) {
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
        int var3 = this.mangroveRootPlacement.maxRootWidth();
        float var4 = this.mangroveRootPlacement.randomSkewChance();
        if (var2 > var3 - 3 && var2 <= var3) {
            return param2.nextFloat() < var4 ? List.of(var0, var1.below()) : List.of(var0);
        } else if (var2 > var3) {
            return List.of(var0);
        } else if (param2.nextFloat() < var4) {
            return List.of(var0);
        } else {
            return param2.nextBoolean() ? List.of(var1) : List.of(var0);
        }
    }

    @Override
    protected boolean canPlaceRoot(LevelSimulatedReader param0, BlockPos param1) {
        return super.canPlaceRoot(param0, param1) || param0.isStateAtPosition(param1, param0x -> param0x.is(this.mangroveRootPlacement.canGrowThrough()));
    }

    @Override
    protected void placeRoot(
        LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, RandomSource param2, BlockPos param3, TreeConfiguration param4
    ) {
        if (param0.isStateAtPosition(param3, param0x -> param0x.is(this.mangroveRootPlacement.muddyRootsIn()))) {
            BlockState var0 = this.mangroveRootPlacement.muddyRootsProvider().getState(param2, param3);
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
