package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

public class DiskFeature extends Feature<DiskConfiguration> {
    public DiskFeature(Codec<DiskConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<DiskConfiguration> param0) {
        DiskConfiguration var0 = param0.config();
        BlockPos var1 = param0.origin();
        WorldGenLevel var2 = param0.level();
        RandomSource var3 = param0.random();
        boolean var4 = false;
        int var5 = var1.getY();
        int var6 = var5 + var0.halfHeight();
        int var7 = var5 - var0.halfHeight() - 1;
        int var8 = var0.radius().sample(var3);
        BlockPos.MutableBlockPos var9 = new BlockPos.MutableBlockPos();

        for(BlockPos var10 : BlockPos.betweenClosed(var1.offset(-var8, 0, -var8), var1.offset(var8, 0, var8))) {
            int var11 = var10.getX() - var1.getX();
            int var12 = var10.getZ() - var1.getZ();
            if (var11 * var11 + var12 * var12 <= var8 * var8) {
                var4 |= this.placeColumn(var0, var2, var3, var6, var7, var9.set(var10));
            }
        }

        return var4;
    }

    protected boolean placeColumn(DiskConfiguration param0, WorldGenLevel param1, RandomSource param2, int param3, int param4, BlockPos.MutableBlockPos param5) {
        boolean var0 = false;
        BlockState var1 = null;

        for(int var2 = param3; var2 > param4; --var2) {
            param5.setY(var2);
            if (param0.target().test(param1, param5)) {
                BlockState var3 = param0.stateProvider().getState(param1, param2, param5);
                param1.setBlock(param5, var3, 2);
                this.markAboveForPostProcessing(param1, param5);
                var0 = true;
            }
        }

        return var0;
    }
}
