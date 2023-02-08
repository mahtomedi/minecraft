package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public interface Equipable extends Vanishable {
    EquipmentSlot getEquipmentSlot();

    default SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_GENERIC;
    }

    default InteractionResultHolder<ItemStack> swapWithEquipmentSlot(Item param0, Level param1, Player param2, InteractionHand param3) {
        ItemStack var0 = param2.getItemInHand(param3);
        EquipmentSlot var1 = Mob.getEquipmentSlotForItem(var0);
        ItemStack var2 = param2.getItemBySlot(var1);
        if (!EnchantmentHelper.hasBindingCurse(var2) && !ItemStack.matches(var0, var2)) {
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
        } else {
            return InteractionResultHolder.fail(var0);
        }
    }

    @Nullable
    static Equipable get(ItemStack param0) {
        Item var2 = param0.getItem();
        if (var2 instanceof Equipable var0) {
            return var0;
        } else {
            Item var3 = param0.getItem();
            if (var3 instanceof BlockItem var1) {
                Block var6 = var1.getBlock();
                if (var6 instanceof Equipable var2) {
                    return var2;
                }
            }

            return null;
        }
    }
}
