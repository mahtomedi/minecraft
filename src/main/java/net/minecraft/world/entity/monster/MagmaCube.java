package net.minecraft.world.entity.monster;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;

public class MagmaCube extends Slime {
    public MagmaCube(EntityType<? extends MagmaCube> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2F);
    }

    public static boolean checkMagmaCubeSpawnRules(EntityType<MagmaCube> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
        return param1.getDifficulty() != Difficulty.PEACEFUL;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader param0) {
        return param0.isUnobstructed(this) && !param0.containsAnyLiquid(this.getBoundingBox());
    }

    @Override
    protected void setSize(int param0, boolean param1) {
        super.setSize(param0, param1);
        this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue((double)(param0 * 3));
    }

    @Override
    public float getBrightness() {
        return 1.0F;
    }

    @Override
    protected ParticleOptions getParticleType() {
        return ParticleTypes.FLAME;
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return this.isTiny() ? BuiltInLootTables.EMPTY : this.getType().getDefaultLootTable();
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected int getJumpDelay() {
        return super.getJumpDelay() * 4;
    }

    @Override
    protected void decreaseSquish() {
        this.targetSquish *= 0.9F;
    }

    @Override
    protected void jumpFromGround() {
        Vec3 var0 = this.getDeltaMovement();
        this.setDeltaMovement(var0.x, (double)(this.getJumpPower() + (float)this.getSize() * 0.1F), var0.z);
        this.hasImpulse = true;
    }

    @Override
    protected void jumpInLiquid(Tag<Fluid> param0) {
        if (param0 == FluidTags.LAVA) {
            Vec3 var0 = this.getDeltaMovement();
            this.setDeltaMovement(var0.x, (double)(0.22F + (float)this.getSize() * 0.05F), var0.z);
            this.hasImpulse = true;
        } else {
            super.jumpInLiquid(param0);
        }

    }

    @Override
    public boolean causeFallDamage(float param0, float param1) {
        return false;
    }

    @Override
    protected boolean isDealsDamage() {
        return this.isEffectiveAi();
    }

    @Override
    protected float getAttackDamage() {
        return super.getAttackDamage() + 2.0F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return this.isTiny() ? SoundEvents.MAGMA_CUBE_HURT_SMALL : SoundEvents.MAGMA_CUBE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isTiny() ? SoundEvents.MAGMA_CUBE_DEATH_SMALL : SoundEvents.MAGMA_CUBE_DEATH;
    }

    @Override
    protected SoundEvent getSquishSound() {
        return this.isTiny() ? SoundEvents.MAGMA_CUBE_SQUISH_SMALL : SoundEvents.MAGMA_CUBE_SQUISH;
    }

    @Override
    protected SoundEvent getJumpSound() {
        return SoundEvents.MAGMA_CUBE_JUMP;
    }
}
