package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class MoveToSkySeeingSpot extends Behavior<LivingEntity> {
    private final float speed;

    public MoveToSkySeeingSpot(float param0) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.speed = param0;
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        Optional<Vec3> var0 = Optional.ofNullable(this.getOutdoorPosition(param0, param1));
        if (var0.isPresent()) {
            param1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, var0.map(param0x -> new WalkTarget(param0x, this.speed, 0)));
        }

    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, LivingEntity param1) {
        return !param0.canSeeSky(new BlockPos(param1));
    }

    @Nullable
    private Vec3 getOutdoorPosition(ServerLevel param0, LivingEntity param1) {
        Random var0 = param1.getRandom();
        BlockPos var1 = new BlockPos(param1);

        for(int var2 = 0; var2 < 10; ++var2) {
            BlockPos var3 = var1.offset(var0.nextInt(20) - 10, var0.nextInt(6) - 3, var0.nextInt(20) - 10);
            if (hasNoBlocksAbove(param0, param1, var3)) {
                return new Vec3(var3);
            }
        }

        return null;
    }

    public static boolean hasNoBlocksAbove(ServerLevel param0, LivingEntity param1, BlockPos param2) {
        return param0.canSeeSky(param2) && (double)param0.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, param2).getY() <= param1.getY();
    }
}
