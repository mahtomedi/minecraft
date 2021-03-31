package net.minecraft.world.entity.projectile;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ShulkerBullet extends Projectile {
    private static final double SPEED = 0.15;
    private Entity finalTarget;
    @Nullable
    private Direction currentMoveDirection;
    private int flightSteps;
    private double targetDeltaX;
    private double targetDeltaY;
    private double targetDeltaZ;
    @Nullable
    private UUID targetId;

    public ShulkerBullet(EntityType<? extends ShulkerBullet> param0, Level param1) {
        super(param0, param1);
        this.noPhysics = true;
    }

    public ShulkerBullet(Level param0, LivingEntity param1, Entity param2, Direction.Axis param3) {
        this(EntityType.SHULKER_BULLET, param0);
        this.setOwner(param1);
        BlockPos var0 = param1.blockPosition();
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
        super.addAdditionalSaveData(param0);
        if (this.finalTarget != null) {
            param0.putUUID("Target", this.finalTarget.getUUID());
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
        super.readAdditionalSaveData(param0);
        this.flightSteps = param0.getInt("Steps");
        this.targetDeltaX = param0.getDouble("TXD");
        this.targetDeltaY = param0.getDouble("TYD");
        this.targetDeltaZ = param0.getDouble("TZD");
        if (param0.contains("Dir", 99)) {
            this.currentMoveDirection = Direction.from3DDataValue(param0.getInt("Dir"));
        }

        if (param0.hasUUID("Target")) {
            this.targetId = param0.getUUID("Target");
        }

    }

    @Override
    protected void defineSynchedData() {
    }

    @Nullable
    private Direction getMoveDirection() {
        return this.currentMoveDirection;
    }

    private void setMoveDirection(@Nullable Direction param0) {
        this.currentMoveDirection = param0;
    }

    private void selectNextMoveDirection(@Nullable Direction.Axis param0) {
        double var0 = 0.5;
        BlockPos var1;
        if (this.finalTarget == null) {
            var1 = this.blockPosition().below();
        } else {
            var0 = (double)this.finalTarget.getBbHeight() * 0.5;
            var1 = new BlockPos(this.finalTarget.getX(), this.finalTarget.getY() + var0, this.finalTarget.getZ());
        }

        double var3 = (double)var1.getX() + 0.5;
        double var4 = (double)var1.getY() + var0;
        double var5 = (double)var1.getZ() + 0.5;
        Direction var6 = null;
        if (!var1.closerThan(this.position(), 2.0)) {
            BlockPos var7 = this.blockPosition();
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

            var6 = Direction.getRandom(this.random);
            if (var8.isEmpty()) {
                for(int var9 = 5; !this.level.isEmptyBlock(var7.relative(var6)) && var9 > 0; --var9) {
                    var6 = Direction.getRandom(this.random);
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
    public void checkDespawn() {
        if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            if (this.finalTarget == null && this.targetId != null) {
                this.finalTarget = ((ServerLevel)this.level).getEntity(this.targetId);
                if (this.finalTarget == null) {
                    this.targetId = null;
                }
            }

            if (this.finalTarget == null || !this.finalTarget.isAlive() || this.finalTarget instanceof Player && ((Player)this.finalTarget).isSpectator()) {
                if (!this.isNoGravity()) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
                }
            } else {
                this.targetDeltaX = Mth.clamp(this.targetDeltaX * 1.025, -1.0, 1.0);
                this.targetDeltaY = Mth.clamp(this.targetDeltaY * 1.025, -1.0, 1.0);
                this.targetDeltaZ = Mth.clamp(this.targetDeltaZ * 1.025, -1.0, 1.0);
                Vec3 var0 = this.getDeltaMovement();
                this.setDeltaMovement(var0.add((this.targetDeltaX - var0.x) * 0.2, (this.targetDeltaY - var0.y) * 0.2, (this.targetDeltaZ - var0.z) * 0.2));
            }

            HitResult var1 = ProjectileUtil.getHitResult(this, this::canHitEntity);
            if (var1.getType() != HitResult.Type.MISS) {
                this.onHit(var1);
            }
        }

        this.checkInsideBlocks();
        Vec3 var2 = this.getDeltaMovement();
        this.setPos(this.getX() + var2.x, this.getY() + var2.y, this.getZ() + var2.z);
        ProjectileUtil.rotateTowardsMovement(this, 0.5F);
        if (this.level.isClientSide) {
            this.level.addParticle(ParticleTypes.END_ROD, this.getX() - var2.x, this.getY() - var2.y + 0.15, this.getZ() - var2.z, 0.0, 0.0, 0.0);
        } else if (this.finalTarget != null && !this.finalTarget.isRemoved()) {
            if (this.flightSteps > 0) {
                --this.flightSteps;
                if (this.flightSteps == 0) {
                    this.selectNextMoveDirection(this.currentMoveDirection == null ? null : this.currentMoveDirection.getAxis());
                }
            }

            if (this.currentMoveDirection != null) {
                BlockPos var3 = this.blockPosition();
                Direction.Axis var4 = this.currentMoveDirection.getAxis();
                if (this.level.loadedAndEntityCanStandOn(var3.relative(this.currentMoveDirection), this)) {
                    this.selectNextMoveDirection(var4);
                } else {
                    BlockPos var5 = this.finalTarget.blockPosition();
                    if (var4 == Direction.Axis.X && var3.getX() == var5.getX()
                        || var4 == Direction.Axis.Z && var3.getZ() == var5.getZ()
                        || var4 == Direction.Axis.Y && var3.getY() == var5.getY()) {
                        this.selectNextMoveDirection(var4);
                    }
                }
            }
        }

    }

    @Override
    protected boolean canHitEntity(Entity param0) {
        return super.canHitEntity(param0) && !param0.noPhysics;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double param0) {
        return param0 < 16384.0;
    }

    @Override
    public float getBrightness() {
        return 1.0F;
    }

    @Override
    protected void onHitEntity(EntityHitResult param0) {
        super.onHitEntity(param0);
        Entity var0 = param0.getEntity();
        Entity var1 = this.getOwner();
        LivingEntity var2 = var1 instanceof LivingEntity ? (LivingEntity)var1 : null;
        boolean var3 = var0.hurt(DamageSource.indirectMobAttack(this, var2).setProjectile(), 4.0F);
        if (var3) {
            this.doEnchantDamageEffects(var2, var0);
            if (var0 instanceof LivingEntity) {
                ((LivingEntity)var0).addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200));
            }
        }

    }

    @Override
    protected void onHitBlock(BlockHitResult param0) {
        super.onHitBlock(param0);
        ((ServerLevel)this.level).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
        this.playSound(SoundEvents.SHULKER_BULLET_HIT, 1.0F, 1.0F);
    }

    @Override
    protected void onHit(HitResult param0) {
        super.onHit(param0);
        this.discard();
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
            this.discard();
        }

        return true;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket param0) {
        super.recreateFromPacket(param0);
        double var0 = param0.getXa();
        double var1 = param0.getYa();
        double var2 = param0.getZa();
        this.setDeltaMovement(var0, var1, var2);
    }
}
