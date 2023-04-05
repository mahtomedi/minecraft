package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CalibratedSculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;

public class CalibratedSculkSensorBlockEntity extends SculkSensorBlockEntity {
    public CalibratedSculkSensorBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.CALIBRATED_SCULK_SENSOR, param0, param1);
    }

    @Override
    public VibrationListener.Config createVibrationConfig() {
        return new CalibratedSculkSensorBlockEntity.VibrationConfig(this);
    }

    public static class VibrationConfig extends SculkSensorBlockEntity.VibrationConfig {
        public VibrationConfig(SculkSensorBlockEntity param0) {
            super(param0);
        }

        @Override
        public int getListenerRadius() {
            return 16;
        }

        @Override
        public boolean shouldListen(ServerLevel param0, GameEventListener param1, BlockPos param2, GameEvent param3, @Nullable GameEvent.Context param4) {
            BlockPos var0 = this.sculkSensor.getBlockPos();
            int var1 = this.getBackSignal(param0, var0, this.sculkSensor.getBlockState());
            return var1 != 0 && VibrationListener.getGameEventFrequency(param3) != var1 ? false : super.shouldListen(param0, param1, param2, param3, param4);
        }

        private int getBackSignal(Level param0, BlockPos param1, BlockState param2) {
            Direction var0 = param2.getValue(CalibratedSculkSensorBlock.FACING).getOpposite();
            return param0.getSignal(param1.relative(var0), var0);
        }
    }
}
