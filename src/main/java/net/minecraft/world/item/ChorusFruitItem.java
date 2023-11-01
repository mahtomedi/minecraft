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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class ChorusFruitItem extends Item {
    public ChorusFruitItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack param0, Level param1, LivingEntity param2) {
        ItemStack var0 = super.finishUsingItem(param0, param1, param2);
        if (!param1.isClientSide) {
            for(int var1 = 0; var1 < 16; ++var1) {
                double var2 = param2.getX() + (param2.getRandom().nextDouble() - 0.5) * 16.0;
                double var3 = Mth.clamp(
                    param2.getY() + (double)(param2.getRandom().nextInt(16) - 8),
                    (double)param1.getMinBuildHeight(),
                    (double)(param1.getMinBuildHeight() + ((ServerLevel)param1).getLogicalHeight() - 1)
                );
                double var4 = param2.getZ() + (param2.getRandom().nextDouble() - 0.5) * 16.0;
                if (param2.isPassenger()) {
                    param2.stopRiding();
                }

                Vec3 var5 = param2.position();
                if (param2.randomTeleport(var2, var3, var4, true)) {
                    param1.gameEvent(GameEvent.TELEPORT, var5, GameEvent.Context.of(param2));
                    SoundSource var7;
                    SoundEvent var6;
                    if (param2 instanceof Fox) {
                        var6 = SoundEvents.FOX_TELEPORT;
                        var7 = SoundSource.NEUTRAL;
                    } else {
                        var6 = SoundEvents.CHORUS_FRUIT_TELEPORT;
                        var7 = SoundSource.PLAYERS;
                    }

                    param1.playSound(null, param2.getX(), param2.getY(), param2.getZ(), var6, var7);
                    param2.resetFallDistance();
                    break;
                }
            }

            if (param2 instanceof Player var10) {
                var10.getCooldowns().addCooldown(this, 20);
            }
        }

        return var0;
    }
}
