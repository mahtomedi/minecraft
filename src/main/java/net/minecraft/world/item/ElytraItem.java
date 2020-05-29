package net.minecraft.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class ElytraItem extends Item implements Wearable {
    public ElytraItem(Item.Properties param0) {
        super(param0);
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    public static boolean isFlyEnabled(ItemStack param0) {
        return param0.getDamageValue() < param0.getMaxDamage() - 1;
    }

    @Override
    public boolean isValidRepairItem(ItemStack param0, ItemStack param1) {
        return param1.getItem() == Items.PHANTOM_MEMBRANE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        EquipmentSlot var1 = Mob.getEquipmentSlotForItem(var0);
        ItemStack var2 = param1.getItemBySlot(var1);
        if (var2.isEmpty()) {
            param1.setItemSlot(var1, var0.copy());
            var0.setCount(0);
            return InteractionResultHolder.sidedSuccess(var0, param0.isClientSide());
        } else {
            return InteractionResultHolder.fail(var0);
        }
    }
}
