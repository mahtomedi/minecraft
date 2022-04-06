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
    public static <T extends Mob> Optional<T> trySpawnMob(
        EntityType<T> param0, MobSpawnType param1, ServerLevel param2, BlockPos param3, int param4, int param5, int param6
    ) {
        BlockPos.MutableBlockPos var0 = param3.mutable();

        for(int var1 = 0; var1 < param4; ++var1) {
            int var2 = Mth.randomBetweenInclusive(param2.random, -param5, param5);
            int var3 = Mth.randomBetweenInclusive(param2.random, -param5, param5);
            if (moveToPossibleSpawnPosition(param2, param6, var0.setWithOffset(param3, var2, param6, var3))) {
                T var4 = param0.create(param2, null, null, null, var0, param1, false, false);
                if (var4 != null) {
                    if (var4.checkSpawnRules(param2, param1) && var4.checkSpawnObstruction(param2)) {
                        param2.addFreshEntityWithPassengers(var4);
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
