package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;

public class HangingSignItem extends SignItem {
    public HangingSignItem(Block param0, Block param1, Item.Properties param2) {
        super(param2, param0, param1, Direction.UP);
    }

    @Override
    protected boolean canPlace(LevelReader param0, BlockState param1, BlockPos param2) {
        Block var5 = param1.getBlock();
        if (var5 instanceof WallHangingSignBlock var0 && !var0.canPlace(param1, param0, param2)) {
            return false;
        }

        return super.canPlace(param0, param1, param2);
    }
}
