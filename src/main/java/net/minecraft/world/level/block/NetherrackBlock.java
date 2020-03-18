package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class NetherrackBlock extends Block implements BonemealableBlock {
    public NetherrackBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter param0, BlockPos param1, BlockState param2, boolean param3) {
        if (!param0.getBlockState(param1.above()).propagatesSkylightDown(param0, param1)) {
            return false;
        } else {
            for(BlockPos var0 : BlockPos.betweenClosed(param1.offset(-1, -1, -1), param1.offset(1, 1, 1))) {
                if (param0.getBlockState(var0).is(BlockTags.NYLIUM)) {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public boolean isBonemealSuccess(Level param0, Random param1, BlockPos param2, BlockState param3) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel param0, Random param1, BlockPos param2, BlockState param3) {
        boolean var0 = false;
        boolean var1 = false;

        for(BlockPos var2 : BlockPos.betweenClosed(param2.offset(-1, -1, -1), param2.offset(1, 1, 1))) {
            Block var3 = param0.getBlockState(var2).getBlock();
            if (var3 == Blocks.WARPED_NYLIUM) {
                var1 = true;
            }

            if (var3 == Blocks.CRIMSON_NYLIUM) {
                var0 = true;
            }

            if (var1 && var0) {
                break;
            }
        }

        if (var1 && var0) {
            param0.setBlock(param2, param1.nextBoolean() ? Blocks.WARPED_NYLIUM.defaultBlockState() : Blocks.CRIMSON_NYLIUM.defaultBlockState(), 3);
        } else if (var1) {
            param0.setBlock(param2, Blocks.WARPED_NYLIUM.defaultBlockState(), 3);
        } else if (var0) {
            param0.setBlock(param2, Blocks.CRIMSON_NYLIUM.defaultBlockState(), 3);
        }

    }
}
