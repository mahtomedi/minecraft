package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HangingSignItem extends StandingAndWallBlockItem {
    public HangingSignItem(Block param0, Block param1, Item.Properties param2) {
        super(param0, param1, param2, Direction.UP);
    }

    @Override
    protected boolean canPlace(LevelReader param0, BlockState param1, BlockPos param2) {
        Block var5 = param1.getBlock();
        if (var5 instanceof WallHangingSignBlock var0 && !var0.canPlace(param1, param0, param2)) {
            return false;
        }

        return super.canPlace(param0, param1, param2);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos param0, Level param1, @Nullable Player param2, ItemStack param3, BlockState param4) {
        boolean var0 = super.updateCustomBlockEntityTag(param0, param1, param2, param3, param4);
        if (!param1.isClientSide && !var0 && param2 != null) {
            BlockEntity var8 = param1.getBlockEntity(param0);
            if (var8 instanceof SignBlockEntity var1) {
                param2.openTextEdit(var1);
            }
        }

        return var0;
    }
}
