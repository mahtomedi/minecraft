package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;

public class EndGatewayFeature extends Feature<EndGatewayConfiguration> {
    public EndGatewayFeature(Codec<EndGatewayConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<EndGatewayConfiguration> param0) {
        BlockPos var0 = param0.origin();
        WorldGenLevel var1 = param0.level();
        EndGatewayConfiguration var2 = param0.config();

        for(BlockPos var3 : BlockPos.betweenClosed(var0.offset(-1, -2, -1), var0.offset(1, 2, 1))) {
            boolean var4 = var3.getX() == var0.getX();
            boolean var5 = var3.getY() == var0.getY();
            boolean var6 = var3.getZ() == var0.getZ();
            boolean var7 = Math.abs(var3.getY() - var0.getY()) == 2;
            if (var4 && var5 && var6) {
                BlockPos var8 = var3.immutable();
                this.setBlock(var1, var8, Blocks.END_GATEWAY.defaultBlockState());
                var2.getExit().ifPresent(param3 -> {
                    BlockEntity var0x = var1.getBlockEntity(var8);
                    if (var0x instanceof TheEndGatewayBlockEntity var1x) {
                        var1x.setExitPosition(param3, var2.isExitExact());
                        var0x.setChanged();
                    }

                });
            } else if (var5) {
                this.setBlock(var1, var3, Blocks.AIR.defaultBlockState());
            } else if (var7 && var4 && var6) {
                this.setBlock(var1, var3, Blocks.BEDROCK.defaultBlockState());
            } else if ((var4 || var6) && !var7) {
                this.setBlock(var1, var3, Blocks.BEDROCK.defaultBlockState());
            } else {
                this.setBlock(var1, var3, Blocks.AIR.defaultBlockState());
            }
        }

        return true;
    }
}
