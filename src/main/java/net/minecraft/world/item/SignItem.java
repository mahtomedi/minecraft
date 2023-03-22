package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SignItem extends StandingAndWallBlockItem {
    public SignItem(Item.Properties param0, Block param1, Block param2) {
        super(param1, param2, param0, Direction.DOWN);
    }

    public SignItem(Item.Properties param0, Block param1, Block param2, Direction param3) {
        super(param1, param2, param0, param3);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos param0, Level param1, @Nullable Player param2, ItemStack param3, BlockState param4) {
        boolean var0 = super.updateCustomBlockEntityTag(param0, param1, param2, param3, param4);
        if (!param1.isClientSide && !var0 && param2 != null) {
            BlockEntity var9 = param1.getBlockEntity(param0);
            if (var9 instanceof SignBlockEntity var1) {
                Block var10 = param1.getBlockState(param0).getBlock();
                if (var10 instanceof SignBlock var2) {
                    var2.openTextEdit(param2, var1, true);
                }
            }
        }

        return var0;
    }
}
