package net.minecraft.world.entity.animal;

import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class Animal extends AgableMob {
    private int inLove;
    private UUID loveCause;

    protected Animal(EntityType<? extends Animal> param0, Level param1) {
        super(param0, param1);
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
                this.level
                    .addParticle(
                        ParticleTypes.HEART,
                        this.x + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                        this.y + 0.5 + (double)(this.random.nextFloat() * this.getBbHeight()),
                        this.z + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                        var0,
                        var1,
                        var2
                    );
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
        return param1.getBlockState(param0.below()).getBlock() == Blocks.GRASS_BLOCK ? 10.0F : param1.getBrightness(param0) - 0.5F;
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
    public double getRidingHeight() {
        return 0.14;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.inLove = param0.getInt("InLove");
        this.loveCause = param0.hasUUID("LoveCause") ? param0.getUUID("LoveCause") : null;
    }

    public static boolean checkAnimalSpawnRules(EntityType<? extends Animal> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
        return param1.getBlockState(param3.below()).getBlock() == Blocks.GRASS_BLOCK && param1.getRawBrightness(param3, 0) > 8;
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
        return param0.getItem() == Items.WHEAT;
    }

    @Override
    public boolean mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (this.isFood(var0)) {
            if (this.getAge() == 0 && this.canFallInLove()) {
                this.usePlayerItem(param0, var0);
                this.setInLove(param0);
                return true;
            }

            if (this.isBaby()) {
                this.usePlayerItem(param0, var0);
                this.ageUp((int)((float)(-this.getAge() / 20) * 0.1F), true);
                return true;
            }
        }

        return super.mobInteract(param0, param1);
    }

    protected void usePlayerItem(Player param0, ItemStack param1) {
        if (!param0.abilities.instabuild) {
            param1.shrink(1);
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 18) {
            for(int var0 = 0; var0 < 7; ++var0) {
                double var1 = this.random.nextGaussian() * 0.02;
                double var2 = this.random.nextGaussian() * 0.02;
                double var3 = this.random.nextGaussian() * 0.02;
                this.level
                    .addParticle(
                        ParticleTypes.HEART,
                        this.x + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                        this.y + 0.5 + (double)(this.random.nextFloat() * this.getBbHeight()),
                        this.z + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth(),
                        var1,
                        var2,
                        var3
                    );
            }
        } else {
            super.handleEntityEvent(param0);
        }

    }
}
