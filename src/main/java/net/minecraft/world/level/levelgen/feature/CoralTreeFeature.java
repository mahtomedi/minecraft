package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CoralTreeFeature extends CoralFeature {
    public CoralTreeFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    protected boolean placeFeature(LevelAccessor param0, RandomSource param1, BlockPos param2, BlockState param3) {
        BlockPos.MutableBlockPos var0 = param2.mutable();
        int var1 = param1.nextInt(3) + 1;

        for(int var2 = 0; var2 < var1; ++var2) {
            if (!this.placeCoralBlock(param0, param1, var0, param3)) {
                return true;
            }

            var0.move(Direction.UP);
        }

        BlockPos var3 = var0.immutable();
        int var4 = param1.nextInt(3) + 2;
        List<Direction> var5 = Direction.Plane.HORIZONTAL.shuffledCopy(param1);

        for(Direction var7 : var5.subList(0, var4)) {
            var0.set(var3);
            var0.move(var7);
            int var8 = param1.nextInt(5) + 2;
            int var9 = 0;

            for(int var10 = 0; var10 < var8 && this.placeCoralBlock(param0, param1, var0, param3); ++var10) {
                ++var9;
                var0.move(Direction.UP);
                if (var10 == 0 || var9 >= 2 && param1.nextFloat() < 0.25F) {
                    var0.move(var7);
                    var9 = 0;
                }
            }
        }

        return true;
    }
}
