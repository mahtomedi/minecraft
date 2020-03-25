package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ItemSteerableMount;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class SaddleItem extends Item {
    public SaddleItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public boolean interactEnemy(ItemStack param0, Player param1, LivingEntity param2, InteractionHand param3) {
        if (param2 instanceof ItemSteerableMount) {
            ItemSteerableMount var0 = (ItemSteerableMount)param2;
            if (param2.isAlive() && !var0.hasSaddle() && !param2.isBaby()) {
                var0.setSaddle(true);
                param2.level.playSound(param1, param2.getX(), param2.getY(), param2.getZ(), SoundEvents.PIG_SADDLE, SoundSource.NEUTRAL, 0.5F, 1.0F);
                param0.shrink(1);
                return true;
            }
        }

        return false;
    }
}
