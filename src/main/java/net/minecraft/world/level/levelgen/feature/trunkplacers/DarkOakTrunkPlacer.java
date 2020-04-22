package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class DarkOakTrunkPlacer extends TrunkPlacer {
    public DarkOakTrunkPlacer(int param0, int param1, int param2) {
        this(param0, param1, param2, TrunkPlacerType.DARK_OAK_TRUNK_PLACER);
    }

    public DarkOakTrunkPlacer(int param0, int param1, int param2, TrunkPlacerType<? extends DarkOakTrunkPlacer> param3) {
        super(param0, param1, param2, param3);
    }

    public <T> DarkOakTrunkPlacer(Dynamic<T> param0) {
        this(param0.get("base_height").asInt(0), param0.get("height_rand_a").asInt(0), param0.get("height_rand_b").asInt(0));
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
        LevelSimulatedRW param0, Random param1, int param2, BlockPos param3, Set<BlockPos> param4, BoundingBox param5, TreeConfiguration param6
    ) {
        List<FoliagePlacer.FoliageAttachment> var0 = Lists.newArrayList();
        BlockPos var1 = param3.below();
        setDirtAt(param0, var1);
        setDirtAt(param0, var1.east());
        setDirtAt(param0, var1.south());
        setDirtAt(param0, var1.south().east());
        Direction var2 = Direction.Plane.HORIZONTAL.getRandomDirection(param1);
        int var3 = param2 - param1.nextInt(4);
        int var4 = 2 - param1.nextInt(3);
        int var5 = param3.getX();
        int var6 = param3.getY();
        int var7 = param3.getZ();
        int var8 = var5;
        int var9 = var7;
        int var10 = var6 + param2 - 1;

        for(int var11 = 0; var11 < param2; ++var11) {
            if (var11 >= var3 && var4 > 0) {
                var8 += var2.getStepX();
                var9 += var2.getStepZ();
                --var4;
            }

            int var12 = var6 + var11;
            BlockPos var13 = new BlockPos(var8, var12, var9);
            if (TreeFeature.isAirOrLeaves(param0, var13)) {
                placeLog(param0, param1, var13, param4, param5, param6);
                placeLog(param0, param1, var13.east(), param4, param5, param6);
                placeLog(param0, param1, var13.south(), param4, param5, param6);
                placeLog(param0, param1, var13.east().south(), param4, param5, param6);
            }
        }

        var0.add(new FoliagePlacer.FoliageAttachment(new BlockPos(var8, var10, var9), 0, true));

        for(int var14 = -1; var14 <= 2; ++var14) {
            for(int var15 = -1; var15 <= 2; ++var15) {
                if ((var14 < 0 || var14 > 1 || var15 < 0 || var15 > 1) && param1.nextInt(3) <= 0) {
                    int var16 = param1.nextInt(3) + 2;

                    for(int var17 = 0; var17 < var16; ++var17) {
                        placeLog(param0, param1, new BlockPos(var5 + var14, var10 - var17 - 1, var7 + var15), param4, param5, param6);
                    }

                    var0.add(new FoliagePlacer.FoliageAttachment(new BlockPos(var8 + var14, var10, var9 + var15), 0, false));
                }
            }
        }

        return var0;
    }
}
