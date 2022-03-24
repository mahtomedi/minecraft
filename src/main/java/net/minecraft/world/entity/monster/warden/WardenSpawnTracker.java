package net.minecraft.world.entity.monster.warden;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
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
    public static final int WARNINGS_UNTIL_SPAWN = 3;
    private static final int WARNING_SOUND_RADIUS = 10;
    private static final int WARNING_CHECK_DIAMETER = 48;
    private static final int DECREASE_WARNING_LEVEL_EVERY_INTERVAL = 12000;
    private static final int WARNING_COOLDOWN_AFTER_DISTANT_SOUND = 200;
    private static final int WARDEN_SPAWN_ATTEMPTS = 20;
    private static final int WARDEN_SPAWN_RANGE_XZ = 5;
    private static final int WARDEN_SPAWN_RANGE_Y = 6;
    private static final int DARKNESS_RADIUS = 40;
    private static final Int2ObjectMap<SoundEvent> SOUND_BY_LEVEL = Util.make(new Int2ObjectOpenHashMap<>(), param0 -> {
        param0.put(1, SoundEvents.WARDEN_NEARBY_CLOSE);
        param0.put(2, SoundEvents.WARDEN_NEARBY_CLOSER);
        param0.put(3, SoundEvents.WARDEN_NEARBY_CLOSEST);
    });
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

    public boolean prepareWarningEvent(ServerLevel param0, BlockPos param1) {
        if (!this.canPrepareWarningEvent(param0, param1)) {
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

    public boolean canPrepareWarningEvent(ServerLevel param0, BlockPos param1) {
        if (this.cooldownTicks > 0) {
            return false;
        } else {
            AABB var0 = AABB.ofSize(Vec3.atCenterOf(param1), 48.0, 48.0, 48.0);
            return param0.getEntitiesOfClass(Warden.class, var0).isEmpty();
        }
    }

    private static List<ServerPlayer> getNearbyPlayers(ServerLevel param0, BlockPos param1) {
        Vec3 var0 = Vec3.atCenterOf(param1);
        double var1 = 16.0;
        Predicate<ServerPlayer> var2 = param1x -> param1x.position().closerThan(var0, 16.0);
        return param0.getPlayers(var2.and(LivingEntity::isAlive));
    }

    public void triggerWarningEvent(ServerLevel param0, BlockPos param1) {
        if (this.getWarningLevel() < 3) {
            Warden.applyDarknessAround(param0, Vec3.atCenterOf(param1), null, 40);
            playWarningSound(param0, param1, this.warningLevel);
        } else {
            summonWarden(param0, param1);
        }

    }

    private static void playWarningSound(ServerLevel param0, BlockPos param1, int param2) {
        SoundEvent var0 = SOUND_BY_LEVEL.get(param2);
        if (var0 != null) {
            int var1 = param1.getX() + Mth.randomBetweenInclusive(param0.random, -10, 10);
            int var2 = param1.getY() + Mth.randomBetweenInclusive(param0.random, -10, 10);
            int var3 = param1.getZ() + Mth.randomBetweenInclusive(param0.random, -10, 10);
            param0.playSound(null, (double)var1, (double)var2, (double)var3, var0, SoundSource.HOSTILE, 5.0F, 1.0F);
        }

    }

    private static void summonWarden(ServerLevel param0, BlockPos param1) {
        Optional<Warden> var0 = SpawnUtil.trySpawnMob(EntityType.WARDEN, param0, param1, 20, 5, 6);
        var0.ifPresent(param1x -> {
            param1x.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_EMERGING, Unit.INSTANCE, (long)WardenAi.EMERGE_DURATION);
            param0.playSound(null, param1x.getX(), param1x.getY(), param1x.getZ(), SoundEvents.WARDEN_AGITATED, SoundSource.BLOCKS, 5.0F, 1.0F);
        });
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

    private int getWarningLevel() {
        return this.warningLevel;
    }

    private void copyWarningLevelFrom(WardenSpawnTracker param0) {
        this.ticksSinceLastWarning = param0.ticksSinceLastWarning;
        this.warningLevel = param0.warningLevel;
        this.cooldownTicks = param0.cooldownTicks;
    }
}
