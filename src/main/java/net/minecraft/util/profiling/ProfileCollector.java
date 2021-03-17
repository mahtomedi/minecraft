package net.minecraft.util.profiling;

import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ProfileCollector extends ProfilerFiller {
    ProfileResults getResults();

    @Nullable
    @OnlyIn(Dist.CLIENT)
    ActiveProfiler.PathEntry getEntry(String var1);
}
