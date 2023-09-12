package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public class ShowTradesToPlayer extends Behavior<Villager> {
    private static final int MAX_LOOK_TIME = 900;
    private static final int STARTING_LOOK_TIME = 40;
    @Nullable
    private ItemStack playerItemStack;
    private final List<ItemStack> displayItems = Lists.newArrayList();
    private int cycleCounter;
    private int displayIndex;
    private int lookTime;

    public ShowTradesToPlayer(int param0, int param1) {
        super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_PRESENT), param0, param1);
    }

    public boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        Brain<?> var0 = param1.getBrain();
        if (var0.getMemory(MemoryModuleType.INTERACTION_TARGET).isEmpty()) {
            return false;
        } else {
            LivingEntity var1 = var0.getMemory(MemoryModuleType.INTERACTION_TARGET).get();
            return var1.getType() == EntityType.PLAYER && param1.isAlive() && var1.isAlive() && !param1.isBaby() && param1.distanceToSqr(var1) <= 17.0;
        }
    }

    public boolean canStillUse(ServerLevel param0, Villager param1, long param2) {
        return this.checkExtraStartConditions(param0, param1)
            && this.lookTime > 0
            && param1.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent();
    }

    public void start(ServerLevel param0, Villager param1, long param2) {
        super.start(param0, param1, param2);
        this.lookAtTarget(param1);
        this.cycleCounter = 0;
        this.displayIndex = 0;
        this.lookTime = 40;
    }

    public void tick(ServerLevel param0, Villager param1, long param2) {
        LivingEntity var0 = this.lookAtTarget(param1);
        this.findItemsToDisplay(var0, param1);
        if (!this.displayItems.isEmpty()) {
            this.displayCyclingItems(param1);
        } else {
            clearHeldItem(param1);
            this.lookTime = Math.min(this.lookTime, 40);
        }

        --this.lookTime;
    }

    public void stop(ServerLevel param0, Villager param1, long param2) {
        super.stop(param0, param1, param2);
        param1.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
        clearHeldItem(param1);
        this.playerItemStack = null;
    }

    private void findItemsToDisplay(LivingEntity param0, Villager param1) {
        boolean var0 = false;
        ItemStack var1 = param0.getMainHandItem();
        if (this.playerItemStack == null || !ItemStack.isSameItem(this.playerItemStack, var1)) {
            this.playerItemStack = var1;
            var0 = true;
            this.displayItems.clear();
        }

        if (var0 && !this.playerItemStack.isEmpty()) {
            this.updateDisplayItems(param1);
            if (!this.displayItems.isEmpty()) {
                this.lookTime = 900;
                this.displayFirstItem(param1);
            }
        }

    }

    private void displayFirstItem(Villager param0) {
        displayAsHeldItem(param0, this.displayItems.get(0));
    }

    private void updateDisplayItems(Villager param0) {
        for(MerchantOffer var0 : param0.getOffers()) {
            if (!var0.isOutOfStock() && this.playerItemStackMatchesCostOfOffer(var0)) {
                this.displayItems.add(var0.assemble());
            }
        }

    }

    private boolean playerItemStackMatchesCostOfOffer(MerchantOffer param0) {
        return ItemStack.isSameItem(this.playerItemStack, param0.getCostA()) || ItemStack.isSameItem(this.playerItemStack, param0.getCostB());
    }

    private static void clearHeldItem(Villager param0) {
        param0.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        param0.setDropChance(EquipmentSlot.MAINHAND, 0.085F);
    }

    private static void displayAsHeldItem(Villager param0, ItemStack param1) {
        param0.setItemSlot(EquipmentSlot.MAINHAND, param1);
        param0.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    private LivingEntity lookAtTarget(Villager param0) {
        Brain<?> var0 = param0.getBrain();
        LivingEntity var1 = var0.getMemory(MemoryModuleType.INTERACTION_TARGET).get();
        var0.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(var1, true));
        return var1;
    }

    private void displayCyclingItems(Villager param0) {
        if (this.displayItems.size() >= 2 && ++this.cycleCounter >= 40) {
            ++this.displayIndex;
            this.cycleCounter = 0;
            if (this.displayIndex > this.displayItems.size() - 1) {
                this.displayIndex = 0;
            }

            displayAsHeldItem(param0, this.displayItems.get(this.displayIndex));
        }

    }
}
