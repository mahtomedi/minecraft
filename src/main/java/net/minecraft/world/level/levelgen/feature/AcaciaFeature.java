package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class AcaciaFeature extends AbstractSmallTreeFeature<SmallTreeConfiguration> {
    public AcaciaFeature(Function<Dynamic<?>, ? extends SmallTreeConfiguration> param0) {
        super(param0);
    }

    public boolean doPlace(
        LevelSimulatedRW param0, Random param1, BlockPos param2, Set<BlockPos> param3, Set<BlockPos> param4, BoundingBox param5, SmallTreeConfiguration param6
    ) {
        int var0 = param6.baseHeight + param1.nextInt(param6.heightRandA + 1) + param1.nextInt(param6.heightRandB + 1);
        int var1 = param6.trunkHeight >= 0
            ? param6.trunkHeight + param1.nextInt(param6.trunkHeightRandom + 1)
            : var0 - (param6.foliageHeight + param1.nextInt(param6.foliageHeightRandom + 1));
        int var2 = param6.foliagePlacer.foliageRadius(param1, var1, var0, param6);
        Optional<BlockPos> var3 = this.getProjectedOrigin(param0, var0, var1, var2, param2, param6);
        if (!var3.isPresent()) {
            return false;
        } else {
            BlockPos var4 = var3.get();
            this.setDirtAt(param0, var4.below());
            Direction var5 = Direction.Plane.HORIZONTAL.getRandomDirection(param1);
            int var6 = var0 - param1.nextInt(4) - 1;
            int var7 = 3 - param1.nextInt(3);
            BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos();
            int var9 = var4.getX();
            int var10 = var4.getZ();
            int var11 = 0;

            for(int var12 = 0; var12 < var0; ++var12) {
                int var13 = var4.getY() + var12;
                if (var12 >= var6 && var7 > 0) {
                    var9 += var5.getStepX();
                    var10 += var5.getStepZ();
                    --var7;
                }

                if (this.placeLog(param0, param1, var8.set(var9, var13, var10), param3, param5, param6)) {
                    var11 = var13;
                }
            }

            BlockPos var14 = new BlockPos(var9, var11, var10);
            param6.foliagePlacer.createFoliage(param0, param1, param6, var0, var1, var2 + 1, var14, param4);
            var9 = var4.getX();
            var10 = var4.getZ();
            Direction var15 = Direction.Plane.HORIZONTAL.getRandomDirection(param1);
            if (var15 != var5) {
                int var16 = var6 - param1.nextInt(2) - 1;
                int var17 = 1 + param1.nextInt(3);
                var11 = 0;

                for(int var18 = var16; var18 < var0 && var17 > 0; --var17) {
                    if (var18 >= 1) {
                        int var19 = var4.getY() + var18;
                        var9 += var15.getStepX();
                        var10 += var15.getStepZ();
                        if (this.placeLog(param0, param1, var8.set(var9, var19, var10), param3, param5, param6)) {
                            var11 = var19;
                        }
                    }

                    ++var18;
                }

                if (var11 > 0) {
                    BlockPos var20 = new BlockPos(var9, var11, var10);
                    param6.foliagePlacer.createFoliage(param0, param1, param6, var0, var1, var2, var20, param4);
                }
            }

            return true;
        }
    }
}
