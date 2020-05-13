package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.BlockBlobConfiguration;

public class BlockBlobFeature extends Feature<BlockBlobConfiguration> {
    public BlockBlobFeature(Function<Dynamic<?>, ? extends BlockBlobConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, BlockBlobConfiguration param5
    ) {
        for(; param4.getY() > 3; param4 = param4.below()) {
            if (!param0.isEmptyBlock(param4.below())) {
                Block var0 = param0.getBlockState(param4.below()).getBlock();
                if (isDirt(var0) || isStone(var0)) {
                    break;
                }
            }
        }

        if (param4.getY() <= 3) {
            return false;
        } else {
            int var1 = param5.startRadius;

            for(int var2 = 0; var1 >= 0 && var2 < 3; ++var2) {
                int var3 = var1 + param3.nextInt(2);
                int var4 = var1 + param3.nextInt(2);
                int var5 = var1 + param3.nextInt(2);
                float var6 = (float)(var3 + var4 + var5) * 0.333F + 0.5F;

                for(BlockPos var7 : BlockPos.betweenClosed(param4.offset(-var3, -var4, -var5), param4.offset(var3, var4, var5))) {
                    if (var7.distSqr(param4) <= (double)(var6 * var6)) {
                        param0.setBlock(var7, param5.state, 4);
                    }
                }

                param4 = param4.offset(-(var1 + 1) + param3.nextInt(2 + var1 * 2), 0 - param3.nextInt(2), -(var1 + 1) + param3.nextInt(2 + var1 * 2));
            }

            return true;
        }
    }
}
