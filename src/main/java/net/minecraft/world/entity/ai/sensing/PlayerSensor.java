package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;

public class PlayerSensor extends Sensor<LivingEntity> {
    @Override
    protected void doTick(ServerLevel param0, LivingEntity param1) {
        List<Player> var0 = param0.players()
            .stream()
            .filter(EntitySelector.NO_SPECTATORS)
            .filter(param1x -> param1.distanceToSqr(param1x) < 256.0)
            .sorted(Comparator.comparingDouble(param1::distanceToSqr))
            .collect(Collectors.toList());
        Brain<?> var1 = param1.getBrain();
        var1.setMemory(MemoryModuleType.NEAREST_PLAYERS, var0);
        var1.setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, var0.stream().filter(param1::canSee).findFirst());
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER);
    }
}
