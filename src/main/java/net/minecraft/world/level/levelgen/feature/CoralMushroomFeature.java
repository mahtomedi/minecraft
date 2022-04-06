package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CoralMushroomFeature extends CoralFeature {
    public CoralMushroomFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    protected boolean placeFeature(LevelAccessor param0, RandomSource param1, BlockPos param2, BlockState param3) {
        int var0 = param1.nextInt(3) + 3;
        int var1 = param1.nextInt(3) + 3;
        int var2 = param1.nextInt(3) + 3;
        int var3 = param1.nextInt(3) + 1;
        BlockPos.MutableBlockPos var4 = param2.mutable();

        for(int var5 = 0; var5 <= var1; ++var5) {
            for(int var6 = 0; var6 <= var0; ++var6) {
                for(int var7 = 0; var7 <= var2; ++var7) {
                    var4.set(var5 + param2.getX(), var6 + param2.getY(), var7 + param2.getZ());
                    var4.move(Direction.DOWN, var3);
                    if ((var5 != 0 && var5 != var1 || var6 != 0 && var6 != var0)
                        && (var7 != 0 && var7 != var2 || var6 != 0 && var6 != var0)
                        && (var5 != 0 && var5 != var1 || var7 != 0 && var7 != var2)
                        && (var5 == 0 || var5 == var1 || var6 == 0 || var6 == var0 || var7 == 0 || var7 == var2)
                        && !(param1.nextFloat() < 0.1F)
                        && !this.placeCoralBlock(param0, param1, var4, param3)) {
                    }
                }
            }
        }

        return true;
    }
}
