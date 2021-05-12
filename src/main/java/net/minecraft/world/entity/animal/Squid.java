package net.minecraft.world.entity.animal;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class Squid extends WaterAnimal {
    public float xBodyRot;
    public float xBodyRotO;
    public float zBodyRot;
    public float zBodyRotO;
    public float tentacleMovement;
    public float oldTentacleMovement;
    public float tentacleAngle;
    public float oldTentacleAngle;
    private float speed;
    private float tentacleSpeed;
    private float rotateSpeed;
    private float tx;
    private float ty;
    private float tz;

    public Squid(EntityType<? extends Squid> param0, Level param1) {
        super(param0, param1);
        this.random.setSeed((long)this.getId());
        this.tentacleSpeed = 1.0F / (this.random.nextFloat() + 1.0F) * 0.2F;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new Squid.SquidRandomMovementGoal(this));
        this.goalSelector.addGoal(1, new Squid.SquidFleeGoal());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0);
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return param1.height * 0.5F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SQUID_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.SQUID_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SQUID_DEATH;
    }

    protected SoundEvent getSquirtSound() {
        return SoundEvents.SQUID_SQUIRT;
    }

    @Override
    public boolean canBeLeashed(Player param0) {
        return !this.isLeashed();
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.xBodyRotO = this.xBodyRot;
        this.zBodyRotO = this.zBodyRot;
        this.oldTentacleMovement = this.tentacleMovement;
        this.oldTentacleAngle = this.tentacleAngle;
        this.tentacleMovement += this.tentacleSpeed;
        if ((double)this.tentacleMovement > Math.PI * 2) {
            if (this.level.isClientSide) {
                this.tentacleMovement = (float) (Math.PI * 2);
            } else {
                this.tentacleMovement = (float)((double)this.tentacleMovement - (Math.PI * 2));
                if (this.random.nextInt(10) == 0) {
                    this.tentacleSpeed = 1.0F / (this.random.nextFloat() + 1.0F) * 0.2F;
                }

                this.level.broadcastEntityEvent(this, (byte)19);
            }
        }

        if (this.isInWaterOrBubble()) {
            if (this.tentacleMovement < (float) Math.PI) {
                float var0 = this.tentacleMovement / (float) Math.PI;
                this.tentacleAngle = Mth.sin(var0 * var0 * (float) Math.PI) * (float) Math.PI * 0.25F;
                if ((double)var0 > 0.75) {
                    this.speed = 1.0F;
                    this.rotateSpeed = 1.0F;
                } else {
                    this.rotateSpeed *= 0.8F;
                }
            } else {
                this.tentacleAngle = 0.0F;
                this.speed *= 0.9F;
                this.rotateSpeed *= 0.99F;
            }

            if (!this.level.isClientSide) {
                this.setDeltaMovement((double)(this.tx * this.speed), (double)(this.ty * this.speed), (double)(this.tz * this.speed));
            }

            Vec3 var1 = this.getDeltaMovement();
            float var2 = Mth.sqrt(getHorizontalDistanceSqr(var1));
            this.yBodyRot += (-((float)Mth.atan2(var1.x, var1.z)) * (180.0F / (float)Math.PI) - this.yBodyRot) * 0.1F;
            this.setYRot(this.yBodyRot);
            this.zBodyRot = (float)((double)this.zBodyRot + Math.PI * (double)this.rotateSpeed * 1.5);
            this.xBodyRot += (-((float)Mth.atan2((double)var2, var1.y)) * (180.0F / (float)Math.PI) - this.xBodyRot) * 0.1F;
        } else {
            this.tentacleAngle = Mth.abs(Mth.sin(this.tentacleMovement)) * (float) Math.PI * 0.25F;
            if (!this.level.isClientSide) {
                double var3 = this.getDeltaMovement().y;
                if (this.hasEffect(MobEffects.LEVITATION)) {
                    var3 = 0.05 * (double)(this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1);
                } else if (!this.isNoGravity()) {
                    var3 -= 0.08;
                }

                this.setDeltaMovement(0.0, var3 * 0.98F, 0.0);
            }

            this.xBodyRot = (float)((double)this.xBodyRot + (double)(-90.0F - this.xBodyRot) * 0.02);
        }

    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (super.hurt(param0, param1) && this.getLastHurtByMob() != null) {
            this.spawnInk();
            return true;
        } else {
            return false;
        }
    }

    private Vec3 rotateVector(Vec3 param0) {
        Vec3 var0 = param0.xRot(this.xBodyRotO * (float) (Math.PI / 180.0));
        return var0.yRot(-this.yBodyRotO * (float) (Math.PI / 180.0));
    }

    private void spawnInk() {
        this.playSound(this.getSquirtSound(), this.getSoundVolume(), this.getVoicePitch());
        Vec3 var0 = this.rotateVector(new Vec3(0.0, -1.0, 0.0)).add(this.getX(), this.getY(), this.getZ());

        for(int var1 = 0; var1 < 30; ++var1) {
            Vec3 var2 = this.rotateVector(new Vec3((double)this.random.nextFloat() * 0.6 - 0.3, -1.0, (double)this.random.nextFloat() * 0.6 - 0.3));
            Vec3 var3 = var2.scale(0.3 + (double)(this.random.nextFloat() * 2.0F));
            ((ServerLevel)this.level).sendParticles(this.getInkParticle(), var0.x, var0.y + 0.5, var0.z, 0, var3.x, var3.y, var3.z, 0.1F);
        }

    }

    protected ParticleOptions getInkParticle() {
        return ParticleTypes.SQUID_INK;
    }

    @Override
    public void travel(Vec3 param0) {
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    public static boolean checkSquidSpawnRules(EntityType<Squid> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
        return param3.getY() > 45 && param3.getY() < param1.getSeaLevel();
    }

    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 19) {
            this.tentacleMovement = 0.0F;
        } else {
            super.handleEntityEvent(param0);
        }

    }

    public void setMovementVector(float param0, float param1, float param2) {
        this.tx = param0;
        this.ty = param1;
        this.tz = param2;
    }

    public boolean hasMovementVector() {
        return this.tx != 0.0F || this.ty != 0.0F || this.tz != 0.0F;
    }

    class SquidFleeGoal extends Goal {
        private static final float SQUID_FLEE_SPEED = 3.0F;
        private static final float SQUID_FLEE_MIN_DISTANCE = 5.0F;
        private static final float SQUID_FLEE_MAX_DISTANCE = 10.0F;
        private int fleeTicks;

        @Override
        public boolean canUse() {
            LivingEntity var0 = Squid.this.getLastHurtByMob();
            if (Squid.this.isInWater() && var0 != null) {
                return Squid.this.distanceToSqr(var0) < 100.0;
            } else {
                return false;
            }
        }

        @Override
        public void start() {
            this.fleeTicks = 0;
        }

        @Override
        public void tick() {
            ++this.fleeTicks;
            LivingEntity var0 = Squid.this.getLastHurtByMob();
            if (var0 != null) {
                Vec3 var1 = new Vec3(Squid.this.getX() - var0.getX(), Squid.this.getY() - var0.getY(), Squid.this.getZ() - var0.getZ());
                BlockState var2 = Squid.this.level
                    .getBlockState(new BlockPos(Squid.this.getX() + var1.x, Squid.this.getY() + var1.y, Squid.this.getZ() + var1.z));
                FluidState var3 = Squid.this.level
                    .getFluidState(new BlockPos(Squid.this.getX() + var1.x, Squid.this.getY() + var1.y, Squid.this.getZ() + var1.z));
                if (var3.is(FluidTags.WATER) || var2.isAir()) {
                    double var4 = var1.length();
                    if (var4 > 0.0) {
                        var1.normalize();
                        float var5 = 3.0F;
                        if (var4 > 5.0) {
                            var5 = (float)((double)var5 - (var4 - 5.0) / 5.0);
                        }

                        if (var5 > 0.0F) {
                            var1 = var1.scale((double)var5);
                        }
                    }

                    if (var2.isAir()) {
                        var1 = var1.subtract(0.0, var1.y, 0.0);
                    }

                    Squid.this.setMovementVector((float)var1.x / 20.0F, (float)var1.y / 20.0F, (float)var1.z / 20.0F);
                }

                if (this.fleeTicks % 10 == 5) {
                    Squid.this.level.addParticle(ParticleTypes.BUBBLE, Squid.this.getX(), Squid.this.getY(), Squid.this.getZ(), 0.0, 0.0, 0.0);
                }

            }
        }
    }

    class SquidRandomMovementGoal extends Goal {
        private final Squid squid;

        public SquidRandomMovementGoal(Squid param0) {
            this.squid = param0;
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void tick() {
            int var0 = this.squid.getNoActionTime();
            if (var0 > 100) {
                this.squid.setMovementVector(0.0F, 0.0F, 0.0F);
            } else if (this.squid.getRandom().nextInt(50) == 0 || !this.squid.wasTouchingWater || !this.squid.hasMovementVector()) {
                float var1 = this.squid.getRandom().nextFloat() * (float) (Math.PI * 2);
                float var2 = Mth.cos(var1) * 0.2F;
                float var3 = -0.1F + this.squid.getRandom().nextFloat() * 0.2F;
                float var4 = Mth.sin(var1) * 0.2F;
                this.squid.setMovementVector(var2, var3, var4);
            }

        }
    }
}
