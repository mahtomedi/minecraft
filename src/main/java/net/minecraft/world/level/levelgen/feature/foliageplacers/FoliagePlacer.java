package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Serializable;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public abstract class FoliagePlacer implements Serializable {
    private final int radius;
    private final int radiusRandom;
    private final int offset;
    private final int offsetRandom;
    private final FoliagePlacerType<?> type;

    public FoliagePlacer(int param0, int param1, int param2, int param3, FoliagePlacerType<?> param4) {
        this.radius = param0;
        this.radiusRandom = param1;
        this.offset = param2;
        this.offsetRandom = param3;
        this.type = param4;
    }

    public void createFoliage(
        LevelSimulatedRW param0,
        Random param1,
        TreeConfiguration param2,
        int param3,
        FoliagePlacer.FoliageAttachment param4,
        int param5,
        int param6,
        Set<BlockPos> param7
    ) {
        this.createFoliage(param0, param1, param2, param3, param4, param5, param6, param7, this.offset(param1));
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
        int var9
    );

    public abstract int foliageHeight(Random var1, int var2, TreeConfiguration var3);

    public int foliageRadius(Random param0, int param1) {
        return this.radius + param0.nextInt(this.radiusRandom + 1);
    }

    private int offset(Random param0) {
        return this.offset + param0.nextInt(this.offsetRandom + 1);
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
        LevelSimulatedRW param0, Random param1, TreeConfiguration param2, BlockPos param3, int param4, Set<BlockPos> param5, int param6, boolean param7
    ) {
        int var0 = param7 ? 1 : 0;
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

        for(int var2 = -param4; var2 <= param4 + var0; ++var2) {
            for(int var3 = -param4; var3 <= param4 + var0; ++var3) {
                if (!this.shouldSkipLocationSigned(param1, var2, param6, var3, param4, param7)) {
                    var1.setWithOffset(param3, var2, param6, var3);
                    if (TreeFeature.validTreePos(param0, var1)) {
                        param0.setBlock(var1, param2.leavesProvider.getState(param1, var1), 19);
                        param5.add(var1.immutable());
                    }
                }
            }
        }

    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("type"), param0.createString(Registry.FOLIAGE_PLACER_TYPES.getKey(this.type).toString()))
            .put(param0.createString("radius"), param0.createInt(this.radius))
            .put(param0.createString("radius_random"), param0.createInt(this.radiusRandom))
            .put(param0.createString("offset"), param0.createInt(this.offset))
            .put(param0.createString("offset_random"), param0.createInt(this.offsetRandom));
        return new Dynamic<>(param0, param0.createMap(var0.build())).getValue();
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
