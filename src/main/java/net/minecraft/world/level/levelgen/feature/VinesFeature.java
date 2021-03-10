package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VinesFeature extends Feature<NoneFeatureConfiguration> {
    public VinesFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        BlockPos var1 = param0.origin();
        param0.config();
        if (!var0.isEmptyBlock(var1)) {
            return false;
        } else {
            for(Direction var2 : Direction.values()) {
                if (var2 != Direction.DOWN && VineBlock.isAcceptableNeighbour(var0, var1.relative(var2), var2)) {
                    var0.setBlock(var1, Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(var2), Boolean.valueOf(true)), 2);
                    return true;
                }
            }

            return false;
        }
    }
}
