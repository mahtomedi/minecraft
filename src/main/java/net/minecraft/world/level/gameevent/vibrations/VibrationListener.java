package net.minecraft.world.level.gameevent.vibrations;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
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
    protected final PositionSource listenerSource;
    protected final int listenerRange;
    protected final VibrationListener.VibrationListenerConfig config;
    protected Optional<GameEvent> receivingEvent = Optional.empty();
    protected int receivingDistance;
    protected int travelTimeInTicks = 0;

    public VibrationListener(PositionSource param0, int param1, VibrationListener.VibrationListenerConfig param2) {
        this.listenerSource = param0;
        this.listenerRange = param1;
        this.config = param2;
    }

    public void tick(Level param0) {
        if (this.receivingEvent.isPresent()) {
            --this.travelTimeInTicks;
            if (this.travelTimeInTicks <= 0) {
                this.travelTimeInTicks = 0;
                this.config.onSignalReceive(param0, this, this.receivingEvent.get(), this.receivingDistance);
                this.receivingEvent = Optional.empty();
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
    public boolean handleGameEvent(Level param0, GameEvent param1, @Nullable Entity param2, BlockPos param3) {
        if (!this.isValidVibration(param1, param2)) {
            return false;
        } else {
            Optional<BlockPos> var0 = this.listenerSource.getPosition(param0);
            if (!var0.isPresent()) {
                return false;
            } else {
                BlockPos var1 = var0.get();
                if (!this.config.shouldListen(param0, this, param3, param1, param2)) {
                    return false;
                } else if (this.isOccluded(param0, param3, var1)) {
                    return false;
                } else {
                    this.sendSignal(param0, param1, param3, var1);
                    return true;
                }
            }
        }
    }

    private boolean isValidVibration(GameEvent param0, @Nullable Entity param1) {
        if (this.receivingEvent.isPresent()) {
            return false;
        } else if (!GameEventTags.VIBRATIONS.contains(param0)) {
            return false;
        } else {
            if (param1 != null) {
                if (GameEventTags.IGNORE_VIBRATIONS_SNEAKING.contains(param0) && param1.isSteppingCarefully()) {
                    return false;
                }

                if (param1.occludesVibrations()) {
                    return false;
                }
            }

            return param1 == null || !param1.isSpectator();
        }
    }

    private void sendSignal(Level param0, GameEvent param1, BlockPos param2, BlockPos param3) {
        this.receivingEvent = Optional.of(param1);
        if (param0 instanceof ServerLevel) {
            this.receivingDistance = Mth.floor(Math.sqrt(param2.distSqr(param3, false)));
            this.travelTimeInTicks = this.receivingDistance;
            ((ServerLevel)param0).sendVibrationParticle(new VibrationPath(param2, this.listenerSource, this.travelTimeInTicks));
        }

    }

    private boolean isOccluded(Level param0, BlockPos param1, BlockPos param2) {
        return param0.isBlockInLine(
                    new ClipBlockStateContext(Vec3.atCenterOf(param1), Vec3.atCenterOf(param2), param0x -> param0x.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS))
                )
                .getType()
            == HitResult.Type.BLOCK;
    }

    public interface VibrationListenerConfig {
        boolean shouldListen(Level var1, GameEventListener var2, BlockPos var3, GameEvent var4, @Nullable Entity var5);

        void onSignalReceive(Level var1, GameEventListener var2, GameEvent var3, int var4);
    }
}
