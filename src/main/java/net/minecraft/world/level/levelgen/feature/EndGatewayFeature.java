package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;

public class EndGatewayFeature extends Feature<EndGatewayConfiguration> {
    public EndGatewayFeature(Codec<EndGatewayConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, EndGatewayConfiguration param4) {
        for(BlockPos var0 : BlockPos.betweenClosed(param3.offset(-1, -2, -1), param3.offset(1, 2, 1))) {
            boolean var1 = var0.getX() == param3.getX();
            boolean var2 = var0.getY() == param3.getY();
            boolean var3 = var0.getZ() == param3.getZ();
            boolean var4 = Math.abs(var0.getY() - param3.getY()) == 2;
            if (var1 && var2 && var3) {
                BlockPos var5 = var0.immutable();
                this.setBlock(param0, var5, Blocks.END_GATEWAY.defaultBlockState());
                param4.getExit().ifPresent(param3x -> {
                    BlockEntity var0x = param0.getBlockEntity(var5);
                    if (var0x instanceof TheEndGatewayBlockEntity) {
                        TheEndGatewayBlockEntity var1x = (TheEndGatewayBlockEntity)var0x;
                        var1x.setExitPosition(param3x, param4.isExitExact());
                        var0x.setChanged();
                    }

                });
            } else if (var2) {
                this.setBlock(param0, var0, Blocks.AIR.defaultBlockState());
            } else if (var4 && var1 && var3) {
                this.setBlock(param0, var0, Blocks.BEDROCK.defaultBlockState());
            } else if ((var1 || var3) && !var4) {
                this.setBlock(param0, var0, Blocks.BEDROCK.defaultBlockState());
            } else {
                this.setBlock(param0, var0, Blocks.AIR.defaultBlockState());
            }
        }

        return true;
    }
}
