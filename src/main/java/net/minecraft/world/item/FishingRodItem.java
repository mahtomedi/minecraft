package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class FishingRodItem extends Item implements Vanishable {
    public FishingRodItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        if (param1.fishing != null) {
            if (!param0.isClientSide) {
                int var1 = param1.fishing.retrieve(var0);
                var0.hurtAndBreak(var1, param1, param1x -> param1x.broadcastBreakEvent(param2));
            }

            param0.playSound(
                null,
                param1.getX(),
                param1.getY(),
                param1.getZ(),
                SoundEvents.FISHING_BOBBER_RETRIEVE,
                SoundSource.NEUTRAL,
                1.0F,
                0.4F / (random.nextFloat() * 0.4F + 0.8F)
            );
        } else {
            param0.playSound(
                null,
                param1.getX(),
                param1.getY(),
                param1.getZ(),
                SoundEvents.FISHING_BOBBER_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (random.nextFloat() * 0.4F + 0.8F)
            );
            if (!param0.isClientSide) {
                int var2 = EnchantmentHelper.getFishingSpeedBonus(var0);
                int var3 = EnchantmentHelper.getFishingLuckBonus(var0);
                param0.addFreshEntity(new FishingHook(param1, param0, var3, var2));
            }

            param1.awardStat(Stats.ITEM_USED.get(this));
        }

        return InteractionResultHolder.sidedSuccess(var0, param0.isClientSide());
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }
}
