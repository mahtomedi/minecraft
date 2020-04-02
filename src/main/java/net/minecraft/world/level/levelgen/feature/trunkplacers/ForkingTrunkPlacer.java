package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class ForkingTrunkPlacer extends TrunkPlacer {
    public ForkingTrunkPlacer(int param0, int param1, int param2) {
        super(param0, param1, param2, TrunkPlacerType.FORKING_TRUNK_PLACER);
    }

    public <T> ForkingTrunkPlacer(Dynamic<T> param0) {
        this(param0.get("base_height").asInt(0), param0.get("height_rand_a").asInt(0), param0.get("height_rand_b").asInt(0));
    }

    @Override
    public Map<BlockPos, Integer> placeTrunk(
        LevelSimulatedRW param0,
        Random param1,
        int param2,
        BlockPos param3,
        int param4,
        Set<BlockPos> param5,
        BoundingBox param6,
        SmallTreeConfiguration param7
    ) {
        Map<BlockPos, Integer> var0 = new Object2ObjectLinkedOpenHashMap<>();
        Direction var1 = Direction.Plane.HORIZONTAL.getRandomDirection(param1);
        int var2 = param2 - param1.nextInt(4) - 1;
        int var3 = 3 - param1.nextInt(3);
        BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();
        int var5 = param3.getX();
        int var6 = param3.getZ();
        int var7 = 0;

        for(int var8 = 0; var8 < param2; ++var8) {
            int var9 = param3.getY() + var8;
            if (var8 >= var2 && var3 > 0) {
                var5 += var1.getStepX();
                var6 += var1.getStepZ();
                --var3;
            }

            if (AbstractTreeFeature.placeLog(param0, param1, var4.set(var5, var9, var6), param5, param6, param7)) {
                var7 = var9 + 1;
            }
        }

        var0.put(new BlockPos(var5, var7, var6), param4 + 1);
        var5 = param3.getX();
        var6 = param3.getZ();
        Direction var10 = Direction.Plane.HORIZONTAL.getRandomDirection(param1);
        if (var10 != var1) {
            int var11 = var2 - param1.nextInt(2) - 1;
            int var12 = 1 + param1.nextInt(3);
            var7 = 0;

            for(int var13 = var11; var13 < param2 && var12 > 0; --var12) {
                if (var13 >= 1) {
                    int var14 = param3.getY() + var13;
                    var5 += var10.getStepX();
                    var6 += var10.getStepZ();
                    if (AbstractTreeFeature.placeLog(param0, param1, var4.set(var5, var14, var6), param5, param6, param7)) {
                        var7 = var14 + 1;
                    }
                }

                ++var13;
            }

            if (var7 > 1) {
                var0.put(new BlockPos(var5, var7, var6), param4);
            }
        }

        return var0;
    }
}
