package net.minecraft.world.entity.animal.frog;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class Tadpole extends AbstractFish {
    @VisibleForTesting
    public static int ticksToBeFrog = Math.abs(-24000);
    private int age;
    protected static final ImmutableList<SensorType<? extends Sensor<? super Tadpole>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY
    );
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.PATH,
        MemoryModuleType.NEAREST_VISIBLE_ADULT
    );

    public Tadpole(EntityType<? extends AbstractFish> param0, Level param1) {
        super(param0, param1);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
    }

    @Override
    protected PathNavigation createNavigation(Level param0) {
        return new WaterBoundPathNavigation(this, param0);
    }

    @Override
    protected Brain.Provider<Tadpole> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> param0) {
        return TadpoleAi.makeBrain(this.brainProvider().makeBrain(param0));
    }

    @Override
    public Brain<Tadpole> getBrain() {
        return super.getBrain();
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.TADPOLE_FLOP;
    }

    @Override
    protected void customServerAiStep() {
        this.level.getProfiler().push("tadpoleBrain");
        this.getBrain().tick((ServerLevel)this.level, this);
        this.level.getProfiler().pop();
        this.level.getProfiler().push("tadpoleActivityUpdate");
        TadpoleAi.updateActivity(this);
        this.level.getProfiler().pop();
        super.customServerAiStep();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 1.0).add(Attributes.MAX_HEALTH, 6.0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            this.setAge(this.age + 1);
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("Age", this.age);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setAge(param0.getInt("Age"));
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.TADPOLE_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.TADPOLE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.TADPOLE_STEP, 0.15F, 1.0F);
    }

    @Override
    public InteractionResult mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (this.isFood(var0)) {
            this.feed(param0, var0);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else {
            return Bucketable.bucketMobPickup(param0, param1, this).orElse(super.mobInteract(param0, param1));
        }
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    public boolean fromBucket() {
        return true;
    }

    @Override
    public void setFromBucket(boolean param0) {
    }

    @Override
    public void saveToBucketTag(ItemStack param0) {
        Bucketable.saveDefaultDataToBucketTag(this, param0);
        CompoundTag var0 = param0.getOrCreateTag();
        var0.putInt("Age", this.getAge());
    }

    @Override
    public void loadFromBucketTag(CompoundTag param0) {
        Bucketable.loadDefaultDataFromBucketTag(this, param0);
        if (param0.contains("Age")) {
            this.setAge(param0.getInt("Age"));
        }

    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(Items.TADPOLE_BUCKET);
    }

    @Override
    public SoundEvent getPickupSound() {
        return SoundEvents.BUCKET_FILL_TADPOLE;
    }

    private boolean isFood(ItemStack param0) {
        return Frog.TEMPTATION_ITEM.test(param0);
    }

    private void feed(Player param0, ItemStack param1) {
        this.usePlayerItem(param0, param1);
        this.ageUp(AgeableMob.getSpeedUpSecondsWhenFeeding(this.getTicksLeftUntilAdult()));
        this.level.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
        this.gameEvent(GameEvent.MOB_INTERACT, this.eyeBlockPosition());
    }

    private void usePlayerItem(Player param0, ItemStack param1) {
        if (!param0.getAbilities().instabuild) {
            param1.shrink(1);
        }

    }

    private int getAge() {
        return this.age;
    }

    private void ageUp(int param0) {
        this.setAge(this.age + param0 * 20);
    }

    private void setAge(int param0) {
        this.age = param0;
        if (this.age >= ticksToBeFrog) {
            this.ageUp();
        }

    }

    private void ageUp() {
        Level var1 = this.level;
        if (var1 instanceof ServerLevel var0) {
            Frog var1x = EntityType.FROG.create(this.level);
            var1x.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            var1x.finalizeSpawn(var0, this.level.getCurrentDifficultyAt(var1x.blockPosition()), MobSpawnType.CONVERSION, null, null);
            var1x.setNoAi(this.isNoAi());
            if (this.hasCustomName()) {
                var1x.setCustomName(this.getCustomName());
                var1x.setCustomNameVisible(this.isCustomNameVisible());
            }

            var1x.setPersistenceRequired();
            this.playSound(SoundEvents.TADPOLE_GROW_UP, 0.15F, 1.0F);
            var0.addFreshEntityWithPassengers(var1x);
            this.discard();
        }

    }

    private int getTicksLeftUntilAdult() {
        return Math.max(0, ticksToBeFrog - this.age);
    }
}
