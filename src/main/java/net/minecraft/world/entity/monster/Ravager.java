package net.minecraft.world.entity.monster;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Ravager extends Raider {
    private static final Predicate<Entity> NO_RAVAGER_AND_ALIVE = param0 -> param0.isAlive() && !(param0 instanceof Ravager);
    private static final double BASE_MOVEMENT_SPEED = 0.3;
    private static final double ATTACK_MOVEMENT_SPEED = 0.35;
    private static final int STUNNED_COLOR = 8356754;
    private static final double STUNNED_COLOR_BLUE = 0.5725490196078431;
    private static final double STUNNED_COLOR_GREEN = 0.5137254901960784;
    private static final double STUNNED_COLOR_RED = 0.4980392156862745;
    private static final int ATTACK_DURATION = 10;
    public static final int STUN_DURATION = 40;
    private int attackTick;
    private int stunnedTick;
    private int roarTick;

    public Ravager(EntityType<? extends Ravager> param0, Level param1) {
        super(param0, param1);
        this.maxUpStep = 1.0F;
        this.xpReward = 20;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new Ravager.RavagerMeleeAttackGoal());
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.4));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true, param0 -> !param0.isBaby()));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    protected void updateControlFlags() {
        boolean var0 = !(this.getControllingPassenger() instanceof Mob) || this.getControllingPassenger().getType().is(EntityTypeTags.RAIDERS);
        boolean var1 = !(this.getVehicle() instanceof Boat);
        this.goalSelector.setControlFlag(Goal.Flag.MOVE, var0);
        this.goalSelector.setControlFlag(Goal.Flag.JUMP, var0 && var1);
        this.goalSelector.setControlFlag(Goal.Flag.LOOK, var0);
        this.goalSelector.setControlFlag(Goal.Flag.TARGET, var0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 100.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.75)
            .add(Attributes.ATTACK_DAMAGE, 12.0)
            .add(Attributes.ATTACK_KNOCKBACK, 1.5)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("AttackTick", this.attackTick);
        param0.putInt("StunTick", this.stunnedTick);
        param0.putInt("RoarTick", this.roarTick);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.attackTick = param0.getInt("AttackTick");
        this.stunnedTick = param0.getInt("StunTick");
        this.roarTick = param0.getInt("RoarTick");
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.RAVAGER_CELEBRATE;
    }

    @Override
    protected PathNavigation createNavigation(Level param0) {
        return new Ravager.RavagerNavigation(this, param0);
    }

    @Override
    public int getMaxHeadYRot() {
        return 45;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 2.1;
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        Entity var0 = this.getFirstPassenger();
        return var0 != null && this.canBeControlledBy(var0) ? var0 : null;
    }

    private boolean canBeControlledBy(Entity param0) {
        return !this.isNoAi() && param0 instanceof LivingEntity;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isAlive()) {
            if (this.isImmobile()) {
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
            } else {
                double var0 = this.getTarget() != null ? 0.35 : 0.3;
                double var1 = this.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Mth.lerp(0.1, var1, var0));
            }

            if (this.horizontalCollision && this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                boolean var2 = false;
                AABB var3 = this.getBoundingBox().inflate(0.2);

                for(BlockPos var4 : BlockPos.betweenClosed(
                    Mth.floor(var3.minX), Mth.floor(var3.minY), Mth.floor(var3.minZ), Mth.floor(var3.maxX), Mth.floor(var3.maxY), Mth.floor(var3.maxZ)
                )) {
                    BlockState var5 = this.level.getBlockState(var4);
                    Block var6 = var5.getBlock();
                    if (var6 instanceof LeavesBlock) {
                        var2 = this.level.destroyBlock(var4, true, this) || var2;
                    }
                }

                if (!var2 && this.onGround) {
                    this.jumpFromGround();
                }
            }

            if (this.roarTick > 0) {
                --this.roarTick;
                if (this.roarTick == 10) {
                    this.roar();
                }
            }

            if (this.attackTick > 0) {
                --this.attackTick;
            }

            if (this.stunnedTick > 0) {
                --this.stunnedTick;
                this.stunEffect();
                if (this.stunnedTick == 0) {
                    this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.0F);
                    this.roarTick = 20;
                }
            }

        }
    }

    private void stunEffect() {
        if (this.random.nextInt(6) == 0) {
            double var0 = this.getX()
                - (double)this.getBbWidth() * Math.sin((double)(this.yBodyRot * (float) (Math.PI / 180.0)))
                + (this.random.nextDouble() * 0.6 - 0.3);
            double var1 = this.getY() + (double)this.getBbHeight() - 0.3;
            double var2 = this.getZ()
                + (double)this.getBbWidth() * Math.cos((double)(this.yBodyRot * (float) (Math.PI / 180.0)))
                + (this.random.nextDouble() * 0.6 - 0.3);
            this.level.addParticle(ParticleTypes.ENTITY_EFFECT, var0, var1, var2, 0.4980392156862745, 0.5137254901960784, 0.5725490196078431);
        }

    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.attackTick > 0 || this.stunnedTick > 0 || this.roarTick > 0;
    }

    @Override
    public boolean hasLineOfSight(Entity param0) {
        return this.stunnedTick <= 0 && this.roarTick <= 0 ? super.hasLineOfSight(param0) : false;
    }

    @Override
    protected void blockedByShield(LivingEntity param0) {
        if (this.roarTick == 0) {
            if (this.random.nextDouble() < 0.5) {
                this.stunnedTick = 40;
                this.playSound(SoundEvents.RAVAGER_STUNNED, 1.0F, 1.0F);
                this.level.broadcastEntityEvent(this, (byte)39);
                param0.push(this);
            } else {
                this.strongKnockback(param0);
            }

            param0.hurtMarked = true;
        }

    }

    private void roar() {
        if (this.isAlive()) {
            for(LivingEntity var1 : this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0), NO_RAVAGER_AND_ALIVE)) {
                if (!(var1 instanceof AbstractIllager)) {
                    var1.hurt(DamageSource.mobAttack(this), 6.0F);
                }

                this.strongKnockback(var1);
            }

            Vec3 var2 = this.getBoundingBox().getCenter();

            for(int var3 = 0; var3 < 40; ++var3) {
                double var4 = this.random.nextGaussian() * 0.2;
                double var5 = this.random.nextGaussian() * 0.2;
                double var6 = this.random.nextGaussian() * 0.2;
                this.level.addParticle(ParticleTypes.POOF, var2.x, var2.y, var2.z, var4, var5, var6);
            }

            this.level.gameEvent(this, GameEvent.ENTITY_ROAR, this.getEyePosition());
        }

    }

    private void strongKnockback(Entity param0) {
        double var0 = param0.getX() - this.getX();
        double var1 = param0.getZ() - this.getZ();
        double var2 = Math.max(var0 * var0 + var1 * var1, 0.001);
        param0.push(var0 / var2 * 4.0, 0.2, var1 / var2 * 4.0);
    }

    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 4) {
            this.attackTick = 10;
            this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 1.0F);
        } else if (param0 == 39) {
            this.stunnedTick = 40;
        }

        super.handleEntityEvent(param0);
    }

    public int getAttackTick() {
        return this.attackTick;
    }

    public int getStunnedTick() {
        return this.stunnedTick;
    }

    public int getRoarTick() {
        return this.roarTick;
    }

    @Override
    public boolean doHurtTarget(Entity param0) {
        this.attackTick = 10;
        this.level.broadcastEntityEvent(this, (byte)4);
        this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 1.0F);
        return super.doHurtTarget(param0);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.RAVAGER_STEP, 0.15F, 1.0F);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader param0) {
        return !param0.containsAnyLiquid(this.getBoundingBox());
    }

    @Override
    public void applyRaidBuffs(int param0, boolean param1) {
    }

    @Override
    public boolean canBeLeader() {
        return false;
    }

    class RavagerMeleeAttackGoal extends MeleeAttackGoal {
        public RavagerMeleeAttackGoal() {
            super(Ravager.this, 1.0, true);
        }

        @Override
        protected double getAttackReachSqr(LivingEntity param0) {
            float var0 = Ravager.this.getBbWidth() - 0.1F;
            return (double)(var0 * 2.0F * var0 * 2.0F + param0.getBbWidth());
        }
    }

    static class RavagerNavigation extends GroundPathNavigation {
        public RavagerNavigation(Mob param0, Level param1) {
            super(param0, param1);
        }

        @Override
        protected PathFinder createPathFinder(int param0) {
            this.nodeEvaluator = new Ravager.RavagerNodeEvaluator();
            return new PathFinder(this.nodeEvaluator, param0);
        }
    }

    static class RavagerNodeEvaluator extends WalkNodeEvaluator {
        @Override
        protected BlockPathTypes evaluateBlockPathType(BlockGetter param0, boolean param1, boolean param2, BlockPos param3, BlockPathTypes param4) {
            return param4 == BlockPathTypes.LEAVES ? BlockPathTypes.OPEN : super.evaluateBlockPathType(param0, param1, param2, param3, param4);
        }
    }
}
