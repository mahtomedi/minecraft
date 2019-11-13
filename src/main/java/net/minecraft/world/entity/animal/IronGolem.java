package net.minecraft.world.entity.animal;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveBackToVillage;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.OfferFlowerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.DefendVillageTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IronGolem extends AbstractGolem {
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(IronGolem.class, EntityDataSerializers.BYTE);
    private int attackAnimationTick;
    private int offerFlowerTick;

    public IronGolem(EntityType<? extends IronGolem> param0, Level param1) {
        super(param0, param1);
        this.maxUpStep = 1.0F;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9, 32.0F));
        this.goalSelector.addGoal(2, new MoveBackToVillage(this, 0.6));
        this.goalSelector.addGoal(3, new MoveThroughVillageGoal(this, 0.6, false, 4, () -> false));
        this.goalSelector.addGoal(5, new OfferFlowerGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new DefendVillageTargetGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector
            .addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 5, false, false, param0 -> param0 instanceof Enemy && !(param0 instanceof Creeper)));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100.0);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25);
        this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(15.0);
    }

    @Override
    protected int decreaseAirSupply(int param0) {
        return param0;
    }

    @Override
    protected void doPush(Entity param0) {
        if (param0 instanceof Enemy && !(param0 instanceof Creeper) && this.getRandom().nextInt(20) == 0) {
            this.setTarget((LivingEntity)param0);
        }

        super.doPush(param0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.attackAnimationTick > 0) {
            --this.attackAnimationTick;
        }

        if (this.offerFlowerTick > 0) {
            --this.offerFlowerTick;
        }

        if (getHorizontalDistanceSqr(this.getDeltaMovement()) > 2.5000003E-7F && this.random.nextInt(5) == 0) {
            int var0 = Mth.floor(this.getX());
            int var1 = Mth.floor(this.getY() - 0.2F);
            int var2 = Mth.floor(this.getZ());
            BlockState var3 = this.level.getBlockState(new BlockPos(var0, var1, var2));
            if (!var3.isAir()) {
                this.level
                    .addParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, var3),
                        this.getX() + ((double)this.random.nextFloat() - 0.5) * (double)this.getBbWidth(),
                        this.getY() + 0.1,
                        this.getZ() + ((double)this.random.nextFloat() - 0.5) * (double)this.getBbWidth(),
                        4.0 * ((double)this.random.nextFloat() - 0.5),
                        0.5,
                        ((double)this.random.nextFloat() - 0.5) * 4.0
                    );
            }
        }

    }

    @Override
    public boolean canAttackType(EntityType<?> param0) {
        if (this.isPlayerCreated() && param0 == EntityType.PLAYER) {
            return false;
        } else {
            return param0 == EntityType.CREEPER ? false : super.canAttackType(param0);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putBoolean("PlayerCreated", this.isPlayerCreated());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setPlayerCreated(param0.getBoolean("PlayerCreated"));
    }

    private float getAttackDamage() {
        return (float)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
    }

    @Override
    public boolean doHurtTarget(Entity param0) {
        this.attackAnimationTick = 10;
        this.level.broadcastEntityEvent(this, (byte)4);
        float var0 = this.getAttackDamage();
        float var1 = var0 > 0.0F ? var0 / 2.0F + (float)this.random.nextInt((int)var0) : 0.0F;
        boolean var2 = param0.hurt(DamageSource.mobAttack(this), var1);
        if (var2) {
            param0.setDeltaMovement(param0.getDeltaMovement().add(0.0, 0.4F, 0.0));
            this.doEnchantDamageEffects(this, param0);
        }

        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        return var2;
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        IronGolem.Crackiness var0 = this.getCrackiness();
        boolean var1 = super.hurt(param0, param1);
        if (var1 && this.getCrackiness() != var0) {
            this.playSound(SoundEvents.IRON_GOLEM_DAMAGE, 1.0F, 1.0F);
        }

        return var1;
    }

    public IronGolem.Crackiness getCrackiness() {
        return IronGolem.Crackiness.byFraction(this.getHealth() / this.getMaxHealth());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 4) {
            this.attackAnimationTick = 10;
            this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        } else if (param0 == 11) {
            this.offerFlowerTick = 400;
        } else if (param0 == 34) {
            this.offerFlowerTick = 0;
        } else {
            super.handleEntityEvent(param0);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public int getAttackAnimationTick() {
        return this.attackAnimationTick;
    }

    public void offerFlower(boolean param0) {
        if (param0) {
            this.offerFlowerTick = 400;
            this.level.broadcastEntityEvent(this, (byte)11);
        } else {
            this.offerFlowerTick = 0;
            this.level.broadcastEntityEvent(this, (byte)34);
        }

    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected boolean mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        Item var1 = var0.getItem();
        if (var1 != Items.IRON_INGOT) {
            return false;
        } else {
            float var2 = this.getHealth();
            this.heal(25.0F);
            if (this.getHealth() == var2) {
                return false;
            } else {
                float var3 = 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F;
                this.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, var3);
                if (!param0.abilities.instabuild) {
                    var0.shrink(1);
                }

                return true;
            }
        }
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public int getOfferFlowerTick() {
        return this.offerFlowerTick;
    }

    public boolean isPlayerCreated() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setPlayerCreated(boolean param0) {
        byte var0 = this.entityData.get(DATA_FLAGS_ID);
        if (param0) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(var0 | 1));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(var0 & -2));
        }

    }

    @Override
    public void die(DamageSource param0) {
        super.die(param0);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader param0) {
        BlockPos var0 = new BlockPos(this);
        BlockPos var1 = var0.below();
        BlockState var2 = param0.getBlockState(var1);
        if (!var2.entityCanStandOn(param0, var1, this)) {
            return false;
        } else {
            for(int var3 = 1; var3 < 3; ++var3) {
                BlockPos var4 = var0.above(var3);
                BlockState var5 = param0.getBlockState(var4);
                if (!NaturalSpawner.isValidEmptySpawnBlock(param0, var4, var5, var5.getFluidState())) {
                    return false;
                }
            }

            return NaturalSpawner.isValidEmptySpawnBlock(param0, var0, param0.getBlockState(var0), Fluids.EMPTY.defaultFluidState())
                && param0.isUnobstructed(this);
        }
    }

    public static enum Crackiness {
        NONE(1.0F),
        LOW(0.75F),
        MEDIUM(0.5F),
        HIGH(0.25F);

        private static final List<IronGolem.Crackiness> BY_DAMAGE = Stream.of(values())
            .sorted(Comparator.comparingDouble(param0 -> (double)param0.fraction))
            .collect(ImmutableList.toImmutableList());
        private final float fraction;

        private Crackiness(float param0) {
            this.fraction = param0;
        }

        public static IronGolem.Crackiness byFraction(float param0) {
            for(IronGolem.Crackiness var0 : BY_DAMAGE) {
                if (param0 < var0.fraction) {
                    return var0;
                }
            }

            return NONE;
        }
    }
}
