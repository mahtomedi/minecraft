package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import java.util.Collection;
import javax.annotation.Nullable;

public class MultipleTestTracker {
    private final Collection<GameTestInfo> tests = Lists.newArrayList();
    @Nullable
    private GameTestListener listener;

    public MultipleTestTracker() {
    }

    public MultipleTestTracker(Collection<GameTestInfo> param0) {
        this.tests.addAll(param0);
    }

    public void add(GameTestInfo param0) {
        this.tests.add(param0);
        if (this.listener != null) {
            param0.addListener(this.listener);
        }

    }

    public void setListener(GameTestListener param0) {
        this.listener = param0;
        this.tests.forEach(param1 -> param1.addListener(param0));
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
                var0.append('\u221a');
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
