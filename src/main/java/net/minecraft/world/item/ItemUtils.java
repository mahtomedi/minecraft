package net.minecraft.world.item;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
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
        Level var0 = param0.level;
        if (!var0.isClientSide) {
            param1.forEach(param2 -> var0.addFreshEntity(new ItemEntity(var0, param0.getX(), param0.getY(), param0.getZ(), param2)));
        }
    }

    public static Optional<InteractionResult> bucketMobPickup(
        Player param0, InteractionHand param1, LivingEntity param2, SoundEvent param3, Supplier<ItemStack> param4
    ) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (var0.getItem() == Items.WATER_BUCKET && param2.isAlive()) {
            param2.playSound(param3, 1.0F, 1.0F);
            var0.shrink(1);
            ItemStack var1 = param4.get();
            Level var2 = param2.level;
            if (!var2.isClientSide) {
                CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)param0, var1);
            }

            if (var0.isEmpty()) {
                param0.setItemInHand(param1, var1);
            } else if (!param0.getInventory().add(var1)) {
                param0.drop(var1, false);
            }

            param2.discard();
            return Optional.of(InteractionResult.sidedSuccess(var2.isClientSide));
        } else {
            return Optional.empty();
        }
    }
}
