package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class GiveGiftToHero extends Behavior<Villager> {
    private static final Map<VillagerProfession, ResourceLocation> gifts = Util.make(Maps.newHashMap(), param0 -> {
        param0.put(VillagerProfession.ARMORER, BuiltInLootTables.ARMORER_GIFT);
        param0.put(VillagerProfession.BUTCHER, BuiltInLootTables.BUTCHER_GIFT);
        param0.put(VillagerProfession.CARTOGRAPHER, BuiltInLootTables.CARTOGRAPHER_GIFT);
        param0.put(VillagerProfession.CLERIC, BuiltInLootTables.CLERIC_GIFT);
        param0.put(VillagerProfession.FARMER, BuiltInLootTables.FARMER_GIFT);
        param0.put(VillagerProfession.FISHERMAN, BuiltInLootTables.FISHERMAN_GIFT);
        param0.put(VillagerProfession.FLETCHER, BuiltInLootTables.FLETCHER_GIFT);
        param0.put(VillagerProfession.LEATHERWORKER, BuiltInLootTables.LEATHERWORKER_GIFT);
        param0.put(VillagerProfession.LIBRARIAN, BuiltInLootTables.LIBRARIAN_GIFT);
        param0.put(VillagerProfession.MASON, BuiltInLootTables.MASON_GIFT);
        param0.put(VillagerProfession.SHEPHERD, BuiltInLootTables.SHEPHERD_GIFT);
        param0.put(VillagerProfession.TOOLSMITH, BuiltInLootTables.TOOLSMITH_GIFT);
        param0.put(VillagerProfession.WEAPONSMITH, BuiltInLootTables.WEAPONSMITH_GIFT);
    });
    private int timeUntilNextGift = 600;
    private boolean giftGivenDuringThisRun;
    private long timeSinceStart;

    public GiveGiftToHero(int param0) {
        super(
            ImmutableMap.of(
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.INTERACTION_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.NEAREST_VISIBLE_PLAYER,
                MemoryStatus.VALUE_PRESENT
            ),
            param0
        );
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        if (!this.isHeroVisible(param1)) {
            return false;
        } else if (this.timeUntilNextGift > 0) {
            --this.timeUntilNextGift;
            return false;
        } else {
            return true;
        }
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        this.giftGivenDuringThisRun = false;
        this.timeSinceStart = param2;
        Player var0 = this.getNearestTargetableHero(param1).get();
        param1.getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, var0);
        BehaviorUtils.lookAtEntity(param1, var0);
    }

    protected boolean canStillUse(ServerLevel param0, Villager param1, long param2) {
        return this.isHeroVisible(param1) && !this.giftGivenDuringThisRun;
    }

    protected void tick(ServerLevel param0, Villager param1, long param2) {
        Player var0 = this.getNearestTargetableHero(param1).get();
        BehaviorUtils.lookAtEntity(param1, var0);
        if (this.isWithinThrowingDistance(param1, var0)) {
            if (param2 - this.timeSinceStart > 20L) {
                this.throwGift(param1, var0);
                this.giftGivenDuringThisRun = true;
            }
        } else {
            BehaviorUtils.walkToEntity(param1, var0, 5);
        }

    }

    protected void stop(ServerLevel param0, Villager param1, long param2) {
        this.timeUntilNextGift = calculateTimeUntilNextGift(param0);
        param1.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
        param1.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        param1.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    private void throwGift(Villager param0, LivingEntity param1) {
        for(ItemStack var1 : this.getItemToThrow(param0)) {
            BehaviorUtils.throwItem(param0, var1, param1);
        }

    }

    private List<ItemStack> getItemToThrow(Villager param0) {
        if (param0.isBaby()) {
            return ImmutableList.of(new ItemStack(Items.POPPY));
        } else {
            VillagerProfession var0 = param0.getVillagerData().getProfession();
            if (gifts.containsKey(var0)) {
                LootTable var1 = param0.level.getServer().getLootTables().get(gifts.get(var0));
                LootContext.Builder var2 = new LootContext.Builder((ServerLevel)param0.level)
                    .withParameter(LootContextParams.BLOCK_POS, new BlockPos(param0))
                    .withParameter(LootContextParams.THIS_ENTITY, param0)
                    .withRandom(param0.getRandom());
                return var1.getRandomItems(var2.create(LootContextParamSets.GIFT));
            } else {
                return ImmutableList.of(new ItemStack(Items.WHEAT_SEEDS));
            }
        }
    }

    private boolean isHeroVisible(Villager param0) {
        return this.getNearestTargetableHero(param0).isPresent();
    }

    private Optional<Player> getNearestTargetableHero(Villager param0) {
        return param0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).filter(this::isHero);
    }

    private boolean isHero(Player param0x) {
        return param0x.hasEffect(MobEffects.HERO_OF_THE_VILLAGE);
    }

    private boolean isWithinThrowingDistance(Villager param0, Player param1) {
        BlockPos var0 = new BlockPos(param1);
        BlockPos var1 = new BlockPos(param0);
        return var1.closerThan(var0, 5.0);
    }

    private static int calculateTimeUntilNextGift(ServerLevel param0) {
        return 600 + param0.random.nextInt(6001);
    }
}
