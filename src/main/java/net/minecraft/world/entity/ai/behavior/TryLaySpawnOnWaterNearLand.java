package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;

public class TryLaySpawnOnWaterNearLand {
    public static BehaviorControl<LivingEntity> create(Block param0) {
        return BehaviorBuilder.create(
            param1 -> param1.<MemoryAccessor, MemoryAccessor, MemoryAccessor>group(
                        param1.absent(MemoryModuleType.ATTACK_TARGET),
                        param1.present(MemoryModuleType.WALK_TARGET),
                        param1.present(MemoryModuleType.IS_PREGNANT)
                    )
                    .apply(
                        param1,
                        (param1x, param2, param3) -> (param2x, param3x, param4) -> {
                                if (!param3x.isInWater() && param3x.onGround()) {
                                    BlockPos var0x = param3x.blockPosition().below();
            
                                    for(Direction var1x : Direction.Plane.HORIZONTAL) {
                                        BlockPos var2x = var0x.relative(var1x);
                                        if (param2x.getBlockState(var2x).getCollisionShape(param2x, var2x).getFaceShape(Direction.UP).isEmpty()
                                            && param2x.getFluidState(var2x).is(Fluids.WATER)) {
                                            BlockPos var3x = var2x.above();
                                            if (param2x.getBlockState(var3x).isAir()) {
                                                BlockState var4 = param0.defaultBlockState();
                                                param2x.setBlock(var3x, var4, 3);
                                                param2x.gameEvent(GameEvent.BLOCK_PLACE, var3x, GameEvent.Context.of(param3x, var4));
                                                param2x.playSound(null, param3x, SoundEvents.FROG_LAY_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
                                                param3.erase();
                                                return true;
                                            }
                                        }
                                    }
            
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                    )
        );
    }
}
