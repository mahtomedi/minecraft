package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.IntRange;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class EnderMan extends Monster implements NeutralMob {
    private static final UUID SPEED_MODIFIER_ATTACKING_UUID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
    private static final AttributeModifier SPEED_MODIFIER_ATTACKING = new AttributeModifier(
        SPEED_MODIFIER_ATTACKING_UUID, "Attacking speed boost", 0.15F, AttributeModifier.Operation.ADDITION
    );
    private static final EntityDataAccessor<Optional<BlockState>> DATA_CARRY_STATE = SynchedEntityData.defineId(
        EnderMan.class, EntityDataSerializers.BLOCK_STATE
    );
    private static final EntityDataAccessor<Boolean> DATA_CREEPY = SynchedEntityData.defineId(EnderMan.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_STARED_AT = SynchedEntityData.defineId(EnderMan.class, EntityDataSerializers.BOOLEAN);
    private static final Predicate<LivingEntity> ENDERMITE_SELECTOR = param0 -> param0 instanceof Endermite && ((Endermite)param0).isPlayerSpawned();
    private int lastStareSound = Integer.MIN_VALUE;
    private int targetChangeTime;
    private static final IntRange PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    private UUID persistentAngerTarget;

    public EnderMan(EntityType<? extends EnderMan> param0, Level param1) {
        super(param0, param1);
        this.maxUpStep = 1.0F;
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new EnderMan.EndermanFreezeWhenLookedAt(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0, 0.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(10, new EnderMan.EndermanLeaveBlockGoal(this));
        this.goalSelector.addGoal(11, new EnderMan.EndermanTakeBlockGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(2, new EnderMan.EndermanLookForPlayerGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Endermite.class, 10, true, false, ENDERMITE_SELECTOR));
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 40.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3F)
            .add(Attributes.ATTACK_DAMAGE, 7.0)
            .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    public void setTarget(@Nullable LivingEntity param0) {
        super.setTarget(param0);
        AttributeInstance var0 = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (param0 == null) {
            this.targetChangeTime = 0;
            this.entityData.set(DATA_CREEPY, false);
            this.entityData.set(DATA_STARED_AT, false);
            var0.removeModifier(SPEED_MODIFIER_ATTACKING);
        } else {
            this.targetChangeTime = this.tickCount;
            this.entityData.set(DATA_CREEPY, true);
            if (!var0.hasModifier(SPEED_MODIFIER_ATTACKING)) {
                var0.addTransientModifier(SPEED_MODIFIER_ATTACKING);
            }
        }

    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CARRY_STATE, Optional.empty());
        this.entityData.define(DATA_CREEPY, false);
        this.entityData.define(DATA_STARED_AT, false);
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.randomValue(this.random));
    }

    @Override
    public void setRemainingPersistentAngerTime(int param0) {
        this.remainingPersistentAngerTime = param0;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID param0) {
        this.persistentAngerTarget = param0;
    }

    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    public void playStareSound() {
        if (this.tickCount >= this.lastStareSound + 400) {
            this.lastStareSound = this.tickCount;
            if (!this.isSilent()) {
                this.level.playLocalSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ENDERMAN_STARE, this.getSoundSource(), 2.5F, 1.0F, false);
            }
        }

    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_CREEPY.equals(param0) && this.hasBeenStaredAt() && this.level.isClientSide) {
            this.playStareSound();
        }

        super.onSyncedDataUpdated(param0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        BlockState var0 = this.getCarriedBlock();
        if (var0 != null) {
            param0.put("carriedBlockState", NbtUtils.writeBlockState(var0));
        }

        this.addPersistentAngerSaveData(param0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        BlockState var0 = null;
        if (param0.contains("carriedBlockState", 10)) {
            var0 = NbtUtils.readBlockState(param0.getCompound("carriedBlockState"));
            if (var0.isAir()) {
                var0 = null;
            }
        }

        this.setCarriedBlock(var0);
        this.readPersistentAngerSaveData((ServerLevel)this.level, param0);
    }

    private boolean isLookingAtMe(Player param0) {
        ItemStack var0 = param0.inventory.armor.get(3);
        if (var0.getItem() == Blocks.CARVED_PUMPKIN.asItem()) {
            return false;
        } else {
            Vec3 var1 = param0.getViewVector(1.0F).normalize();
            Vec3 var2 = new Vec3(this.getX() - param0.getX(), this.getEyeY() - param0.getEyeY(), this.getZ() - param0.getZ());
            double var3 = var2.length();
            var2 = var2.normalize();
            double var4 = var1.dot(var2);
            return var4 > 1.0 - 0.025 / var3 ? param0.canSee(this) : false;
        }
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return 2.55F;
    }

    @Override
    public void aiStep() {
        if (this.level.isClientSide) {
            for(int var0 = 0; var0 < 2; ++var0) {
                this.level
                    .addParticle(
                        ParticleTypes.PORTAL,
                        this.getRandomX(0.5),
                        this.getRandomY() - 0.25,
                        this.getRandomZ(0.5),
                        (this.random.nextDouble() - 0.5) * 2.0,
                        -this.random.nextDouble(),
                        (this.random.nextDouble() - 0.5) * 2.0
                    );
            }
        }

        this.jumping = false;
        if (!this.level.isClientSide) {
            this.updatePersistentAnger((ServerLevel)this.level, true);
        }

        super.aiStep();
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    protected void customServerAiStep() {
        if (this.level.isDay() && this.tickCount >= this.targetChangeTime + 600) {
            float var0 = this.getBrightness();
            if (var0 > 0.5F && this.level.canSeeSky(this.blockPosition()) && this.random.nextFloat() * 30.0F < (var0 - 0.4F) * 2.0F) {
                this.setTarget(null);
                this.teleport();
            }
        }

        super.customServerAiStep();
    }

    protected boolean teleport() {
        if (!this.level.isClientSide() && this.isAlive()) {
            double var0 = this.getX() + (this.random.nextDouble() - 0.5) * 64.0;
            double var1 = this.getY() + (double)(this.random.nextInt(64) - 32);
            double var2 = this.getZ() + (this.random.nextDouble() - 0.5) * 64.0;
            return this.teleport(var0, var1, var2);
        } else {
            return false;
        }
    }

    private boolean teleportTowards(Entity param0) {
        Vec3 var0 = new Vec3(this.getX() - param0.getX(), this.getY(0.5) - param0.getEyeY(), this.getZ() - param0.getZ());
        var0 = var0.normalize();
        double var1 = 16.0;
        double var2 = this.getX() + (this.random.nextDouble() - 0.5) * 8.0 - var0.x * 16.0;
        double var3 = this.getY() + (double)(this.random.nextInt(16) - 8) - var0.y * 16.0;
        double var4 = this.getZ() + (this.random.nextDouble() - 0.5) * 8.0 - var0.z * 16.0;
        return this.teleport(var2, var3, var4);
    }

    private boolean teleport(double param0, double param1, double param2) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos(param0, param1, param2);

        while(var0.getY() > 0 && !this.level.getBlockState(var0).getMaterial().blocksMotion()) {
            var0.move(Direction.DOWN);
        }

        BlockState var1 = this.level.getBlockState(var0);
        boolean var2 = var1.getMaterial().blocksMotion();
        boolean var3 = var1.getFluidState().is(FluidTags.WATER);
        if (var2 && !var3) {
            boolean var4 = this.randomTeleport(param0, param1, param2, true);
            if (var4 && !this.isSilent()) {
                this.level.playSound(null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
                this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }

            return var4;
        } else {
            return false;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isCreepy() ? SoundEvents.ENDERMAN_SCREAM : SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource param0, int param1, boolean param2) {
        super.dropCustomDeathLoot(param0, param1, param2);
        BlockState var0 = this.getCarriedBlock();
        if (var0 != null) {
            this.spawnAtLocation(var0.getBlock());
        }

    }

    public void setCarriedBlock(@Nullable BlockState param0) {
        this.entityData.set(DATA_CARRY_STATE, Optional.ofNullable(param0));
    }

    @Nullable
    public BlockState getCarriedBlock() {
        return this.entityData.get(DATA_CARRY_STATE).orElse(null);
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else if (param0 instanceof IndirectEntityDamageSource) {
            for(int var0 = 0; var0 < 64; ++var0) {
                if (this.teleport()) {
                    return true;
                }
            }

            return false;
        } else {
            boolean var1 = super.hurt(param0, param1);
            if (!this.level.isClientSide() && this.random.nextInt(10) != 0) {
                this.teleport();
            }

            return var1;
        }
    }

    public boolean isCreepy() {
        return this.entityData.get(DATA_CREEPY);
    }

    public boolean hasBeenStaredAt() {
        return this.entityData.get(DATA_STARED_AT);
    }

    public void setBeingStaredAt() {
        this.entityData.set(DATA_STARED_AT, true);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.getCarriedBlock() != null;
    }

    static class EndermanFreezeWhenLookedAt extends Goal {
        private final EnderMan enderman;
        private LivingEntity target;

        public EndermanFreezeWhenLookedAt(EnderMan param0) {
            this.enderman = param0;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            this.target = this.enderman.getTarget();
            if (!(this.target instanceof Player)) {
                return false;
            } else {
                double var0 = this.target.distanceToSqr(this.enderman);
                return var0 > 256.0 ? false : this.enderman.isLookingAtMe((Player)this.target);
            }
        }

        @Override
        public void start() {
            this.enderman.getNavigation().stop();
        }

        @Override
        public void tick() {
            this.enderman.getLookControl().setLookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
        }
    }

    static class EndermanLeaveBlockGoal extends Goal {
        private final EnderMan enderman;

        public EndermanLeaveBlockGoal(EnderMan param0) {
            this.enderman = param0;
        }

        @Override
        public boolean canUse() {
            if (this.enderman.getCarriedBlock() == null) {
                return false;
            } else if (!this.enderman.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return false;
            } else {
                return this.enderman.getRandom().nextInt(2000) == 0;
            }
        }

        @Override
        public void tick() {
            Random var0 = this.enderman.getRandom();
            LevelAccessor var1 = this.enderman.level;
            int var2 = Mth.floor(this.enderman.getX() - 1.0 + var0.nextDouble() * 2.0);
            int var3 = Mth.floor(this.enderman.getY() + var0.nextDouble() * 2.0);
            int var4 = Mth.floor(this.enderman.getZ() - 1.0 + var0.nextDouble() * 2.0);
            BlockPos var5 = new BlockPos(var2, var3, var4);
            BlockState var6 = var1.getBlockState(var5);
            BlockPos var7 = var5.below();
            BlockState var8 = var1.getBlockState(var7);
            BlockState var9 = this.enderman.getCarriedBlock();
            if (var9 != null && this.canPlaceBlock(var1, var5, var9, var6, var8, var7)) {
                var1.setBlock(var5, var9, 3);
                this.enderman.setCarriedBlock(null);
            }

        }

        private boolean canPlaceBlock(LevelReader param0, BlockPos param1, BlockState param2, BlockState param3, BlockState param4, BlockPos param5) {
            return param3.isAir() && !param4.isAir() && param4.isCollisionShapeFullBlock(param0, param5) && param2.canSurvive(param0, param1);
        }
    }

    static class EndermanLookForPlayerGoal extends NearestAttackableTargetGoal<Player> {
        private final EnderMan enderman;
        private Player pendingTarget;
        private int aggroTime;
        private int teleportTime;
        private final TargetingConditions startAggroTargetConditions;
        private final TargetingConditions continueAggroTargetConditions = new TargetingConditions().allowUnseeable();

        public EndermanLookForPlayerGoal(EnderMan param0) {
            super(param0, Player.class, false);
            this.enderman = param0;
            this.startAggroTargetConditions = new TargetingConditions()
                .range(this.getFollowDistance())
                .selector(param1 -> param0.isLookingAtMe((Player)param1));
        }

        @Override
        public boolean canUse() {
            this.pendingTarget = this.enderman.level.getNearestPlayer(this.startAggroTargetConditions, this.enderman);
            return this.pendingTarget != null;
        }

        @Override
        public void start() {
            this.aggroTime = 5;
            this.teleportTime = 0;
            this.enderman.setBeingStaredAt();
        }

        @Override
        public void stop() {
            this.pendingTarget = null;
            super.stop();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.pendingTarget != null) {
                if (!this.enderman.isLookingAtMe(this.pendingTarget)) {
                    return false;
                } else {
                    this.enderman.lookAt(this.pendingTarget, 10.0F, 10.0F);
                    return true;
                }
            } else {
                return this.target != null && this.continueAggroTargetConditions.test(this.enderman, this.target) ? true : super.canContinueToUse();
            }
        }

        @Override
        public void tick() {
            if (this.enderman.getTarget() == null) {
                super.setTarget(null);
            }

            if (this.pendingTarget != null) {
                if (--this.aggroTime <= 0) {
                    this.target = this.pendingTarget;
                    this.pendingTarget = null;
                    super.start();
                }
            } else {
                if (this.target != null && !this.enderman.isPassenger()) {
                    if (this.enderman.isLookingAtMe((Player)this.target)) {
                        if (this.target.distanceToSqr(this.enderman) < 16.0) {
                            this.enderman.teleport();
                        }

                        this.teleportTime = 0;
                    } else if (this.target.distanceToSqr(this.enderman) > 256.0 && this.teleportTime++ >= 30 && this.enderman.teleportTowards(this.target)) {
                        this.teleportTime = 0;
                    }
                }

                super.tick();
            }

        }
    }

    static class EndermanTakeBlockGoal extends Goal {
        private final EnderMan enderman;

        public EndermanTakeBlockGoal(EnderMan param0) {
            this.enderman = param0;
        }

        @Override
        public boolean canUse() {
            if (this.enderman.getCarriedBlock() != null) {
                return false;
            } else if (!this.enderman.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return false;
            } else {
                return this.enderman.getRandom().nextInt(20) == 0;
            }
        }

        @Override
        public void tick() {
            Random var0 = this.enderman.getRandom();
            Level var1 = this.enderman.level;
            int var2 = Mth.floor(this.enderman.getX() - 2.0 + var0.nextDouble() * 4.0);
            int var3 = Mth.floor(this.enderman.getY() + var0.nextDouble() * 3.0);
            int var4 = Mth.floor(this.enderman.getZ() - 2.0 + var0.nextDouble() * 4.0);
            BlockPos var5 = new BlockPos(var2, var3, var4);
            BlockState var6 = var1.getBlockState(var5);
            Block var7 = var6.getBlock();
            Vec3 var8 = new Vec3((double)Mth.floor(this.enderman.getX()) + 0.5, (double)var3 + 0.5, (double)Mth.floor(this.enderman.getZ()) + 0.5);
            Vec3 var9 = new Vec3((double)var2 + 0.5, (double)var3 + 0.5, (double)var4 + 0.5);
            BlockHitResult var10 = var1.clip(new ClipContext(var8, var9, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this.enderman));
            boolean var11 = var10.getBlockPos().equals(var5);
            if (var7.is(BlockTags.ENDERMAN_HOLDABLE) && var11) {
                this.enderman.setCarriedBlock(var6);
                var1.removeBlock(var5, false);
            }

        }
    }
}
