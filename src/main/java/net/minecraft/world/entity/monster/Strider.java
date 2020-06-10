package net.minecraft.world.entity.monster;

import com.google.common.collect.Sets;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Strider extends Animal implements ItemSteerable, Saddleable {
    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WARPED_FUNGUS);
    private static final Ingredient TEMPT_ITEMS = Ingredient.of(Items.WARPED_FUNGUS, Items.WARPED_FUNGUS_ON_A_STICK);
    private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SUFFOCATING = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SADDLE_ID = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
    private final ItemBasedSteering steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME, DATA_SADDLE_ID);
    private TemptGoal temptGoal;
    private PanicGoal panicGoal;

    public Strider(EntityType<? extends Strider> param0, Level param1) {
        super(param0, param1);
        this.blocksBuilding = true;
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
    }

    public static boolean checkStriderSpawnRules(EntityType<Strider> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
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
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.temptGoal = new TemptGoal(this, 1.4, false, TEMPT_ITEMS);
        this.goalSelector.addGoal(4, this.temptGoal);
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0, 60));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Strider.class, 8.0F));
    }

    public void setSuffocating(boolean param0) {
        this.entityData.set(DATA_SUFFOCATING, param0);
    }

    public boolean isSuffocating() {
        return this.getVehicle() instanceof Strider ? ((Strider)this.getVehicle()).isSuffocating() : this.entityData.get(DATA_SUFFOCATING);
    }

    @Override
    public boolean canStandOnFluid(Fluid param0) {
        return param0.is(FluidTags.LAVA);
    }

    @Nullable
    @Override
    public AABB getCollideAgainstBox(Entity param0) {
        return param0.isPushable() ? param0.getBoundingBox() : null;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public double getPassengersRidingOffset() {
        float var0 = Math.min(0.25F, this.animationSpeed);
        float var1 = this.animationPosition;
        return (double)this.getBbHeight() - 0.2 + (double)(0.12F * Mth.cos(var1 * 1.5F) * 2.0F * var0);
    }

    @Override
    public boolean canBeControlledByRider() {
        Entity var0 = this.getControllingPassenger();
        if (!(var0 instanceof Player)) {
            return false;
        } else {
            Player var1 = (Player)var0;
            return var1.getMainHandItem().getItem() == Items.WARPED_FUNGUS_ON_A_STICK || var1.getOffhandItem().getItem() == Items.WARPED_FUNGUS_ON_A_STICK;
        }
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader param0) {
        return param0.isUnobstructed(this);
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity param0) {
        Vec3[] var0 = new Vec3[]{
            getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)param0.getBbWidth(), param0.yRot),
            getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)param0.getBbWidth(), param0.yRot - 22.5F),
            getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)param0.getBbWidth(), param0.yRot + 22.5F),
            getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)param0.getBbWidth(), param0.yRot - 45.0F),
            getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)param0.getBbWidth(), param0.yRot + 45.0F)
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
                for(Pose var8 : param0.getDismountPoses()) {
                    double var9 = this.level.getRelativeFloorHeight(var7);
                    if (DismountHelper.isFloorValid(var9)) {
                        AABB var10 = param0.getLocalBoundsForPose(var8);
                        Vec3 var11 = Vec3.upFromBottomCenterOf(var7, var9);
                        if (DismountHelper.canDismountTo(this.level, param0, var10.move(var11))) {
                            param0.setPose(var8);
                            return var11;
                        }
                    }
                }
            }
        }

        return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
    }

    @Override
    public void travel(Vec3 param0) {
        this.setSpeed(this.getMoveSpeed());
        this.travel(this, this.steering, param0);
    }

    public float getMoveSpeed() {
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (this.isSuffocating() ? 0.66F : 1.0F);
    }

    @Override
    public float getSteeringSpeed() {
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (this.isSuffocating() ? 0.23F : 0.55F);
    }

    @Override
    public void travelWithInput(Vec3 param0) {
        super.travel(param0);
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
            this.fallDistance = 0.0F;
        } else {
            super.checkFallDamage(param0, param1, param2, param3);
        }
    }

    @Override
    public void tick() {
        if (this.temptGoal != null && this.temptGoal.isRunning() && this.random.nextInt(100) == 0) {
            this.playSound(SoundEvents.STRIDER_HAPPY, 1.0F, this.getVoicePitch());
        }

        if (this.panicGoal != null && this.panicGoal.isRunning() && this.random.nextInt(60) == 0) {
            this.playSound(SoundEvents.STRIDER_RETREAT, 1.0F, this.getVoicePitch());
        }

        BlockState var0 = this.level.getBlockState(this.blockPosition());
        BlockState var1 = this.getBlockStateOn();
        boolean var2 = var0.is(BlockTags.STRIDER_WARM_BLOCKS) || var1.is(BlockTags.STRIDER_WARM_BLOCKS) || this.getFluidHeight(FluidTags.LAVA) > 0.0;
        this.setSuffocating(!var2);
        super.tick();
        this.floatStrider();
        this.checkInsideBlocks();
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
        return SoundEvents.STRIDER_AMBIENT;
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
        return this.getPassengers().isEmpty() && !this.isEyeInFluid(FluidTags.LAVA);
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
        return param1.getBlockState(param0).getFluidState().is(FluidTags.LAVA) ? 10.0F : 0.0F;
    }

    public Strider getBreedOffspring(AgableMob param0) {
        return EntityType.STRIDER.create(this.level);
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
        if (!var0 && this.isSaddled() && !this.isVehicle()) {
            if (!this.level.isClientSide) {
                param0.startRiding(this);
            }

            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else {
            InteractionResult var1 = super.mobInteract(param0, param1);
            if (!var1.consumesAction()) {
                ItemStack var2 = param0.getItemInHand(param1);
                return var2.getItem() == Items.SADDLE ? var2.interactLivingEntity(param0, this, param1) : InteractionResult.PASS;
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, (double)(0.6F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        LevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        SpawnGroupData var0 = null;
        Strider.StriderGroupData.Rider var1;
        if (param3 instanceof Strider.StriderGroupData) {
            var1 = ((Strider.StriderGroupData)param3).rider;
        } else if (!this.isBaby()) {
            if (this.random.nextInt(30) == 0) {
                var1 = Strider.StriderGroupData.Rider.PIGLIN_RIDER;
                var0 = new Zombie.ZombieGroupData(Zombie.getSpawnAsBabyOdds(this.random), false);
            } else if (this.random.nextInt(10) == 0) {
                var1 = Strider.StriderGroupData.Rider.BABY_RIDER;
            } else {
                var1 = Strider.StriderGroupData.Rider.NO_RIDER;
            }

            param3 = new Strider.StriderGroupData(var1);
            ((AgableMob.AgableMobGroupData)param3).setBabySpawnChance(var1 == Strider.StriderGroupData.Rider.NO_RIDER ? 0.5F : 0.0F);
        } else {
            var1 = Strider.StriderGroupData.Rider.NO_RIDER;
        }

        Mob var6 = null;
        if (var1 == Strider.StriderGroupData.Rider.BABY_RIDER) {
            Strider var7 = EntityType.STRIDER.create(param0.getLevel());
            if (var7 != null) {
                var6 = var7;
                var7.setAge(-24000);
            }
        } else if (var1 == Strider.StriderGroupData.Rider.PIGLIN_RIDER) {
            ZombifiedPiglin var8 = EntityType.ZOMBIFIED_PIGLIN.create(param0.getLevel());
            if (var8 != null) {
                var6 = var8;
                this.equipSaddle(null);
            }
        }

        if (var6 != null) {
            var6.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, 0.0F);
            var6.finalizeSpawn(param0, param1, MobSpawnType.JOCKEY, var0, null);
            var6.startRiding(this, true);
            param0.addFreshEntity(var6);
        }

        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    public static class StriderGroupData extends AgableMob.AgableMobGroupData {
        public final Strider.StriderGroupData.Rider rider;

        public StriderGroupData(Strider.StriderGroupData.Rider param0) {
            this.rider = param0;
        }

        public static enum Rider {
            NO_RIDER,
            BABY_RIDER,
            PIGLIN_RIDER;
        }
    }

    static class StriderPathNavigation extends GroundPathNavigation {
        StriderPathNavigation(Strider param0, Level param1) {
            super(param0, param1);
        }

        @Override
        protected PathFinder createPathFinder(int param0) {
            this.nodeEvaluator = new WalkNodeEvaluator();
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
