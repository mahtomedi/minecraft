package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
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

        for(OreConfiguration.TargetBlockState var3 : var2.targetStates) {
            if (var3.target.test(var0.getBlockState(var1), param0.random())) {
                var0.setBlock(var1, var3.state, 2);
                break;
            }
        }

        return true;
    }
}
