package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CoralClawFeature extends CoralFeature {
    public CoralClawFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    protected boolean placeFeature(LevelAccessor param0, Random param1, BlockPos param2, BlockState param3) {
        if (!this.placeCoralBlock(param0, param1, param2, param3)) {
            return false;
        } else {
            Direction var0 = Direction.Plane.HORIZONTAL.getRandomDirection(param1);
            int var1 = param1.nextInt(2) + 2;
            List<Direction> var2 = Lists.newArrayList(var0, var0.getClockWise(), var0.getCounterClockWise());
            Collections.shuffle(var2, param1);

            for(Direction var4 : var2.subList(0, var1)) {
                BlockPos.MutableBlockPos var5 = param2.mutable();
                int var6 = param1.nextInt(2) + 1;
                var5.move(var4);
                int var8;
                Direction var7;
                if (var4 == var0) {
                    var7 = var0;
                    var8 = param1.nextInt(3) + 2;
                } else {
                    var5.move(Direction.UP);
                    Direction[] var9 = new Direction[]{var4, Direction.UP};
                    var7 = var9[param1.nextInt(var9.length)];
                    var8 = param1.nextInt(3) + 3;
                }

                for(int var12 = 0; var12 < var6 && this.placeCoralBlock(param0, param1, var5, param3); ++var12) {
                    var5.move(var7);
                }

                var5.move(var7.getOpposite());
                var5.move(Direction.UP);

                for(int var13 = 0; var13 < var8; ++var13) {
                    var5.move(var0);
                    if (!this.placeCoralBlock(param0, param1, var5, param3)) {
                        break;
                    }

                    if (param1.nextFloat() < 0.25F) {
                        var5.move(Direction.UP);
                    }
                }
            }

            return true;
        }
    }
}
