package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class TemptingSensor extends Sensor<PathfinderMob> {
    public static final int TEMPTATION_RANGE = 10;
    private static final TargetingConditions TEMPT_TARGETING = TargetingConditions.forNonCombat().range(10.0).ignoreLineOfSight();
    private final Ingredient temptations;

    public TemptingSensor(Ingredient param0) {
        this.temptations = param0;
    }

    protected void doTick(ServerLevel param0, PathfinderMob param1) {
        Brain<?> var0 = param1.getBrain();
        List<Player> var1 = param0.players()
            .stream()
            .filter(EntitySelector.NO_SPECTATORS)
            .filter(param1x -> TEMPT_TARGETING.test(param1, param1x))
            .filter(param1x -> param1.closerThan(param1x, 10.0))
            .filter(this::playerHoldingTemptation)
            .sorted(Comparator.comparingDouble(param1::distanceToSqr))
            .collect(Collectors.toList());
        if (!var1.isEmpty()) {
            Player var2 = var1.get(0);
            var0.setMemory(MemoryModuleType.TEMPTING_PLAYER, var2);
        } else {
            var0.eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
        }

    }

    private boolean playerHoldingTemptation(Player param0x) {
        return this.isTemptation(param0x.getMainHandItem()) || this.isTemptation(param0x.getOffhandItem());
    }

    private boolean isTemptation(ItemStack param0) {
        return this.temptations.test(param0);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.TEMPTING_PLAYER);
    }
}
