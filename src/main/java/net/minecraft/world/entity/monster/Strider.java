package net.minecraft.world.entity.monster;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
        return param1.getBlockState(param3.above()).isAir();
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
        return this.entityData.get(DATA_SUFFOCATING);
    }

    @Override
    public boolean canFloatInLava() {
        return true;
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
    public double getRideHeight() {
        float var0 = Math.min(0.25F, this.animationSpeed);
        float var1 = this.animationPosition;
        return 1.4 + (double)(0.12F * Mth.cos(var1 * 1.5F) * 2.0F * var0);
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
    public void tick() {
        if (this.temptGoal != null && this.temptGoal.isRunning() && this.random.nextInt(100) == 0) {
            this.playSound(SoundEvents.STRIDER_HAPPY, 1.0F, this.getVoicePitch());
        }

        if (this.panicGoal != null && this.panicGoal.isRunning() && this.random.nextInt(60) == 0) {
            this.playSound(SoundEvents.STRIDER_RETREAT, 1.0F, this.getVoicePitch());
        }

        BlockState var0 = this.level.getBlockState(this.blockPosition());
        BlockState var1 = this.getBlockStateOn();
        boolean var2 = var0.is(BlockTags.STRIDER_WARM_BLOCKS) || var1.is(BlockTags.STRIDER_WARM_BLOCKS);
        this.setSuffocating(!var2 && !this.isPassenger());
        if (this.isInLava()) {
            this.onGround = true;
        }

        super.tick();
        this.floatStrider();
        this.checkInsideBlocks();
    }

    @Override
    protected boolean shouldPassengersInheritMalus() {
        return true;
    }

    public float getLavaLevel() {
        AABB var0 = this.getBoundingBox();
        float var1 = -1.0F;
        float var2 = 0.0F;
        BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos(var0.getCenter().x, var0.minY + 0.5, var0.getCenter().z);

        for(FluidState var4 = this.level.getFluidState(var3); var4.is(FluidTags.LAVA); var4 = this.level.getFluidState(var3)) {
            var1 = (float)var3.getY();
            var2 = var4.getHeight(this.level, var3);
            var3.move(0, 1, 0);
        }

        return var1 + var2;
    }

    private void floatStrider() {
        Vec3 var0 = this.getDeltaMovement();
        AABB var1 = this.getBoundingBox();
        if (this.isInLava()) {
            boolean var2 = var1.minY <= (double)this.getLavaLevel() - (this.isBaby() ? 0.0 : 0.25);
            this.setDeltaMovement(var0.x, var2 ? var0.y + 0.01 : -0.01, var0.z);
        }

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.15F).add(Attributes.FOLLOW_RANGE, 16.0);
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
        return this.getPassengers().isEmpty() && !this.isUnderLiquid(FluidTags.LAVA);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isInWaterRainOrBubble()) {
            this.hurt(DamageSource.DROWN, 1.0F);
        }

        super.customServerAiStep();
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return this.isInLava() || super.isNoGravity();
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
    public boolean mobInteract(Player param0, InteractionHand param1) {
        boolean var0 = this.isFood(param0.getItemInHand(param1));
        if (!super.mobInteract(param0, param1)) {
            if (this.isSaddled() && !this.isVehicle() && !this.isBaby()) {
                if (!this.level.isClientSide) {
                    param0.startRiding(this);
                }

                return true;
            } else {
                ItemStack var1 = param0.getItemInHand(param1);
                return var1.getItem() == Items.SADDLE && var1.interactEnemy(param0, this, param1);
            }
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

            return false;
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        LevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        Strider.StriderGroupData.Rider var0;
        if (param3 instanceof Strider.StriderGroupData) {
            var0 = ((Strider.StriderGroupData)param3).rider;
        } else {
            if (this.random.nextInt(30) == 0) {
                var0 = Strider.StriderGroupData.Rider.PIGLIN_RIDER;
            } else if (this.random.nextInt(10) == 0) {
                var0 = Strider.StriderGroupData.Rider.BABY_RIDER;
            } else {
                var0 = Strider.StriderGroupData.Rider.NO_RIDER;
            }

            param3 = new Strider.StriderGroupData(var0);
            ((AgableMob.AgableMobGroupData)param3).setBabySpawnChance(var0 == Strider.StriderGroupData.Rider.NO_RIDER ? 0.5F : 0.0F);
        }

        Mob var4 = null;
        if (var0 == Strider.StriderGroupData.Rider.BABY_RIDER) {
            Strider var5 = EntityType.STRIDER.create(param0.getLevel());
            if (var5 != null) {
                var4 = var5;
                var5.setAge(-24000);
            }
        } else if (var0 == Strider.StriderGroupData.Rider.PIGLIN_RIDER) {
            ZombifiedPiglin var6 = EntityType.ZOMBIFIED_PIGLIN.create(param0.getLevel());
            if (var6 != null) {
                var4 = var6;
                this.equipSaddle(null);
            }
        }

        if (var4 != null) {
            var4.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, 0.0F);
            var4.finalizeSpawn(param0, param1, MobSpawnType.JOCKEY, null, null);
            var4.startRiding(this, true);
            param0.addFreshEntity(var4);
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
            return this.level.getBlockState(param0).getBlock() == Blocks.LAVA || super.isStableDestination(param0);
        }
    }
}
