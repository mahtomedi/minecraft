package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class SetHiddenState extends Behavior<LivingEntity> {
    private static final int HIDE_TIMEOUT = 300;
    private final int closeEnoughDist;
    private final int stayHiddenTicks;
    private int ticksHidden;

    public SetHiddenState(int param0, int param1) {
        super(ImmutableMap.of(MemoryModuleType.HIDING_PLACE, MemoryStatus.VALUE_PRESENT, MemoryModuleType.HEARD_BELL_TIME, MemoryStatus.VALUE_PRESENT));
        this.stayHiddenTicks = param0 * 20;
        this.ticksHidden = 0;
        this.closeEnoughDist = param1;
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        Brain<?> var0 = param1.getBrain();
        Optional<Long> var1 = var0.getMemory(MemoryModuleType.HEARD_BELL_TIME);
        boolean var2 = var1.get() + 300L <= param2;
        if (this.ticksHidden <= this.stayHiddenTicks && !var2) {
            BlockPos var3 = var0.getMemory(MemoryModuleType.HIDING_PLACE).get().pos();
            if (var3.closerThan(param1.blockPosition(), (double)this.closeEnoughDist)) {
                ++this.ticksHidden;
            }

        } else {
            var0.eraseMemory(MemoryModuleType.HEARD_BELL_TIME);
            var0.eraseMemory(MemoryModuleType.HIDING_PLACE);
            var0.updateActivityFromSchedule(param0.getDayTime(), param0.getGameTime());
            this.ticksHidden = 0;
        }
    }
}
