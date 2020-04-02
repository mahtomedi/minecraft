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
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SnowGolem extends AbstractGolem implements RangedAttackMob {
    private static final EntityDataAccessor<Byte> DATA_PUMPKIN_ID = SynchedEntityData.defineId(SnowGolem.class, EntityDataSerializers.BYTE);

    public SnowGolem(EntityType<? extends SnowGolem> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.25, 20, 10.0F));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0, 1.0000001E-5F));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, param0 -> param0 instanceof Enemy));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.MOVEMENT_SPEED, 0.2F);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PUMPKIN_ID, (byte)16);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putBoolean("Pumpkin", this.hasPumpkin());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("Pumpkin")) {
            this.setPumpkin(param0.getBoolean("Pumpkin"));
        }

    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            int var0 = Mth.floor(this.getX());
            int var1 = Mth.floor(this.getY());
            int var2 = Mth.floor(this.getZ());
            if (this.isInWaterRainOrBubble()) {
                this.hurt(DamageSource.DROWN, 1.0F);
            }

            if (this.level.getBiome(new BlockPos(var0, 0, var2)).getTemperature(new BlockPos(var0, var1, var2)) > 1.0F) {
                this.hurt(DamageSource.ON_FIRE, 1.0F);
            }

            if (!this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return;
            }

            BlockState var3 = Blocks.SNOW.defaultBlockState();

            for(int var4 = 0; var4 < 4; ++var4) {
                var0 = Mth.floor(this.getX() + (double)((float)(var4 % 2 * 2 - 1) * 0.25F));
                var1 = Mth.floor(this.getY());
                var2 = Mth.floor(this.getZ() + (double)((float)(var4 / 2 % 2 * 2 - 1) * 0.25F));
                BlockPos var5 = new BlockPos(var0, var1, var2);
                if (this.level.getBlockState(var5).isAir() && this.level.getBiome(var5).getTemperature(var5) < 0.8F && var3.canSurvive(this.level, var5)) {
                    this.level.setBlockAndUpdate(var5, var3);
                }
            }
        }

    }

    @Override
    public void performRangedAttack(LivingEntity param0, float param1) {
        Snowball var0 = new Snowball(this.level, this);
        double var1 = param0.getEyeY() - 1.1F;
        double var2 = param0.getX() - this.getX();
        double var3 = var1 - var0.getY();
        double var4 = param0.getZ() - this.getZ();
        float var5 = Mth.sqrt(var2 * var2 + var4 * var4) * 0.2F;
        var0.shoot(var2, var3 + (double)var5, var4, 1.6F, 12.0F);
        this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level.addFreshEntity(var0);
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return 1.7F;
    }

    @Override
    protected boolean mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (var0.getItem() == Items.SHEARS && this.hasPumpkin()) {
            if (!this.level.isClientSide) {
                this.setPumpkin(false);
                var0.hurtAndBreak(1, param0, param1x -> param1x.broadcastBreakEvent(param1));
                this.spawnAtLocation(new ItemStack(Items.CARVED_PUMPKIN), 1.7F);
                this.playSound(SoundEvents.SNOW_GOLEM_SHEAR, 1.0F, 1.0F);
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean hasPumpkin() {
        return (this.entityData.get(DATA_PUMPKIN_ID) & 16) != 0;
    }

    public void setPumpkin(boolean param0) {
        byte var0 = this.entityData.get(DATA_PUMPKIN_ID);
        if (param0) {
            this.entityData.set(DATA_PUMPKIN_ID, (byte)(var0 | 16));
        } else {
            this.entityData.set(DATA_PUMPKIN_ID, (byte)(var0 & -17));
        }

    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SNOW_GOLEM_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.SNOW_GOLEM_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SNOW_GOLEM_DEATH;
    }
}
