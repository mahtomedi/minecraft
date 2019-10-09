package net.minecraft.world.entity.projectile;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ShulkerBullet extends Entity {
    private LivingEntity owner;
    private Entity finalTarget;
    @Nullable
    private Direction currentMoveDirection;
    private int flightSteps;
    private double targetDeltaX;
    private double targetDeltaY;
    private double targetDeltaZ;
    @Nullable
    private UUID ownerId;
    private BlockPos lastKnownOwnerPos;
    @Nullable
    private UUID targetId;
    private BlockPos lastKnownTargetPos;

    public ShulkerBullet(EntityType<? extends ShulkerBullet> param0, Level param1) {
        super(param0, param1);
        this.noPhysics = true;
    }

    @OnlyIn(Dist.CLIENT)
    public ShulkerBullet(Level param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        this(EntityType.SHULKER_BULLET, param0);
        this.moveTo(param1, param2, param3, this.yRot, this.xRot);
        this.setDeltaMovement(param4, param5, param6);
    }

    public ShulkerBullet(Level param0, LivingEntity param1, Entity param2, Direction.Axis param3) {
        this(EntityType.SHULKER_BULLET, param0);
        this.owner = param1;
        BlockPos var0 = new BlockPos(param1);
        double var1 = (double)var0.getX() + 0.5;
        double var2 = (double)var0.getY() + 0.5;
        double var3 = (double)var0.getZ() + 0.5;
        this.moveTo(var1, var2, var3, this.yRot, this.xRot);
        this.finalTarget = param2;
        this.currentMoveDirection = Direction.UP;
        this.selectNextMoveDirection(param3);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        if (this.owner != null) {
            BlockPos var0 = new BlockPos(this.owner);
            CompoundTag var1 = NbtUtils.createUUIDTag(this.owner.getUUID());
            var1.putInt("X", var0.getX());
            var1.putInt("Y", var0.getY());
            var1.putInt("Z", var0.getZ());
            param0.put("Owner", var1);
        }

        if (this.finalTarget != null) {
            BlockPos var2 = new BlockPos(this.finalTarget);
            CompoundTag var3 = NbtUtils.createUUIDTag(this.finalTarget.getUUID());
            var3.putInt("X", var2.getX());
            var3.putInt("Y", var2.getY());
            var3.putInt("Z", var2.getZ());
            param0.put("Target", var3);
        }

        if (this.currentMoveDirection != null) {
            param0.putInt("Dir", this.currentMoveDirection.get3DDataValue());
        }

        param0.putInt("Steps", this.flightSteps);
        param0.putDouble("TXD", this.targetDeltaX);
        param0.putDouble("TYD", this.targetDeltaY);
        param0.putDouble("TZD", this.targetDeltaZ);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        this.flightSteps = param0.getInt("Steps");
        this.targetDeltaX = param0.getDouble("TXD");
        this.targetDeltaY = param0.getDouble("TYD");
        this.targetDeltaZ = param0.getDouble("TZD");
        if (param0.contains("Dir", 99)) {
            this.currentMoveDirection = Direction.from3DDataValue(param0.getInt("Dir"));
        }

        if (param0.contains("Owner", 10)) {
            CompoundTag var0 = param0.getCompound("Owner");
            this.ownerId = NbtUtils.loadUUIDTag(var0);
            this.lastKnownOwnerPos = new BlockPos(var0.getInt("X"), var0.getInt("Y"), var0.getInt("Z"));
        }

        if (param0.contains("Target", 10)) {
            CompoundTag var1 = param0.getCompound("Target");
            this.targetId = NbtUtils.loadUUIDTag(var1);
            this.lastKnownTargetPos = new BlockPos(var1.getInt("X"), var1.getInt("Y"), var1.getInt("Z"));
        }

    }

    @Override
    protected void defineSynchedData() {
    }

    private void setMoveDirection(@Nullable Direction param0) {
        this.currentMoveDirection = param0;
    }

    private void selectNextMoveDirection(@Nullable Direction.Axis param0) {
        double var0 = 0.5;
        BlockPos var1;
        if (this.finalTarget == null) {
            var1 = new BlockPos(this).below();
        } else {
            var0 = (double)this.finalTarget.getBbHeight() * 0.5;
            var1 = new BlockPos(this.finalTarget.getX(), this.finalTarget.getY() + var0, this.finalTarget.getZ());
        }

        double var3 = (double)var1.getX() + 0.5;
        double var4 = (double)var1.getY() + var0;
        double var5 = (double)var1.getZ() + 0.5;
        Direction var6 = null;
        if (!var1.closerThan(this.position(), 2.0)) {
            BlockPos var7 = new BlockPos(this);
            List<Direction> var8 = Lists.newArrayList();
            if (param0 != Direction.Axis.X) {
                if (var7.getX() < var1.getX() && this.level.isEmptyBlock(var7.east())) {
                    var8.add(Direction.EAST);
                } else if (var7.getX() > var1.getX() && this.level.isEmptyBlock(var7.west())) {
                    var8.add(Direction.WEST);
                }
            }

            if (param0 != Direction.Axis.Y) {
                if (var7.getY() < var1.getY() && this.level.isEmptyBlock(var7.above())) {
                    var8.add(Direction.UP);
                } else if (var7.getY() > var1.getY() && this.level.isEmptyBlock(var7.below())) {
                    var8.add(Direction.DOWN);
                }
            }

            if (param0 != Direction.Axis.Z) {
                if (var7.getZ() < var1.getZ() && this.level.isEmptyBlock(var7.south())) {
                    var8.add(Direction.SOUTH);
                } else if (var7.getZ() > var1.getZ() && this.level.isEmptyBlock(var7.north())) {
                    var8.add(Direction.NORTH);
                }
            }

            var6 = Direction.getRandomFace(this.random);
            if (var8.isEmpty()) {
                for(int var9 = 5; !this.level.isEmptyBlock(var7.relative(var6)) && var9 > 0; --var9) {
                    var6 = Direction.getRandomFace(this.random);
                }
            } else {
                var6 = var8.get(this.random.nextInt(var8.size()));
            }

            var3 = this.getX() + (double)var6.getStepX();
            var4 = this.getY() + (double)var6.getStepY();
            var5 = this.getZ() + (double)var6.getStepZ();
        }

        this.setMoveDirection(var6);
        double var10 = var3 - this.getX();
        double var11 = var4 - this.getY();
        double var12 = var5 - this.getZ();
        double var13 = (double)Mth.sqrt(var10 * var10 + var11 * var11 + var12 * var12);
        if (var13 == 0.0) {
            this.targetDeltaX = 0.0;
            this.targetDeltaY = 0.0;
            this.targetDeltaZ = 0.0;
        } else {
            this.targetDeltaX = var10 / var13 * 0.15;
            this.targetDeltaY = var11 / var13 * 0.15;
            this.targetDeltaZ = var12 / var13 * 0.15;
        }

        this.hasImpulse = true;
        this.flightSteps = 10 + this.random.nextInt(5) * 10;
    }

    @Override
    public void tick() {
        if (!this.level.isClientSide && this.level.getDifficulty() == Difficulty.PEACEFUL) {
            this.remove();
        } else {
            super.tick();
            if (!this.level.isClientSide) {
                if (this.finalTarget == null && this.targetId != null) {
                    for(LivingEntity var1 : this.level
                        .getEntitiesOfClass(LivingEntity.class, new AABB(this.lastKnownTargetPos.offset(-2, -2, -2), this.lastKnownTargetPos.offset(2, 2, 2)))) {
                        if (var1.getUUID().equals(this.targetId)) {
                            this.finalTarget = var1;
                            break;
                        }
                    }

                    this.targetId = null;
                }

                if (this.owner == null && this.ownerId != null) {
                    for(LivingEntity var3 : this.level
                        .getEntitiesOfClass(LivingEntity.class, new AABB(this.lastKnownOwnerPos.offset(-2, -2, -2), this.lastKnownOwnerPos.offset(2, 2, 2)))) {
                        if (var3.getUUID().equals(this.ownerId)) {
                            this.owner = var3;
                            break;
                        }
                    }

                    this.ownerId = null;
                }

                if (this.finalTarget == null || !this.finalTarget.isAlive() || this.finalTarget instanceof Player && ((Player)this.finalTarget).isSpectator()) {
                    if (!this.isNoGravity()) {
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
                    }
                } else {
                    this.targetDeltaX = Mth.clamp(this.targetDeltaX * 1.025, -1.0, 1.0);
                    this.targetDeltaY = Mth.clamp(this.targetDeltaY * 1.025, -1.0, 1.0);
                    this.targetDeltaZ = Mth.clamp(this.targetDeltaZ * 1.025, -1.0, 1.0);
                    Vec3 var4 = this.getDeltaMovement();
                    this.setDeltaMovement(var4.add((this.targetDeltaX - var4.x) * 0.2, (this.targetDeltaY - var4.y) * 0.2, (this.targetDeltaZ - var4.z) * 0.2));
                }

                HitResult var5 = ProjectileUtil.forwardsRaycast(this, true, false, this.owner, ClipContext.Block.COLLIDER);
                if (var5.getType() != HitResult.Type.MISS) {
                    this.onHit(var5);
                }
            }

            Vec3 var6 = this.getDeltaMovement();
            this.setPos(this.getX() + var6.x, this.getY() + var6.y, this.getZ() + var6.z);
            ProjectileUtil.rotateTowardsMovement(this, 0.5F);
            if (this.level.isClientSide) {
                this.level.addParticle(ParticleTypes.END_ROD, this.getX() - var6.x, this.getY() - var6.y + 0.15, this.getZ() - var6.z, 0.0, 0.0, 0.0);
            } else if (this.finalTarget != null && !this.finalTarget.removed) {
                if (this.flightSteps > 0) {
                    --this.flightSteps;
                    if (this.flightSteps == 0) {
                        this.selectNextMoveDirection(this.currentMoveDirection == null ? null : this.currentMoveDirection.getAxis());
                    }
                }

                if (this.currentMoveDirection != null) {
                    BlockPos var7 = new BlockPos(this);
                    Direction.Axis var8 = this.currentMoveDirection.getAxis();
                    if (this.level.loadedAndEntityCanStandOn(var7.relative(this.currentMoveDirection), this)) {
                        this.selectNextMoveDirection(var8);
                    } else {
                        BlockPos var9 = new BlockPos(this.finalTarget);
                        if (var8 == Direction.Axis.X && var7.getX() == var9.getX()
                            || var8 == Direction.Axis.Z && var7.getZ() == var9.getZ()
                            || var8 == Direction.Axis.Y && var7.getY() == var9.getY()) {
                            this.selectNextMoveDirection(var8);
                        }
                    }
                }
            }

        }
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRenderAtSqrDistance(double param0) {
        return param0 < 16384.0;
    }

    @Override
    public float getBrightness() {
        return 1.0F;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getLightColor() {
        return 15728880;
    }

    protected void onHit(HitResult param0) {
        if (param0.getType() == HitResult.Type.ENTITY) {
            Entity var0 = ((EntityHitResult)param0).getEntity();
            boolean var1 = var0.hurt(DamageSource.indirectMobAttack(this, this.owner).setProjectile(), 4.0F);
            if (var1) {
                this.doEnchantDamageEffects(this.owner, var0);
                if (var0 instanceof LivingEntity) {
                    ((LivingEntity)var0).addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200));
                }
            }
        } else {
            ((ServerLevel)this.level).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
            this.playSound(SoundEvents.SHULKER_BULLET_HIT, 1.0F, 1.0F);
        }

        this.remove();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (!this.level.isClientSide) {
            this.playSound(SoundEvents.SHULKER_BULLET_HURT, 1.0F, 1.0F);
            ((ServerLevel)this.level).sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 15, 0.2, 0.2, 0.2, 0.0);
            this.remove();
        }

        return true;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
