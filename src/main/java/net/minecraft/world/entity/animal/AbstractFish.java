package net.minecraft.world.entity.animal;

import java.util.Random;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFish extends WaterAnimal {
    private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(AbstractFish.class, EntityDataSerializers.BOOLEAN);

    public AbstractFish(EntityType<? extends AbstractFish> param0, Level param1) {
        super(param0, param1);
        this.moveControl = new AbstractFish.FishMoveControl(this);
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return param1.height * 0.65F;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 3.0);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.fromBucket();
    }

    public static boolean checkFishSpawnRules(
        EntityType<? extends AbstractFish> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4
    ) {
        return param1.getBlockState(param3).is(Blocks.WATER) && param1.getBlockState(param3.above()).is(Blocks.WATER);
    }

    @Override
    public boolean removeWhenFarAway(double param0) {
        return !this.fromBucket() && !this.hasCustomName();
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 8;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FROM_BUCKET, false);
    }

    private boolean fromBucket() {
        return this.entityData.get(FROM_BUCKET);
    }

    public void setFromBucket(boolean param0) {
        this.entityData.set(FROM_BUCKET, param0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putBoolean("FromBucket", this.fromBucket());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setFromBucket(param0.getBoolean("FromBucket"));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.6, 1.4, EntitySelector.NO_SPECTATORS::test));
        this.goalSelector.addGoal(4, new AbstractFish.FishSwimGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level param0) {
        return new WaterBoundPathNavigation(this, param0);
    }

    @Override
    public void travel(Vec3 param0) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(0.01F, param0);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
            if (this.getTarget() == null) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
            }
        } else {
            super.travel(param0);
        }

    }

    @Override
    public void aiStep() {
        if (!this.isInWater() && this.onGround && this.verticalCollision) {
            this.setDeltaMovement(
                this.getDeltaMovement()
                    .add((double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.05F), 0.4F, (double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.05F))
            );
            this.onGround = false;
            this.hasImpulse = true;
            this.playSound(this.getFlopSound(), this.getSoundVolume(), this.getVoicePitch());
        }

        super.aiStep();
    }

    @Override
    protected InteractionResult mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (var0.getItem() == Items.WATER_BUCKET && this.isAlive()) {
            this.playSound(SoundEvents.BUCKET_FILL_FISH, 1.0F, 1.0F);
            var0.shrink(1);
            ItemStack var1 = this.getBucketItemStack();
            this.saveToBucketTag(var1);
            if (!this.level.isClientSide) {
                CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)param0, var1);
            }

            if (var0.isEmpty()) {
                param0.setItemInHand(param1, var1);
            } else if (!param0.inventory.add(var1)) {
                param0.drop(var1, false);
            }

            this.remove();
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else {
            return super.mobInteract(param0, param1);
        }
    }

    protected void saveToBucketTag(ItemStack param0) {
        if (this.hasCustomName()) {
            param0.setHoverName(this.getCustomName());
        }

    }

    protected abstract ItemStack getBucketItemStack();

    protected boolean canRandomSwim() {
        return true;
    }

    protected abstract SoundEvent getFlopSound();

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.FISH_SWIM;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
    }

    static class FishMoveControl extends MoveControl {
        private final AbstractFish fish;

        FishMoveControl(AbstractFish param0) {
            super(param0);
            this.fish = param0;
        }

        @Override
        public void tick() {
            if (this.fish.isUnderLiquid(FluidTags.WATER)) {
                this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(0.0, 0.005, 0.0));
            }

            if (this.operation == MoveControl.Operation.MOVE_TO && !this.fish.getNavigation().isDone()) {
                float var0 = (float)(this.speedModifier * this.fish.getAttributeValue(Attributes.MOVEMENT_SPEED));
                this.fish.setSpeed(Mth.lerp(0.125F, this.fish.getSpeed(), var0));
                double var1 = this.wantedX - this.fish.getX();
                double var2 = this.wantedY - this.fish.getY();
                double var3 = this.wantedZ - this.fish.getZ();
                if (var2 != 0.0) {
                    double var4 = (double)Mth.sqrt(var1 * var1 + var2 * var2 + var3 * var3);
                    this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(0.0, (double)this.fish.getSpeed() * (var2 / var4) * 0.1, 0.0));
                }

                if (var1 != 0.0 || var3 != 0.0) {
                    float var5 = (float)(Mth.atan2(var3, var1) * 180.0F / (float)Math.PI) - 90.0F;
                    this.fish.yRot = this.rotlerp(this.fish.yRot, var5, 90.0F);
                    this.fish.yBodyRot = this.fish.yRot;
                }

            } else {
                this.fish.setSpeed(0.0F);
            }
        }
    }

    static class FishSwimGoal extends RandomSwimmingGoal {
        private final AbstractFish fish;

        public FishSwimGoal(AbstractFish param0) {
            super(param0, 1.0, 40);
            this.fish = param0;
        }

        @Override
        public boolean canUse() {
            return this.fish.canRandomSwim() && super.canUse();
        }
    }
}
