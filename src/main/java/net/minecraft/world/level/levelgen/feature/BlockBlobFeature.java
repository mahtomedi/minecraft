package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class BlockBlobFeature extends Feature<BlockStateConfiguration> {
    public BlockBlobFeature(Codec<BlockStateConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, BlockStateConfiguration param4) {
        for(; param3.getY() > param0.getMinBuildHeight() + 3; param3 = param3.below()) {
            if (!param0.isEmptyBlock(param3.below())) {
                BlockState var0 = param0.getBlockState(param3.below());
                if (isDirt(var0) || isStone(var0)) {
                    break;
                }
            }
        }

        if (param3.getY() <= param0.getMinBuildHeight() + 3) {
            return false;
        } else {
            for(int var1 = 0; var1 < 3; ++var1) {
                int var2 = param2.nextInt(2);
                int var3 = param2.nextInt(2);
                int var4 = param2.nextInt(2);
                float var5 = (float)(var2 + var3 + var4) * 0.333F + 0.5F;

                for(BlockPos var6 : BlockPos.betweenClosed(param3.offset(-var2, -var3, -var4), param3.offset(var2, var3, var4))) {
                    if (var6.distSqr(param3) <= (double)(var5 * var5)) {
                        param0.setBlock(var6, param4.state, 4);
                    }
                }

                param3 = param3.offset(-1 + param2.nextInt(2), -param2.nextInt(2), -1 + param2.nextInt(2));
            }

            return true;
        }
    }
}
