package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableList;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PiglinSpecificSensor extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_NEMESIS,
            MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD,
            MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM,
            MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN,
            MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN,
            MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS,
            MemoryModuleType.NEARBY_ADULT_PIGLINS,
            MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT,
            MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT,
            MemoryModuleType.NEAREST_REPELLENT
        );
    }

    @Override
    protected void doTick(ServerLevel param0, LivingEntity param1) {
        Brain<?> var0 = param1.getBrain();
        var0.setMemory(MemoryModuleType.NEAREST_REPELLENT, findNearestRepellent(param0, param1));
        Optional<Mob> var1 = Optional.empty();
        Optional<Hoglin> var2 = Optional.empty();
        Optional<Hoglin> var3 = Optional.empty();
        Optional<Piglin> var4 = Optional.empty();
        Optional<LivingEntity> var5 = Optional.empty();
        Optional<Player> var6 = Optional.empty();
        Optional<Player> var7 = Optional.empty();
        int var8 = 0;
        List<AbstractPiglin> var9 = Lists.newArrayList();
        List<AbstractPiglin> var10 = Lists.newArrayList();

        for(LivingEntity var12 : var0.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(ImmutableList.of())) {
            if (var12 instanceof Hoglin) {
                Hoglin var13 = (Hoglin)var12;
                if (var13.isBaby() && !var3.isPresent()) {
                    var3 = Optional.of(var13);
                } else if (var13.isAdult()) {
                    ++var8;
                    if (!var2.isPresent() && var13.canBeHunted()) {
                        var2 = Optional.of(var13);
                    }
                }
            } else if (var12 instanceof PiglinBrute) {
                var9.add((PiglinBrute)var12);
            } else if (var12 instanceof Piglin) {
                Piglin var14 = (Piglin)var12;
                if (var14.isBaby() && !var4.isPresent()) {
                    var4 = Optional.of(var14);
                } else if (var14.isAdult()) {
                    var9.add(var14);
                }
            } else if (var12 instanceof Player) {
                Player var15 = (Player)var12;
                if (!var6.isPresent() && EntitySelector.ATTACK_ALLOWED.test(var12) && !PiglinAi.isWearingGold(var15)) {
                    var6 = Optional.of(var15);
                }

                if (!var7.isPresent() && !var15.isSpectator() && PiglinAi.isPlayerHoldingLovedItem(var15)) {
                    var7 = Optional.of(var15);
                }
            } else if (var1.isPresent() || !(var12 instanceof WitherSkeleton) && !(var12 instanceof WitherBoss)) {
                if (!var5.isPresent() && PiglinAi.isZombified(var12.getType())) {
                    var5 = Optional.of(var12);
                }
            } else {
                var1 = Optional.of((Mob)var12);
            }
        }

        for(LivingEntity var17 : var0.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(ImmutableList.of())) {
            if (var17 instanceof AbstractPiglin && ((AbstractPiglin)var17).isAdult()) {
                var10.add((AbstractPiglin)var17);
            }
        }

        var0.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, var1);
        var0.setMemory(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, var2);
        var0.setMemory(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, var3);
        var0.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, var5);
        var0.setMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, var6);
        var0.setMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, var7);
        var0.setMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS, var10);
        var0.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, var9);
        var0.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, var9.size());
        var0.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, var8);
    }

    private static Optional<BlockPos> findNearestRepellent(ServerLevel param0, LivingEntity param1) {
        return BlockPos.findClosestMatch(param1.blockPosition(), 8, 4, param1x -> isValidRepellent(param0, param1x));
    }

    private static boolean isValidRepellent(ServerLevel param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        boolean var1 = var0.is(BlockTags.PIGLIN_REPELLENTS);
        return var1 && var0.is(Blocks.SOUL_CAMPFIRE) ? CampfireBlock.isLitCampfire(var0) : var1;
    }
}
