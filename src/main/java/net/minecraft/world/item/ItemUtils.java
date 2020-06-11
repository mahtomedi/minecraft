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

    public static ItemStack createBucketResult(ItemStack param0, Player param1, ItemStack param2) {
        if (param1.abilities.instabuild) {
            if (!param1.inventory.contains(param2)) {
                param1.inventory.add(param2);
            }

            return param0;
        } else {
            param0.shrink(1);
            if (param0.isEmpty()) {
                return param2;
            } else {
                if (!param1.inventory.add(param2)) {
                    param1.drop(param2, false);
                }

                return param0;
            }
        }
    }
}
