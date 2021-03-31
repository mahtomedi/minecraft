package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class MultipleTestTracker {
    private static final char NOT_STARTED_TEST_CHAR = ' ';
    private static final char ONGOING_TEST_CHAR = '_';
    private static final char SUCCESSFUL_TEST_CHAR = '+';
    private static final char FAILED_OPTIONAL_TEST_CHAR = 'x';
    private static final char FAILED_REQUIRED_TEST_CHAR = 'X';
    private final Collection<GameTestInfo> tests = Lists.newArrayList();
    @Nullable
    private final Collection<GameTestListener> listeners = Lists.newArrayList();

    public MultipleTestTracker() {
    }

    public MultipleTestTracker(Collection<GameTestInfo> param0) {
        this.tests.addAll(param0);
    }

    public void addTestToTrack(GameTestInfo param0) {
        this.tests.add(param0);
        this.listeners.forEach(param0::addListener);
    }

    public void addListener(GameTestListener param0) {
        this.listeners.add(param0);
        this.tests.forEach(param1 -> param1.addListener(param0));
    }

    public void addFailureListener(final Consumer<GameTestInfo> param0) {
        this.addListener(new GameTestListener() {
            @Override
            public void testStructureLoaded(GameTestInfo param0x) {
            }

            @Override
            public void testPassed(GameTestInfo param0x) {
            }

            @Override
            public void testFailed(GameTestInfo param0x) {
                param0.accept(param0);
            }
        });
    }

    public int getFailedRequiredCount() {
        return (int)this.tests.stream().filter(GameTestInfo::hasFailed).filter(GameTestInfo::isRequired).count();
    }

    public int getFailedOptionalCount() {
        return (int)this.tests.stream().filter(GameTestInfo::hasFailed).filter(GameTestInfo::isOptional).count();
    }

    public int getDoneCount() {
        return (int)this.tests.stream().filter(GameTestInfo::isDone).count();
    }

    public boolean hasFailedRequired() {
        return this.getFailedRequiredCount() > 0;
    }

    public boolean hasFailedOptional() {
        return this.getFailedOptionalCount() > 0;
    }

    public Collection<GameTestInfo> getFailedRequired() {
        return this.tests.stream().filter(GameTestInfo::hasFailed).filter(GameTestInfo::isRequired).collect(Collectors.toList());
    }

    public Collection<GameTestInfo> getFailedOptional() {
        return this.tests.stream().filter(GameTestInfo::hasFailed).filter(GameTestInfo::isOptional).collect(Collectors.toList());
    }

    public int getTotalCount() {
        return this.tests.size();
    }

    public boolean isDone() {
        return this.getDoneCount() == this.getTotalCount();
    }

    public String getProgressBar() {
        StringBuffer var0 = new StringBuffer();
        var0.append('[');
        this.tests.forEach(param1 -> {
            if (!param1.hasStarted()) {
                var0.append(' ');
            } else if (param1.hasSucceeded()) {
                var0.append('+');
            } else if (param1.hasFailed()) {
                var0.append((char)(param1.isRequired() ? 'X' : 'x'));
            } else {
                var0.append('_');
            }

        });
        var0.append(']');
        return var0.toString();
    }

    @Override
    public String toString() {
        return this.getProgressBar();
    }
}
