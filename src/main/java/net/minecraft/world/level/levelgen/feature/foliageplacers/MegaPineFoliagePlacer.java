package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class MegaPineFoliagePlacer extends FoliagePlacer {
    private final int heightRand;
    private final int crownHeight;

    public MegaPineFoliagePlacer(int param0, int param1, int param2, int param3, int param4, int param5) {
        super(param0, param1, param2, param3, FoliagePlacerType.MEGA_PINE_FOLIAGE_PLACER);
        this.heightRand = param4;
        this.crownHeight = param5;
    }

    public <T> MegaPineFoliagePlacer(Dynamic<T> param0) {
        this(
            param0.get("radius").asInt(0),
            param0.get("radius_random").asInt(0),
            param0.get("offset").asInt(0),
            param0.get("offset_random").asInt(0),
            param0.get("height_rand").asInt(0),
            param0.get("crown_height").asInt(0)
        );
    }

    @Override
    protected void createFoliage(
        LevelSimulatedRW param0,
        Random param1,
        TreeConfiguration param2,
        int param3,
        FoliagePlacer.FoliageAttachment param4,
        int param5,
        int param6,
        Set<BlockPos> param7,
        int param8
    ) {
        BlockPos var0 = param4.foliagePos();
        int var1 = 0;

        for(int var2 = var0.getY() - param5 + param8; var2 <= var0.getY() + param8; ++var2) {
            int var3 = var0.getY() - var2;
            int var4 = param6 + param4.radiusOffset() + Mth.floor((float)var3 / (float)param5 * 3.5F);
            int var5;
            if (var3 > 0 && var4 == var1 && (var2 & 1) == 0) {
                var5 = var4 + 1;
            } else {
                var5 = var4;
            }

            this.placeLeavesRow(param0, param1, param2, new BlockPos(var0.getX(), var2, var0.getZ()), var5, param7, 0, param4.doubleTrunk());
            var1 = var4;
        }

    }

    @Override
    public int foliageHeight(Random param0, int param1, TreeConfiguration param2) {
        return param0.nextInt(this.heightRand + 1) + this.crownHeight;
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, boolean param5) {
        if (param1 + param3 >= 7) {
            return true;
        } else {
            return param1 * param1 + param3 * param3 > param4 * param4;
        }
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("height_rand"), param0.createInt(this.heightRand));
        var0.put(param0.createString("crown_height"), param0.createInt(this.crownHeight));
        return param0.merge(super.serialize(param0), param0.createMap(var0.build()));
    }
}
