package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class VibrationListener implements GameEventListener {
    protected final PositionSource listenerSource;
    protected final int listenerRange;
    protected final VibrationListener.VibrationListenerConfig config;
    @Nullable
    protected VibrationListener.ReceivingEvent receivingEvent;
    protected int receivingDistance;
    protected int travelTimeInTicks;

    public static Codec<VibrationListener> codec(VibrationListener.VibrationListenerConfig param0) {
        return RecordCodecBuilder.create(
            param1 -> param1.group(
                        PositionSource.CODEC.fieldOf("source").forGetter(param0x -> param0x.listenerSource),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("range").forGetter(param0x -> param0x.listenerRange),
                        VibrationListener.ReceivingEvent.CODEC.optionalFieldOf("event").forGetter(param0x -> Optional.ofNullable(param0x.receivingEvent)),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_distance").orElse(0).forGetter(param0x -> param0x.receivingDistance),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay").orElse(0).forGetter(param0x -> param0x.travelTimeInTicks)
                    )
                    .apply(
                        param1,
                        (param1x, param2, param3, param4, param5) -> new VibrationListener(param1x, param2, param0, param3.orElse(null), param4, param5)
                    )
        );
    }

    public VibrationListener(
        PositionSource param0,
        int param1,
        VibrationListener.VibrationListenerConfig param2,
        @Nullable VibrationListener.ReceivingEvent param3,
        int param4,
        int param5
    ) {
        this.listenerSource = param0;
        this.listenerRange = param1;
        this.config = param2;
        this.receivingEvent = param3;
        this.receivingDistance = param4;
        this.travelTimeInTicks = param5;
    }

    public void tick(Level param0) {
        if (param0 instanceof ServerLevel var0 && this.receivingEvent != null) {
            --this.travelTimeInTicks;
            if (this.travelTimeInTicks <= 0) {
                this.travelTimeInTicks = 0;
                this.config
                    .onSignalReceive(
                        var0,
                        this,
                        new BlockPos(this.receivingEvent.pos),
                        this.receivingEvent.gameEvent,
                        this.receivingEvent.getEntity(var0).orElse(null),
                        this.receivingEvent.getProjectileOwner(var0).orElse(null),
                        this.receivingDistance
                    );
                this.receivingEvent = null;
            }
        }

    }

    @Override
    public PositionSource getListenerSource() {
        return this.listenerSource;
    }

    @Override
    public int getListenerRadius() {
        return this.listenerRange;
    }

    @Override
    public boolean handleGameEvent(ServerLevel param0, GameEvent param1, GameEvent.Context param2, Vec3 param3) {
        if (this.receivingEvent != null) {
            return false;
        } else if (!this.config.isValidVibration(param1, param2)) {
            return false;
        } else {
            Optional<Vec3> var0 = this.listenerSource.getPosition(param0);
            if (var0.isEmpty()) {
                return false;
            } else {
                Vec3 var1 = var0.get();
                if (!this.config.shouldListen(param0, this, new BlockPos(param3), param1, param2)) {
                    return false;
                } else if (isOccluded(param0, param3, var1)) {
                    return false;
                } else {
                    this.scheduleSignal(param0, param1, param2, param3, var1);
                    return true;
                }
            }
        }
    }

    private void scheduleSignal(ServerLevel param0, GameEvent param1, GameEvent.Context param2, Vec3 param3, Vec3 param4) {
        this.receivingDistance = Mth.floor(param3.distanceTo(param4));
        this.receivingEvent = new VibrationListener.ReceivingEvent(param1, this.receivingDistance, param3, param2.sourceEntity());
        this.travelTimeInTicks = this.receivingDistance;
        param0.sendParticles(new VibrationParticleOption(this.listenerSource, this.travelTimeInTicks), param3.x, param3.y, param3.z, 1, 0.0, 0.0, 0.0, 0.0);
        this.config.onSignalSchedule();
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

    public static record ReceivingEvent(
        GameEvent gameEvent, int distance, Vec3 pos, @Nullable UUID uuid, @Nullable UUID projectileOwnerUuid, @Nullable Entity entity
    ) {
        public static final Codec<VibrationListener.ReceivingEvent> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Registry.GAME_EVENT.byNameCodec().fieldOf("game_event").forGetter(VibrationListener.ReceivingEvent::gameEvent),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("distance").forGetter(VibrationListener.ReceivingEvent::distance),
                        Vec3.CODEC.fieldOf("pos").forGetter(VibrationListener.ReceivingEvent::pos),
                        ExtraCodecs.UUID.optionalFieldOf("source").forGetter(param0x -> Optional.ofNullable(param0x.uuid())),
                        ExtraCodecs.UUID.optionalFieldOf("projectile_owner").forGetter(param0x -> Optional.ofNullable(param0x.projectileOwnerUuid()))
                    )
                    .apply(
                        param0,
                        (param0x, param1, param2, param3, param4) -> new VibrationListener.ReceivingEvent(
                                param0x, param1, param2, param3.orElse(null), param4.orElse(null)
                            )
                    )
        );

        public ReceivingEvent(GameEvent param0, int param1, Vec3 param2, @Nullable UUID param3, @Nullable UUID param4) {
            this(param0, param1, param2, param3, param4, null);
        }

        public ReceivingEvent(GameEvent param0, int param1, Vec3 param2, @Nullable Entity param3) {
            this(param0, param1, param2, param3 == null ? null : param3.getUUID(), getProjectileOwner(param3), param3);
        }

        @Nullable
        private static UUID getProjectileOwner(@Nullable Entity param0) {
            if (param0 instanceof Projectile var0 && var0.getOwner() != null) {
                return var0.getOwner().getUUID();
            }

            return null;
        }

        public Optional<Entity> getEntity(ServerLevel param0) {
            return Optional.ofNullable(this.entity).or(() -> Optional.ofNullable(this.uuid).map(param0::getEntity));
        }

        public Optional<Entity> getProjectileOwner(ServerLevel param0) {
            return this.getEntity(param0)
                .filter(param0x -> param0x instanceof Projectile)
                .map(param0x -> (Projectile)param0x)
                .map(Projectile::getOwner)
                .or(() -> Optional.ofNullable(this.projectileOwnerUuid).map(param0::getEntity));
        }
    }

    public interface VibrationListenerConfig {
        default TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.VIBRATIONS;
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
                        if (var0 instanceof ServerPlayer var1) {
                            CriteriaTriggers.AVOID_VIBRATION.trigger(var1);
                        }

                        return false;
                    }

                    if (var0.dampensVibrations()) {
                        return false;
                    }

                    if (param0.is(GameEventTags.DAMPENABLE_VIBRATIONS)) {
                        return !var0.getBlockStateOn().is(BlockTags.DAMPENS_VIBRATIONS);
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

        void onSignalReceive(ServerLevel var1, GameEventListener var2, BlockPos var3, GameEvent var4, @Nullable Entity var5, @Nullable Entity var6, int var7);

        default void onSignalSchedule() {
        }
    }
}
