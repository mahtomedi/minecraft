package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products.P2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.material.Fluids;

public abstract class FoliagePlacer {
    public static final Codec<FoliagePlacer> CODEC = BuiltInRegistries.FOLIAGE_PLACER_TYPE
        .byNameCodec()
        .dispatch(FoliagePlacer::type, FoliagePlacerType::codec);
    protected final IntProvider radius;
    protected final IntProvider offset;

    protected static <P extends FoliagePlacer> P2<Mu<P>, IntProvider, IntProvider> foliagePlacerParts(Instance<P> param0) {
        return param0.group(
            IntProvider.codec(0, 16).fieldOf("radius").forGetter(param0x -> param0x.radius),
            IntProvider.codec(0, 16).fieldOf("offset").forGetter(param0x -> param0x.offset)
        );
    }

    public FoliagePlacer(IntProvider param0, IntProvider param1) {
        this.radius = param0;
        this.offset = param1;
    }

    protected abstract FoliagePlacerType<?> type();

    public void createFoliage(
        LevelSimulatedReader param0,
        FoliagePlacer.FoliageSetter param1,
        RandomSource param2,
        TreeConfiguration param3,
        int param4,
        FoliagePlacer.FoliageAttachment param5,
        int param6,
        int param7
    ) {
        this.createFoliage(param0, param1, param2, param3, param4, param5, param6, param7, this.offset(param2));
    }

    protected abstract void createFoliage(
        LevelSimulatedReader var1,
        FoliagePlacer.FoliageSetter var2,
        RandomSource var3,
        TreeConfiguration var4,
        int var5,
        FoliagePlacer.FoliageAttachment var6,
        int var7,
        int var8,
        int var9
    );

    public abstract int foliageHeight(RandomSource var1, int var2, TreeConfiguration var3);

    public int foliageRadius(RandomSource param0, int param1) {
        return this.radius.sample(param0);
    }

    private int offset(RandomSource param0) {
        return this.offset.sample(param0);
    }

    protected abstract boolean shouldSkipLocation(RandomSource var1, int var2, int var3, int var4, int var5, boolean var6);

    protected boolean shouldSkipLocationSigned(RandomSource param0, int param1, int param2, int param3, int param4, boolean param5) {
        int var0;
        int var1;
        if (param5) {
            var0 = Math.min(Math.abs(param1), Math.abs(param1 - 1));
            var1 = Math.min(Math.abs(param3), Math.abs(param3 - 1));
        } else {
            var0 = Math.abs(param1);
            var1 = Math.abs(param3);
        }

        return this.shouldSkipLocation(param0, var0, param2, var1, param4, param5);
    }

    protected void placeLeavesRow(
        LevelSimulatedReader param0,
        FoliagePlacer.FoliageSetter param1,
        RandomSource param2,
        TreeConfiguration param3,
        BlockPos param4,
        int param5,
        int param6,
        boolean param7
    ) {
        int var0 = param7 ? 1 : 0;
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

        for(int var2 = -param5; var2 <= param5 + var0; ++var2) {
            for(int var3 = -param5; var3 <= param5 + var0; ++var3) {
                if (!this.shouldSkipLocationSigned(param2, var2, param6, var3, param5, param7)) {
                    var1.setWithOffset(param4, var2, param6, var3);
                    tryPlaceLeaf(param0, param1, param2, param3, var1);
                }
            }
        }

    }

    protected final void placeLeavesRowWithHangingLeavesBelow(
        LevelSimulatedReader param0,
        FoliagePlacer.FoliageSetter param1,
        RandomSource param2,
        TreeConfiguration param3,
        BlockPos param4,
        int param5,
        int param6,
        boolean param7,
        float param8,
        float param9
    ) {
        this.placeLeavesRow(param0, param1, param2, param3, param4, param5, param6, param7);
        int var0 = param7 ? 1 : 0;
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

        for(Direction var2 : Direction.Plane.HORIZONTAL) {
            Direction var3 = var2.getClockWise();
            int var4 = var3.getAxisDirection() == Direction.AxisDirection.POSITIVE ? param5 + var0 : param5;
            var1.setWithOffset(param4, 0, param6 - 1, 0).move(var3, var4).move(var2, -param5);
            int var5 = -param5;

            while(var5 < param5 + var0) {
                boolean var6 = param1.isSet(var1.move(Direction.UP));
                var1.move(Direction.DOWN);
                if (var6 && !(param2.nextFloat() > param8) && tryPlaceLeaf(param0, param1, param2, param3, var1) && !(param2.nextFloat() > param9)) {
                    tryPlaceLeaf(param0, param1, param2, param3, var1.move(Direction.DOWN));
                    var1.move(Direction.UP);
                }

                ++var5;
                var1.move(var2);
            }
        }

    }

    protected static boolean tryPlaceLeaf(
        LevelSimulatedReader param0, FoliagePlacer.FoliageSetter param1, RandomSource param2, TreeConfiguration param3, BlockPos param4
    ) {
        if (!TreeFeature.validTreePos(param0, param4)) {
            return false;
        } else {
            BlockState var0 = param3.foliageProvider.getState(param2, param4);
            if (var0.hasProperty(BlockStateProperties.WATERLOGGED)) {
                var0 = var0.setValue(
                    BlockStateProperties.WATERLOGGED, Boolean.valueOf(param0.isFluidAtPosition(param4, param0x -> param0x.isSourceOfType(Fluids.WATER)))
                );
            }

            param1.set(param4, var0);
            return true;
        }
    }

    public static final class FoliageAttachment {
        private final BlockPos pos;
        private final int radiusOffset;
        private final boolean doubleTrunk;

        public FoliageAttachment(BlockPos param0, int param1, boolean param2) {
            this.pos = param0;
            this.radiusOffset = param1;
            this.doubleTrunk = param2;
        }

        public BlockPos pos() {
            return this.pos;
        }

        public int radiusOffset() {
            return this.radiusOffset;
        }

        public boolean doubleTrunk() {
            return this.doubleTrunk;
        }
    }

    public interface FoliageSetter {
        void set(BlockPos var1, BlockState var2);

        boolean isSet(BlockPos var1);
    }
}
