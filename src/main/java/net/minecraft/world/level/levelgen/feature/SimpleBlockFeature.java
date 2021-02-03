package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

public class SimpleBlockFeature extends Feature<SimpleBlockConfiguration> {
    public SimpleBlockFeature(Codec<SimpleBlockConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<SimpleBlockConfiguration> param0) {
        SimpleBlockConfiguration var0 = param0.config();
        WorldGenLevel var1 = param0.level();
        BlockPos var2 = param0.origin();
        if (var0.placeOn.contains(var1.getBlockState(var2.below()))
            && var0.placeIn.contains(var1.getBlockState(var2))
            && var0.placeUnder.contains(var1.getBlockState(var2.above()))) {
            var1.setBlock(var2, var0.toPlace, 2);
            return true;
        } else {
            return false;
        }
    }
}
