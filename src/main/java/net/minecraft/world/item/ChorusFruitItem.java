package net.minecraft.world.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ChorusFruitItem extends Item {
    public ChorusFruitItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack param0, Level param1, LivingEntity param2) {
        ItemStack var0 = super.finishUsingItem(param0, param1, param2);
        if (!param1.isClientSide) {
            double var1 = param2.getX();
            double var2 = param2.getY();
            double var3 = param2.getZ();

            for(int var4 = 0; var4 < 16; ++var4) {
                double var5 = param2.getX() + (param2.getRandom().nextDouble() - 0.5) * 16.0;
                double var6 = Mth.clamp(
                    param2.getY() + (double)(param2.getRandom().nextInt(16) - 8),
                    (double)param1.getMinBuildHeight(),
                    (double)(param1.getMinBuildHeight() + ((ServerLevel)param1).getLogicalHeight() - 1)
                );
                double var7 = param2.getZ() + (param2.getRandom().nextDouble() - 0.5) * 16.0;
                if (param2.isPassenger()) {
                    param2.stopRiding();
                }

                if (param2.randomTeleport(var5, var6, var7, true)) {
                    SoundEvent var8 = param2 instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
                    param1.playSound(null, var1, var2, var3, var8, SoundSource.PLAYERS, 1.0F, 1.0F);
                    param2.playSound(var8, 1.0F, 1.0F);
                    break;
                }
            }

            if (param2 instanceof Player) {
                ((Player)param2).getCooldowns().addCooldown(this, 20);
            }
        }

        return var0;
    }
}
