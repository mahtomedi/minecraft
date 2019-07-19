package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;

public class SaddleItem extends Item {
    public SaddleItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public boolean interactEnemy(ItemStack param0, Player param1, LivingEntity param2, InteractionHand param3) {
        if (param2 instanceof Pig) {
            Pig var0 = (Pig)param2;
            if (var0.isAlive() && !var0.hasSaddle() && !var0.isBaby()) {
                var0.setSaddle(true);
                var0.level.playSound(param1, var0.x, var0.y, var0.z, SoundEvents.PIG_SADDLE, SoundSource.NEUTRAL, 0.5F, 1.0F);
                param0.shrink(1);
            }

            return true;
        } else {
            return false;
        }
    }
}
