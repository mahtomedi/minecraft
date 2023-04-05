package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import org.slf4j.Logger;

public class SculkSensorBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private VibrationListener listener;
    private final VibrationListener.Config vibrationConfig = this.createVibrationConfig();
    private int lastVibrationFrequency;

    protected SculkSensorBlockEntity(BlockEntityType<?> param0, BlockPos param1, BlockState param2) {
        super(param0, param1, param2);
        this.listener = new VibrationListener(new BlockPositionSource(this.worldPosition), this.vibrationConfig);
    }

    public SculkSensorBlockEntity(BlockPos param0, BlockState param1) {
        this(BlockEntityType.SCULK_SENSOR, param0, param1);
    }

    public VibrationListener.Config createVibrationConfig() {
        return new SculkSensorBlockEntity.VibrationConfig(this);
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.lastVibrationFrequency = param0.getInt("last_vibration_frequency");
        if (param0.contains("listener", 10)) {
            VibrationListener.codec(this.vibrationConfig)
                .parse(new Dynamic<>(NbtOps.INSTANCE, param0.getCompound("listener")))
                .resultOrPartial(LOGGER::error)
                .ifPresent(param0x -> this.listener = param0x);
        }

    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        param0.putInt("last_vibration_frequency", this.lastVibrationFrequency);
        VibrationListener.codec(this.vibrationConfig)
            .encodeStart(NbtOps.INSTANCE, this.listener)
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1 -> param0.put("listener", param1));
    }

    public VibrationListener getListener() {
        return this.listener;
    }

    public int getLastVibrationFrequency() {
        return this.lastVibrationFrequency;
    }

    public void setLastVibrationFrequency(int param0) {
        this.lastVibrationFrequency = param0;
    }

    public static class VibrationConfig implements VibrationListener.Config {
        public static final int LISTENER_RANGE = 8;
        protected final SculkSensorBlockEntity sculkSensor;

        public VibrationConfig(SculkSensorBlockEntity param0) {
            this.sculkSensor = param0;
        }

        @Override
        public boolean canTriggerAvoidVibration() {
            return true;
        }

        @Override
        public boolean shouldListen(ServerLevel param0, GameEventListener param1, BlockPos param2, GameEvent param3, @Nullable GameEvent.Context param4) {
            return !param2.equals(this.sculkSensor.getBlockPos()) || param3 != GameEvent.BLOCK_DESTROY && param3 != GameEvent.BLOCK_PLACE
                ? SculkSensorBlock.canActivate(this.sculkSensor.getBlockState())
                : false;
        }

        @Override
        public void onSignalReceive(
            ServerLevel param0, GameEventListener param1, BlockPos param2, GameEvent param3, @Nullable Entity param4, @Nullable Entity param5, float param6
        ) {
            BlockState var0 = this.sculkSensor.getBlockState();
            BlockPos var1 = this.sculkSensor.getBlockPos();
            if (SculkSensorBlock.canActivate(var0)) {
                this.sculkSensor.setLastVibrationFrequency(VibrationListener.getGameEventFrequency(param3));
                int var2 = getRedstoneStrengthForDistance(param6, param1.getListenerRadius());
                Block var12 = var0.getBlock();
                if (var12 instanceof SculkSensorBlock var3) {
                    var3.activate(param4, param0, var1, var0, var2, this.sculkSensor.getLastVibrationFrequency());
                }
            }

        }

        @Override
        public void onSignalSchedule() {
            this.sculkSensor.setChanged();
        }

        @Override
        public int getListenerRadius() {
            return 8;
        }

        public static int getRedstoneStrengthForDistance(float param0, int param1) {
            double var0 = 15.0 / (double)param1;
            return Math.max(1, 15 - Mth.floor(var0 * (double)param0));
        }
    }
}
