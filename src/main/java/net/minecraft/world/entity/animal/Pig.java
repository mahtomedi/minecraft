package net.minecraft.world.entity.animal;

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
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class Pig extends Animal implements ItemSteerable, Saddleable {
    private static final EntityDataAccessor<Boolean> DATA_SADDLE_ID = SynchedEntityData.defineId(Pig.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Pig.class, EntityDataSerializers.INT);
    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.CARROT, Items.POTATO, Items.BEETROOT);
    private final ItemBasedSteering steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME, DATA_SADDLE_ID);

    public Pig(EntityType<? extends Pig> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, Ingredient.of(Items.CARROT_ON_A_STICK), false));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, FOOD_ITEMS, false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (this.isSaddled()) {
            Entity var2 = this.getFirstPassenger();
            if (var2 instanceof Player var0 && var0.isHolding(Items.CARROT_ON_A_STICK)) {
                return var0;
            }
        }

        return super.getControllingPassenger();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (DATA_BOOST_TIME.equals(param0) && this.level().isClientSide) {
            this.steering.onSynced();
        }

        super.onSyncedDataUpdated(param0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SADDLE_ID, false);
        this.entityData.define(DATA_BOOST_TIME, 0);
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
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIG_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.PIG_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIG_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.PIG_STEP, 0.15F, 1.0F);
    }

    @Override
    public InteractionResult mobInteract(Player param0, InteractionHand param1) {
        boolean var0 = this.isFood(param0.getItemInHand(param1));
        if (!var0 && this.isSaddled() && !this.isVehicle() && !param0.isSecondaryUseActive()) {
            if (!this.level().isClientSide) {
                param0.startRiding(this);
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            InteractionResult var1 = super.mobInteract(param0, param1);
            if (!var1.consumesAction()) {
                ItemStack var2 = param0.getItemInHand(param1);
                return var2.is(Items.SADDLE) ? var2.interactLivingEntity(param0, this, param1) : InteractionResult.PASS;
            } else {
                return var1;
            }
        }
    }

    @Override
    public boolean isSaddleable() {
        return this.isAlive() && !this.isBaby();
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.isSaddled()) {
            this.spawnAtLocation(Items.SADDLE);
        }

    }

    @Override
    public boolean isSaddled() {
        return this.steering.hasSaddle();
    }

    @Override
    public void equipSaddle(@Nullable SoundSource param0) {
        this.steering.setSaddle(true);
        if (param0 != null) {
            this.level().playSound(null, this, SoundEvents.PIG_SADDLE, param0, 0.5F, 1.0F);
        }

    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity param0) {
        Direction var0 = this.getMotionDirection();
        if (var0.getAxis() == Direction.Axis.Y) {
            return super.getDismountLocationForPassenger(param0);
        } else {
            int[][] var1 = DismountHelper.offsetsForDirection(var0);
            BlockPos var2 = this.blockPosition();
            BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();

            for(Pose var4 : param0.getDismountPoses()) {
                AABB var5 = param0.getLocalBoundsForPose(var4);

                for(int[] var6 : var1) {
                    var3.set(var2.getX() + var6[0], var2.getY(), var2.getZ() + var6[1]);
                    double var7 = this.level().getBlockFloorHeight(var3);
                    if (DismountHelper.isBlockFloorValid(var7)) {
                        Vec3 var8 = Vec3.upFromBottomCenterOf(var3, var7);
                        if (DismountHelper.canDismountTo(this.level(), param0, var5.move(var8))) {
                            param0.setPose(var4);
                            return var8;
                        }
                    }
                }
            }

            return super.getDismountLocationForPassenger(param0);
        }
    }

    @Override
    public void thunderHit(ServerLevel param0, LightningBolt param1) {
        if (param0.getDifficulty() != Difficulty.PEACEFUL) {
            ZombifiedPiglin var0 = EntityType.ZOMBIFIED_PIGLIN.create(param0);
            if (var0 != null) {
                var0.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
                var0.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                var0.setNoAi(this.isNoAi());
                var0.setBaby(this.isBaby());
                if (this.hasCustomName()) {
                    var0.setCustomName(this.getCustomName());
                    var0.setCustomNameVisible(this.isCustomNameVisible());
                }

                var0.setPersistenceRequired();
                param0.addFreshEntity(var0);
                this.discard();
            } else {
                super.thunderHit(param0, param1);
            }
        } else {
            super.thunderHit(param0, param1);
        }

    }

    @Override
    protected void tickRidden(Player param0, Vec3 param1) {
        super.tickRidden(param0, param1);
        this.setRot(param0.getYRot(), param0.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        this.steering.tickBoost();
    }

    @Override
    protected Vec3 getRiddenInput(Player param0, Vec3 param1) {
        return new Vec3(0.0, 0.0, 1.0);
    }

    @Override
    protected float getRiddenSpeed(Player param0) {
        return (float)(this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.225 * (double)this.steering.boostFactor());
    }

    @Override
    public boolean boost() {
        return this.steering.boost(this.getRandom());
    }

    @Nullable
    public Pig getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        return EntityType.PIG.create(param0);
    }

    @Override
    public boolean isFood(ItemStack param0) {
        return FOOD_ITEMS.test(param0);
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, (double)(0.6F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
    }

    @Override
    protected Vector3f getPassengerAttachmentPoint(Entity param0, EntityDimensions param1, float param2) {
        return new Vector3f(0.0F, param1.height - 0.03125F * param2, 0.0F);
    }
}
