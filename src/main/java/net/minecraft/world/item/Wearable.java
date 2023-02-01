package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface Wearable extends Vanishable {
    default InteractionResultHolder<ItemStack> swapWithEquipmentSlot(Item param0, Level param1, Player param2, InteractionHand param3) {
        ItemStack var0 = param2.getItemInHand(param3);
        EquipmentSlot var1 = Mob.getEquipmentSlotForItem(var0);
        ItemStack var2 = param2.getItemBySlot(var1);
        if (ItemStack.matches(var0, var2)) {
            return InteractionResultHolder.fail(var0);
        } else {
            param2.setItemSlot(var1, var0.copy());
            if (!param1.isClientSide()) {
                param2.awardStat(Stats.ITEM_USED.get(param0));
            }

            if (var2.isEmpty()) {
                var0.setCount(0);
            } else {
                param2.setItemInHand(param3, var2.copy());
            }

            return InteractionResultHolder.sidedSuccess(var0, param1.isClientSide());
        }
    }
}
