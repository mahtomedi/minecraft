package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class DiskReplaceFeature extends Feature<DiskConfiguration> {
    public DiskReplaceFeature(Codec<DiskConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, DiskConfiguration param5) {
        if (!param0.getFluidState(param4).is(FluidTags.WATER)) {
            return false;
        } else {
            int var0 = 0;
            int var1 = param3.nextInt(param5.radius - 2) + 2;

            for(int var2 = param4.getX() - var1; var2 <= param4.getX() + var1; ++var2) {
                for(int var3 = param4.getZ() - var1; var3 <= param4.getZ() + var1; ++var3) {
                    int var4 = var2 - param4.getX();
                    int var5 = var3 - param4.getZ();
                    if (var4 * var4 + var5 * var5 <= var1 * var1) {
                        for(int var6 = param4.getY() - param5.ySize; var6 <= param4.getY() + param5.ySize; ++var6) {
                            BlockPos var7 = new BlockPos(var2, var6, var3);
                            BlockState var8 = param0.getBlockState(var7);

                            for(BlockState var9 : param5.targets) {
                                if (var9.is(var8.getBlock())) {
                                    param0.setBlock(var7, param5.state, 2);
                                    ++var0;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            return var0 > 0;
        }
    }
}
