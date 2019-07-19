package net.minecraft.world.entity.ai.goal;

import com.google.common.collect.Sets;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GoalSelector {
    private static final Logger LOGGER = LogManager.getLogger();
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
    private final ProfilerFiller profiler;
    private final EnumSet<Goal.Flag> disabledFlags = EnumSet.noneOf(Goal.Flag.class);
    private int newGoalRate = 3;

    public GoalSelector(ProfilerFiller param0) {
        this.profiler = param0;
    }

    public void addGoal(int param0, Goal param1) {
        this.availableGoals.add(new WrappedGoal(param0, param1));
    }

    public void removeGoal(Goal param0) {
        this.availableGoals.stream().filter(param1 -> param1.getGoal() == param0).filter(WrappedGoal::isRunning).forEach(WrappedGoal::stop);
        this.availableGoals.removeIf(param1 -> param1.getGoal() == param0);
    }

    public void tick() {
        this.profiler.push("goalCleanup");
        this.getRunningGoals()
            .filter(param0 -> !param0.isRunning() || param0.getFlags().stream().anyMatch(this.disabledFlags::contains) || !param0.canContinueToUse())
            .forEach(Goal::stop);
        this.lockedFlags.forEach((param0, param1) -> {
            if (!param1.isRunning()) {
                this.lockedFlags.remove(param0);
            }

        });
        this.profiler.pop();
        this.profiler.push("goalUpdate");
        this.availableGoals
            .stream()
            .filter(param0 -> !param0.isRunning())
            .filter(param0 -> param0.getFlags().stream().noneMatch(this.disabledFlags::contains))
            .filter(param0 -> param0.getFlags().stream().allMatch(param1 -> this.lockedFlags.getOrDefault(param1, NO_GOAL).canBeReplacedBy(param0)))
            .filter(WrappedGoal::canUse)
            .forEach(param0 -> {
                param0.getFlags().forEach(param1 -> {
                    WrappedGoal var0 = this.lockedFlags.getOrDefault(param1, NO_GOAL);
                    var0.stop();
                    this.lockedFlags.put(param1, param0);
                });
                param0.start();
            });
        this.profiler.pop();
        this.profiler.push("goalTick");
        this.getRunningGoals().forEach(WrappedGoal::tick);
        this.profiler.pop();
    }

    public Stream<WrappedGoal> getRunningGoals() {
        return this.availableGoals.stream().filter(WrappedGoal::isRunning);
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
