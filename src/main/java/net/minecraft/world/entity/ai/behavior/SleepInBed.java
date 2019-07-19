package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SleepInBed extends Behavior<LivingEntity> {
    private long nextOkStartTime;

    public SleepInBed() {
        super(ImmutableMap.of(MemoryModuleType.HOME, MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, LivingEntity param1) {
        if (param1.isPassenger()) {
            return false;
        } else {
            GlobalPos var0 = param1.getBrain().getMemory(MemoryModuleType.HOME).get();
            if (!Objects.equals(param0.getDimension().getType(), var0.dimension())) {
                return false;
            } else {
                BlockState var1 = param0.getBlockState(var0.pos());
                return var0.pos().closerThan(param1.position(), 2.0) && var1.getBlock().is(BlockTags.BEDS) && !var1.getValue(BedBlock.OCCUPIED);
            }
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel param0, LivingEntity param1, long param2) {
        Optional<GlobalPos> var0 = param1.getBrain().getMemory(MemoryModuleType.HOME);
        if (!var0.isPresent()) {
            return false;
        } else {
            BlockPos var1 = var0.get().pos();
            return param1.getBrain().isActive(Activity.REST) && param1.y > (double)var1.getY() + 0.4 && var1.closerThan(param1.position(), 1.14);
        }
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        if (param2 > this.nextOkStartTime) {
            param1.getBrain()
                .getMemory(MemoryModuleType.OPENED_DOORS)
                .ifPresent(param2x -> InteractWithDoor.closeAllOpenedDoors(param0, ImmutableList.of(), 0, param1, param1.getBrain()));
            param1.startSleeping(param1.getBrain().getMemory(MemoryModuleType.HOME).get().pos());
        }

    }

    @Override
    protected boolean timedOut(long param0) {
        return false;
    }

    @Override
    protected void stop(ServerLevel param0, LivingEntity param1, long param2) {
        if (param1.isSleeping()) {
            param1.stopSleeping();
            this.nextOkStartTime = param2 + 40L;
        }

    }
}
