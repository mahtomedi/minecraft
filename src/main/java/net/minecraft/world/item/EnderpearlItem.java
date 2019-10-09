package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;

public class EnderpearlItem extends Item {
    public EnderpearlItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        param0.playSound(
            null,
            param1.getX(),
            param1.getY(),
            param1.getZ(),
            SoundEvents.ENDER_PEARL_THROW,
            SoundSource.NEUTRAL,
            0.5F,
            0.4F / (random.nextFloat() * 0.4F + 0.8F)
        );
        param1.getCooldowns().addCooldown(this, 20);
        if (!param0.isClientSide) {
            ThrownEnderpearl var1 = new ThrownEnderpearl(param0, param1);
            var1.setItem(var0);
            var1.shootFromRotation(param1, param1.xRot, param1.yRot, 0.0F, 1.5F, 1.0F);
            param0.addFreshEntity(var1);
        }

        param1.awardStat(Stats.ITEM_USED.get(this));
        if (!param1.abilities.instabuild) {
            var0.shrink(1);
        }

        return InteractionResultHolder.success(var0);
    }
}
