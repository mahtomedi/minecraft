package net.minecraft.world.level.gameevent.vibrations;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class VibrationListener implements GameEventListener {
    public static final GameEvent[] RESONANCE_EVENTS = new GameEvent[]{
        GameEvent.RESONATE_1,
        GameEvent.RESONATE_2,
        GameEvent.RESONATE_3,
        GameEvent.RESONATE_4,
        GameEvent.RESONATE_5,
        GameEvent.RESONATE_6,
        GameEvent.RESONATE_7,
        GameEvent.RESONATE_8,
        GameEvent.RESONATE_9,
        GameEvent.RESONATE_10,
        GameEvent.RESONATE_11,
        GameEvent.RESONATE_12,
        GameEvent.RESONATE_13,
        GameEvent.RESONATE_14,
        GameEvent.RESONATE_15
    };
    @VisibleForTesting
    public static final Object2IntMap<GameEvent> VIBRATION_FREQUENCY_FOR_EVENT = Object2IntMaps.unmodifiable(
        Util.make(new Object2IntOpenHashMap<>(), param0 -> {
            param0.put(GameEvent.STEP, 1);
            param0.put(GameEvent.SWIM, 1);
            param0.put(GameEvent.FLAP, 1);
            param0.put(GameEvent.PROJECTILE_LAND, 2);
            param0.put(GameEvent.HIT_GROUND, 2);
            param0.put(GameEvent.SPLASH, 2);
            param0.put(GameEvent.ITEM_INTERACT_FINISH, 3);
            param0.put(GameEvent.PROJECTILE_SHOOT, 3);
            param0.put(GameEvent.INSTRUMENT_PLAY, 3);
            param0.put(GameEvent.ENTITY_ROAR, 4);
            param0.put(GameEvent.ENTITY_SHAKE, 4);
            param0.put(GameEvent.ELYTRA_GLIDE, 4);
            param0.put(GameEvent.ENTITY_DISMOUNT, 5);
            param0.put(GameEvent.EQUIP, 5);
            param0.put(GameEvent.ENTITY_INTERACT, 6);
            param0.put(GameEvent.SHEAR, 6);
            param0.put(GameEvent.ENTITY_MOUNT, 6);
            param0.put(GameEvent.ENTITY_DAMAGE, 7);
            param0.put(GameEvent.DRINK, 8);
            param0.put(GameEvent.EAT, 8);
            param0.put(GameEvent.CONTAINER_CLOSE, 9);
            param0.put(GameEvent.BLOCK_CLOSE, 9);
            param0.put(GameEvent.BLOCK_DEACTIVATE, 9);
            param0.put(GameEvent.BLOCK_DETACH, 9);
            param0.put(GameEvent.CONTAINER_OPEN, 10);
            param0.put(GameEvent.BLOCK_OPEN, 10);
            param0.put(GameEvent.BLOCK_ACTIVATE, 10);
            param0.put(GameEvent.BLOCK_ATTACH, 10);
            param0.put(GameEvent.PRIME_FUSE, 10);
            param0.put(GameEvent.NOTE_BLOCK_PLAY, 10);
            param0.put(GameEvent.BLOCK_CHANGE, 11);
            param0.put(GameEvent.BLOCK_DESTROY, 12);
            param0.put(GameEvent.FLUID_PICKUP, 12);
            param0.put(GameEvent.BLOCK_PLACE, 13);
            param0.put(GameEvent.FLUID_PLACE, 13);
            param0.put(GameEvent.ENTITY_PLACE, 14);
            param0.put(GameEvent.LIGHTNING_STRIKE, 14);
            param0.put(GameEvent.TELEPORT, 14);
            param0.put(GameEvent.ENTITY_DIE, 15);
            param0.put(GameEvent.EXPLODE, 15);
    
            for(int var0 = 1; var0 <= 15; ++var0) {
                param0.put(getResonanceEventByFrequency(var0), var0);
            }
    
        })
    );
    private final PositionSource listenerSource;
    private final VibrationListener.Config config;
    @Nullable
    private VibrationInfo currentVibration;
    private int travelTimeInTicks;
    private final VibrationSelector selectionStrategy;

    public static Codec<VibrationListener> codec(VibrationListener.Config param0) {
        return RecordCodecBuilder.create(
            param1 -> param1.group(
                        PositionSource.CODEC.fieldOf("source").forGetter(param0x -> param0x.listenerSource),
                        VibrationInfo.CODEC.optionalFieldOf("event").forGetter(param0x -> Optional.ofNullable(param0x.currentVibration)),
                        VibrationSelector.CODEC.fieldOf("selector").forGetter(param0x -> param0x.selectionStrategy),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay").orElse(0).forGetter(param0x -> param0x.travelTimeInTicks)
                    )
                    .apply(param1, (param1x, param2, param3, param4) -> new VibrationListener(param1x, param0, param2.orElse(null), param3, param4))
        );
    }

    private VibrationListener(PositionSource param0, VibrationListener.Config param1, @Nullable VibrationInfo param2, VibrationSelector param3, int param4) {
        this.listenerSource = param0;
        this.config = param1;
        this.currentVibration = param2;
        this.travelTimeInTicks = param4;
        this.selectionStrategy = param3;
    }

    public VibrationListener(PositionSource param0, VibrationListener.Config param1) {
        this(param0, param1, null, new VibrationSelector(), 0);
    }

    public static int getGameEventFrequency(GameEvent param0) {
        return VIBRATION_FREQUENCY_FOR_EVENT.getOrDefault(param0, 0);
    }

    public static GameEvent getResonanceEventByFrequency(int param0) {
        return RESONANCE_EVENTS[param0 - 1];
    }

    public VibrationListener.Config getConfig() {
        return this.config;
    }

    public void tick(Level param0) {
        if (param0 instanceof ServerLevel var0) {
            if (this.currentVibration == null) {
                this.selectionStrategy
                    .chosenCandidate(var0.getGameTime())
                    .ifPresent(
                        param1 -> {
                            this.currentVibration = param1;
                            Vec3 var0x = this.currentVibration.pos();
                            this.travelTimeInTicks = Mth.floor(this.currentVibration.distance());
                            var0.sendParticles(
                                new VibrationParticleOption(this.listenerSource, this.travelTimeInTicks), var0x.x, var0x.y, var0x.z, 1, 0.0, 0.0, 0.0, 0.0
                            );
                            this.config.onSignalSchedule();
                            this.selectionStrategy.startOver();
                        }
                    );
            }

            if (this.currentVibration != null) {
                --this.travelTimeInTicks;
                if (this.travelTimeInTicks <= 0) {
                    this.travelTimeInTicks = 0;
                    this.config
                        .onSignalReceive(
                            var0,
                            this,
                            BlockPos.containing(this.currentVibration.pos()),
                            this.currentVibration.gameEvent(),
                            this.currentVibration.getEntity(var0).orElse(null),
                            this.currentVibration.getProjectileOwner(var0).orElse(null),
                            this.currentVibration.distance()
                        );
                    this.currentVibration = null;
                }
            }
        }

    }

    @Override
    public PositionSource getListenerSource() {
        return this.listenerSource;
    }

    @Override
    public int getListenerRadius() {
        return this.config.getListenerRadius();
    }

    @Override
    public boolean handleGameEvent(ServerLevel param0, GameEvent param1, GameEvent.Context param2, Vec3 param3) {
        if (this.currentVibration != null) {
            return false;
        } else if (!this.config.isValidVibration(param1, param2)) {
            return false;
        } else {
            Optional<Vec3> var0 = this.listenerSource.getPosition(param0);
            if (var0.isEmpty()) {
                return false;
            } else {
                Vec3 var1 = var0.get();
                if (!this.config.shouldListen(param0, this, BlockPos.containing(param3), param1, param2)) {
                    return false;
                } else if (isOccluded(param0, param3, var1)) {
                    return false;
                } else {
                    this.scheduleVibration(param0, param1, param2, param3, var1);
                    return true;
                }
            }
        }
    }

    public void forceGameEvent(ServerLevel param0, GameEvent param1, GameEvent.Context param2, Vec3 param3) {
        this.listenerSource.getPosition(param0).ifPresent(param4 -> this.scheduleVibration(param0, param1, param2, param3, param4));
    }

    public void scheduleVibration(ServerLevel param0, GameEvent param1, GameEvent.Context param2, Vec3 param3, Vec3 param4) {
        this.selectionStrategy.addCandidate(new VibrationInfo(param1, (float)param3.distanceTo(param4), param3, param2.sourceEntity()), param0.getGameTime());
    }

    private static boolean isOccluded(Level param0, Vec3 param1, Vec3 param2) {
        Vec3 var0 = new Vec3((double)Mth.floor(param1.x) + 0.5, (double)Mth.floor(param1.y) + 0.5, (double)Mth.floor(param1.z) + 0.5);
        Vec3 var1 = new Vec3((double)Mth.floor(param2.x) + 0.5, (double)Mth.floor(param2.y) + 0.5, (double)Mth.floor(param2.z) + 0.5);

        for(Direction var2 : Direction.values()) {
            Vec3 var3 = var0.relative(var2, 1.0E-5F);
            if (param0.isBlockInLine(new ClipBlockStateContext(var3, var1, param0x -> param0x.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS))).getType()
                != HitResult.Type.BLOCK) {
                return false;
            }
        }

        return true;
    }

    public interface Config {
        int getListenerRadius();

        default TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.VIBRATIONS;
        }

        default boolean canTriggerAvoidVibration() {
            return false;
        }

        default boolean isValidVibration(GameEvent param0, GameEvent.Context param1) {
            if (!param0.is(this.getListenableEvents())) {
                return false;
            } else {
                Entity var0 = param1.sourceEntity();
                if (var0 != null) {
                    if (var0.isSpectator()) {
                        return false;
                    }

                    if (var0.isSteppingCarefully() && param0.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)) {
                        if (this.canTriggerAvoidVibration() && var0 instanceof ServerPlayer var1) {
                            CriteriaTriggers.AVOID_VIBRATION.trigger(var1);
                        }

                        return false;
                    }

                    if (var0.dampensVibrations()) {
                        return false;
                    }
                }

                if (param1.affectedState() != null) {
                    return !param1.affectedState().is(BlockTags.DAMPENS_VIBRATIONS);
                } else {
                    return true;
                }
            }
        }

        boolean shouldListen(ServerLevel var1, GameEventListener var2, BlockPos var3, GameEvent var4, GameEvent.Context var5);

        void onSignalReceive(ServerLevel var1, GameEventListener var2, BlockPos var3, GameEvent var4, @Nullable Entity var5, @Nullable Entity var6, float var7);

        default void onSignalSchedule() {
        }
    }
}
