package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;

public class PiglinSpecificSensor extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
            MemoryModuleType.VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_WITHER_SKELETON,
            MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD,
            MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
            MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLIN,
            MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN,
            MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS,
            MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,
            MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT,
            MemoryModuleType.NEAREST_REPELLENT
        );
    }

    @Override
    protected void doTick(ServerLevel param0, LivingEntity param1) {
        Brain<?> var0 = param1.getBrain();
        var0.setMemory(MemoryModuleType.NEAREST_REPELLENT, findNearestRepellent(param0, param1));
        Optional<WitherSkeleton> var1 = Optional.empty();
        Optional<Hoglin> var2 = Optional.empty();
        Optional<Hoglin> var3 = Optional.empty();
        Optional<Piglin> var4 = Optional.empty();
        Optional<ZombifiedPiglin> var5 = Optional.empty();
        Optional<Player> var6 = Optional.empty();
        Optional<Player> var7 = Optional.empty();
        int var8 = 0;
        List<Piglin> var9 = Lists.newArrayList();

        for(LivingEntity var11 : var0.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(Lists.newArrayList())) {
            if (var11 instanceof Hoglin) {
                Hoglin var12 = (Hoglin)var11;
                if (var12.isBaby() && !var3.isPresent()) {
                    var3 = Optional.of(var12);
                } else if (var12.isAdult()) {
                    ++var8;
                    if (!var2.isPresent()) {
                        var2 = Optional.of(var12);
                    }
                }
            } else if (var11 instanceof Piglin) {
                Piglin var13 = (Piglin)var11;
                if (var13.isBaby() && !var4.isPresent()) {
                    var4 = Optional.of(var13);
                } else if (var13.isAdult()) {
                    var9.add(var13);
                }
            } else if (var11 instanceof Player) {
                Player var14 = (Player)var11;
                if (!var6.isPresent() && EntitySelector.ATTACK_ALLOWED.test(var11) && !PiglinAi.isWearingGold(var14)) {
                    var6 = Optional.of(var14);
                }

                if (!var7.isPresent() && !var14.isSpectator() && PiglinAi.isPlayerHoldingLovedItem(var14)) {
                    var7 = Optional.of(var14);
                }
            } else if (!var1.isPresent() && var11 instanceof WitherSkeleton) {
                var1 = Optional.of((WitherSkeleton)var11);
            } else if (!var5.isPresent() && var11 instanceof ZombifiedPiglin) {
                var5 = Optional.of((ZombifiedPiglin)var11);
            }
        }

        var0.setMemory(MemoryModuleType.NEAREST_VISIBLE_WITHER_SKELETON, var1);
        var0.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLIN, var2);
        var0.setMemory(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, var3);
        var0.setMemory(MemoryModuleType.NEAREST_VISIBLE_BABY_PIGLIN, var4);
        var0.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED_PIGLIN, var5);
        var0.setMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, var6);
        var0.setMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, var7);
        var0.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, var9);
        var0.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, var9.size());
        var0.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, var8);
    }

    private static Optional<BlockPos> findNearestRepellent(ServerLevel param0, LivingEntity param1) {
        return BlockPos.findClosestMatch(param1.blockPosition(), 8, 4, param1x -> param0.getBlockState(param1x).is(BlockTags.PIGLIN_REPELLENTS));
    }
}
