package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import org.slf4j.Logger;

public class SculkShriekerBlockEntity extends BlockEntity implements VibrationListener.VibrationListenerConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int LISTENER_RADIUS = 8;
    private VibrationListener listener = new VibrationListener(new BlockPositionSource(this.worldPosition), 8, this, null, 0, 0);

    public SculkShriekerBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.SCULK_SHRIEKER, param0, param1);
    }

    public VibrationListener getListener() {
        return this.listener;
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        if (param0.contains("listener", 10)) {
            VibrationListener.codec(this)
                .parse(new Dynamic<>(NbtOps.INSTANCE, param0.getCompound("listener")))
                .resultOrPartial(LOGGER::error)
                .ifPresent(param0x -> this.listener = param0x);
        }

    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        VibrationListener.codec(this)
            .encodeStart(NbtOps.INSTANCE, this.listener)
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1 -> param0.put("listener", param1));
    }

    @Override
    public boolean isValidVibration(GameEvent param0, @Nullable Entity param1) {
        return true;
    }

    @Override
    public boolean shouldListen(ServerLevel param0, GameEventListener param1, BlockPos param2, GameEvent param3, @Nullable Entity param4) {
        return param3 == GameEvent.SCULK_SENSOR_TENDRILS_CLICKING && SculkShriekerBlock.canShriek(param0, this.getBlockPos(), this.getBlockState());
    }

    @Override
    public void onSignalReceive(ServerLevel param0, GameEventListener param1, BlockPos param2, GameEvent param3, @Nullable Entity param4, int param5) {
        SculkShriekerBlock.shriek(param0, this.getBlockState(), this.getBlockPos());
    }
}
