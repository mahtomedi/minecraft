package net.minecraft.util.profiling;

import java.util.function.Supplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ProfileCollector extends ProfilerFiller {
    @Override
    void push(String var1);

    @Override
    void push(Supplier<String> var1);

    @Override
    void pop();

    @Override
    void popPush(String var1);

    @OnlyIn(Dist.CLIENT)
    @Override
    void popPush(Supplier<String> var1);

    ProfileResults getResults();
}
