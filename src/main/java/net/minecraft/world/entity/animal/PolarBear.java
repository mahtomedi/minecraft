package net.minecraft.world.entity.animal;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PolarBear extends Animal {
    private static final EntityDataAccessor<Boolean> DATA_STANDING_ID = SynchedEntityData.defineId(PolarBear.class, EntityDataSerializers.BOOLEAN);
    private float clientSideStandAnimationO;
    private float clientSideStandAnimation;
    private int warningSoundTicks;

    public PolarBear(EntityType<? extends PolarBear> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    public AgableMob getBreedOffspring(AgableMob param0) {
        return EntityType.POLAR_BEAR.create(this.level);
    }

    @Override
    public boolean isFood(ItemStack param0) {
        return false;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PolarBear.PolarBearMeleeAttackGoal());
        this.goalSelector.addGoal(1, new PolarBear.PolarBearPanicGoal());
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new PolarBear.PolarBearHurtByTargetGoal());
        this.targetSelector.addGoal(2, new PolarBear.PolarBearAttackPlayersGoal());
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Fox.class, 10, true, true, null));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 30.0)
            .add(Attributes.FOLLOW_RANGE, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    public static boolean checkPolarBearSpawnRules(EntityType<PolarBear> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
        Biome var0 = param1.getBiome(param3);
        if (var0 != Biomes.FROZEN_OCEAN && var0 != Biomes.DEEP_FROZEN_OCEAN) {
            return checkAnimalSpawnRules(param0, param1, param2, param3, param4);
        } else {
            return param1.getRawBrightness(param3, 0) > 8 && param1.getBlockState(param3.below()).getBlock() == Blocks.ICE;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isBaby() ? SoundEvents.POLAR_BEAR_AMBIENT_BABY : SoundEvents.POLAR_BEAR_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.POLAR_BEAR_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.POLAR_BEAR_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.POLAR_BEAR_STEP, 0.15F, 1.0F);
    }

    protected void playWarningSound() {
        if (this.warningSoundTicks <= 0) {
            this.playSound(SoundEvents.POLAR_BEAR_WARNING, 1.0F, this.getVoicePitch());
            this.warningSoundTicks = 40;
        }

    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STANDING_ID, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            if (this.clientSideStandAnimation != this.clientSideStandAnimationO) {
                this.refreshDimensions();
            }

            this.clientSideStandAnimationO = this.clientSideStandAnimation;
            if (this.isStanding()) {
                this.clientSideStandAnimation = Mth.clamp(this.clientSideStandAnimation + 1.0F, 0.0F, 6.0F);
            } else {
                this.clientSideStandAnimation = Mth.clamp(this.clientSideStandAnimation - 1.0F, 0.0F, 6.0F);
            }
        }

        if (this.warningSoundTicks > 0) {
            --this.warningSoundTicks;
        }

    }

    @Override
    public EntityDimensions getDimensions(Pose param0) {
        if (this.clientSideStandAnimation > 0.0F) {
            float var0 = this.clientSideStandAnimation / 6.0F;
            float var1 = 1.0F + var0;
            return super.getDimensions(param0).scale(1.0F, var1);
        } else {
            return super.getDimensions(param0);
        }
    }

    @Override
    public boolean doHurtTarget(Entity param0) {
        boolean var0 = param0.hurt(DamageSource.mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
        if (var0) {
            this.doEnchantDamageEffects(this, param0);
        }

        return var0;
    }

    public boolean isStanding() {
        return this.entityData.get(DATA_STANDING_ID);
    }

    public void setStanding(boolean param0) {
        this.entityData.set(DATA_STANDING_ID, param0);
    }

    @OnlyIn(Dist.CLIENT)
    public float getStandingAnimationScale(float param0) {
        return Mth.lerp(param0, this.clientSideStandAnimationO, this.clientSideStandAnimation) / 6.0F;
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.98F;
    }

    @Override
    public SpawnGroupData finalizeSpawn(
        LevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        if (param3 == null) {
            param3 = new AgableMob.AgableMobGroupData();
            ((AgableMob.AgableMobGroupData)param3).setBabySpawnChance(1.0F);
        }

        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    class PolarBearAttackPlayersGoal extends NearestAttackableTargetGoal<Player> {
        public PolarBearAttackPlayersGoal() {
            super(PolarBear.this, Player.class, 20, true, true, null);
        }

        @Override
        public boolean canUse() {
            if (PolarBear.this.isBaby()) {
                return false;
            } else {
                if (super.canUse()) {
                    for(PolarBear var1 : PolarBear.this.level.getEntitiesOfClass(PolarBear.class, PolarBear.this.getBoundingBox().inflate(8.0, 4.0, 8.0))) {
                        if (var1.isBaby()) {
                            return true;
                        }
                    }
                }

                return false;
            }
        }

        @Override
        protected double getFollowDistance() {
            return super.getFollowDistance() * 0.5;
        }
    }

    class PolarBearHurtByTargetGoal extends HurtByTargetGoal {
        public PolarBearHurtByTargetGoal() {
            super(PolarBear.this);
        }

        @Override
        public void start() {
            super.start();
            if (PolarBear.this.isBaby()) {
                this.alertOthers();
                this.stop();
            }

        }

        @Override
        protected void alertOther(Mob param0, LivingEntity param1) {
            if (param0 instanceof PolarBear && !param0.isBaby()) {
                super.alertOther(param0, param1);
            }

        }
    }

    class PolarBearMeleeAttackGoal extends MeleeAttackGoal {
        public PolarBearMeleeAttackGoal() {
            super(PolarBear.this, 1.25, true);
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity param0, double param1) {
            double var0 = this.getAttackReachSqr(param0);
            if (param1 <= var0 && this.attackTime <= 0) {
                this.attackTime = 20;
                this.mob.doHurtTarget(param0);
                PolarBear.this.setStanding(false);
            } else if (param1 <= var0 * 2.0) {
                if (this.attackTime <= 0) {
                    PolarBear.this.setStanding(false);
                    this.attackTime = 20;
                }

                if (this.attackTime <= 10) {
                    PolarBear.this.setStanding(true);
                    PolarBear.this.playWarningSound();
                }
            } else {
                this.attackTime = 20;
                PolarBear.this.setStanding(false);
            }

        }

        @Override
        public void stop() {
            PolarBear.this.setStanding(false);
            super.stop();
        }

        @Override
        protected double getAttackReachSqr(LivingEntity param0) {
            return (double)(4.0F + param0.getBbWidth());
        }
    }

    class PolarBearPanicGoal extends PanicGoal {
        public PolarBearPanicGoal() {
            super(PolarBear.this, 2.0);
        }

        @Override
        public boolean canUse() {
            return !PolarBear.this.isBaby() && !PolarBear.this.isOnFire() ? false : super.canUse();
        }
    }
}
