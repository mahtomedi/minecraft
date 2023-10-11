package net.minecraft.world.entity;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;

public abstract class TamableAnimal extends Animal implements OwnableEntity {
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(TamableAnimal.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(
        TamableAnimal.class, EntityDataSerializers.OPTIONAL_UUID
    );
    private boolean orderedToSit;

    protected TamableAnimal(EntityType<? extends TamableAnimal> param0, Level param1) {
        super(param0, param1);
        this.reassessTameGoals();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
        this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        if (this.getOwnerUUID() != null) {
            param0.putUUID("Owner", this.getOwnerUUID());
        }

        param0.putBoolean("Sitting", this.orderedToSit);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        UUID var0;
        if (param0.hasUUID("Owner")) {
            var0 = param0.getUUID("Owner");
        } else {
            String var1 = param0.getString("Owner");
            var0 = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), var1);
        }

        if (var0 != null) {
            try {
                this.setOwnerUUID(var0);
                this.setTame(true);
            } catch (Throwable var4) {
                this.setTame(false);
            }
        }

        this.orderedToSit = param0.getBoolean("Sitting");
        this.setInSittingPose(this.orderedToSit);
    }

    @Override
    public boolean canBeLeashed(Player param0) {
        return !this.isLeashed();
    }

    protected void spawnTamingParticles(boolean param0) {
        ParticleOptions var0 = ParticleTypes.HEART;
        if (!param0) {
            var0 = ParticleTypes.SMOKE;
        }

        for(int var1 = 0; var1 < 7; ++var1) {
            double var2 = this.random.nextGaussian() * 0.02;
            double var3 = this.random.nextGaussian() * 0.02;
            double var4 = this.random.nextGaussian() * 0.02;
            this.level().addParticle(var0, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), var2, var3, var4);
        }

    }

    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 7) {
            this.spawnTamingParticles(true);
        } else if (param0 == 6) {
            this.spawnTamingParticles(false);
        } else {
            super.handleEntityEvent(param0);
        }

    }

    public boolean isTame() {
        return (this.entityData.get(DATA_FLAGS_ID) & 4) != 0;
    }

    public void setTame(boolean param0) {
        byte var0 = this.entityData.get(DATA_FLAGS_ID);
        if (param0) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(var0 | 4));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(var0 & -5));
        }

        this.reassessTameGoals();
    }

    protected void reassessTameGoals() {
    }

    public boolean isInSittingPose() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setInSittingPose(boolean param0) {
        byte var0 = this.entityData.get(DATA_FLAGS_ID);
        if (param0) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(var0 | 1));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(var0 & -2));
        }

    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID param0) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(param0));
    }

    public void tame(Player param0) {
        this.setTame(true);
        this.setOwnerUUID(param0.getUUID());
        if (param0 instanceof ServerPlayer) {
            CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer)param0, this);
        }

    }

    @Override
    public boolean canAttack(LivingEntity param0) {
        return this.isOwnedBy(param0) ? false : super.canAttack(param0);
    }

    public boolean isOwnedBy(LivingEntity param0) {
        return param0 == this.getOwner();
    }

    public boolean wantsToAttack(LivingEntity param0, LivingEntity param1) {
        return true;
    }

    @Override
    public PlayerTeam getTeam() {
        if (this.isTame()) {
            LivingEntity var0 = this.getOwner();
            if (var0 != null) {
                return var0.getTeam();
            }
        }

        return super.getTeam();
    }

    @Override
    public boolean isAlliedTo(Entity param0) {
        if (this.isTame()) {
            LivingEntity var0 = this.getOwner();
            if (param0 == var0) {
                return true;
            }

            if (var0 != null) {
                return var0.isAlliedTo(param0);
            }
        }

        return super.isAlliedTo(param0);
    }

    @Override
    public void die(DamageSource param0) {
        if (!this.level().isClientSide && this.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && this.getOwner() instanceof ServerPlayer) {
            this.getOwner().sendSystemMessage(this.getCombatTracker().getDeathMessage());
        }

        super.die(param0);
    }

    public boolean isOrderedToSit() {
        return this.orderedToSit;
    }

    public void setOrderedToSit(boolean param0) {
        this.orderedToSit = param0;
    }
}
