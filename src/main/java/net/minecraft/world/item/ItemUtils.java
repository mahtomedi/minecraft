package net.minecraft.world.item;

import java.util.stream.Stream;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ItemUtils {
    public static InteractionResultHolder<ItemStack> startUsingInstantly(Level param0, Player param1, InteractionHand param2) {
        param1.startUsingItem(param2);
        return InteractionResultHolder.consume(param1.getItemInHand(param2));
    }

    public static ItemStack createFilledResult(ItemStack param0, Player param1, ItemStack param2, boolean param3) {
        boolean var0 = param1.getAbilities().instabuild;
        if (param3 && var0) {
            if (!param1.getInventory().contains(param2)) {
                param1.getInventory().add(param2);
            }

            return param0;
        } else {
            if (!var0) {
                param0.shrink(1);
            }

            if (param0.isEmpty()) {
                return param2;
            } else {
                if (!param1.getInventory().add(param2)) {
                    param1.drop(param2, false);
                }

                return param0;
            }
        }
    }

    public static ItemStack createFilledResult(ItemStack param0, Player param1, ItemStack param2) {
        return createFilledResult(param0, param1, param2, true);
    }

    public static void onContainerDestroyed(ItemEntity param0, Stream<ItemStack> param1) {
        Level var0 = param0.level();
        if (!var0.isClientSide) {
            param1.forEach(param2 -> var0.addFreshEntity(new ItemEntity(var0, param0.getX(), param0.getY(), param0.getZ(), param2)));
        }
    }
}
