package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.level.Level;

public class ExperienceBottleItem extends Item {
    public ExperienceBottleItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public boolean isFoil(ItemStack param0) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        param0.playSound(
            null,
            param1.getX(),
            param1.getY(),
            param1.getZ(),
            SoundEvents.EXPERIENCE_BOTTLE_THROW,
            SoundSource.NEUTRAL,
            0.5F,
            0.4F / (random.nextFloat() * 0.4F + 0.8F)
        );
        if (!param0.isClientSide) {
            ThrownExperienceBottle var1 = new ThrownExperienceBottle(param0, param1);
            var1.setItem(var0);
            var1.shootFromRotation(param1, param1.xRot, param1.yRot, -20.0F, 0.7F, 1.0F);
            param0.addFreshEntity(var1);
        }

        param1.awardStat(Stats.ITEM_USED.get(this));
        if (!param1.abilities.instabuild) {
            var0.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(var0, param0.isClientSide());
    }
}
