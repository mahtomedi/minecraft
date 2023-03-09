package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Spider extends Monster {
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Spider.class, EntityDataSerializers.BYTE);
    private static final float SPIDER_SPECIAL_EFFECT_CHANCE = 0.1F;

    public Spider(EntityType<? extends Spider> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new Spider.SpiderAttackGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new Spider.SpiderTargetGoal<>(this, Player.class));
        this.targetSelector.addGoal(3, new Spider.SpiderTargetGoal<>(this, IronGolem.class));
    }

    @Override
    public double getPassengersRidingOffset() {
        return (double)(this.getBbHeight() * 0.5F);
    }

    @Override
    protected PathNavigation createNavigation(Level param0) {
        return new WallClimberNavigation(this, param0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            this.setClimbing(this.horizontalCollision);
        }

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0).add(Attributes.MOVEMENT_SPEED, 0.3F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.SPIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbing();
    }

    @Override
    public void makeStuckInBlock(BlockState param0, Vec3 param1) {
        if (!param0.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(param0, param1);
        }

    }

    @Override
    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance param0) {
        return param0.getEffect() == MobEffects.POISON ? false : super.canBeAffected(param0);
    }

    public boolean isClimbing() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setClimbing(boolean param0) {
        byte var0 = this.entityData.get(DATA_FLAGS_ID);
        if (param0) {
            var0 = (byte)(var0 | 1);
        } else {
            var0 = (byte)(var0 & -2);
        }

        this.entityData.set(DATA_FLAGS_ID, var0);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        param3 = super.finalizeSpawn(param0, param1, param2, param3, param4);
        RandomSource var0 = param0.getRandom();
        if (var0.nextInt(100) == 0) {
            Skeleton var1 = EntityType.SKELETON.create(this.level);
            if (var1 != null) {
                var1.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                var1.finalizeSpawn(param0, param1, param2, null, null);
                var1.startRiding(this);
            }
        }

        if (param3 == null) {
            param3 = new Spider.SpiderEffectsGroupData();
            if (param0.getDifficulty() == Difficulty.HARD && var0.nextFloat() < 0.1F * param1.getSpecialMultiplier()) {
                ((Spider.SpiderEffectsGroupData)param3).setRandomEffect(var0);
            }
        }

        if (param3 instanceof Spider.SpiderEffectsGroupData var2) {
            MobEffect var3 = var2.effect;
            if (var3 != null) {
                this.addEffect(new MobEffectInstance(var3, -1));
            }
        }

        return param3;
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return 0.65F;
    }

    static class SpiderAttackGoal extends MeleeAttackGoal {
        public SpiderAttackGoal(Spider param0) {
            super(param0, 1.0, true);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.mob.isVehicle();
        }

        @Override
        public boolean canContinueToUse() {
            float var0 = this.mob.getLightLevelDependentMagicValue();
            if (var0 >= 0.5F && this.mob.getRandom().nextInt(100) == 0) {
                this.mob.setTarget(null);
                return false;
            } else {
                return super.canContinueToUse();
            }
        }

        @Override
        protected double getAttackReachSqr(LivingEntity param0) {
            return (double)(4.0F + param0.getBbWidth());
        }
    }

    public static class SpiderEffectsGroupData implements SpawnGroupData {
        @Nullable
        public MobEffect effect;

        public void setRandomEffect(RandomSource param0) {
            int var0 = param0.nextInt(5);
            if (var0 <= 1) {
                this.effect = MobEffects.MOVEMENT_SPEED;
            } else if (var0 <= 2) {
                this.effect = MobEffects.DAMAGE_BOOST;
            } else if (var0 <= 3) {
                this.effect = MobEffects.REGENERATION;
            } else if (var0 <= 4) {
                this.effect = MobEffects.INVISIBILITY;
            }

        }
    }

    static class SpiderTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        public SpiderTargetGoal(Spider param0, Class<T> param1) {
            super(param0, param1, true);
        }

        @Override
        public boolean canUse() {
            float var0 = this.mob.getLightLevelDependentMagicValue();
            return var0 >= 0.5F ? false : super.canUse();
        }
    }
}
