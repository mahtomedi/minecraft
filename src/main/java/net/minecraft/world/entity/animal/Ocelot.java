package net.minecraft.world.entity.animal;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Ocelot extends Animal {
    private static final Ingredient TEMPT_INGREDIENT = Ingredient.of(Items.COD, Items.SALMON);
    private static final EntityDataAccessor<Boolean> DATA_TRUSTING = SynchedEntityData.defineId(Ocelot.class, EntityDataSerializers.BOOLEAN);
    private Ocelot.OcelotAvoidEntityGoal<Player> ocelotAvoidPlayersGoal;
    private Ocelot.OcelotTemptGoal temptGoal;

    public Ocelot(EntityType<? extends Ocelot> param0, Level param1) {
        super(param0, param1);
        this.reassessTrustingGoals();
    }

    private boolean isTrusting() {
        return this.entityData.get(DATA_TRUSTING);
    }

    private void setTrusting(boolean param0) {
        this.entityData.set(DATA_TRUSTING, param0);
        this.reassessTrustingGoals();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putBoolean("Trusting", this.isTrusting());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setTrusting(param0.getBoolean("Trusting"));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TRUSTING, false);
    }

    @Override
    protected void registerGoals() {
        this.temptGoal = new Ocelot.OcelotTemptGoal(this, 0.6, TEMPT_INGREDIENT, true);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, this.temptGoal);
        this.goalSelector.addGoal(7, new LeapAtTargetGoal(this, 0.3F));
        this.goalSelector.addGoal(8, new OcelotAttackGoal(this));
        this.goalSelector.addGoal(9, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(10, new WaterAvoidingRandomStrollGoal(this, 0.8, 1.0000001E-5F));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Chicken.class, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, false, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    @Override
    public void customServerAiStep() {
        if (this.getMoveControl().hasWanted()) {
            double var0 = this.getMoveControl().getSpeedModifier();
            if (var0 == 0.6) {
                this.setPose(Pose.CROUCHING);
                this.setSprinting(false);
            } else if (var0 == 1.33) {
                this.setPose(Pose.STANDING);
                this.setSprinting(true);
            } else {
                this.setPose(Pose.STANDING);
                this.setSprinting(false);
            }
        } else {
            this.setPose(Pose.STANDING);
            this.setSprinting(false);
        }

    }

    @Override
    public boolean removeWhenFarAway(double param0) {
        return !this.isTrusting() && this.tickCount > 2400;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.3F).add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    public boolean causeFallDamage(float param0, float param1) {
        return false;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.OCELOT_AMBIENT;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 900;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.OCELOT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.OCELOT_DEATH;
    }

    private float getAttackDamage() {
        return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    @Override
    public boolean doHurtTarget(Entity param0) {
        return param0.hurt(DamageSource.mobAttack(this), this.getAttackDamage());
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        return this.isInvulnerableTo(param0) ? false : super.hurt(param0, param1);
    }

    @Override
    public boolean mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if ((this.temptGoal == null || this.temptGoal.isRunning()) && !this.isTrusting() && this.isFood(var0) && param0.distanceToSqr(this) < 9.0) {
            this.usePlayerItem(param0, var0);
            if (!this.level.isClientSide) {
                if (this.random.nextInt(3) == 0) {
                    this.setTrusting(true);
                    this.spawnTrustingParticles(true);
                    this.level.broadcastEntityEvent(this, (byte)41);
                } else {
                    this.spawnTrustingParticles(false);
                    this.level.broadcastEntityEvent(this, (byte)40);
                }
            }

            return true;
        } else {
            return super.mobInteract(param0, param1);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 41) {
            this.spawnTrustingParticles(true);
        } else if (param0 == 40) {
            this.spawnTrustingParticles(false);
        } else {
            super.handleEntityEvent(param0);
        }

    }

    private void spawnTrustingParticles(boolean param0) {
        ParticleOptions var0 = ParticleTypes.HEART;
        if (!param0) {
            var0 = ParticleTypes.SMOKE;
        }

        for(int var1 = 0; var1 < 7; ++var1) {
            double var2 = this.random.nextGaussian() * 0.02;
            double var3 = this.random.nextGaussian() * 0.02;
            double var4 = this.random.nextGaussian() * 0.02;
            this.level.addParticle(var0, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), var2, var3, var4);
        }

    }

    protected void reassessTrustingGoals() {
        if (this.ocelotAvoidPlayersGoal == null) {
            this.ocelotAvoidPlayersGoal = new Ocelot.OcelotAvoidEntityGoal<>(this, Player.class, 16.0F, 0.8, 1.33);
        }

        this.goalSelector.removeGoal(this.ocelotAvoidPlayersGoal);
        if (!this.isTrusting()) {
            this.goalSelector.addGoal(4, this.ocelotAvoidPlayersGoal);
        }

    }

    public Ocelot getBreedOffspring(AgableMob param0) {
        return EntityType.OCELOT.create(this.level);
    }

    @Override
    public boolean isFood(ItemStack param0) {
        return TEMPT_INGREDIENT.test(param0);
    }

    public static boolean checkOcelotSpawnRules(EntityType<Ocelot> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
        return param4.nextInt(3) != 0;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader param0) {
        if (param0.isUnobstructed(this) && !param0.containsAnyLiquid(this.getBoundingBox())) {
            BlockPos var0 = this.blockPosition();
            if (var0.getY() < param0.getSeaLevel()) {
                return false;
            }

            BlockState var1 = param0.getBlockState(var0.below());
            if (var1.is(Blocks.GRASS_BLOCK) || var1.is(BlockTags.LEAVES)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        LevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        if (param3 == null) {
            param3 = new AgableMob.AgableMobGroupData();
            ((AgableMob.AgableMobGroupData)param3).setBabySpawnChance(1.0F);
        }

        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    static class OcelotAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final Ocelot ocelot;

        public OcelotAvoidEntityGoal(Ocelot param0, Class<T> param1, float param2, double param3, double param4) {
            super(param0, param1, param2, param3, param4, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
            this.ocelot = param0;
        }

        @Override
        public boolean canUse() {
            return !this.ocelot.isTrusting() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.ocelot.isTrusting() && super.canContinueToUse();
        }
    }

    static class OcelotTemptGoal extends TemptGoal {
        private final Ocelot ocelot;

        public OcelotTemptGoal(Ocelot param0, double param1, Ingredient param2, boolean param3) {
            super(param0, param1, param2, param3);
            this.ocelot = param0;
        }

        @Override
        protected boolean canScare() {
            return super.canScare() && !this.ocelot.isTrusting();
        }
    }
}
