package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class BaseDiskFeature extends Feature<DiskConfiguration> {
    public BaseDiskFeature(Codec<DiskConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, DiskConfiguration param4) {
        boolean var0 = false;
        int var1 = param4.radius.sample(param2);

        for(int var2 = param3.getX() - var1; var2 <= param3.getX() + var1; ++var2) {
            for(int var3 = param3.getZ() - var1; var3 <= param3.getZ() + var1; ++var3) {
                int var4 = var2 - param3.getX();
                int var5 = var3 - param3.getZ();
                if (var4 * var4 + var5 * var5 <= var1 * var1) {
                    for(int var6 = param3.getY() - param4.halfHeight; var6 <= param3.getY() + param4.halfHeight; ++var6) {
                        BlockPos var7 = new BlockPos(var2, var6, var3);
                        Block var8 = param0.getBlockState(var7).getBlock();

                        for(BlockState var9 : param4.targets) {
                            if (var9.is(var8)) {
                                param0.setBlock(var7, param4.state, 2);
                                var0 = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        return var0;
    }
}
