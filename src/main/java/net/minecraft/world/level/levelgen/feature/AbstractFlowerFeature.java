package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public abstract class AbstractFlowerFeature<U extends FeatureConfiguration> extends Feature<U> {
    public AbstractFlowerFeature(Codec<U> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<U> param0) {
        Random var0 = param0.random();
        BlockPos var1 = param0.origin();
        WorldGenLevel var2 = param0.level();
        U var3 = param0.config();
        BlockState var4 = this.getRandomFlower(var0, var1, var3);
        int var5 = 0;

        for(int var6 = 0; var6 < this.getCount(var3); ++var6) {
            BlockPos var7 = this.getPos(var0, var1, var3);
            if (var2.isEmptyBlock(var7) && var4.canSurvive(var2, var7) && this.isValid(var2, var7, var3)) {
                var2.setBlock(var7, var4, 2);
                ++var5;
            }
        }

        return var5 > 0;
    }

    public abstract boolean isValid(LevelAccessor var1, BlockPos var2, U var3);

    public abstract int getCount(U var1);

    public abstract BlockPos getPos(Random var1, BlockPos var2, U var3);

    public abstract BlockState getRandomFlower(Random var1, BlockPos var2, U var3);
}
