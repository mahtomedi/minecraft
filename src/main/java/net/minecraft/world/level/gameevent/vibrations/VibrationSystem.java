package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Optional;
import java.util.function.ToIntFunction;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public interface VibrationSystem {
    GameEvent[] RESONANCE_EVENTS = new GameEvent[]{
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
    ToIntFunction<GameEvent> VIBRATION_FREQUENCY_FOR_EVENT = Util.make(new Object2IntOpenHashMap<>(), param0 -> {
        param0.defaultReturnValue(0);
        param0.put(GameEvent.STEP, 1);
        param0.put(GameEvent.SWIM, 1);
        param0.put(GameEvent.FLAP, 1);
        param0.put(GameEvent.PROJECTILE_LAND, 2);
        param0.put(GameEvent.HIT_GROUND, 2);
        param0.put(GameEvent.SPLASH, 2);
        param0.put(GameEvent.ITEM_INTERACT_FINISH, 3);
        param0.put(GameEvent.PROJECTILE_SHOOT, 3);
        param0.put(GameEvent.INSTRUMENT_PLAY, 3);
        param0.put(GameEvent.ENTITY_ACTION, 4);
        param0.put(GameEvent.ELYTRA_GLIDE, 4);
        param0.put(GameEvent.UNEQUIP, 4);
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

    });

    VibrationSystem.Data getVibrationData();

    VibrationSystem.User getVibrationUser();

    static int getGameEventFrequency(GameEvent param0) {
        return VIBRATION_FREQUENCY_FOR_EVENT.applyAsInt(param0);
    }

    static GameEvent getResonanceEventByFrequency(int param0) {
        return RESONANCE_EVENTS[param0 - 1];
    }

    static int getRedstoneStrengthForDistance(float param0, int param1) {
        double var0 = 15.0 / (double)param1;
        return Math.max(1, 15 - Mth.floor(var0 * (double)param0));
    }

    public static final class Data {
        public static Codec<VibrationSystem.Data> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        VibrationInfo.CODEC.optionalFieldOf("event").forGetter(param0x -> Optional.ofNullable(param0x.currentVibration)),
                        VibrationSelector.CODEC.fieldOf("selector").forGetter(VibrationSystem.Data::getSelectionStrategy),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay").orElse(0).forGetter(VibrationSystem.Data::getTravelTimeInTicks)
                    )
                    .apply(param0, (param0x, param1, param2) -> new VibrationSystem.Data(param0x.orElse(null), param1, param2, true))
        );
        public static final String NBT_TAG_KEY = "listener";
        @Nullable
        VibrationInfo currentVibration;
        private int travelTimeInTicks;
        final VibrationSelector selectionStrategy;
        private boolean reloadVibrationParticle;

        private Data(@Nullable VibrationInfo param0, VibrationSelector param1, int param2, boolean param3) {
            this.currentVibration = param0;
            this.travelTimeInTicks = param2;
            this.selectionStrategy = param1;
            this.reloadVibrationParticle = param3;
        }

        public Data() {
            this(null, new VibrationSelector(), 0, false);
        }

        public VibrationSelector getSelectionStrategy() {
            return this.selectionStrategy;
        }

        @Nullable
        public VibrationInfo getCurrentVibration() {
            return this.currentVibration;
        }

        public void setCurrentVibration(@Nullable VibrationInfo param0) {
            this.currentVibration = param0;
        }

        public int getTravelTimeInTicks() {
            return this.travelTimeInTicks;
        }

        public void setTravelTimeInTicks(int param0) {
            this.travelTimeInTicks = param0;
        }

        public void decrementTravelTime() {
            this.travelTimeInTicks = Math.max(0, this.travelTimeInTicks - 1);
        }

        public boolean shouldReloadVibrationParticle() {
            return this.reloadVibrationParticle;
        }

        public void setReloadVibrationParticle(boolean param0) {
            this.reloadVibrationParticle = param0;
        }
    }

    public static class Listener implements GameEventListener {
        private final VibrationSystem system;

        public Listener(VibrationSystem param0) {
            this.system = param0;
        }

        @Override
        public PositionSource getListenerSource() {
            return this.system.getVibrationUser().getPositionSource();
        }

        @Override
        public int getListenerRadius() {
            return this.system.getVibrationUser().getListenerRadius();
        }

        @Override
        public boolean handleGameEvent(ServerLevel param0, GameEvent param1, GameEvent.Context param2, Vec3 param3) {
            VibrationSystem.Data var0 = this.system.getVibrationData();
            VibrationSystem.User var1 = this.system.getVibrationUser();
            if (var0.getCurrentVibration() != null) {
                return false;
            } else if (!var1.isValidVibration(param1, param2)) {
                return false;
            } else {
                Optional<Vec3> var2 = var1.getPositionSource().getPosition(param0);
                if (var2.isEmpty()) {
                    return false;
                } else {
                    Vec3 var3 = var2.get();
                    if (!var1.canReceiveVibration(param0, BlockPos.containing(param3), param1, param2)) {
                        return false;
                    } else if (isOccluded(param0, param3, var3)) {
                        return false;
                    } else {
                        this.scheduleVibration(param0, var0, param1, param2, param3, var3);
                        return true;
                    }
                }
            }
        }

        public void forceScheduleVibration(ServerLevel param0, GameEvent param1, GameEvent.Context param2, Vec3 param3) {
            this.system
                .getVibrationUser()
                .getPositionSource()
                .getPosition(param0)
                .ifPresent(param4 -> this.scheduleVibration(param0, this.system.getVibrationData(), param1, param2, param3, param4));
        }

        private void scheduleVibration(ServerLevel param0, VibrationSystem.Data param1, GameEvent param2, GameEvent.Context param3, Vec3 param4, Vec3 param5) {
            param1.selectionStrategy
                .addCandidate(new VibrationInfo(param2, (float)param4.distanceTo(param5), param4, param3.sourceEntity()), param0.getGameTime());
        }

        public static float distanceBetweenInBlocks(BlockPos param0, BlockPos param1) {
            return (float)Math.sqrt(param0.distSqr(param1));
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
    }

    public interface Ticker {
        static void tick(Level param0, VibrationSystem.Data param1, VibrationSystem.User param2) {
            if (param0 instanceof ServerLevel var0) {
                if (param1.currentVibration == null) {
                    trySelectAndScheduleVibration(var0, param1, param2);
                }

                if (param1.currentVibration != null) {
                    boolean var2 = param1.getTravelTimeInTicks() > 0;
                    tryReloadVibrationParticle(var0, param1, param2);
                    param1.decrementTravelTime();
                    if (param1.getTravelTimeInTicks() <= 0) {
                        var2 = receiveVibration(var0, param1, param2, param1.currentVibration);
                    }

                    if (var2) {
                        param2.onDataChanged();
                    }

                }
            }
        }

        private static void trySelectAndScheduleVibration(ServerLevel param0, VibrationSystem.Data param1, VibrationSystem.User param2) {
            param1.getSelectionStrategy()
                .chosenCandidate(param0.getGameTime())
                .ifPresent(
                    param3 -> {
                        param1.setCurrentVibration(param3);
                        Vec3 var0x = param3.pos();
                        param1.setTravelTimeInTicks(param2.calculateTravelTimeInTicks(param3.distance()));
                        param0.sendParticles(
                            new VibrationParticleOption(param2.getPositionSource(), param1.getTravelTimeInTicks()),
                            var0x.x,
                            var0x.y,
                            var0x.z,
                            1,
                            0.0,
                            0.0,
                            0.0,
                            0.0
                        );
                        param2.onDataChanged();
                        param1.getSelectionStrategy().startOver();
                    }
                );
        }

        private static void tryReloadVibrationParticle(ServerLevel param0, VibrationSystem.Data param1, VibrationSystem.User param2) {
            if (param1.shouldReloadVibrationParticle()) {
                if (param1.currentVibration == null) {
                    param1.setReloadVibrationParticle(false);
                } else {
                    Vec3 var0 = param1.currentVibration.pos();
                    PositionSource var1 = param2.getPositionSource();
                    Vec3 var2 = var1.getPosition(param0).orElse(var0);
                    int var3 = param1.getTravelTimeInTicks();
                    int var4 = param2.calculateTravelTimeInTicks(param1.currentVibration.distance());
                    double var5 = 1.0 - (double)var3 / (double)var4;
                    double var6 = Mth.lerp(var5, var0.x, var2.x);
                    double var7 = Mth.lerp(var5, var0.y, var2.y);
                    double var8 = Mth.lerp(var5, var0.z, var2.z);
                    boolean var9 = param0.sendParticles(new VibrationParticleOption(var1, var3), var6, var7, var8, 1, 0.0, 0.0, 0.0, 0.0) > 0;
                    if (var9) {
                        param1.setReloadVibrationParticle(false);
                    }

                }
            }
        }

        private static boolean receiveVibration(ServerLevel param0, VibrationSystem.Data param1, VibrationSystem.User param2, VibrationInfo param3) {
            BlockPos var0 = BlockPos.containing(param3.pos());
            BlockPos var1 = param2.getPositionSource().getPosition(param0).map(BlockPos::containing).orElse(var0);
            if (param2.requiresAdjacentChunksToBeTicking() && !areAdjacentChunksTicking(param0, var1)) {
                return false;
            } else {
                param2.onReceiveVibration(
                    param0,
                    var0,
                    param3.gameEvent(),
                    param3.getEntity(param0).orElse(null),
                    param3.getProjectileOwner(param0).orElse(null),
                    VibrationSystem.Listener.distanceBetweenInBlocks(var0, var1)
                );
                param1.setCurrentVibration(null);
                return true;
            }
        }

        private static boolean areAdjacentChunksTicking(Level param0, BlockPos param1) {
            ChunkPos var0 = new ChunkPos(param1);

            for(int var1 = var0.x - 1; var1 <= var0.x + 1; ++var1) {
                for(int var2 = var0.z - 1; var2 <= var0.z + 1; ++var2) {
                    if (!param0.shouldTickBlocksAt(ChunkPos.asLong(var1, var2)) || param0.getChunkSource().getChunkNow(var1, var2) == null) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    public interface User {
        int getListenerRadius();

        PositionSource getPositionSource();

        boolean canReceiveVibration(ServerLevel var1, BlockPos var2, GameEvent var3, GameEvent.Context var4);

        void onReceiveVibration(ServerLevel var1, BlockPos var2, GameEvent var3, @Nullable Entity var4, @Nullable Entity var5, float var6);

        default TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.VIBRATIONS;
        }

        default boolean canTriggerAvoidVibration() {
            return false;
        }

        default boolean requiresAdjacentChunksToBeTicking() {
            return false;
        }

        default int calculateTravelTimeInTicks(float param0) {
            return Mth.floor(param0);
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

        default void onDataChanged() {
        }
    }
}
