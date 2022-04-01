package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.CarryableTrade;
import net.minecraft.world.item.trading.MerchantOffer;

public class ShowTradesToPlayer extends Behavior<Villager> {
    private static final int MAX_LOOK_TIME = 900;
    private static final int STARTING_LOOK_TIME = 40;
    public static final int CYCLE_INTERVAL_TICKS = 100;
    @Nullable
    private CarryableTrade playerTradeProposition;
    private final List<MerchantOffer> availableOffers = Lists.newArrayList();
    private int cycleCounter;
    private int displayIndex;
    private int lookTime;

    public ShowTradesToPlayer(int param0, int param1) {
        super(ImmutableMap.of(MemoryModuleType.NEAREST_PLAYERS, MemoryStatus.VALUE_PRESENT), param0, param1);
    }

    public boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        Brain<?> var0 = param1.getBrain();
        if (!var0.getMemory(MemoryModuleType.NEAREST_PLAYERS).isPresent()) {
            return false;
        } else if (param1.isAlive() && !param1.isBaby()) {
            for(Player var2 : var0.getMemory(MemoryModuleType.NEAREST_PLAYERS).get()) {
                if (var2.isAlive() && var2.position().closerThan(param1.position(), 6.0)) {
                    CarryableTrade var3 = var2.getTradeProposition();
                    if (var3 != null && this.hasMatchingOffers(param1, var3)) {
                        param1.getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, var2);
                        return true;
                    }
                }
            }

            return false;
        } else {
            return false;
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
        if (!this.availableOffers.isEmpty()) {
            this.displayCyclingItems(param1);
        } else {
            param1.setCurrentOffer(null);
            this.lookTime = Math.min(this.lookTime, 40);
        }

        --this.lookTime;
    }

    public void stop(ServerLevel param0, Villager param1, long param2) {
        super.stop(param0, param1, param2);
        param1.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
        param1.setCurrentOffer(null);
        this.playerTradeProposition = null;
    }

    private void findItemsToDisplay(LivingEntity param0, Villager param1) {
        boolean var0 = false;
        CarryableTrade var1 = param0.getTradeProposition();
        if (this.playerTradeProposition == null || var1 == null || !var1.matches(this.playerTradeProposition)) {
            this.playerTradeProposition = var1;
            var0 = true;
            this.availableOffers.clear();
        }

        if (var0 && this.playerTradeProposition != null) {
            this.updateDisplayItems(param1, this.playerTradeProposition);
            if (!this.availableOffers.isEmpty()) {
                this.lookTime = 900;
                this.displayFirstItem(param1);
            }
        }

    }

    private void displayFirstItem(Villager param0) {
        MerchantOffer var0 = this.availableOffers.get(0);
        param0.setCurrentOffer(var0);
    }

    private void updateDisplayItems(Villager param0, CarryableTrade param1) {
        getMatchingOffers(param0, param1).forEach(this.availableOffers::add);
        Collections.shuffle(this.availableOffers);
    }

    private boolean hasMatchingOffers(Villager param0, CarryableTrade param1) {
        return !Iterables.isEmpty(getMatchingOffers(param0, param1));
    }

    private static Iterable<MerchantOffer> getMatchingOffers(Villager param0, CarryableTrade param1) {
        return Iterables.filter(param0.getOffers(), param1x -> !param1x.isOutOfStock() && param1x.accepts(param1));
    }

    private LivingEntity lookAtTarget(Villager param0) {
        Brain<?> var0 = param0.getBrain();
        LivingEntity var1 = var0.getMemory(MemoryModuleType.INTERACTION_TARGET).get();
        var0.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(var1, true));
        return var1;
    }

    private void displayCyclingItems(Villager param0) {
        if (this.availableOffers.size() >= 2 && ++this.cycleCounter >= 100) {
            ++this.displayIndex;
            this.cycleCounter = 0;
            if (this.displayIndex > this.availableOffers.size() - 1) {
                this.displayIndex = 0;
            }

            param0.setCurrentOffer(this.availableOffers.get(this.displayIndex));
        }

    }
}
