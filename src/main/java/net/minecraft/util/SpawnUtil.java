package net.minecraft.util;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnUtil {
    public static <T extends Mob> Optional<T> trySpawnMob(EntityType<T> param0, ServerLevel param1, BlockPos param2, int param3, int param4, int param5) {
        BlockPos.MutableBlockPos var0 = param2.mutable();

        for(int var1 = 0; var1 < param3; ++var1) {
            int var2 = Mth.randomBetweenInclusive(param1.random, -param4, param4);
            int var3 = Mth.randomBetweenInclusive(param1.random, -param4, param4);
            if (moveToPossibleSpawnPosition(param1, param5, var0.setWithOffset(param2, var2, param5, var3))) {
                T var4 = param0.create(param1, null, null, null, var0, MobSpawnType.MOB_SUMMONED, false, false);
                if (var4 != null) {
                    if (var4.checkSpawnRules(param1, MobSpawnType.MOB_SUMMONED) && var4.checkSpawnObstruction(param1)) {
                        param1.addFreshEntityWithPassengers(var4);
                        return Optional.of(var4);
                    }

                    var4.discard();
                }
            }
        }

        return Optional.empty();
    }

    private static boolean moveToPossibleSpawnPosition(ServerLevel param0, int param1, BlockPos.MutableBlockPos param2) {
        BlockState var0 = param0.getBlockState(param2);

        for(int var1 = param1; var1 >= -param1; --var1) {
            param2.move(Direction.DOWN);
            BlockState var2 = param0.getBlockState(param2);
            if ((var0.isAir() || var0.getMaterial().isLiquid()) && var2.getMaterial().isSolidBlocking()) {
                param2.move(Direction.UP);
                return true;
            }

            var0 = var2;
        }

        return false;
    }
}
