package net.minecraft.world.entity.animal;

import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public abstract class Animal extends AgeableMob {
    static final int PARENT_AGE_AFTER_BREEDING = 6000;
    private int inLove;
    @Nullable
    private UUID loveCause;

    protected Animal(EntityType<? extends Animal> param0, Level param1) {
        super(param0, param1);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
    }

    @Override
    protected void customServerAiStep() {
        if (this.getAge() != 0) {
            this.inLove = 0;
        }

        super.customServerAiStep();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getAge() != 0) {
            this.inLove = 0;
        }

        if (this.inLove > 0) {
            --this.inLove;
            if (this.inLove % 10 == 0) {
                double var0 = this.random.nextGaussian() * 0.02;
                double var1 = this.random.nextGaussian() * 0.02;
                double var2 = this.random.nextGaussian() * 0.02;
                this.level.addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), var0, var1, var2);
            }
        }

    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else {
            this.inLove = 0;
            return super.hurt(param0, param1);
        }
    }

    @Override
    public float getWalkTargetValue(BlockPos param0, LevelReader param1) {
        return param1.getBlockState(param0.below()).is(Blocks.GRASS_BLOCK) ? 10.0F : param1.getBrightness(param0) - 0.5F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("InLove", this.inLove);
        if (this.loveCause != null) {
            param0.putUUID("LoveCause", this.loveCause);
        }

    }

    @Override
    public double getMyRidingOffset() {
        return 0.14;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.inLove = param0.getInt("InLove");
        this.loveCause = param0.hasUUID("LoveCause") ? param0.getUUID("LoveCause") : null;
    }

    public static boolean checkAnimalSpawnRules(EntityType<? extends Animal> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
        return param1.getBlockState(param3.below()).is(Blocks.GRASS_BLOCK) && param1.getRawBrightness(param3, 0) > 8;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    public boolean removeWhenFarAway(double param0) {
        return false;
    }

    @Override
    protected int getExperienceReward(Player param0) {
        return 1 + this.level.random.nextInt(3);
    }

    public boolean isFood(ItemStack param0) {
        return param0.is(Items.WHEAT);
    }

    @Override
    public InteractionResult mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (this.isFood(var0)) {
            int var1 = this.getAge();
            if (!this.level.isClientSide && var1 == 0 && this.canFallInLove()) {
                this.usePlayerItem(param0, param1, var0);
                this.setInLove(param0);
                this.gameEvent(GameEvent.MOB_INTERACT, this.eyeBlockPosition());
                return InteractionResult.SUCCESS;
            }

            if (this.isBaby()) {
                this.usePlayerItem(param0, param1, var0);
                this.ageUp((int)((float)(-var1 / 20) * 0.1F), true);
                this.gameEvent(GameEvent.MOB_INTERACT, this.eyeBlockPosition());
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }

            if (this.level.isClientSide) {
                return InteractionResult.CONSUME;
            }
        }

        return super.mobInteract(param0, param1);
    }

    protected void usePlayerItem(Player param0, InteractionHand param1, ItemStack param2) {
        if (!param0.getAbilities().instabuild) {
            param2.shrink(1);
        }

    }

    public boolean canFallInLove() {
        return this.inLove <= 0;
    }

    public void setInLove(@Nullable Player param0) {
        this.inLove = 600;
        if (param0 != null) {
            this.loveCause = param0.getUUID();
        }

        this.level.broadcastEntityEvent(this, (byte)18);
    }

    public void setInLoveTime(int param0) {
        this.inLove = param0;
    }

    public int getInLoveTime() {
        return this.inLove;
    }

    @Nullable
    public ServerPlayer getLoveCause() {
        if (this.loveCause == null) {
            return null;
        } else {
            Player var0 = this.level.getPlayerByUUID(this.loveCause);
            return var0 instanceof ServerPlayer ? (ServerPlayer)var0 : null;
        }
    }

    public boolean isInLove() {
        return this.inLove > 0;
    }

    public void resetLove() {
        this.inLove = 0;
    }

    public boolean canMate(Animal param0) {
        if (param0 == this) {
            return false;
        } else if (param0.getClass() != this.getClass()) {
            return false;
        } else {
            return this.isInLove() && param0.isInLove();
        }
    }

    public void spawnChildFromBreeding(ServerLevel param0, Animal param1) {
        AgeableMob var0 = this.getBreedOffspring(param0, param1);
        if (var0 != null) {
            ServerPlayer var1 = this.getLoveCause();
            if (var1 == null && param1.getLoveCause() != null) {
                var1 = param1.getLoveCause();
            }

            if (var1 != null) {
                var1.awardStat(Stats.ANIMALS_BRED);
                CriteriaTriggers.BRED_ANIMALS.trigger(var1, this, param1, var0);
            }

            this.setAge(6000);
            param1.setAge(6000);
            this.resetLove();
            param1.resetLove();
            var0.setBaby(true);
            var0.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
            param0.addFreshEntityWithPassengers(var0);
            param0.broadcastEntityEvent(this, (byte)18);
            if (param0.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                param0.addFreshEntity(new ExperienceOrb(param0, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
            }

        }
    }

    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 18) {
            for(int var0 = 0; var0 < 7; ++var0) {
                double var1 = this.random.nextGaussian() * 0.02;
                double var2 = this.random.nextGaussian() * 0.02;
                double var3 = this.random.nextGaussian() * 0.02;
                this.level.addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), var1, var2, var3);
            }
        } else {
            super.handleEntityEvent(param0);
        }

    }
}
