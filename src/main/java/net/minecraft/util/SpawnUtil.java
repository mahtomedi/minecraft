package net.minecraft.util;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnUtil {
    public static <T extends Mob> Optional<T> trySpawnMob(
        EntityType<T> param0, MobSpawnType param1, ServerLevel param2, BlockPos param3, int param4, int param5, int param6, SpawnUtil.Strategy param7
    ) {
        BlockPos.MutableBlockPos var0 = param3.mutable();

        for(int var1 = 0; var1 < param4; ++var1) {
            int var2 = Mth.randomBetweenInclusive(param2.random, -param5, param5);
            int var3 = Mth.randomBetweenInclusive(param2.random, -param5, param5);
            var0.setWithOffset(param3, var2, param6, var3);
            if (param2.getWorldBorder().isWithinBounds(var0) && moveToPossibleSpawnPosition(param2, param6, var0, param7)) {
                T var4 = param0.create(param2, null, null, var0, param1, false, false);
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

    private static boolean moveToPossibleSpawnPosition(ServerLevel param0, int param1, BlockPos.MutableBlockPos param2, SpawnUtil.Strategy param3) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos().set(param2);
        BlockState var1 = param0.getBlockState(var0);

        for(int var2 = param1; var2 >= -param1; --var2) {
            param2.move(Direction.DOWN);
            var0.setWithOffset(param2, Direction.UP);
            BlockState var3 = param0.getBlockState(param2);
            if (param3.canSpawnOn(param0, param2, var3, var0, var1)) {
                param2.move(Direction.UP);
                return true;
            }

            var1 = var3;
        }

        return false;
    }

    public interface Strategy {
        SpawnUtil.Strategy LEGACY_IRON_GOLEM = (param0, param1, param2, param3, param4) -> (param4.isAir() || param4.liquid())
                && param2.getMaterial().isSolidBlocking();
        SpawnUtil.Strategy ON_TOP_OF_COLLIDER = (param0, param1, param2, param3, param4) -> param4.getCollisionShape(param0, param3).isEmpty()
                && Block.isFaceFull(param2.getCollisionShape(param0, param1), Direction.UP);

        boolean canSpawnOn(ServerLevel var1, BlockPos var2, BlockState var3, BlockPos var4, BlockState var5);
    }
}
