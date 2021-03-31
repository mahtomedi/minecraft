package net.minecraft.world.entity.animal;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class Pufferfish extends AbstractFish {
    private static final EntityDataAccessor<Integer> PUFF_STATE = SynchedEntityData.defineId(Pufferfish.class, EntityDataSerializers.INT);
    private int inflateCounter;
    private int deflateTimer;
    private static final Predicate<LivingEntity> NO_SPECTATORS_AND_NO_WATER_MOB = param0 -> {
        if (param0 == null) {
            return false;
        } else if (!(param0 instanceof Player) || !param0.isSpectator() && !((Player)param0).isCreative()) {
            return param0.getMobType() != MobType.WATER;
        } else {
            return false;
        }
    };
    public static final int STATE_SMALL = 0;
    public static final int STATE_MID = 1;
    public static final int STATE_FULL = 2;

    public Pufferfish(EntityType<? extends Pufferfish> param0, Level param1) {
        super(param0, param1);
        this.refreshDimensions();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PUFF_STATE, 0);
    }

    public int getPuffState() {
        return this.entityData.get(PUFF_STATE);
    }

    public void setPuffState(int param0) {
        this.entityData.set(PUFF_STATE, param0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (PUFF_STATE.equals(param0)) {
            this.refreshDimensions();
        }

        super.onSyncedDataUpdated(param0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("PuffState", this.getPuffState());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setPuffState(param0.getInt("PuffState"));
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(Items.PUFFERFISH_BUCKET);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new Pufferfish.PufferfishPuffGoal(this));
    }

    @Override
    public void tick() {
        if (!this.level.isClientSide && this.isAlive() && this.isEffectiveAi()) {
            if (this.inflateCounter > 0) {
                if (this.getPuffState() == 0) {
                    this.playSound(SoundEvents.PUFFER_FISH_BLOW_UP, this.getSoundVolume(), this.getVoicePitch());
                    this.setPuffState(1);
                } else if (this.inflateCounter > 40 && this.getPuffState() == 1) {
                    this.playSound(SoundEvents.PUFFER_FISH_BLOW_UP, this.getSoundVolume(), this.getVoicePitch());
                    this.setPuffState(2);
                }

                ++this.inflateCounter;
            } else if (this.getPuffState() != 0) {
                if (this.deflateTimer > 60 && this.getPuffState() == 2) {
                    this.playSound(SoundEvents.PUFFER_FISH_BLOW_OUT, this.getSoundVolume(), this.getVoicePitch());
                    this.setPuffState(1);
                } else if (this.deflateTimer > 100 && this.getPuffState() == 1) {
                    this.playSound(SoundEvents.PUFFER_FISH_BLOW_OUT, this.getSoundVolume(), this.getVoicePitch());
                    this.setPuffState(0);
                }

                ++this.deflateTimer;
            }
        }

        super.tick();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isAlive() && this.getPuffState() > 0) {
            for(Mob var1 : this.level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(0.3), NO_SPECTATORS_AND_NO_WATER_MOB)) {
                if (var1.isAlive()) {
                    this.touch(var1);
                }
            }
        }

    }

    private void touch(Mob param0) {
        int var0 = this.getPuffState();
        if (param0.hurt(DamageSource.mobAttack(this), (float)(1 + var0))) {
            param0.addEffect(new MobEffectInstance(MobEffects.POISON, 60 * var0, 0));
            this.playSound(SoundEvents.PUFFER_FISH_STING, 1.0F, 1.0F);
        }

    }

    @Override
    public void playerTouch(Player param0) {
        int var0 = this.getPuffState();
        if (param0 instanceof ServerPlayer && var0 > 0 && param0.hurt(DamageSource.mobAttack(this), (float)(1 + var0))) {
            if (!this.isSilent()) {
                ((ServerPlayer)param0).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PUFFER_FISH_STING, 0.0F));
            }

            param0.addEffect(new MobEffectInstance(MobEffects.POISON, 60 * var0, 0));
        }

    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PUFFER_FISH_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PUFFER_FISH_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.PUFFER_FISH_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.PUFFER_FISH_FLOP;
    }

    @Override
    public EntityDimensions getDimensions(Pose param0) {
        return super.getDimensions(param0).scale(getScale(this.getPuffState()));
    }

    private static float getScale(int param0) {
        switch(param0) {
            case 0:
                return 0.5F;
            case 1:
                return 0.7F;
            default:
                return 1.0F;
        }
    }

    static class PufferfishPuffGoal extends Goal {
        private final Pufferfish fish;

        public PufferfishPuffGoal(Pufferfish param0) {
            this.fish = param0;
        }

        @Override
        public boolean canUse() {
            List<LivingEntity> var0 = this.fish
                .level
                .getEntitiesOfClass(LivingEntity.class, this.fish.getBoundingBox().inflate(2.0), Pufferfish.NO_SPECTATORS_AND_NO_WATER_MOB);
            return !var0.isEmpty();
        }

        @Override
        public void start() {
            this.fish.inflateCounter = 1;
            this.fish.deflateTimer = 0;
        }

        @Override
        public void stop() {
            this.fish.inflateCounter = 0;
        }

        @Override
        public boolean canContinueToUse() {
            List<LivingEntity> var0 = this.fish
                .level
                .getEntitiesOfClass(LivingEntity.class, this.fish.getBoundingBox().inflate(2.0), Pufferfish.NO_SPECTATORS_AND_NO_WATER_MOB);
            return !var0.isEmpty();
        }
    }
}
