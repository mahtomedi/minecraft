package net.minecraft.util.profiling;

import java.util.function.Supplier;
import net.minecraft.util.profiling.metrics.MetricCategory;

public interface ProfilerFiller {
    String ROOT = "root";

    void startTick();

    void endTick();

    void push(String var1);

    void push(Supplier<String> var1);

    void pop();

    void popPush(String var1);

    void popPush(Supplier<String> var1);

    void markForCharting(MetricCategory var1);

    default void incrementCounter(String param0) {
        this.incrementCounter(param0, 1);
    }

    void incrementCounter(String var1, int var2);

    default void incrementCounter(Supplier<String> param0) {
        this.incrementCounter(param0, 1);
    }

    void incrementCounter(Supplier<String> var1, int var2);

    static ProfilerFiller tee(final ProfilerFiller param0, final ProfilerFiller param1) {
        if (param0 == InactiveProfiler.INSTANCE) {
            return param1;
        } else {
            return param1 == InactiveProfiler.INSTANCE ? param0 : new ProfilerFiller() {
                @Override
                public void startTick() {
                    param0.startTick();
                    param1.startTick();
                }

                @Override
                public void endTick() {
                    param0.endTick();
                    param1.endTick();
                }

                @Override
                public void push(String param0x) {
                    param0.push(param0);
                    param1.push(param0);
                }

                @Override
                public void push(Supplier<String> param0x) {
                    param0.push(param0);
                    param1.push(param0);
                }

                @Override
                public void markForCharting(MetricCategory param0x) {
                    param0.markForCharting(param0);
                    param1.markForCharting(param0);
                }

                @Override
                public void pop() {
                    param0.pop();
                    param1.pop();
                }

                @Override
                public void popPush(String param0x) {
                    param0.popPush(param0);
                    param1.popPush(param0);
                }

                @Override
                public void popPush(Supplier<String> param0x) {
                    param0.popPush(param0);
                    param1.popPush(param0);
                }

                @Override
                public void incrementCounter(String param0x, int param1x) {
                    param0.incrementCounter(param0, param1);
                    param1.incrementCounter(param0, param1);
                }

                @Override
                public void incrementCounter(Supplier<String> param0x, int param1x) {
                    param0.incrementCounter(param0, param1);
                    param1.incrementCounter(param0, param1);
                }
            };
        }
    }
}
