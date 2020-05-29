package net.minecraft.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ItemUtils {
    public static InteractionResultHolder<ItemStack> useDrink(Level param0, Player param1, InteractionHand param2) {
        param1.startUsingItem(param2);
        return InteractionResultHolder.consume(param1.getItemInHand(param2));
    }
}
