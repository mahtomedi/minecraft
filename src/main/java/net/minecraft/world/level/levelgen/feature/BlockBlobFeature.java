package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.BlockBlobConfiguration;

public class BlockBlobFeature extends Feature<BlockBlobConfiguration> {
    public BlockBlobFeature(Codec<BlockBlobConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, BlockBlobConfiguration param4) {
        for(; param3.getY() > 3; param3 = param3.below()) {
            if (!param0.isEmptyBlock(param3.below())) {
                Block var0 = param0.getBlockState(param3.below()).getBlock();
                if (isDirt(var0) || isStone(var0)) {
                    break;
                }
            }
        }

        if (param3.getY() <= 3) {
            return false;
        } else {
            int var1 = param4.startRadius;

            for(int var2 = 0; var1 >= 0 && var2 < 3; ++var2) {
                int var3 = var1 + param2.nextInt(2);
                int var4 = var1 + param2.nextInt(2);
                int var5 = var1 + param2.nextInt(2);
                float var6 = (float)(var3 + var4 + var5) * 0.333F + 0.5F;

                for(BlockPos var7 : BlockPos.betweenClosed(param3.offset(-var3, -var4, -var5), param3.offset(var3, var4, var5))) {
                    if (var7.distSqr(param3) <= (double)(var6 * var6)) {
                        param0.setBlock(var7, param4.state, 4);
                    }
                }

                param3 = param3.offset(-(var1 + 1) + param2.nextInt(2 + var1 * 2), 0 - param2.nextInt(2), -(var1 + 1) + param2.nextInt(2 + var1 * 2));
            }

            return true;
        }
    }
}
