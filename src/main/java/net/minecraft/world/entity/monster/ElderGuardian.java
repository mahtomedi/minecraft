package net.minecraft.world.entity.monster;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ElderGuardian extends Guardian {
    public static final float ELDER_SIZE_SCALE = EntityType.ELDER_GUARDIAN.getWidth() / EntityType.GUARDIAN.getWidth();

    public ElderGuardian(EntityType<? extends ElderGuardian> param0, Level param1) {
        super(param0, param1);
        this.setPersistenceRequired();
        if (this.randomStrollGoal != null) {
            this.randomStrollGoal.setInterval(400);
        }

    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3F);
        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8.0);
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(80.0);
    }

    @Override
    public int getAttackDuration() {
        return 60;
    }

    @OnlyIn(Dist.CLIENT)
    public void setGhost() {
        this.clientSideSpikesAnimation = 1.0F;
        this.clientSideSpikesAnimationO = this.clientSideSpikesAnimation;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_AMBIENT : SoundEvents.ELDER_GUARDIAN_AMBIENT_LAND;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return this.isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_HURT : SoundEvents.ELDER_GUARDIAN_HURT_LAND;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_DEATH : SoundEvents.ELDER_GUARDIAN_DEATH_LAND;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.ELDER_GUARDIAN_FLOP;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        int var0 = 1200;
        if ((this.tickCount + this.getId()) % 1200 == 0) {
            MobEffect var1 = MobEffects.DIG_SLOWDOWN;
            List<ServerPlayer> var2 = ((ServerLevel)this.level).getPlayers(param0 -> this.distanceToSqr(param0) < 2500.0 && param0.gameMode.isSurvival());
            int var3 = 2;
            int var4 = 6000;
            int var5 = 1200;

            for(ServerPlayer var6 : var2) {
                if (!var6.hasEffect(var1) || var6.getEffect(var1).getAmplifier() < 2 || var6.getEffect(var1).getDuration() < 1200) {
                    var6.connection.send(new ClientboundGameEventPacket(10, 0.0F));
                    var6.addEffect(new MobEffectInstance(var1, 6000, 2));
                }
            }
        }

        if (!this.hasRestriction()) {
            this.restrictTo(new BlockPos(this), 16);
        }

    }
}
