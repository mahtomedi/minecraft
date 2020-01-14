package net.minecraft.util.profiling;

import java.util.function.Supplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ProfilerFiller {
    void startTick();

    void endTick();

    void push(String var1);

    void push(Supplier<String> var1);

    void pop();

    void popPush(String var1);

    @OnlyIn(Dist.CLIENT)
    void popPush(Supplier<String> var1);

    void incrementCounter(String var1);

    void incrementCounter(Supplier<String> var1);
}
