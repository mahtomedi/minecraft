package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;

public class SculkCatalystBlockEntity extends BlockEntity implements GameEventListener {
    private final BlockPositionSource blockPosSource = new BlockPositionSource(this.worldPosition);
    private final SculkSpreader sculkSpreader = new SculkSpreader();

    public SculkCatalystBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.SCULK_CATALYST, param0, param1);
    }

    @Override
    public PositionSource getListenerSource() {
        return this.blockPosSource;
    }

    @Override
    public int getListenerRadius() {
        return 8;
    }

    @Override
    public boolean handleGameEvent(Level param0, GameEvent param1, @Nullable Entity param2, BlockPos param3) {
        if (!param0.isClientSide() && param1 == GameEvent.ENTITY_DYING && param2 instanceof LivingEntity var0) {
            if (!var0.wasExperienceConsumed()) {
                this.sculkSpreader.addCursors(param3, var0.getExperienceReward());
                var0.skipDropExperience();
                SculkCatalystBlock.bloom((ServerLevel)param0, this.worldPosition, this.getBlockState(), param0.getRandom());
            }

            return true;
        } else {
            return false;
        }
    }

    public static void serverTick(Level param0, BlockPos param1, BlockState param2, SculkCatalystBlockEntity param3) {
        param3.sculkSpreader.updateCursors(param0, param1, param0.getRandom());
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.sculkSpreader.load(param0);
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        this.sculkSpreader.save(param0);
        super.saveAdditional(param0);
    }

    @VisibleForTesting
    public SculkSpreader getSculkSpreader() {
        return this.sculkSpreader;
    }
}
