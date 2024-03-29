package net.minecraft.world.entity.monster;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;

public abstract class Monster extends PathfinderMob implements Enemy {
    protected Monster(EntityType<? extends Monster> param0, Level param1) {
        super(param0, param1);
        this.xpReward = 5;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    public void aiStep() {
        this.updateSwingTime();
        this.updateNoActionTime();
        super.aiStep();
    }

    protected void updateNoActionTime() {
        float var0 = this.getLightLevelDependentMagicValue();
        if (var0 > 0.5F) {
            this.noActionTime += 2;
        }

    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.HOSTILE_SWIM;
    }

    @Override
    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.HOSTILE_SPLASH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.HOSTILE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HOSTILE_DEATH;
    }

    @Override
    public LivingEntity.Fallsounds getFallSounds() {
        return new LivingEntity.Fallsounds(SoundEvents.HOSTILE_SMALL_FALL, SoundEvents.HOSTILE_BIG_FALL);
    }

    @Override
    public float getWalkTargetValue(BlockPos param0, LevelReader param1) {
        return -param1.getPathfindingCostFromLightLevels(param0);
    }

    public static boolean isDarkEnoughToSpawn(ServerLevelAccessor param0, BlockPos param1, RandomSource param2) {
        if (param0.getBrightness(LightLayer.SKY, param1) > param2.nextInt(32)) {
            return false;
        } else {
            DimensionType var0 = param0.dimensionType();
            int var1 = var0.monsterSpawnBlockLightLimit();
            if (var1 < 15 && param0.getBrightness(LightLayer.BLOCK, param1) > var1) {
                return false;
            } else {
                int var2 = param0.getLevel().isThundering() ? param0.getMaxLocalRawBrightness(param1, 10) : param0.getMaxLocalRawBrightness(param1);
                return var2 <= var0.monsterSpawnLightTest().sample(param2);
            }
        }
    }

    public static boolean checkMonsterSpawnRules(
        EntityType<? extends Monster> param0, ServerLevelAccessor param1, MobSpawnType param2, BlockPos param3, RandomSource param4
    ) {
        return param1.getDifficulty() != Difficulty.PEACEFUL
            && (MobSpawnType.ignoresLightRequirements(param2) || isDarkEnoughToSpawn(param1, param3, param4))
            && checkMobSpawnRules(param0, param1, param2, param3, param4);
    }

    public static boolean checkAnyLightMonsterSpawnRules(
        EntityType<? extends Monster> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, RandomSource param4
    ) {
        return param1.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules(param0, param1, param2, param3, param4);
    }

    public static AttributeSupplier.Builder createMonsterAttributes() {
        return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE);
    }

    @Override
    public boolean shouldDropExperience() {
        return true;
    }

    @Override
    protected boolean shouldDropLoot() {
        return true;
    }

    public boolean isPreventingPlayerRest(Player param0) {
        return true;
    }

    @Override
    public ItemStack getProjectile(ItemStack param0) {
        if (param0.getItem() instanceof ProjectileWeaponItem) {
            Predicate<ItemStack> var0 = ((ProjectileWeaponItem)param0.getItem()).getSupportedHeldProjectiles();
            ItemStack var1 = ProjectileWeaponItem.getHeldProjectile(this, var0);
            return var1.isEmpty() ? new ItemStack(Items.ARROW) : var1;
        } else {
            return ItemStack.EMPTY;
        }
    }
}
