package net.minecraft.util.profiling.registry;

import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ProfilerMeasured {
    @OnlyIn(Dist.CLIENT)
    List<MeasuredMetric> metrics();
}
