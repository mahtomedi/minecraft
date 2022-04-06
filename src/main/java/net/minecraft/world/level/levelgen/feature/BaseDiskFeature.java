package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
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
        int var7 = var0.radius().sample(param0.random());
        BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos();

        for(BlockPos var9 : BlockPos.betweenClosed(var1.offset(-var7, 0, -var7), var1.offset(var7, 0, var7))) {
            int var10 = var9.getX() - var1.getX();
            int var11 = var9.getZ() - var1.getZ();
            if (var10 * var10 + var11 * var11 <= var7 * var7) {
                var3 |= this.placeColumn(var0, var2, var5, var6, var8.set(var9));
            }
        }

        return var3;
    }

    protected boolean placeColumn(DiskConfiguration param0, WorldGenLevel param1, int param2, int param3, BlockPos.MutableBlockPos param4) {
        boolean var0 = false;
        boolean var1 = false;
        boolean var2 = param0.state().getBlock() instanceof FallingBlock;

        for(int var3 = param2; var3 >= param3; --var3) {
            param4.setY(var3);
            BlockState var4 = param1.getBlockState(param4);
            boolean var5 = false;
            if (var3 > param3 && this.matchesTargetBlock(param0, var4)) {
                param1.setBlock(param4, param0.state(), 2);
                this.markAboveForPostProcessing(param1, param4);
                var1 = true;
                var5 = true;
            }

            if (var2 && var0 && var4.isAir()) {
                param1.setBlock(param4.move(Direction.UP), this.getSupportState(param0), 2);
            }

            var0 = var5;
        }

        return var1;
    }

    protected BlockState getSupportState(DiskConfiguration param0) {
        return param0.state().is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
    }

    protected boolean matchesTargetBlock(DiskConfiguration param0, BlockState param1) {
        for(BlockState var0 : param0.targets()) {
            if (var0.is(param1.getBlock())) {
                return true;
            }
        }

        return false;
    }
}
