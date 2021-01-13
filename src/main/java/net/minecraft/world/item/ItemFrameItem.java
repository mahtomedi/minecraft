package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ItemFrameItem extends HangingEntityItem {
    public ItemFrameItem(Item.Properties param0) {
        super(EntityType.ITEM_FRAME, param0);
    }

    @Override
    protected boolean mayPlace(Player param0, Direction param1, ItemStack param2, BlockPos param3) {
        return !Level.isOutsideBuildHeight(param3) && param0.mayUseItemAt(param3, param1, param2);
    }
}
