package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SignItem extends StandingAndWallBlockItem {
    public SignItem(Item.Properties param0, Block param1, Block param2) {
        super(param1, param2, param0);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos param0, Level param1, @Nullable Player param2, ItemStack param3, BlockState param4) {
        boolean var0 = super.updateCustomBlockEntityTag(param0, param1, param2, param3, param4);
        if (!param1.isClientSide && !var0 && param2 != null) {
            param2.openTextEdit((SignBlockEntity)param1.getBlockEntity(param0));
        }

        return var0;
    }
}
