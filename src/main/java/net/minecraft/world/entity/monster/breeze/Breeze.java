package net.minecraft.world.entity.monster.breeze;

import com.mojang.serialization.Dynamic;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class Breeze extends Monster {
    private static final int SLIDE_PARTICLES_AMOUNT = 20;
    private static final int IDLE_PARTICLES_AMOUNT = 1;
    private static final int JUMP_DUST_PARTICLES_AMOUNT = 20;
    private static final int JUMP_TRAIL_PARTICLES_AMOUNT = 3;
    private static final int JUMP_TRAIL_DURATION_TICKS = 5;
    private static final int JUMP_CIRCLE_DISTANCE_Y = 10;
    private static final float FALL_DISTANCE_SOUND_TRIGGER_THRESHOLD = 3.0F;
    public AnimationState idle = new AnimationState();
    public AnimationState slide = new AnimationState();
    public AnimationState longJump = new AnimationState();
    public AnimationState shoot = new AnimationState();
    public AnimationState inhale = new AnimationState();
    private int jumpTrailStartedTick = 0;

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.6F)
            .add(Attributes.MAX_HEALTH, 30.0)
            .add(Attributes.FOLLOW_RANGE, 24.0)
            .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    public Breeze(EntityType<? extends Monster> param0, Level param1) {
        super(param0, param1);
        this.setPathfindingMalus(BlockPathTypes.DANGER_TRAPDOOR, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> param0) {
        return BreezeAi.makeBrain(this.brainProvider().makeBrain(param0));
    }

    @Override
    public Brain<Breeze> getBrain() {
        return super.getBrain();
    }

    @Override
    protected Brain.Provider<Breeze> brainProvider() {
        return Brain.provider(BreezeAi.MEMORY_TYPES, BreezeAi.SENSOR_TYPES);
    }

    @Override
    public boolean canAttack(LivingEntity param0) {
        return param0.getType() != EntityType.BREEZE && super.canAttack(param0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> param0) {
        if (this.level().isClientSide() && DATA_POSE.equals(param0)) {
            this.resetAnimations();
            Pose var0 = this.getPose();
            switch(var0) {
                case SHOOTING:
                    this.shoot.startIfStopped(this.tickCount);
                    break;
                case INHALING:
                    this.longJump.startIfStopped(this.tickCount);
                    break;
                case SLIDING:
                    this.slide.startIfStopped(this.tickCount);
            }
        }

        super.onSyncedDataUpdated(param0);
    }

    private void resetAnimations() {
        this.shoot.stop();
        this.idle.stop();
        this.inhale.stop();
        this.longJump.stop();
        this.slide.stop();
    }

    @Override
    public void tick() {
        switch(this.getPose()) {
            case SHOOTING:
            case INHALING:
            case STANDING:
                this.resetJumpTrail().emitGroundParticles(1 + this.getRandom().nextInt(1));
                break;
            case SLIDING:
                this.emitGroundParticles(20);
                break;
            case LONG_JUMPING:
                this.emitJumpTrailParticles();
        }

        super.tick();
    }

    public Breeze resetJumpTrail() {
        this.jumpTrailStartedTick = 0;
        return this;
    }

    public Breeze emitJumpDustParticles() {
        Vec3 var0 = this.position().add(0.0, 0.1F, 0.0);

        for(int var1 = 0; var1 < 20; ++var1) {
            this.level().addParticle(ParticleTypes.GUST_DUST, var0.x, var0.y, var0.z, 0.0, 0.0, 0.0);
        }

        return this;
    }

    public void emitJumpTrailParticles() {
        if (++this.jumpTrailStartedTick <= 5) {
            BlockState var0 = this.level().getBlockState(this.blockPosition().below());
            Vec3 var1 = this.getDeltaMovement();
            Vec3 var2 = this.position().add(var1).add(0.0, 0.1F, 0.0);

            for(int var3 = 0; var3 < 3; ++var3) {
                this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, var0), var2.x, var2.y, var2.z, 0.0, 0.0, 0.0);
            }

        }
    }

    public void emitGroundParticles(int param0) {
        Vec3 var0 = this.getBoundingBox().getCenter();
        Vec3 var1 = new Vec3(var0.x, this.position().y, var0.z);
        BlockState var2 = this.level().getBlockState(this.blockPosition().below());
        if (var2.getRenderShape() != RenderShape.INVISIBLE) {
            for(int var3 = 0; var3 < param0; ++var3) {
                this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, var2), var1.x, var1.y, var1.z, 0.0, 0.0, 0.0);
            }

        }
    }

    @Override
    public void playAmbientSound() {
        this.level().playLocalSound(this, this.getAmbientSound(), this.getSoundSource(), 1.0F, 1.0F);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BREEZE_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.BREEZE_HURT;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.onGround() ? SoundEvents.BREEZE_IDLE_GROUND : SoundEvents.BREEZE_IDLE_AIR;
    }

    public boolean withinOuterCircleRange(Vec3 param0) {
        Vec3 var0 = this.blockPosition().getCenter();
        return param0.closerThan(var0, 20.0, 10.0) && !param0.closerThan(var0, 8.0, 10.0);
    }

    public boolean withinMiddleCircleRange(Vec3 param0) {
        Vec3 var0 = this.blockPosition().getCenter();
        return param0.closerThan(var0, 8.0, 10.0) && !param0.closerThan(var0, 4.0, 10.0);
    }

    public boolean withinInnerCircleRange(Vec3 param0) {
        Vec3 var0 = this.blockPosition().getCenter();
        return param0.closerThan(var0, 4.0, 10.0);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("breezeBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        this.level().getProfiler().popPush("breezeActivityUpdate");
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
        DebugPackets.sendBreezeInfo(this);
    }

    @Override
    public boolean canAttackType(EntityType<?> param0) {
        return param0 == EntityType.PLAYER;
    }

    @Override
    public int getMaxHeadYRot() {
        return 30;
    }

    @Override
    public int getHeadRotSpeed() {
        return 25;
    }

    public double getSnoutYPosition() {
        return this.getEyeY() - 0.4;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource param0) {
        return param0.is(DamageTypeTags.BREEZE_IMMUNE_TO) || param0.getEntity() instanceof Breeze || super.isInvulnerableTo(param0);
    }

    @Override
    public double getFluidJumpThreshold() {
        return (double)this.getEyeHeight();
    }

    @Override
    public boolean causeFallDamage(float param0, float param1, DamageSource param2) {
        if (param0 > 3.0F) {
            this.playSound(SoundEvents.BREEZE_LAND, 1.0F, 1.0F);
        }

        return super.causeFallDamage(param0, param1, param2);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }
}
