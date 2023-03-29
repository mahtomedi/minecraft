package net.minecraft.world.entity.monster;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class Strider extends Animal implements ItemSteerable, Saddleable {
    private static final UUID SUFFOCATING_MODIFIER_UUID = UUID.fromString("9e362924-01de-4ddd-a2b2-d0f7a405a174");
    private static final AttributeModifier SUFFOCATING_MODIFIER = new AttributeModifier(
        SUFFOCATING_MODIFIER_UUID, "Strider suffocating modifier", -0.34F, AttributeModifier.Operation.MULTIPLY_BASE
    );
    private static final float SUFFOCATE_STEERING_MODIFIER = 0.35F;
    private static final float STEERING_MODIFIER = 0.55F;
    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WARPED_FUNGUS);
    private static final Ingredient TEMPT_ITEMS = Ingredient.of(Items.WARPED_FUNGUS, Items.WARPED_FUNGUS_ON_A_STICK);
    private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SUFFOCATING = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SADDLE_ID = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
    private final ItemBasedSteering steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME, DATA_SADDLE_ID);
    @Nullable
    private TemptGoal temptGoal;
    @Nullable
    private PanicGoal panicGoal;

    public Strider(EntityType<? extends Strider> param0, Level param1) {
        super(param0, param1);
        this.blocksBuilding = true;
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
    }

    public static boolean checkStriderSpawnRules(EntityType<Strider> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, RandomSource param4) {
        BlockPos.MutableBlockPos var0 = param3.mutable();

        do {
            var0.move(Direction.UP);
        } while(param1.getFluidState(var0).is(FluidTags.LAVA));

        return param1.getBlockState(var0).isAir();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_BOOST_TIME.equals(param0) && this.level.isClientSide) {
            this.steering.onSynced();
        }

        super.onSyncedDataUpdated(param0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BOOST_TIME, 0);
        this.entityData.define(DATA_SUFFOCATING, false);
        this.entityData.define(DATA_SADDLE_ID, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        this.steering.addAdditionalSaveData(param0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.steering.readAdditionalSaveData(param0);
    }

    @Override
    public boolean isSaddled() {
        return this.steering.hasSaddle();
    }

    @Override
    public boolean isSaddleable() {
        return this.isAlive() && !this.isBaby();
    }

    @Override
    public void equipSaddle(@Nullable SoundSource param0) {
        this.steering.setSaddle(true);
        if (param0 != null) {
            this.level.playSound(null, this, SoundEvents.STRIDER_SADDLE, param0, 0.5F, 1.0F);
        }

    }

    @Override
    protected void registerGoals() {
        this.panicGoal = new PanicGoal(this, 1.65);
        this.goalSelector.addGoal(1, this.panicGoal);
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.temptGoal = new TemptGoal(this, 1.4, TEMPT_ITEMS, false);
        this.goalSelector.addGoal(3, this.temptGoal);
        this.goalSelector.addGoal(4, new Strider.StriderGoToLavaGoal(this, 1.0));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0, 60));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Strider.class, 8.0F));
    }

    public void setSuffocating(boolean param0) {
        this.entityData.set(DATA_SUFFOCATING, param0);
        AttributeInstance var0 = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (var0 != null) {
            var0.removeModifier(SUFFOCATING_MODIFIER_UUID);
            if (param0) {
                var0.addTransientModifier(SUFFOCATING_MODIFIER);
            }
        }

    }

    public boolean isSuffocating() {
        return this.entityData.get(DATA_SUFFOCATING);
    }

    @Override
    public boolean canStandOnFluid(FluidState param0) {
        return param0.is(FluidTags.LAVA);
    }

    @Override
    public double getPassengersRidingOffset() {
        float var0 = Math.min(0.25F, this.walkAnimation.speed());
        float var1 = this.walkAnimation.position();
        return (double)this.getBbHeight() - 0.19 + (double)(0.12F * Mth.cos(var1 * 1.5F) * 2.0F * var0);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader param0) {
        return param0.isUnobstructed(this);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity var2 = this.getFirstPassenger();
        if (var2 instanceof Player var0
            && (var0.getMainHandItem().is(Items.WARPED_FUNGUS_ON_A_STICK) || var0.getOffhandItem().is(Items.WARPED_FUNGUS_ON_A_STICK))) {
            return var0;
        }

        return null;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity param0) {
        Vec3[] var0 = new Vec3[]{
            getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)param0.getBbWidth(), param0.getYRot()),
            getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)param0.getBbWidth(), param0.getYRot() - 22.5F),
            getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)param0.getBbWidth(), param0.getYRot() + 22.5F),
            getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)param0.getBbWidth(), param0.getYRot() - 45.0F),
            getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)param0.getBbWidth(), param0.getYRot() + 45.0F)
        };
        Set<BlockPos> var1 = Sets.newLinkedHashSet();
        double var2 = this.getBoundingBox().maxY;
        double var3 = this.getBoundingBox().minY - 0.5;
        BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();

        for(Vec3 var5 : var0) {
            var4.set(this.getX() + var5.x, var2, this.getZ() + var5.z);

            for(double var6 = var2; var6 > var3; --var6) {
                var1.add(var4.immutable());
                var4.move(Direction.DOWN);
            }
        }

        for(BlockPos var7 : var1) {
            if (!this.level.getFluidState(var7).is(FluidTags.LAVA)) {
                double var8 = this.level.getBlockFloorHeight(var7);
                if (DismountHelper.isBlockFloorValid(var8)) {
                    Vec3 var9 = Vec3.upFromBottomCenterOf(var7, var8);

                    for(Pose var10 : param0.getDismountPoses()) {
                        AABB var11 = param0.getLocalBoundsForPose(var10);
                        if (DismountHelper.canDismountTo(this.level, param0, var11.move(var9))) {
                            param0.setPose(var10);
                            return var9;
                        }
                    }
                }
            }
        }

        return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
    }

    @Override
    protected void tickRidden(Player param0, Vec3 param1) {
        this.setRot(param0.getYRot(), param0.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        this.steering.tickBoost();
        super.tickRidden(param0, param1);
    }

    @Override
    protected Vec3 getRiddenInput(Player param0, Vec3 param1) {
        return new Vec3(0.0, 0.0, 1.0);
    }

    @Override
    protected float getRiddenSpeed(Player param0) {
        return (float)(this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (double)(this.isSuffocating() ? 0.35F : 0.55F) * (double)this.steering.boostFactor());
    }

    @Override
    protected float nextStep() {
        return this.moveDist + 0.6F;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(this.isInLava() ? SoundEvents.STRIDER_STEP_LAVA : SoundEvents.STRIDER_STEP, 1.0F, 1.0F);
    }

    @Override
    public boolean boost() {
        return this.steering.boost(this.getRandom());
    }

    @Override
    protected void checkFallDamage(double param0, boolean param1, BlockState param2, BlockPos param3) {
        this.checkInsideBlocks();
        if (this.isInLava()) {
            this.resetFallDistance();
        } else {
            super.checkFallDamage(param0, param1, param2, param3);
        }
    }

    @Override
    public void tick() {
        if (this.isBeingTempted() && this.random.nextInt(140) == 0) {
            this.playSound(SoundEvents.STRIDER_HAPPY, 1.0F, this.getVoicePitch());
        } else if (this.isPanicking() && this.random.nextInt(60) == 0) {
            this.playSound(SoundEvents.STRIDER_RETREAT, 1.0F, this.getVoicePitch());
        }

        if (!this.isNoAi()) {
            boolean var2;
            boolean var10000;
            label36: {
                BlockState var0 = this.level.getBlockState(this.blockPosition());
                BlockState var1 = this.getBlockStateOnLegacy();
                var2 = var0.is(BlockTags.STRIDER_WARM_BLOCKS) || var1.is(BlockTags.STRIDER_WARM_BLOCKS) || this.getFluidHeight(FluidTags.LAVA) > 0.0;
                Entity var6 = this.getVehicle();
                if (var6 instanceof Strider var3 && var3.isSuffocating()) {
                    var10000 = true;
                    break label36;
                }

                var10000 = false;
            }

            boolean var4 = var10000;
            this.setSuffocating(!var2 || var4);
        }

        super.tick();
        this.floatStrider();
        this.checkInsideBlocks();
    }

    private boolean isPanicking() {
        return this.panicGoal != null && this.panicGoal.isRunning();
    }

    private boolean isBeingTempted() {
        return this.temptGoal != null && this.temptGoal.isRunning();
    }

    @Override
    protected boolean shouldPassengersInheritMalus() {
        return true;
    }

    private void floatStrider() {
        if (this.isInLava()) {
            CollisionContext var0 = CollisionContext.of(this);
            if (var0.isAbove(LiquidBlock.STABLE_SHAPE, this.blockPosition(), true)
                && !this.level.getFluidState(this.blockPosition().above()).is(FluidTags.LAVA)) {
                this.onGround = true;
            } else {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5).add(0.0, 0.05, 0.0));
            }
        }

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.175F).add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return !this.isPanicking() && !this.isBeingTempted() ? SoundEvents.STRIDER_AMBIENT : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.STRIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STRIDER_DEATH;
    }

    @Override
    protected boolean canAddPassenger(Entity param0) {
        return !this.isVehicle() && !this.isEyeInFluid(FluidTags.LAVA);
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected PathNavigation createNavigation(Level param0) {
        return new Strider.StriderPathNavigation(this, param0);
    }

    @Override
    public float getWalkTargetValue(BlockPos param0, LevelReader param1) {
        if (param1.getBlockState(param0).getFluidState().is(FluidTags.LAVA)) {
            return 10.0F;
        } else {
            return this.isInLava() ? Float.NEGATIVE_INFINITY : 0.0F;
        }
    }

    @Nullable
    public Strider getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        return EntityType.STRIDER.create(param0);
    }

    @Override
    public boolean isFood(ItemStack param0) {
        return FOOD_ITEMS.test(param0);
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.isSaddled()) {
            this.spawnAtLocation(Items.SADDLE);
        }

    }

    @Override
    public InteractionResult mobInteract(Player param0, InteractionHand param1) {
        boolean var0 = this.isFood(param0.getItemInHand(param1));
        if (!var0 && this.isSaddled() && !this.isVehicle() && !param0.isSecondaryUseActive()) {
            if (!this.level.isClientSide) {
                param0.startRiding(this);
            }

            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else {
            InteractionResult var1 = super.mobInteract(param0, param1);
            if (!var1.consumesAction()) {
                ItemStack var2 = param0.getItemInHand(param1);
                return var2.is(Items.SADDLE) ? var2.interactLivingEntity(param0, this, param1) : InteractionResult.PASS;
            } else {
                if (var0 && !this.isSilent()) {
                    this.level
                        .playSound(
                            null,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            SoundEvents.STRIDER_EAT,
                            this.getSoundSource(),
                            1.0F,
                            1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
                        );
                }

                return var1;
            }
        }
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, (double)(0.6F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        if (this.isBaby()) {
            return super.finalizeSpawn(param0, param1, param2, param3, param4);
        } else {
            RandomSource var0 = param0.getRandom();
            if (var0.nextInt(30) == 0) {
                Mob var1 = EntityType.ZOMBIFIED_PIGLIN.create(param0.getLevel());
                if (var1 != null) {
                    param3 = this.spawnJockey(param0, param1, var1, new Zombie.ZombieGroupData(Zombie.getSpawnAsBabyOdds(var0), false));
                    var1.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
                    this.equipSaddle(null);
                }
            } else if (var0.nextInt(10) == 0) {
                AgeableMob var2 = EntityType.STRIDER.create(param0.getLevel());
                if (var2 != null) {
                    var2.setAge(-24000);
                    param3 = this.spawnJockey(param0, param1, var2, null);
                }
            } else {
                param3 = new AgeableMob.AgeableMobGroupData(0.5F);
            }

            return super.finalizeSpawn(param0, param1, param2, param3, param4);
        }
    }

    private SpawnGroupData spawnJockey(ServerLevelAccessor param0, DifficultyInstance param1, Mob param2, @Nullable SpawnGroupData param3) {
        param2.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
        param2.finalizeSpawn(param0, param1, MobSpawnType.JOCKEY, param3, null);
        param2.startRiding(this, true);
        return new AgeableMob.AgeableMobGroupData(0.0F);
    }

    static class StriderGoToLavaGoal extends MoveToBlockGoal {
        private final Strider strider;

        StriderGoToLavaGoal(Strider param0, double param1) {
            super(param0, param1, 8, 2);
            this.strider = param0;
        }

        @Override
        public BlockPos getMoveToTarget() {
            return this.blockPos;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.strider.isInLava() && this.isValidTarget(this.strider.level, this.blockPos);
        }

        @Override
        public boolean canUse() {
            return !this.strider.isInLava() && super.canUse();
        }

        @Override
        public boolean shouldRecalculatePath() {
            return this.tryTicks % 20 == 0;
        }

        @Override
        protected boolean isValidTarget(LevelReader param0, BlockPos param1) {
            return param0.getBlockState(param1).is(Blocks.LAVA)
                && param0.getBlockState(param1.above()).isPathfindable(param0, param1, PathComputationType.LAND);
        }
    }

    static class StriderPathNavigation extends GroundPathNavigation {
        StriderPathNavigation(Strider param0, Level param1) {
            super(param0, param1);
        }

        @Override
        protected PathFinder createPathFinder(int param0) {
            this.nodeEvaluator = new WalkNodeEvaluator();
            this.nodeEvaluator.setCanPassDoors(true);
            return new PathFinder(this.nodeEvaluator, param0);
        }

        @Override
        protected boolean hasValidPathType(BlockPathTypes param0) {
            return param0 != BlockPathTypes.LAVA && param0 != BlockPathTypes.DAMAGE_FIRE && param0 != BlockPathTypes.DANGER_FIRE
                ? super.hasValidPathType(param0)
                : true;
        }

        @Override
        public boolean isStableDestination(BlockPos param0) {
            return this.level.getBlockState(param0).is(Blocks.LAVA) || super.isStableDestination(param0);
        }
    }
}
