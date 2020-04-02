package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class SpruceFoliagePlacer extends FoliagePlacer {
    private final int trunkHeight;
    private final int trunkHeightRandom;

    public SpruceFoliagePlacer(int param0, int param1, int param2, int param3, int param4, int param5) {
        super(param0, param1, param2, param3, FoliagePlacerType.SPRUCE_FOLIAGE_PLACER);
        this.trunkHeight = param4;
        this.trunkHeightRandom = param5;
    }

    public <T> SpruceFoliagePlacer(Dynamic<T> param0) {
        this(
            param0.get("radius").asInt(0),
            param0.get("radius_random").asInt(0),
            param0.get("offset").asInt(0),
            param0.get("offset_random").asInt(0),
            param0.get("trunk_height").asInt(0),
            param0.get("trunk_height_random").asInt(0)
        );
    }

    @Override
    public void createFoliage(
        LevelSimulatedRW param0, Random param1, SmallTreeConfiguration param2, int param3, BlockPos param4, int param5, int param6, Set<BlockPos> param7
    ) {
        int var0 = this.offset + param1.nextInt(this.offsetRandom + 1);
        int var1 = param1.nextInt(2);
        int var2 = 1;
        int var3 = 0;

        for(int var4 = param5 + var0; var4 >= 0; --var4) {
            this.placeLeavesRow(param0, param1, param2, param4, param5, var4, var1, param7);
            if (var1 >= var2) {
                var1 = var3;
                var3 = 1;
                var2 = Math.min(var2 + 1, param6);
            } else {
                ++var1;
            }
        }

    }

    @Override
    public int foliageRadius(Random param0, int param1, SmallTreeConfiguration param2) {
        return this.radius + param0.nextInt(this.radiusRandom + 1);
    }

    @Override
    public int foliageHeight(Random param0, int param1) {
        return param1 - this.trunkHeight - param0.nextInt(this.trunkHeightRandom + 1);
    }

    @Override
    protected boolean shouldSkipLocation(Random param0, int param1, int param2, int param3, int param4, int param5) {
        return Math.abs(param2) == param5 && Math.abs(param4) == param5 && param5 > 0;
    }

    @Override
    public int getTreeRadiusForHeight(int param0, int param1, int param2) {
        return param2 <= 1 ? 0 : 2;
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("trunk_height"), param0.createInt(this.trunkHeight))
            .put(param0.createString("trunk_height_random"), param0.createInt(this.trunkHeightRandom));
        return param0.merge(super.serialize(param0), param0.createMap(var0.build()));
    }
}
