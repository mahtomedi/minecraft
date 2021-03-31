package net.minecraft.util.profiling;

import java.util.function.Supplier;
import javax.annotation.Nullable;

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
    public void pop() {
    }

    @Override
    public void popPush(String param0) {
    }

    @Override
    public void popPush(Supplier<String> param0) {
    }

    @Override
    public void incrementCounter(String param0) {
    }

    @Override
    public void incrementCounter(Supplier<String> param0) {
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
}
