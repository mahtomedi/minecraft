package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class HangingEntityItem extends Item {
    private final EntityType<? extends HangingEntity> type;

    public HangingEntityItem(EntityType<? extends HangingEntity> param0, Item.Properties param1) {
        super(param1);
        this.type = param0;
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        BlockPos var0 = param0.getClickedPos();
        Direction var1 = param0.getClickedFace();
        BlockPos var2 = var0.relative(var1);
        Player var3 = param0.getPlayer();
        ItemStack var4 = param0.getItemInHand();
        if (var3 != null && !this.mayPlace(var3, var1, var4, var2)) {
            return InteractionResult.FAIL;
        } else {
            Level var5 = param0.getLevel();
            HangingEntity var6;
            if (this.type == EntityType.PAINTING) {
                var6 = new Painting(var5, var2, var1);
            } else {
                if (this.type != EntityType.ITEM_FRAME) {
                    return InteractionResult.sidedSuccess(var5.isClientSide);
                }

                var6 = new ItemFrame(var5, var2, var1);
            }

            CompoundTag var9 = var4.getTag();
            if (var9 != null) {
                EntityType.updateCustomEntityTag(var5, var3, var6, var9);
            }

            if (var6.survives()) {
                if (!var5.isClientSide) {
                    var6.playPlacementSound();
                    var5.addFreshEntity(var6);
                }

                var4.shrink(1);
                return InteractionResult.sidedSuccess(var5.isClientSide);
            } else {
                return InteractionResult.CONSUME;
            }
        }
    }

    protected boolean mayPlace(Player param0, Direction param1, ItemStack param2, BlockPos param3) {
        return !param1.getAxis().isVertical() && param0.mayUseItemAt(param3, param1, param2);
    }
}
