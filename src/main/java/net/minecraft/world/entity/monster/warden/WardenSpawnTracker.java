package net.minecraft.world.entity.monster.warden;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WardenSpawnTracker {
    public static final Codec<WardenSpawnTracker> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("ticks_since_last_warning").orElse(0).forGetter(param0x -> param0x.ticksSinceLastWarning),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("warning_level").orElse(0).forGetter(param0x -> param0x.warningLevel),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("cooldown_ticks").orElse(0).forGetter(param0x -> param0x.cooldownTicks)
                )
                .apply(param0, WardenSpawnTracker::new)
    );
    public static final int MAX_WARNING_LEVEL = 3;
    private static final double PLAYER_SEARCH_RADIUS = 16.0;
    private static final int WARNING_CHECK_DIAMETER = 48;
    private static final int DECREASE_WARNING_LEVEL_EVERY_INTERVAL = 12000;
    private static final int WARNING_COOLDOWN_AFTER_DISTANT_SOUND = 200;
    private int ticksSinceLastWarning;
    private int warningLevel;
    private int cooldownTicks;

    public WardenSpawnTracker(int param0, int param1, int param2) {
        this.ticksSinceLastWarning = param0;
        this.warningLevel = param1;
        this.cooldownTicks = param2;
    }

    public void tick() {
        if (this.ticksSinceLastWarning >= 12000) {
            this.decreaseWarningLevel();
            this.ticksSinceLastWarning = 0;
        } else {
            ++this.ticksSinceLastWarning;
        }

        if (this.cooldownTicks > 0) {
            --this.cooldownTicks;
        }

    }

    public void reset() {
        this.ticksSinceLastWarning = 0;
        this.warningLevel = 0;
        this.cooldownTicks = 0;
    }

    public boolean warn(ServerLevel param0, BlockPos param1) {
        if (!this.canWarn(param0, param1)) {
            return false;
        } else {
            List<ServerPlayer> var0 = getNearbyPlayers(param0, param1);
            if (var0.isEmpty()) {
                return false;
            } else {
                Optional<WardenSpawnTracker> var1 = var0.stream()
                    .map(Player::getWardenSpawnTracker)
                    .max(Comparator.comparingInt(param0x -> param0x.warningLevel));
                var1.ifPresent(param1x -> {
                    param1x.increaseWarningLevel();
                    var0.forEach(param1xx -> param1xx.getWardenSpawnTracker().copyWarningLevelFrom(param1x));
                });
                return true;
            }
        }
    }

    public boolean canWarn(ServerLevel param0, BlockPos param1) {
        if (this.cooldownTicks <= 0 && param0.getDifficulty() != Difficulty.PEACEFUL) {
            AABB var0 = AABB.ofSize(Vec3.atCenterOf(param1), 48.0, 48.0, 48.0);
            return param0.getEntitiesOfClass(Warden.class, var0).isEmpty();
        } else {
            return false;
        }
    }

    private static List<ServerPlayer> getNearbyPlayers(ServerLevel param0, BlockPos param1) {
        Vec3 var0 = Vec3.atCenterOf(param1);
        Predicate<ServerPlayer> var1 = param1x -> param1x.position().closerThan(var0, 16.0);
        return param0.getPlayers(var1.and(LivingEntity::isAlive));
    }

    private void increaseWarningLevel() {
        if (this.cooldownTicks <= 0) {
            this.ticksSinceLastWarning = 0;
            this.cooldownTicks = 200;
            this.setWarningLevel(this.getWarningLevel() + 1);
        }

    }

    private void decreaseWarningLevel() {
        this.setWarningLevel(this.getWarningLevel() - 1);
    }

    public void setWarningLevel(int param0) {
        this.warningLevel = Mth.clamp(param0, 0, 3);
    }

    public int getWarningLevel() {
        return this.warningLevel;
    }

    private void copyWarningLevelFrom(WardenSpawnTracker param0) {
        this.ticksSinceLastWarning = param0.ticksSinceLastWarning;
        this.warningLevel = param0.warningLevel;
        this.cooldownTicks = param0.cooldownTicks;
    }
}
