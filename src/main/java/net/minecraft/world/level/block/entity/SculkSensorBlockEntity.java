package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;

public class SculkSensorBlockEntity extends BlockEntity implements VibrationListener.VibrationListenerConfig {
    private final VibrationListener listener;
    private int lastVibrationFrequency;

    public SculkSensorBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.SCULK_SENSOR, param0, param1);
        this.listener = new VibrationListener(new BlockPositionSource(this.worldPosition), ((SculkSensorBlock)param1.getBlock()).getListenerRange(), this);
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.lastVibrationFrequency = param0.getInt("last_vibration_frequency");
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        param0.putInt("last_vibration_frequency", this.lastVibrationFrequency);
        return param0;
    }

    public VibrationListener getListener() {
        return this.listener;
    }

    public int getLastVibrationFrequency() {
        return this.lastVibrationFrequency;
    }

    @Override
    public boolean shouldListen(Level param0, GameEventListener param1, BlockPos param2, GameEvent param3, @Nullable Entity param4) {
        boolean var0 = param3 == GameEvent.BLOCK_DESTROY && param2.equals(this.getBlockPos());
        return !var0 && SculkSensorBlock.canActivate(this.getBlockState());
    }

    @Override
    public void onSignalReceive(Level param0, GameEventListener param1, GameEvent param2, int param3) {
        BlockState var0 = this.getBlockState();
        if (!param0.isClientSide() && SculkSensorBlock.canActivate(var0)) {
            this.lastVibrationFrequency = SculkSensorBlock.VIBRATION_STRENGTH_FOR_EVENT.getInt(param2);
            SculkSensorBlock.activate(param0, this.worldPosition, var0, getRedstoneStrengthForDistance(param3, param1.getListenerRadius()));
        }

    }

    public static int getRedstoneStrengthForDistance(int param0, int param1) {
        double var0 = (double)param0 / (double)param1;
        return Math.max(1, 15 - Mth.floor(var0 * 15.0));
    }
}
