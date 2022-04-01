package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TradeWithVillager extends Behavior<Villager> {
    private static final int INTERACT_DIST_SQR = 5;
    private static final float SPEED_MODIFIER = 0.5F;
    private Set<Item> trades = ImmutableSet.of();

    public TradeWithVillager() {
        super(
            ImmutableMap.of(
                MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT
            )
        );
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        return BehaviorUtils.targetIsValid(param1.getBrain(), MemoryModuleType.INTERACTION_TARGET, EntityType.VILLAGER);
    }

    protected boolean canStillUse(ServerLevel param0, Villager param1, long param2) {
        return this.checkExtraStartConditions(param0, param1);
    }

    protected void start(ServerLevel param0, Villager param1, long param2) {
        Villager var0 = (Villager)param1.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
        BehaviorUtils.lockGazeAndWalkToEachOther(param1, var0, 0.5F);
        this.trades = figureOutWhatIAmWillingToTrade(param1, var0);
    }

    protected void tick(ServerLevel param0, Villager param1, long param2) {
        Villager var0 = (Villager)param1.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
        if (!(param1.distanceToSqr(var0) > 5.0)) {
            BehaviorUtils.lockGazeAndWalkToEachOther(param1, var0, 0.5F);
            if (param1.hasExcessFood() && (param1.getVillagerData().getProfession() == VillagerProfession.FARMER || var0.wantsMoreFood())) {
                throwHalfStack(param1, Villager.FOOD_POINTS.keySet(), var0);
            }

            if (var0.getVillagerData().getProfession() == VillagerProfession.FARMER
                && param1.getInventory().countItem(Items.WHEAT) > Items.WHEAT.getMaxStackSize() / 2) {
                throwHalfStack(param1, ImmutableSet.of(Items.WHEAT), var0);
            }

            if (!this.trades.isEmpty() && param1.getInventory().hasAnyOf(this.trades)) {
                throwHalfStack(param1, this.trades, var0);
            }

        }
    }

    protected void stop(ServerLevel param0, Villager param1, long param2) {
        param1.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
    }

    private static Set<Item> figureOutWhatIAmWillingToTrade(Villager param0, Villager param1) {
        ImmutableSet<Item> var0 = param1.getVillagerData().getProfession().getRequestedItems();
        ImmutableSet<Item> var1 = param0.getVillagerData().getProfession().getRequestedItems();
        return var0.stream().filter(param1x -> !var1.contains(param1x)).collect(Collectors.toSet());
    }

    private static void throwHalfStack(Villager param0, Set<Item> param1, LivingEntity param2) {
        SimpleContainer var0 = param0.getInventory();
        ItemStack var1 = ItemStack.EMPTY;
        int var2 = 0;

        while(var2 < var0.getContainerSize()) {
            ItemStack var3;
            Item var4;
            int var5;
            label28: {
                var3 = var0.getItem(var2);
                if (!var3.isEmpty()) {
                    var4 = var3.getItem();
                    if (param1.contains(var4)) {
                        if (var3.getCount() > var3.getMaxStackSize() / 2) {
                            var5 = var3.getCount() / 2;
                            break label28;
                        }

                        if (var3.getCount() > 24) {
                            var5 = var3.getCount() - 24;
                            break label28;
                        }
                    }
                }

                ++var2;
                continue;
            }

            var3.shrink(var5);
            var1 = new ItemStack(var4, var5);
            break;
        }

        if (!var1.isEmpty()) {
            BehaviorUtils.throwItem(param0, var1, param2.position());
        }

    }
}
