package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;

public class ThrowablePotionItem extends PotionItem {
    public ThrowablePotionItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        if (!param0.isClientSide) {
            ThrownPotion var1 = new ThrownPotion(param0, param1);
            var1.setItem(var0);
            var1.shootFromRotation(param1, param1.xRot, param1.yRot, -20.0F, 0.5F, 1.0F);
            param0.addFreshEntity(var1);
        }

        param1.awardStat(Stats.ITEM_USED.get(this));
        if (!param1.abilities.instabuild) {
            var0.shrink(1);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, var0);
    }
}
