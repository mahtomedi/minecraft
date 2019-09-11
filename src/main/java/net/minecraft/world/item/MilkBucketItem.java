package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MilkBucketItem extends Item {
    public MilkBucketItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack param0, Level param1, LivingEntity param2) {
        if (param2 instanceof ServerPlayer) {
            ServerPlayer var0 = (ServerPlayer)param2;
            CriteriaTriggers.CONSUME_ITEM.trigger(var0, param0);
            var0.awardStat(Stats.ITEM_USED.get(this));
        }

        if (param2 instanceof Player && !((Player)param2).abilities.instabuild) {
            param0.shrink(1);
        }

        if (!param1.isClientSide) {
            param2.removeAllEffects();
        }

        return param0.isEmpty() ? new ItemStack(Items.BUCKET) : param0;
    }

    @Override
    public int getUseDuration(ItemStack param0) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack param0) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        param1.startUsingItem(param2);
        return InteractionResultHolder.success(param1.getItemInHand(param2));
    }
}
