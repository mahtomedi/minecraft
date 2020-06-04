package net.minecraft.world.entity;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public interface NeutralMob {
    int getRemainingPersistentAngerTime();

    void setRemainingPersistentAngerTime(int var1);

    @Nullable
    UUID getPersistentAngerTarget();

    void setPersistentAngerTarget(@Nullable UUID var1);

    void startPersistentAngerTimer();

    default void addPersistentAngerSaveData(CompoundTag param0) {
        param0.putInt("AngerTime", this.getRemainingPersistentAngerTime());
        if (this.getPersistentAngerTarget() != null) {
            param0.putUUID("AngryAt", this.getPersistentAngerTarget());
        }

    }

    default void readPersistentAngerSaveData(Level param0, CompoundTag param1) {
        this.setRemainingPersistentAngerTime(param1.getInt("AngerTime"));
        if (param1.hasUUID("AngryAt")) {
            this.setPersistentAngerTarget(param1.getUUID("AngryAt"));
            UUID var0 = this.getPersistentAngerTarget();
            Player var1 = var0 == null ? null : param0.getPlayerByUUID(var0);
            if (var1 != null) {
                this.setLastHurtByMob(var1);
                this.setLastHurtByPlayer(var1);
            }
        }

    }

    default void updatePersistentAnger() {
        LivingEntity var0 = this.getTarget();
        if (var0 != null && var0.getType() == EntityType.PLAYER) {
            this.setPersistentAngerTarget(var0.getUUID());
            if (this.getRemainingPersistentAngerTime() <= 0) {
                this.startPersistentAngerTimer();
            }
        } else {
            int var1 = this.getRemainingPersistentAngerTime();
            if (var1 > 0) {
                this.setRemainingPersistentAngerTime(var1 - 1);
                if (this.getRemainingPersistentAngerTime() == 0) {
                    this.setPersistentAngerTarget(null);
                }
            }
        }

    }

    default boolean isAngryAt(LivingEntity param0) {
        if (param0 instanceof Player && EntitySelector.ATTACK_ALLOWED.test(param0)) {
            boolean var0 = param0.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER);
            return var0 ? this.isAngry() : param0.getUUID().equals(this.getPersistentAngerTarget());
        } else {
            return false;
        }
    }

    default boolean isAngry() {
        return this.getRemainingPersistentAngerTime() > 0;
    }

    default void playerDied(Player param0) {
        if (!param0.level.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
        }

        if (param0.getUUID().equals(this.getPersistentAngerTarget())) {
            this.setLastHurtByMob(null);
            this.setPersistentAngerTarget(null);
            this.setTarget(null);
            this.setRemainingPersistentAngerTime(0);
        }
    }

    void setLastHurtByMob(@Nullable LivingEntity var1);

    void setLastHurtByPlayer(@Nullable Player var1);

    void setTarget(@Nullable LivingEntity var1);

    @Nullable
    LivingEntity getTarget();
}
