package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;

public class ReplaceBlockFeature extends Feature<ReplaceBlockConfiguration> {
    public ReplaceBlockFeature(Codec<ReplaceBlockConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<ReplaceBlockConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        BlockPos var1 = param0.origin();
        ReplaceBlockConfiguration var2 = param0.config();
        if (var0.getBlockState(var1).is(var2.target.getBlock())) {
            var0.setBlock(var1, var2.state, 2);
        }

        return true;
    }
}
