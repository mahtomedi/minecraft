package net.minecraft.world.entity.ai.goal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class GoalSelector {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final WrappedGoal NO_GOAL = new WrappedGoal(Integer.MAX_VALUE, new Goal() {
        @Override
        public boolean canUse() {
            return false;
        }
    }) {
        @Override
        public boolean isRunning() {
            return false;
        }
    };
    private final Map<Goal.Flag, WrappedGoal> lockedFlags = new EnumMap<>(Goal.Flag.class);
    private final Set<WrappedGoal> availableGoals = Sets.newLinkedHashSet();
    private final Supplier<ProfilerFiller> profiler;
    private final EnumSet<Goal.Flag> disabledFlags = EnumSet.noneOf(Goal.Flag.class);
    private int tickCount;
    private int newGoalRate = 3;

    public GoalSelector(Supplier<ProfilerFiller> param0) {
        this.profiler = param0;
    }

    public void addGoal(int param0, Goal param1) {
        this.availableGoals.add(new WrappedGoal(param0, param1));
    }

    @VisibleForTesting
    public void removeAllGoals(Predicate<Goal> param0) {
        this.availableGoals.removeIf(param1 -> param0.test(param1.getGoal()));
    }

    public void removeGoal(Goal param0) {
        this.availableGoals.stream().filter(param1 -> param1.getGoal() == param0).filter(WrappedGoal::isRunning).forEach(WrappedGoal::stop);
        this.availableGoals.removeIf(param1 -> param1.getGoal() == param0);
    }

    private static boolean goalContainsAnyFlags(WrappedGoal param0, EnumSet<Goal.Flag> param1) {
        for(Goal.Flag var0 : param0.getFlags()) {
            if (param1.contains(var0)) {
                return true;
            }
        }

        return false;
    }

    private static boolean goalCanBeReplacedForAllFlags(WrappedGoal param0, Map<Goal.Flag, WrappedGoal> param1) {
        for(Goal.Flag var0 : param0.getFlags()) {
            if (!param1.getOrDefault(var0, NO_GOAL).canBeReplacedBy(param0)) {
                return false;
            }
        }

        return true;
    }

    public void tick() {
        ProfilerFiller var0 = this.profiler.get();
        var0.push("goalCleanup");

        for(WrappedGoal var1 : this.availableGoals) {
            if (var1.isRunning() && (goalContainsAnyFlags(var1, this.disabledFlags) || !var1.canContinueToUse())) {
                var1.stop();
            }
        }

        Iterator<Entry<Goal.Flag, WrappedGoal>> var2 = this.lockedFlags.entrySet().iterator();

        while(var2.hasNext()) {
            Entry<Goal.Flag, WrappedGoal> var3 = var2.next();
            if (!var3.getValue().isRunning()) {
                var2.remove();
            }
        }

        var0.pop();
        var0.push("goalUpdate");

        for(WrappedGoal var4 : this.availableGoals) {
            if (!var4.isRunning() && !goalContainsAnyFlags(var4, this.disabledFlags) && goalCanBeReplacedForAllFlags(var4, this.lockedFlags) && var4.canUse()) {
                for(Goal.Flag var5 : var4.getFlags()) {
                    WrappedGoal var6 = this.lockedFlags.getOrDefault(var5, NO_GOAL);
                    var6.stop();
                    this.lockedFlags.put(var5, var4);
                }

                var4.start();
            }
        }

        var0.pop();
        this.tickRunningGoals(true);
    }

    public void tickRunningGoals(boolean param0) {
        ProfilerFiller var0 = this.profiler.get();
        var0.push("goalTick");

        for(WrappedGoal var1 : this.availableGoals) {
            if (var1.isRunning() && (param0 || var1.requiresUpdateEveryTick())) {
                var1.tick();
            }
        }

        var0.pop();
    }

    public Set<WrappedGoal> getAvailableGoals() {
        return this.availableGoals;
    }

    public Stream<WrappedGoal> getRunningGoals() {
        return this.availableGoals.stream().filter(WrappedGoal::isRunning);
    }

    public void setNewGoalRate(int param0) {
        this.newGoalRate = param0;
    }

    public void disableControlFlag(Goal.Flag param0) {
        this.disabledFlags.add(param0);
    }

    public void enableControlFlag(Goal.Flag param0) {
        this.disabledFlags.remove(param0);
    }

    public void setControlFlag(Goal.Flag param0, boolean param1) {
        if (param1) {
            this.enableControlFlag(param0);
        } else {
            this.disableControlFlag(param0);
        }

    }
}
