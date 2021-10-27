package net.minecraft.util.profiling;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.apache.commons.lang3.tuple.Pair;

public class InactiveProfiler implements ProfileCollector {
    public static final InactiveProfiler INSTANCE = new InactiveProfiler();

    private InactiveProfiler() {
    }

    @Override
    public void startTick() {
    }

    @Override
    public void endTick() {
    }

    @Override
    public void push(String param0) {
    }

    @Override
    public void push(Supplier<String> param0) {
    }

    @Override
    public void markForCharting(MetricCategory param0) {
    }

    @Override
    public void pop() {
    }

    @Override
    public void popPush(String param0) {
    }

    @Override
    public void popPush(Supplier<String> param0) {
    }

    @Override
    public void incrementCounter(String param0, int param1) {
    }

    @Override
    public void incrementCounter(Supplier<String> param0, int param1) {
    }

    @Override
    public ProfileResults getResults() {
        return EmptyProfileResults.EMPTY;
    }

    @Nullable
    @Override
    public ActiveProfiler.PathEntry getEntry(String param0) {
        return null;
    }

    @Override
    public Set<Pair<String, MetricCategory>> getChartedPaths() {
        return ImmutableSet.of();
    }
}
