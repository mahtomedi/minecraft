package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class BaseDiskFeature extends Feature<DiskConfiguration> {
    public BaseDiskFeature(Codec<DiskConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<DiskConfiguration> param0) {
        DiskConfiguration var0 = param0.config();
        BlockPos var1 = param0.origin();
        WorldGenLevel var2 = param0.level();
        boolean var3 = false;
        int var4 = var1.getY();
        int var5 = var4 + var0.halfHeight();
        int var6 = var4 - var0.halfHeight() - 1;
        boolean var7 = var0.state().getBlock() instanceof FallingBlock;
        int var8 = var0.radius().sample(param0.random());

        for(int var9 = var1.getX() - var8; var9 <= var1.getX() + var8; ++var9) {
            for(int var10 = var1.getZ() - var8; var10 <= var1.getZ() + var8; ++var10) {
                int var11 = var9 - var1.getX();
                int var12 = var10 - var1.getZ();
                if (var11 * var11 + var12 * var12 <= var8 * var8) {
                    boolean var13 = false;

                    for(int var14 = var5; var14 >= var6; --var14) {
                        BlockPos var15 = new BlockPos(var9, var14, var10);
                        BlockState var16 = var2.getBlockState(var15);
                        Block var17 = var16.getBlock();
                        boolean var18 = false;
                        if (var14 > var6) {
                            for(BlockState var19 : var0.targets()) {
                                if (var19.is(var17)) {
                                    var2.setBlock(var15, var0.state(), 2);
                                    this.markAboveForPostProcessing(var2, var15);
                                    var3 = true;
                                    var18 = true;
                                    break;
                                }
                            }
                        }

                        if (var7 && var13 && var16.isAir()) {
                            BlockState var20 = var0.state().is(Blocks.RED_SAND)
                                ? Blocks.RED_SANDSTONE.defaultBlockState()
                                : Blocks.SANDSTONE.defaultBlockState();
                            var2.setBlock(new BlockPos(var9, var14 + 1, var10), var20, 2);
                        }

                        var13 = var18;
                    }
                }
            }
        }

        return var3;
    }
}
