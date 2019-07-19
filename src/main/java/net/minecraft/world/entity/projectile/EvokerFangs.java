package net.minecraft.world.entity.projectile;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EvokerFangs extends Entity {
    private int warmupDelayTicks;
    private boolean sentSpikeEvent;
    private int lifeTicks = 22;
    private boolean clientSideAttackStarted;
    private LivingEntity owner;
    private UUID ownerUUID;

    public EvokerFangs(EntityType<? extends EvokerFangs> param0, Level param1) {
        super(param0, param1);
    }

    public EvokerFangs(Level param0, double param1, double param2, double param3, float param4, int param5, LivingEntity param6) {
        this(EntityType.EVOKER_FANGS, param0);
        this.warmupDelayTicks = param5;
        this.setOwner(param6);
        this.yRot = param4 * (180.0F / (float)Math.PI);
        this.setPos(param1, param2, param3);
    }

    @Override
    protected void defineSynchedData() {
    }

    public void setOwner(@Nullable LivingEntity param0) {
        this.owner = param0;
        this.ownerUUID = param0 == null ? null : param0.getUUID();
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUUID != null && this.level instanceof ServerLevel) {
            Entity var0 = ((ServerLevel)this.level).getEntity(this.ownerUUID);
            if (var0 instanceof LivingEntity) {
                this.owner = (LivingEntity)var0;
            }
        }

        return this.owner;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        this.warmupDelayTicks = param0.getInt("Warmup");
        if (param0.hasUUID("OwnerUUID")) {
            this.ownerUUID = param0.getUUID("OwnerUUID");
        }

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        param0.putInt("Warmup", this.warmupDelayTicks);
        if (this.ownerUUID != null) {
            param0.putUUID("OwnerUUID", this.ownerUUID);
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            if (this.clientSideAttackStarted) {
                --this.lifeTicks;
                if (this.lifeTicks == 14) {
                    for(int var0 = 0; var0 < 12; ++var0) {
                        double var1 = this.x + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getBbWidth() * 0.5;
                        double var2 = this.y + 0.05 + this.random.nextDouble();
                        double var3 = this.z + (this.random.nextDouble() * 2.0 - 1.0) * (double)this.getBbWidth() * 0.5;
                        double var4 = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
                        double var5 = 0.3 + this.random.nextDouble() * 0.3;
                        double var6 = (this.random.nextDouble() * 2.0 - 1.0) * 0.3;
                        this.level.addParticle(ParticleTypes.CRIT, var1, var2 + 1.0, var3, var4, var5, var6);
                    }
                }
            }
        } else if (--this.warmupDelayTicks < 0) {
            if (this.warmupDelayTicks == -8) {
                for(LivingEntity var8 : this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.2, 0.0, 0.2))) {
                    this.dealDamageTo(var8);
                }
            }

            if (!this.sentSpikeEvent) {
                this.level.broadcastEntityEvent(this, (byte)4);
                this.sentSpikeEvent = true;
            }

            if (--this.lifeTicks < 0) {
                this.remove();
            }
        }

    }

    private void dealDamageTo(LivingEntity param0) {
        LivingEntity var0 = this.getOwner();
        if (param0.isAlive() && !param0.isInvulnerable() && param0 != var0) {
            if (var0 == null) {
                param0.hurt(DamageSource.MAGIC, 6.0F);
            } else {
                if (var0.isAlliedTo(param0)) {
                    return;
                }

                param0.hurt(DamageSource.indirectMagic(this, var0), 6.0F);
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte param0) {
        super.handleEntityEvent(param0);
        if (param0 == 4) {
            this.clientSideAttackStarted = true;
            if (!this.isSilent()) {
                this.level
                    .playLocalSound(
                        this.x, this.y, this.z, SoundEvents.EVOKER_FANGS_ATTACK, this.getSoundSource(), 1.0F, this.random.nextFloat() * 0.2F + 0.85F, false
                    );
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public float getAnimationProgress(float param0) {
        if (!this.clientSideAttackStarted) {
            return 0.0F;
        } else {
            int var0 = this.lifeTicks - 2;
            return var0 <= 0 ? 1.0F : 1.0F - ((float)var0 - param0) / 20.0F;
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
