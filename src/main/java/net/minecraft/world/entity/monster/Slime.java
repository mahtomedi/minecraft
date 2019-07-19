package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;

public class Slime extends Mob implements Enemy {
    private static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(Slime.class, EntityDataSerializers.INT);
    public float targetSquish;
    public float squish;
    public float oSquish;
    private boolean wasOnGround;

    public Slime(EntityType<? extends Slime> param0, Level param1) {
        super(param0, param1);
        this.moveControl = new Slime.SlimeMoveControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new Slime.SlimeFloatGoal(this));
        this.goalSelector.addGoal(2, new Slime.SlimeAttackGoal(this));
        this.goalSelector.addGoal(3, new Slime.SlimeRandomDirectionGoal(this));
        this.goalSelector.addGoal(5, new Slime.SlimeKeepOnJumpingGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, param0 -> Math.abs(param0.y - this.y) <= 4.0));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_SIZE, 1);
    }

    protected void setSize(int param0, boolean param1) {
        this.entityData.set(ID_SIZE, param0);
        this.setPos(this.x, this.y, this.z);
        this.refreshDimensions();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue((double)(param0 * param0));
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue((double)(0.2F + 0.1F * (float)param0));
        if (param1) {
            this.setHealth(this.getMaxHealth());
        }

        this.xpReward = param0;
    }

    public int getSize() {
        return this.entityData.get(ID_SIZE);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("Size", this.getSize() - 1);
        param0.putBoolean("wasOnGround", this.wasOnGround);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        int var0 = param0.getInt("Size");
        if (var0 < 0) {
            var0 = 0;
        }

        this.setSize(var0 + 1, false);
        this.wasOnGround = param0.getBoolean("wasOnGround");
    }

    public boolean isTiny() {
        return this.getSize() <= 1;
    }

    protected ParticleOptions getParticleType() {
        return ParticleTypes.ITEM_SLIME;
    }

    @Override
    public void tick() {
        if (!this.level.isClientSide && this.level.getDifficulty() == Difficulty.PEACEFUL && this.getSize() > 0) {
            this.removed = true;
        }

        this.squish += (this.targetSquish - this.squish) * 0.5F;
        this.oSquish = this.squish;
        super.tick();
        if (this.onGround && !this.wasOnGround) {
            int var0 = this.getSize();

            for(int var1 = 0; var1 < var0 * 8; ++var1) {
                float var2 = this.random.nextFloat() * (float) (Math.PI * 2);
                float var3 = this.random.nextFloat() * 0.5F + 0.5F;
                float var4 = Mth.sin(var2) * (float)var0 * 0.5F * var3;
                float var5 = Mth.cos(var2) * (float)var0 * 0.5F * var3;
                Level var10000 = this.level;
                ParticleOptions var10001 = this.getParticleType();
                double var10002 = this.x + (double)var4;
                double var10004 = this.z + (double)var5;
                var10000.addParticle(var10001, var10002, this.getBoundingBox().minY, var10004, 0.0, 0.0, 0.0);
            }

            this.playSound(this.getSquishSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) / 0.8F);
            this.targetSquish = -0.5F;
        } else if (!this.onGround && this.wasOnGround) {
            this.targetSquish = 1.0F;
        }

        this.wasOnGround = this.onGround;
        this.decreaseSquish();
    }

    protected void decreaseSquish() {
        this.targetSquish *= 0.6F;
    }

    protected int getJumpDelay() {
        return this.random.nextInt(20) + 10;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (ID_SIZE.equals(param0)) {
            this.refreshDimensions();
            this.yRot = this.yHeadRot;
            this.yBodyRot = this.yHeadRot;
            if (this.isInWater() && this.random.nextInt(20) == 0) {
                this.doWaterSplashEffect();
            }
        }

        super.onSyncedDataUpdated(param0);
    }

    @Override
    public EntityType<? extends Slime> getType() {
        return super.getType();
    }

    @Override
    public void remove() {
        int var0 = this.getSize();
        if (!this.level.isClientSide && var0 > 1 && this.getHealth() <= 0.0F) {
            int var1 = 2 + this.random.nextInt(3);

            for(int var2 = 0; var2 < var1; ++var2) {
                float var3 = ((float)(var2 % 2) - 0.5F) * (float)var0 / 4.0F;
                float var4 = ((float)(var2 / 2) - 0.5F) * (float)var0 / 4.0F;
                Slime var5 = this.getType().create(this.level);
                if (this.hasCustomName()) {
                    var5.setCustomName(this.getCustomName());
                }

                if (this.isPersistenceRequired()) {
                    var5.setPersistenceRequired();
                }

                var5.setSize(var0 / 2, true);
                var5.moveTo(this.x + (double)var3, this.y + 0.5, this.z + (double)var4, this.random.nextFloat() * 360.0F, 0.0F);
                this.level.addFreshEntity(var5);
            }
        }

        super.remove();
    }

    @Override
    public void push(Entity param0) {
        super.push(param0);
        if (param0 instanceof IronGolem && this.isDealsDamage()) {
            this.dealDamage((LivingEntity)param0);
        }

    }

    @Override
    public void playerTouch(Player param0) {
        if (this.isDealsDamage()) {
            this.dealDamage(param0);
        }

    }

    protected void dealDamage(LivingEntity param0) {
        if (this.isAlive()) {
            int var0 = this.getSize();
            if (this.distanceToSqr(param0) < 0.6 * (double)var0 * 0.6 * (double)var0
                && this.canSee(param0)
                && param0.hurt(DamageSource.mobAttack(this), (float)this.getAttackDamage())) {
                this.playSound(SoundEvents.SLIME_ATTACK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                this.doEnchantDamageEffects(this, param0);
            }
        }

    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return 0.625F * param1.height;
    }

    protected boolean isDealsDamage() {
        return !this.isTiny() && this.isEffectiveAi();
    }

    protected int getAttackDamage() {
        return this.getSize();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return this.isTiny() ? SoundEvents.SLIME_HURT_SMALL : SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isTiny() ? SoundEvents.SLIME_DEATH_SMALL : SoundEvents.SLIME_DEATH;
    }

    protected SoundEvent getSquishSound() {
        return this.isTiny() ? SoundEvents.SLIME_SQUISH_SMALL : SoundEvents.SLIME_SQUISH;
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return this.getSize() == 1 ? this.getType().getDefaultLootTable() : BuiltInLootTables.EMPTY;
    }

    public static boolean checkSlimeSpawnRules(EntityType<Slime> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
        if (param1.getLevelData().getGeneratorType() == LevelType.FLAT && param4.nextInt(4) != 1) {
            return false;
        } else {
            if (param1.getDifficulty() != Difficulty.PEACEFUL) {
                Biome var0 = param1.getBiome(param3);
                if (var0 == Biomes.SWAMP
                    && param3.getY() > 50
                    && param3.getY() < 70
                    && param4.nextFloat() < 0.5F
                    && param4.nextFloat() < param1.getMoonBrightness()
                    && param1.getMaxLocalRawBrightness(param3) <= param4.nextInt(8)) {
                    return checkMobSpawnRules(param0, param1, param2, param3, param4);
                }

                ChunkPos var1 = new ChunkPos(param3);
                boolean var2 = WorldgenRandom.seedSlimeChunk(var1.x, var1.z, param1.getSeed(), 987234911L).nextInt(10) == 0;
                if (param4.nextInt(10) == 0 && var2 && param3.getY() < 40) {
                    return checkMobSpawnRules(param0, param1, param2, param3, param4);
                }
            }

            return false;
        }
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F * (float)this.getSize();
    }

    @Override
    public int getMaxHeadXRot() {
        return 0;
    }

    protected boolean doPlayJumpSound() {
        return this.getSize() > 0;
    }

    @Override
    protected void jumpFromGround() {
        Vec3 var0 = this.getDeltaMovement();
        this.setDeltaMovement(var0.x, 0.42F, var0.z);
        this.hasImpulse = true;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        LevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        int var0 = this.random.nextInt(3);
        if (var0 < 2 && this.random.nextFloat() < 0.5F * param1.getSpecialMultiplier()) {
            ++var0;
        }

        int var1 = 1 << var0;
        this.setSize(var1, true);
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    protected SoundEvent getJumpSound() {
        return this.isTiny() ? SoundEvents.SLIME_JUMP_SMALL : SoundEvents.SLIME_JUMP;
    }

    @Override
    public EntityDimensions getDimensions(Pose param0) {
        return super.getDimensions(param0).scale(0.255F * (float)this.getSize());
    }

    static class SlimeAttackGoal extends Goal {
        private final Slime slime;
        private int growTiredTimer;

        public SlimeAttackGoal(Slime param0) {
            this.slime = param0;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity var0 = this.slime.getTarget();
            if (var0 == null) {
                return false;
            } else if (!var0.isAlive()) {
                return false;
            } else {
                return var0 instanceof Player && ((Player)var0).abilities.invulnerable ? false : this.slime.getMoveControl() instanceof Slime.SlimeMoveControl;
            }
        }

        @Override
        public void start() {
            this.growTiredTimer = 300;
            super.start();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity var0 = this.slime.getTarget();
            if (var0 == null) {
                return false;
            } else if (!var0.isAlive()) {
                return false;
            } else if (var0 instanceof Player && ((Player)var0).abilities.invulnerable) {
                return false;
            } else {
                return --this.growTiredTimer > 0;
            }
        }

        @Override
        public void tick() {
            this.slime.lookAt(this.slime.getTarget(), 10.0F, 10.0F);
            ((Slime.SlimeMoveControl)this.slime.getMoveControl()).setDirection(this.slime.yRot, this.slime.isDealsDamage());
        }
    }

    static class SlimeFloatGoal extends Goal {
        private final Slime slime;

        public SlimeFloatGoal(Slime param0) {
            this.slime = param0;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
            param0.getNavigation().setCanFloat(true);
        }

        @Override
        public boolean canUse() {
            return (this.slime.isInWater() || this.slime.isInLava()) && this.slime.getMoveControl() instanceof Slime.SlimeMoveControl;
        }

        @Override
        public void tick() {
            if (this.slime.getRandom().nextFloat() < 0.8F) {
                this.slime.getJumpControl().jump();
            }

            ((Slime.SlimeMoveControl)this.slime.getMoveControl()).setWantedMovement(1.2);
        }
    }

    static class SlimeKeepOnJumpingGoal extends Goal {
        private final Slime slime;

        public SlimeKeepOnJumpingGoal(Slime param0) {
            this.slime = param0;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !this.slime.isPassenger();
        }

        @Override
        public void tick() {
            ((Slime.SlimeMoveControl)this.slime.getMoveControl()).setWantedMovement(1.0);
        }
    }

    static class SlimeMoveControl extends MoveControl {
        private float yRot;
        private int jumpDelay;
        private final Slime slime;
        private boolean isAggressive;

        public SlimeMoveControl(Slime param0) {
            super(param0);
            this.slime = param0;
            this.yRot = 180.0F * param0.yRot / (float) Math.PI;
        }

        public void setDirection(float param0, boolean param1) {
            this.yRot = param0;
            this.isAggressive = param1;
        }

        public void setWantedMovement(double param0) {
            this.speedModifier = param0;
            this.operation = MoveControl.Operation.MOVE_TO;
        }

        @Override
        public void tick() {
            this.mob.yRot = this.rotlerp(this.mob.yRot, this.yRot, 90.0F);
            this.mob.yHeadRot = this.mob.yRot;
            this.mob.yBodyRot = this.mob.yRot;
            if (this.operation != MoveControl.Operation.MOVE_TO) {
                this.mob.setZza(0.0F);
            } else {
                this.operation = MoveControl.Operation.WAIT;
                if (this.mob.onGround) {
                    this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue()));
                    if (this.jumpDelay-- <= 0) {
                        this.jumpDelay = this.slime.getJumpDelay();
                        if (this.isAggressive) {
                            this.jumpDelay /= 3;
                        }

                        this.slime.getJumpControl().jump();
                        if (this.slime.doPlayJumpSound()) {
                            this.slime
                                .playSound(
                                    this.slime.getJumpSound(),
                                    this.slime.getSoundVolume(),
                                    ((this.slime.getRandom().nextFloat() - this.slime.getRandom().nextFloat()) * 0.2F + 1.0F) * 0.8F
                                );
                        }
                    } else {
                        this.slime.xxa = 0.0F;
                        this.slime.zza = 0.0F;
                        this.mob.setSpeed(0.0F);
                    }
                } else {
                    this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue()));
                }

            }
        }
    }

    static class SlimeRandomDirectionGoal extends Goal {
        private final Slime slime;
        private float chosenDegrees;
        private int nextRandomizeTime;

        public SlimeRandomDirectionGoal(Slime param0) {
            this.slime = param0;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.slime.getTarget() == null
                && (this.slime.onGround || this.slime.isInWater() || this.slime.isInLava() || this.slime.hasEffect(MobEffects.LEVITATION))
                && this.slime.getMoveControl() instanceof Slime.SlimeMoveControl;
        }

        @Override
        public void tick() {
            if (--this.nextRandomizeTime <= 0) {
                this.nextRandomizeTime = 40 + this.slime.getRandom().nextInt(60);
                this.chosenDegrees = (float)this.slime.getRandom().nextInt(360);
            }

            ((Slime.SlimeMoveControl)this.slime.getMoveControl()).setDirection(this.chosenDegrees, false);
        }
    }
}
