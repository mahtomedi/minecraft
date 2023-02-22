package net.minecraft.world.entity.boss.wither;

import com.google.common.collect.ImmutableList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WitherBoss extends Monster implements PowerableMob, RangedAttackMob {
    private static final EntityDataAccessor<Integer> DATA_TARGET_A = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_B = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_C = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final List<EntityDataAccessor<Integer>> DATA_TARGETS = ImmutableList.of(DATA_TARGET_A, DATA_TARGET_B, DATA_TARGET_C);
    private static final EntityDataAccessor<Integer> DATA_ID_INV = SynchedEntityData.defineId(WitherBoss.class, EntityDataSerializers.INT);
    private static final int INVULNERABLE_TICKS = 220;
    private final float[] xRotHeads = new float[2];
    private final float[] yRotHeads = new float[2];
    private final float[] xRotOHeads = new float[2];
    private final float[] yRotOHeads = new float[2];
    private final int[] nextHeadUpdate = new int[2];
    private final int[] idleHeadUpdates = new int[2];
    private int destroyBlocksTick;
    private final ServerBossEvent bossEvent = (ServerBossEvent)new ServerBossEvent(
            this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS
        )
        .setDarkenScreen(true);
    private static final Predicate<LivingEntity> LIVING_ENTITY_SELECTOR = param0 -> param0.getMobType() != MobType.UNDEAD && param0.attackable();
    private static final TargetingConditions TARGETING_CONDITIONS = TargetingConditions.forCombat().range(20.0).selector(LIVING_ENTITY_SELECTOR);

    public WitherBoss(EntityType<? extends WitherBoss> param0, Level param1) {
        super(param0, param1);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setHealth(this.getMaxHealth());
        this.xpReward = 50;
    }

    @Override
    protected PathNavigation createNavigation(Level param0) {
        FlyingPathNavigation var0 = new FlyingPathNavigation(this, param0);
        var0.setCanOpenDoors(false);
        var0.setCanFloat(true);
        var0.setCanPassDoors(true);
        return var0;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new WitherBoss.WitherDoNothingGoal());
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 40, 20.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, false, false, LIVING_ENTITY_SELECTOR));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TARGET_A, 0);
        this.entityData.define(DATA_TARGET_B, 0);
        this.entityData.define(DATA_TARGET_C, 0);
        this.entityData.define(DATA_ID_INV, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("Invul", this.getInvulnerableTicks());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setInvulnerableTicks(param0.getInt("Invul"));
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }

    }

    @Override
    public void setCustomName(@Nullable Component param0) {
        super.setCustomName(param0);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.WITHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    public void aiStep() {
        Vec3 var0 = this.getDeltaMovement().multiply(1.0, 0.6, 1.0);
        if (!this.level.isClientSide && this.getAlternativeTarget(0) > 0) {
            Entity var1 = this.level.getEntity(this.getAlternativeTarget(0));
            if (var1 != null) {
                double var2 = var0.y;
                if (this.getY() < var1.getY() || !this.isPowered() && this.getY() < var1.getY() + 5.0) {
                    var2 = Math.max(0.0, var2);
                    var2 += 0.3 - var2 * 0.6F;
                }

                var0 = new Vec3(var0.x, var2, var0.z);
                Vec3 var3 = new Vec3(var1.getX() - this.getX(), 0.0, var1.getZ() - this.getZ());
                if (var3.horizontalDistanceSqr() > 9.0) {
                    Vec3 var4 = var3.normalize();
                    var0 = var0.add(var4.x * 0.3 - var0.x * 0.6, 0.0, var4.z * 0.3 - var0.z * 0.6);
                }
            }
        }

        this.setDeltaMovement(var0);
        if (var0.horizontalDistanceSqr() > 0.05) {
            this.setYRot((float)Mth.atan2(var0.z, var0.x) * (180.0F / (float)Math.PI) - 90.0F);
        }

        super.aiStep();

        for(int var5 = 0; var5 < 2; ++var5) {
            this.yRotOHeads[var5] = this.yRotHeads[var5];
            this.xRotOHeads[var5] = this.xRotHeads[var5];
        }

        for(int var6 = 0; var6 < 2; ++var6) {
            int var7 = this.getAlternativeTarget(var6 + 1);
            Entity var8 = null;
            if (var7 > 0) {
                var8 = this.level.getEntity(var7);
            }

            if (var8 != null) {
                double var9 = this.getHeadX(var6 + 1);
                double var10 = this.getHeadY(var6 + 1);
                double var11 = this.getHeadZ(var6 + 1);
                double var12 = var8.getX() - var9;
                double var13 = var8.getEyeY() - var10;
                double var14 = var8.getZ() - var11;
                double var15 = Math.sqrt(var12 * var12 + var14 * var14);
                float var16 = (float)(Mth.atan2(var14, var12) * 180.0F / (float)Math.PI) - 90.0F;
                float var17 = (float)(-(Mth.atan2(var13, var15) * 180.0F / (float)Math.PI));
                this.xRotHeads[var6] = this.rotlerp(this.xRotHeads[var6], var17, 40.0F);
                this.yRotHeads[var6] = this.rotlerp(this.yRotHeads[var6], var16, 10.0F);
            } else {
                this.yRotHeads[var6] = this.rotlerp(this.yRotHeads[var6], this.yBodyRot, 10.0F);
            }
        }

        boolean var18 = this.isPowered();

        for(int var19 = 0; var19 < 3; ++var19) {
            double var20 = this.getHeadX(var19);
            double var21 = this.getHeadY(var19);
            double var22 = this.getHeadZ(var19);
            this.level
                .addParticle(
                    ParticleTypes.SMOKE,
                    var20 + this.random.nextGaussian() * 0.3F,
                    var21 + this.random.nextGaussian() * 0.3F,
                    var22 + this.random.nextGaussian() * 0.3F,
                    0.0,
                    0.0,
                    0.0
                );
            if (var18 && this.level.random.nextInt(4) == 0) {
                this.level
                    .addParticle(
                        ParticleTypes.ENTITY_EFFECT,
                        var20 + this.random.nextGaussian() * 0.3F,
                        var21 + this.random.nextGaussian() * 0.3F,
                        var22 + this.random.nextGaussian() * 0.3F,
                        0.7F,
                        0.7F,
                        0.5
                    );
            }
        }

        if (this.getInvulnerableTicks() > 0) {
            for(int var23 = 0; var23 < 3; ++var23) {
                this.level
                    .addParticle(
                        ParticleTypes.ENTITY_EFFECT,
                        this.getX() + this.random.nextGaussian(),
                        this.getY() + (double)(this.random.nextFloat() * 3.3F),
                        this.getZ() + this.random.nextGaussian(),
                        0.7F,
                        0.7F,
                        0.9F
                    );
            }
        }

    }

    @Override
    protected void customServerAiStep() {
        if (this.getInvulnerableTicks() > 0) {
            int var0 = this.getInvulnerableTicks() - 1;
            this.bossEvent.setProgress(1.0F - (float)var0 / 220.0F);
            if (var0 <= 0) {
                this.level.explode(this, this.getX(), this.getEyeY(), this.getZ(), 7.0F, false, Level.ExplosionInteraction.MOB);
                if (!this.isSilent()) {
                    this.level.globalLevelEvent(1023, this.blockPosition(), 0);
                }
            }

            this.setInvulnerableTicks(var0);
            if (this.tickCount % 10 == 0) {
                this.heal(10.0F);
            }

        } else {
            super.customServerAiStep();

            for(int var1 = 1; var1 < 3; ++var1) {
                if (this.tickCount >= this.nextHeadUpdate[var1 - 1]) {
                    this.nextHeadUpdate[var1 - 1] = this.tickCount + 10 + this.random.nextInt(10);
                    if ((this.level.getDifficulty() == Difficulty.NORMAL || this.level.getDifficulty() == Difficulty.HARD)
                        && this.idleHeadUpdates[var1 - 1]++ > 15) {
                        float var2 = 10.0F;
                        float var3 = 5.0F;
                        double var4 = Mth.nextDouble(this.random, this.getX() - 10.0, this.getX() + 10.0);
                        double var5 = Mth.nextDouble(this.random, this.getY() - 5.0, this.getY() + 5.0);
                        double var6 = Mth.nextDouble(this.random, this.getZ() - 10.0, this.getZ() + 10.0);
                        this.performRangedAttack(var1 + 1, var4, var5, var6, true);
                        this.idleHeadUpdates[var1 - 1] = 0;
                    }

                    int var7 = this.getAlternativeTarget(var1);
                    if (var7 > 0) {
                        LivingEntity var8 = (LivingEntity)this.level.getEntity(var7);
                        if (var8 != null && this.canAttack(var8) && !(this.distanceToSqr(var8) > 900.0) && this.hasLineOfSight(var8)) {
                            this.performRangedAttack(var1 + 1, var8);
                            this.nextHeadUpdate[var1 - 1] = this.tickCount + 40 + this.random.nextInt(20);
                            this.idleHeadUpdates[var1 - 1] = 0;
                        } else {
                            this.setAlternativeTarget(var1, 0);
                        }
                    } else {
                        List<LivingEntity> var9 = this.level
                            .getNearbyEntities(LivingEntity.class, TARGETING_CONDITIONS, this, this.getBoundingBox().inflate(20.0, 8.0, 20.0));
                        if (!var9.isEmpty()) {
                            LivingEntity var10 = var9.get(this.random.nextInt(var9.size()));
                            this.setAlternativeTarget(var1, var10.getId());
                        }
                    }
                }
            }

            if (this.getTarget() != null) {
                this.setAlternativeTarget(0, this.getTarget().getId());
            } else {
                this.setAlternativeTarget(0, 0);
            }

            if (this.destroyBlocksTick > 0) {
                --this.destroyBlocksTick;
                if (this.destroyBlocksTick == 0 && this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                    int var11 = Mth.floor(this.getY());
                    int var12 = Mth.floor(this.getX());
                    int var13 = Mth.floor(this.getZ());
                    boolean var14 = false;

                    for(int var15 = -1; var15 <= 1; ++var15) {
                        for(int var16 = -1; var16 <= 1; ++var16) {
                            for(int var17 = 0; var17 <= 3; ++var17) {
                                int var18 = var12 + var15;
                                int var19 = var11 + var17;
                                int var20 = var13 + var16;
                                BlockPos var21 = new BlockPos(var18, var19, var20);
                                BlockState var22 = this.level.getBlockState(var21);
                                if (canDestroy(var22)) {
                                    var14 = this.level.destroyBlock(var21, true, this) || var14;
                                }
                            }
                        }
                    }

                    if (var14) {
                        this.level.levelEvent(null, 1022, this.blockPosition(), 0);
                    }
                }
            }

            if (this.tickCount % 20 == 0) {
                this.heal(1.0F);
            }

            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }
    }

    public static boolean canDestroy(BlockState param0) {
        return !param0.isAir() && !param0.is(BlockTags.WITHER_IMMUNE);
    }

    public void makeInvulnerable() {
        this.setInvulnerableTicks(220);
        this.bossEvent.setProgress(0.0F);
        this.setHealth(this.getMaxHealth() / 3.0F);
    }

    @Override
    public void makeStuckInBlock(BlockState param0, Vec3 param1) {
    }

    @Override
    public void startSeenByPlayer(ServerPlayer param0) {
        super.startSeenByPlayer(param0);
        this.bossEvent.addPlayer(param0);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer param0) {
        super.stopSeenByPlayer(param0);
        this.bossEvent.removePlayer(param0);
    }

    private double getHeadX(int param0) {
        if (param0 <= 0) {
            return this.getX();
        } else {
            float var0 = (this.yBodyRot + (float)(180 * (param0 - 1))) * (float) (Math.PI / 180.0);
            float var1 = Mth.cos(var0);
            return this.getX() + (double)var1 * 1.3;
        }
    }

    private double getHeadY(int param0) {
        return param0 <= 0 ? this.getY() + 3.0 : this.getY() + 2.2;
    }

    private double getHeadZ(int param0) {
        if (param0 <= 0) {
            return this.getZ();
        } else {
            float var0 = (this.yBodyRot + (float)(180 * (param0 - 1))) * (float) (Math.PI / 180.0);
            float var1 = Mth.sin(var0);
            return this.getZ() + (double)var1 * 1.3;
        }
    }

    private float rotlerp(float param0, float param1, float param2) {
        float var0 = Mth.wrapDegrees(param1 - param0);
        if (var0 > param2) {
            var0 = param2;
        }

        if (var0 < -param2) {
            var0 = -param2;
        }

        return param0 + var0;
    }

    private void performRangedAttack(int param0, LivingEntity param1) {
        this.performRangedAttack(
            param0, param1.getX(), param1.getY() + (double)param1.getEyeHeight() * 0.5, param1.getZ(), param0 == 0 && this.random.nextFloat() < 0.001F
        );
    }

    private void performRangedAttack(int param0, double param1, double param2, double param3, boolean param4) {
        if (!this.isSilent()) {
            this.level.levelEvent(null, 1024, this.blockPosition(), 0);
        }

        double var0 = this.getHeadX(param0);
        double var1 = this.getHeadY(param0);
        double var2 = this.getHeadZ(param0);
        double var3 = param1 - var0;
        double var4 = param2 - var1;
        double var5 = param3 - var2;
        WitherSkull var6 = new WitherSkull(this.level, this, var3, var4, var5);
        var6.setOwner(this);
        if (param4) {
            var6.setDangerous(true);
        }

        var6.setPosRaw(var0, var1, var2);
        this.level.addFreshEntity(var6);
    }

    @Override
    public void performRangedAttack(LivingEntity param0, float param1) {
        this.performRangedAttack(0, param0);
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else if (param0.is(DamageTypeTags.WITHER_IMMUNE_TO) || param0.getEntity() instanceof WitherBoss) {
            return false;
        } else if (this.getInvulnerableTicks() > 0 && !param0.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        } else {
            if (this.isPowered()) {
                Entity var0 = param0.getDirectEntity();
                if (var0 instanceof AbstractArrow) {
                    return false;
                }
            }

            Entity var1 = param0.getEntity();
            if (var1 != null && !(var1 instanceof Player) && var1 instanceof LivingEntity && ((LivingEntity)var1).getMobType() == this.getMobType()) {
                return false;
            } else {
                if (this.destroyBlocksTick <= 0) {
                    this.destroyBlocksTick = 20;
                }

                for(int var2 = 0; var2 < this.idleHeadUpdates.length; ++var2) {
                    this.idleHeadUpdates[var2] += 3;
                }

                return super.hurt(param0, param1);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource param0, int param1, boolean param2) {
        super.dropCustomDeathLoot(param0, param1, param2);
        ItemEntity var0 = this.spawnAtLocation(Items.NETHER_STAR);
        if (var0 != null) {
            var0.setExtendedLifetime();
        }

    }

    @Override
    public void checkDespawn() {
        if (this.level.getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
            this.discard();
        } else {
            this.noActionTime = 0;
        }
    }

    @Override
    public boolean addEffect(MobEffectInstance param0, @Nullable Entity param1) {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 300.0)
            .add(Attributes.MOVEMENT_SPEED, 0.6F)
            .add(Attributes.FLYING_SPEED, 0.6F)
            .add(Attributes.FOLLOW_RANGE, 40.0)
            .add(Attributes.ARMOR, 4.0);
    }

    public float getHeadYRot(int param0) {
        return this.yRotHeads[param0];
    }

    public float getHeadXRot(int param0) {
        return this.xRotHeads[param0];
    }

    public int getInvulnerableTicks() {
        return this.entityData.get(DATA_ID_INV);
    }

    public void setInvulnerableTicks(int param0) {
        this.entityData.set(DATA_ID_INV, param0);
    }

    public int getAlternativeTarget(int param0) {
        return this.entityData.get(DATA_TARGETS.get(param0));
    }

    public void setAlternativeTarget(int param0, int param1) {
        this.entityData.set(DATA_TARGETS.get(param0), param1);
    }

    @Override
    public boolean isPowered() {
        return this.getHealth() <= this.getMaxHealth() / 2.0F;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    protected boolean canRide(Entity param0) {
        return false;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance param0) {
        return param0.getEffect() == MobEffects.WITHER ? false : super.canBeAffected(param0);
    }

    class WitherDoNothingGoal extends Goal {
        public WitherDoNothingGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return WitherBoss.this.getInvulnerableTicks() > 0;
        }
    }
}
