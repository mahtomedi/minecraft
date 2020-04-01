package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class NetherPortalBlock extends PortalBlock {
    public NetherPortalBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (param1.dimension.isNaturalDimension()
            && param1.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
            && param3.nextInt(2000) < param1.getDifficulty().getId()) {
            while(param1.getBlockState(param2).getBlock() == this) {
                param2 = param2.below();
            }

            if (param1.getBlockState(param2).isValidSpawn(param1, param2, EntityType.ZOMBIFIED_PIGLIN)) {
                Entity var0 = EntityType.ZOMBIFIED_PIGLIN.spawn(param1, null, null, null, param2.above(), MobSpawnType.STRUCTURE, false, false);
                if (var0 != null) {
                    var0.changingDimensionDelay = var0.getDimensionChangingDelay();
                }
            }
        }

    }
}
