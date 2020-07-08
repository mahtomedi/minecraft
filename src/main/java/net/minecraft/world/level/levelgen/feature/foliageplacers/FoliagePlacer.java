package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products.P2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public abstract class FoliagePlacer {
    public static final Codec<FoliagePlacer> CODEC = Registry.FOLIAGE_PLACER_TYPES.dispatch(FoliagePlacer::type, FoliagePlacerType::codec);
    protected final UniformInt radius;
    protected final UniformInt offset;

    protected static <P extends FoliagePlacer> P2<Mu<P>, UniformInt, UniformInt> foliagePlacerParts(Instance<P> param0) {
        return param0.group(
            UniformInt.codec(0, 8, 8).fieldOf("radius").forGetter(param0x -> param0x.radius),
            UniformInt.codec(0, 8, 8).fieldOf("offset").forGetter(param0x -> param0x.offset)
        );
    }

    public FoliagePlacer(UniformInt param0, UniformInt param1) {
        this.radius = param0;
        this.offset = param1;
    }

    protected abstract FoliagePlacerType<?> type();

    public void createFoliage(
        LevelSimulatedRW param0,
        Random param1,
        TreeConfiguration param2,
        int param3,
        FoliagePlacer.FoliageAttachment param4,
        int param5,
        int param6,
        Set<BlockPos> param7,
        BoundingBox param8
    ) {
        this.createFoliage(param0, param1, param2, param3, param4, param5, param6, param7, this.offset(param1), param8);
    }

    protected abstract void createFoliage(
        LevelSimulatedRW var1,
        Random var2,
        TreeConfiguration var3,
        int var4,
        FoliagePlacer.FoliageAttachment var5,
        int var6,
        int var7,
        Set<BlockPos> var8,
        int var9,
        BoundingBox var10
    );

    public abstract int foliageHeight(Random var1, int var2, TreeConfiguration var3);

    public int foliageRadius(Random param0, int param1) {
        return this.radius.sample(param0);
    }

    private int offset(Random param0) {
        return this.offset.sample(param0);
    }

    protected abstract boolean shouldSkipLocation(Random var1, int var2, int var3, int var4, int var5, boolean var6);

    protected boolean shouldSkipLocationSigned(Random param0, int param1, int param2, int param3, int param4, boolean param5) {
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
        LevelSimulatedRW param0,
        Random param1,
        TreeConfiguration param2,
        BlockPos param3,
        int param4,
        Set<BlockPos> param5,
        int param6,
        boolean param7,
        BoundingBox param8
    ) {
        int var0 = param7 ? 1 : 0;
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

        for(int var2 = -param4; var2 <= param4 + var0; ++var2) {
            for(int var3 = -param4; var3 <= param4 + var0; ++var3) {
                if (!this.shouldSkipLocationSigned(param1, var2, param6, var3, param4, param7)) {
                    var1.setWithOffset(param3, var2, param6, var3);
                    if (TreeFeature.validTreePos(param0, var1)) {
                        param0.setBlock(var1, param2.leavesProvider.getState(param1, var1), 19);
                        param8.expand(new BoundingBox(var1, var1));
                        param5.add(var1.immutable());
                    }
                }
            }
        }

    }

    public static final class FoliageAttachment {
        private final BlockPos foliagePos;
        private final int radiusOffset;
        private final boolean doubleTrunk;

        public FoliageAttachment(BlockPos param0, int param1, boolean param2) {
            this.foliagePos = param0;
            this.radiusOffset = param1;
            this.doubleTrunk = param2;
        }

        public BlockPos foliagePos() {
            return this.foliagePos;
        }

        public int radiusOffset() {
            return this.radiusOffset;
        }

        public boolean doubleTrunk() {
            return this.doubleTrunk;
        }
    }
}
