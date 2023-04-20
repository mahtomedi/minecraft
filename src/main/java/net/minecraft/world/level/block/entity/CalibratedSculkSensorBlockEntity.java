package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CalibratedSculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

public class CalibratedSculkSensorBlockEntity extends SculkSensorBlockEntity {
    public CalibratedSculkSensorBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.CALIBRATED_SCULK_SENSOR, param0, param1);
    }

    @Override
    public VibrationSystem.User createVibrationUser() {
        return new CalibratedSculkSensorBlockEntity.VibrationUser(this.getBlockPos());
    }

    protected class VibrationUser extends SculkSensorBlockEntity.VibrationUser {
        public VibrationUser(BlockPos param1) {
            super(param1);
        }

        @Override
        public int getListenerRadius() {
            return 16;
        }

        @Override
        public boolean canReceiveVibration(ServerLevel param0, BlockPos param1, GameEvent param2, @Nullable GameEvent.Context param3) {
            int var0 = this.getBackSignal(param0, this.blockPos, CalibratedSculkSensorBlockEntity.this.getBlockState());
            return var0 != 0 && VibrationSystem.getGameEventFrequency(param2) != var0 ? false : super.canReceiveVibration(param0, param1, param2, param3);
        }

        private int getBackSignal(Level param0, BlockPos param1, BlockState param2) {
            Direction var0 = param2.getValue(CalibratedSculkSensorBlock.FACING).getOpposite();
            return param0.getSignal(param1.relative(var0), var0);
        }
    }
}
