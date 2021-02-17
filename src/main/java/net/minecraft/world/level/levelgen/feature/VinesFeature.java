package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VinesFeature extends Feature<NoneFeatureConfiguration> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public VinesFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        Random var0 = param0.random();
        WorldGenLevel var1 = param0.level();
        BlockPos var2 = param0.origin();
        BlockPos.MutableBlockPos var3 = var2.mutable();
        BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();

        for(int var5 = 64; var5 < 384; ++var5) {
            var3.set(var2);
            var3.move(var0.nextInt(4) - var0.nextInt(4), 0, var0.nextInt(4) - var0.nextInt(4));
            var3.setY(var5);
            if (var1.isEmptyBlock(var3)) {
                for(Direction var6 : DIRECTIONS) {
                    if (var6 != Direction.DOWN) {
                        var4.setWithOffset(var3, var6);
                        if (VineBlock.isAcceptableNeighbour(var1, var4, var6)) {
                            var1.setBlock(var3, Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(var6), Boolean.valueOf(true)), 2);
                            break;
                        }
                    }
                }
            }
        }

        return true;
    }
}
