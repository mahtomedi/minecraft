package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.level.Level;

public class EggItem extends Item {
    public EggItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level param0, Player param1, InteractionHand param2) {
        ItemStack var0 = param1.getItemInHand(param2);
        if (!param1.abilities.instabuild) {
            var0.shrink(1);
        }

        param0.playSound(null, param1.x, param1.y, param1.z, SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
        if (!param0.isClientSide) {
            ThrownEgg var1 = new ThrownEgg(param0, param1);
            var1.setItem(var0);
            var1.shootFromRotation(param1, param1.xRot, param1.yRot, 0.0F, 1.5F, 1.0F);
            param0.addFreshEntity(var1);
        }

        param1.awardStat(Stats.ITEM_USED.get(this));
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, var0);
    }
}
