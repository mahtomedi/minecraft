package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;

public class TryLaySpawnOnWaterNearLand extends Behavior<Frog> {
    private final Block spawnBlock;
    private final MemoryModuleType<?> memoryModule;

    public TryLaySpawnOnWaterNearLand(Block param0, MemoryModuleType<?> param1) {
        super(
            ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.IS_PREGNANT,
                MemoryStatus.VALUE_PRESENT
            )
        );
        this.spawnBlock = param0;
        this.memoryModule = param1;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Frog param1) {
        return !param1.isInWater() && param1.isOnGround();
    }

    protected void start(ServerLevel param0, Frog param1, long param2) {
        BlockPos var0 = param1.blockPosition().below();

        for(Direction var1 : Direction.Plane.HORIZONTAL) {
            BlockPos var2 = var0.relative(var1);
            if (param0.getBlockState(var2).getCollisionShape(param0, var2).getFaceShape(Direction.UP).isEmpty() && param0.getFluidState(var2).is(Fluids.WATER)) {
                BlockPos var3 = var2.above();
                if (param0.getBlockState(var3).isAir()) {
                    param0.setBlock(var3, this.spawnBlock.defaultBlockState(), 3);
                    param0.playSound(null, param1, SoundEvents.FROG_LAY_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
                    param1.getBrain().eraseMemory(this.memoryModule);
                    return;
                }
            }
        }

    }
}
