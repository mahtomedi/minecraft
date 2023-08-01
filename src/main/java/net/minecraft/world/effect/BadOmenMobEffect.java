package net.minecraft.world.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;

class BadOmenMobEffect extends MobEffect {
    protected BadOmenMobEffect(MobEffectCategory param0, int param1) {
        super(param0, param1);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int param0, int param1) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity param0, int param1) {
        super.applyEffectTick(param0, param1);
        if (param0 instanceof ServerPlayer var0 && !param0.isSpectator()) {
            ServerLevel var1 = var0.serverLevel();
            if (var1.getDifficulty() == Difficulty.PEACEFUL) {
                return;
            }

            if (var1.isVillage(param0.blockPosition())) {
                var1.getRaids().createOrExtendRaid(var0);
            }
        }

    }
}
