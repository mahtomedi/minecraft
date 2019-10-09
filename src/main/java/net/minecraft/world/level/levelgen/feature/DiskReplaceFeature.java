package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class DiskReplaceFeature extends Feature<DiskConfiguration> {
    public DiskReplaceFeature(Function<Dynamic<?>, ? extends DiskConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, DiskConfiguration param4
    ) {
        if (!param0.getFluidState(param3).is(FluidTags.WATER)) {
            return false;
        } else {
            int var0 = 0;
            int var1 = param2.nextInt(param4.radius - 2) + 2;

            for(int var2 = param3.getX() - var1; var2 <= param3.getX() + var1; ++var2) {
                for(int var3 = param3.getZ() - var1; var3 <= param3.getZ() + var1; ++var3) {
                    int var4 = var2 - param3.getX();
                    int var5 = var3 - param3.getZ();
                    if (var4 * var4 + var5 * var5 <= var1 * var1) {
                        for(int var6 = param3.getY() - param4.ySize; var6 <= param3.getY() + param4.ySize; ++var6) {
                            BlockPos var7 = new BlockPos(var2, var6, var3);
                            BlockState var8 = param0.getBlockState(var7);

                            for(BlockState var9 : param4.targets) {
                                if (var9.getBlock() == var8.getBlock()) {
                                    param0.setBlock(var7, param4.state, 2);
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
