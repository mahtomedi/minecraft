package net.minecraft.world.entity.animal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerableMount;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Pig extends Animal implements ItemSteerableMount {
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
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, false, FOOD_ITEMS));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25);
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
    }

    @Override
    public boolean canBeControlledByRider() {
        Entity var0 = this.getControllingPassenger();
        if (!(var0 instanceof Player)) {
            return false;
        } else {
            Player var1 = (Player)var0;
            return var1.getMainHandItem().getItem() == Items.CARROT_ON_A_STICK || var1.getOffhandItem().getItem() == Items.CARROT_ON_A_STICK;
        }
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
    public boolean mobInteract(Player param0, InteractionHand param1) {
        return !super.mobInteract(param0, param1) ? this.mobInteract(this, param0, param1, true) : true;
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.hasSaddle()) {
            this.spawnAtLocation(Items.SADDLE);
        }

    }

    @Override
    public boolean hasSaddle() {
        return this.steering.hasSaddle();
    }

    @Override
    public void setSaddle(boolean param0) {
        this.steering.setSaddle(param0);
    }

    @Override
    public void thunderHit(LightningBolt param0) {
        ZombifiedPiglin var0 = EntityType.ZOMBIFIED_PIGLIN.create(this.level);
        var0.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
        var0.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
        var0.setNoAi(this.isNoAi());
        var0.setBaby(this.isBaby());
        if (this.hasCustomName()) {
            var0.setCustomName(this.getCustomName());
            var0.setCustomNameVisible(this.isCustomNameVisible());
        }

        this.level.addFreshEntity(var0);
        this.remove();
    }

    @Override
    public void travel(Vec3 param0) {
        if (this.travel(this, this.steering, param0)) {
            this.animationSpeedOld = this.animationSpeed;
            double var0 = this.getX() - this.xo;
            double var1 = this.getZ() - this.zo;
            float var2 = Mth.sqrt(var0 * var0 + var1 * var1) * 4.0F;
            if (var2 > 1.0F) {
                var2 = 1.0F;
            }

            this.animationSpeed += (var2 - this.animationSpeed) * 0.4F;
            this.animationPosition += this.animationSpeed;
        }

    }

    @Override
    public float getSteeringSpeed() {
        return (float)this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue() * 0.225F;
    }

    @Override
    public void travelWithInput(Vec3 param0) {
        super.travel(param0);
    }

    @Override
    public boolean boost() {
        return this.steering.boost(this.getRandom());
    }

    public Pig getBreedOffspring(AgableMob param0) {
        return EntityType.PIG.create(this.level);
    }

    @Override
    public boolean isFood(ItemStack param0) {
        return FOOD_ITEMS.test(param0);
    }
}
