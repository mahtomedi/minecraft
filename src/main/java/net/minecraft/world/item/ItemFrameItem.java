package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;

public class ItemFrameItem extends HangingEntityItem {
    public ItemFrameItem(EntityType<? extends HangingEntity> param0, Item.Properties param1) {
        super(param0, param1);
    }

    @Override
    protected boolean mayPlace(Player param0, Direction param1, ItemStack param2, BlockPos param3) {
        return !param0.level.isOutsideBuildHeight(param3) && param0.mayUseItemAt(param3, param1, param2);
    }
}
