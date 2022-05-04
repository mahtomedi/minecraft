package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
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
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class UpwardsBranchingTrunkPlacer extends TrunkPlacer {
    public static final Codec<UpwardsBranchingTrunkPlacer> CODEC = RecordCodecBuilder.create(
        param0 -> trunkPlacerParts(param0)
                .and(
                    param0.group(
                        IntProvider.POSITIVE_CODEC.fieldOf("extra_branch_steps").forGetter(param0x -> param0x.extraBranchSteps),
                        Codec.floatRange(0.0F, 1.0F).fieldOf("place_branch_per_log_probability").forGetter(param0x -> param0x.placeBranchPerLogProbability),
                        IntProvider.NON_NEGATIVE_CODEC.fieldOf("extra_branch_length").forGetter(param0x -> param0x.extraBranchLength),
                        RegistryCodecs.homogeneousList(Registry.BLOCK_REGISTRY).fieldOf("can_grow_through").forGetter(param0x -> param0x.canGrowThrough)
                    )
                )
                .apply(param0, UpwardsBranchingTrunkPlacer::new)
    );
    private final IntProvider extraBranchSteps;
    private final float placeBranchPerLogProbability;
    private final IntProvider extraBranchLength;
    private final HolderSet<Block> canGrowThrough;

    public UpwardsBranchingTrunkPlacer(int param0, int param1, int param2, IntProvider param3, float param4, IntProvider param5, HolderSet<Block> param6) {
        super(param0, param1, param2);
        this.extraBranchSteps = param3;
        this.placeBranchPerLogProbability = param4;
        this.extraBranchLength = param5;
        this.canGrowThrough = param6;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.UPWARDS_BRANCHING_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
        LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, RandomSource param2, int param3, BlockPos param4, TreeConfiguration param5
    ) {
        List<FoliagePlacer.FoliageAttachment> var0 = Lists.newArrayList();
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

        for(int var2 = 0; var2 < param3; ++var2) {
            int var3 = param4.getY() + var2;
            if (this.placeLog(param0, param1, param2, var1.set(param4.getX(), var3, param4.getZ()), param5)
                && var2 < param3 - 1
                && param2.nextFloat() < this.placeBranchPerLogProbability) {
                Direction var4 = Direction.Plane.HORIZONTAL.getRandomDirection(param2);
                int var5 = this.extraBranchLength.sample(param2);
                int var6 = Math.max(0, var5 - this.extraBranchLength.sample(param2) - 1);
                int var7 = this.extraBranchSteps.sample(param2);
                this.placeBranch(param0, param1, param2, param3, param5, var0, var1, var3, var4, var6, var7);
            }

            if (var2 == param3 - 1) {
                var0.add(new FoliagePlacer.FoliageAttachment(var1.set(param4.getX(), var3 + 1, param4.getZ()), 0, false));
            }
        }

        return var0;
    }

    private void placeBranch(
        LevelSimulatedReader param0,
        BiConsumer<BlockPos, BlockState> param1,
        RandomSource param2,
        int param3,
        TreeConfiguration param4,
        List<FoliagePlacer.FoliageAttachment> param5,
        BlockPos.MutableBlockPos param6,
        int param7,
        Direction param8,
        int param9,
        int param10
    ) {
        int var0 = param7 + param9;
        int var1 = param6.getX();
        int var2 = param6.getZ();

        for(int var3 = param9; var3 < param3 && param10 > 0; --param10) {
            if (var3 >= 1) {
                int var4 = param7 + var3;
                var1 += param8.getStepX();
                var2 += param8.getStepZ();
                var0 = var4;
                if (this.placeLog(param0, param1, param2, param6.set(var1, var4, var2), param4)) {
                    var0 = var4 + 1;
                }

                param5.add(new FoliagePlacer.FoliageAttachment(param6.immutable(), 0, false));
            }

            ++var3;
        }

        if (var0 - param7 > 1) {
            BlockPos var5 = new BlockPos(var1, var0, var2);
            param5.add(new FoliagePlacer.FoliageAttachment(var5, 0, false));
            param5.add(new FoliagePlacer.FoliageAttachment(var5.below(2), 0, false));
        }

    }

    @Override
    protected boolean validTreePos(LevelSimulatedReader param0, BlockPos param1) {
        return super.validTreePos(param0, param1) || param0.isStateAtPosition(param1, param0x -> param0x.is(this.canGrowThrough));
    }
}
