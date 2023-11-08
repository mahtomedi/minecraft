package net.minecraft.world.level.block.entity.trialspawner;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public interface PlayerDetector {
    PlayerDetector PLAYERS = (param0, param1, param2) -> param0.getPlayers(
                param2x -> param2x.blockPosition().closerThan(param1, (double)param2) && !param2x.isCreative() && !param2x.isSpectator()
            )
            .stream()
            .map(Entity::getUUID)
            .toList();
    PlayerDetector SHEEP = (param0, param1, param2) -> {
        AABB var0 = new AABB(param1).inflate((double)param2);
        return param0.getEntities(EntityType.SHEEP, var0, LivingEntity::isAlive).stream().map(Entity::getUUID).toList();
    };

    List<UUID> detect(ServerLevel var1, BlockPos var2, int var3);
}
