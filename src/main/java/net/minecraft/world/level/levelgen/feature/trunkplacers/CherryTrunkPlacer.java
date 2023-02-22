package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class CherryTrunkPlacer extends TrunkPlacer {
    private static final Codec<UniformInt> BRANCH_START_CODEC = ExtraCodecs.validate(
        UniformInt.CODEC,
        param0 -> param0.getMaxValue() - param0.getMinValue() < 1
                ? DataResult.error(() -> "Need at least 2 blocks variation for the branch starts to fit both branches")
                : DataResult.success(param0)
    );
    public static final Codec<CherryTrunkPlacer> CODEC = RecordCodecBuilder.create(
        param0 -> trunkPlacerParts(param0)
                .and(
                    param0.group(
                        IntProvider.codec(1, 3).fieldOf("branch_count").forGetter(param0x -> param0x.branchCount),
                        IntProvider.codec(2, 16).fieldOf("branch_horizontal_length").forGetter(param0x -> param0x.branchHorizontalLength),
                        IntProvider.codec(-16, 0, BRANCH_START_CODEC)
                            .fieldOf("branch_start_offset_from_top")
                            .forGetter(param0x -> param0x.branchStartOffsetFromTop),
                        IntProvider.codec(-16, 16).fieldOf("branch_end_offset_from_top").forGetter(param0x -> param0x.branchEndOffsetFromTop)
                    )
                )
                .apply(param0, CherryTrunkPlacer::new)
    );
    private final IntProvider branchCount;
    private final IntProvider branchHorizontalLength;
    private final UniformInt branchStartOffsetFromTop;
    private final UniformInt secondBranchStartOffsetFromTop;
    private final IntProvider branchEndOffsetFromTop;

    public CherryTrunkPlacer(int param0, int param1, int param2, IntProvider param3, IntProvider param4, UniformInt param5, IntProvider param6) {
        super(param0, param1, param2);
        this.branchCount = param3;
        this.branchHorizontalLength = param4;
        this.branchStartOffsetFromTop = param5;
        this.secondBranchStartOffsetFromTop = UniformInt.of(param5.getMinValue(), param5.getMaxValue() - 1);
        this.branchEndOffsetFromTop = param6;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return TrunkPlacerType.CHERRY_TRUNK_PLACER;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
        LevelSimulatedReader param0, BiConsumer<BlockPos, BlockState> param1, RandomSource param2, int param3, BlockPos param4, TreeConfiguration param5
    ) {
        setDirtAt(param0, param1, param2, param4.below(), param5);
        int var0 = Math.max(0, param3 - 1 + this.branchStartOffsetFromTop.sample(param2));
        int var1 = Math.max(0, param3 - 1 + this.secondBranchStartOffsetFromTop.sample(param2));
        if (var1 >= var0) {
            ++var1;
        }

        int var2 = this.branchCount.sample(param2);
        boolean var3 = var2 == 3;
        boolean var4 = var2 >= 2;
        int var5;
        if (var3) {
            var5 = param3;
        } else if (var4) {
            var5 = Math.max(var0, var1) + 1;
        } else {
            var5 = var0 + 1;
        }

        for(int var8 = 0; var8 < var5; ++var8) {
            this.placeLog(param0, param1, param2, param4.above(var8), param5);
        }

        List<FoliagePlacer.FoliageAttachment> var9 = new ArrayList<>();
        if (var3) {
            var9.add(new FoliagePlacer.FoliageAttachment(param4.above(var5), 0, false));
        }

        BlockPos.MutableBlockPos var10 = new BlockPos.MutableBlockPos();
        Direction var11 = Direction.Plane.HORIZONTAL.getRandomDirection(param2);
        Function<BlockState, BlockState> var12 = param1x -> param1x.trySetValue(RotatedPillarBlock.AXIS, var11.getAxis());
        var9.add(this.generateBranch(param0, param1, param2, param3, param4, param5, var12, var11, var0, var0 < var5 - 1, var10));
        if (var4) {
            var9.add(this.generateBranch(param0, param1, param2, param3, param4, param5, var12, var11.getOpposite(), var1, var1 < var5 - 1, var10));
        }

        return var9;
    }

    private FoliagePlacer.FoliageAttachment generateBranch(
        LevelSimulatedReader param0,
        BiConsumer<BlockPos, BlockState> param1,
        RandomSource param2,
        int param3,
        BlockPos param4,
        TreeConfiguration param5,
        Function<BlockState, BlockState> param6,
        Direction param7,
        int param8,
        boolean param9,
        BlockPos.MutableBlockPos param10
    ) {
        param10.set(param4).move(Direction.UP, param8);
        int var0 = param3 - 1 + this.branchEndOffsetFromTop.sample(param2);
        boolean var1 = param9 || var0 < param8;
        int var2 = this.branchHorizontalLength.sample(param2) + (var1 ? 1 : 0);
        BlockPos var3 = param4.relative(param7, var2).above(var0);
        int var4 = var1 ? 2 : 1;

        for(int var5 = 0; var5 < var4; ++var5) {
            this.placeLog(param0, param1, param2, param10.move(param7), param5, param6);
        }

        Direction var6 = var3.getY() > param10.getY() ? Direction.UP : Direction.DOWN;

        while(true) {
            int var7 = param10.distManhattan(var3);
            if (var7 == 0) {
                return new FoliagePlacer.FoliageAttachment(var3.above(), 0, false);
            }

            float var8 = (float)Math.abs(var3.getY() - param10.getY()) / (float)var7;
            boolean var9 = param2.nextFloat() < var8;
            param10.move(var9 ? var6 : param7);
            this.placeLog(param0, param1, param2, param10, param5, var9 ? Function.identity() : param6);
        }
    }
}
