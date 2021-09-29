package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
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
        BlockState var3 = var0.toPlace().getState(param0.random(), var2);
        if (var3.canSurvive(var1, var2)) {
            if (var3.getBlock() instanceof DoublePlantBlock) {
                if (!var1.isEmptyBlock(var2.above())) {
                    return false;
                }

                DoublePlantBlock.placeAt(var1, var3, var2, 2);
            } else {
                var1.setBlock(var2, var3, 2);
            }

            return true;
        } else {
            return false;
        }
    }
}
