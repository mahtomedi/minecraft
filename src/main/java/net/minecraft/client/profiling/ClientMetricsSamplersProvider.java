package net.minecraft.client.profiling;

import com.mojang.blaze3d.systems.TimerQuery;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsSamplerProvider;
import net.minecraft.util.profiling.metrics.profiling.ProfilerSamplerAdapter;
import net.minecraft.util.profiling.metrics.profiling.ServerMetricsSamplersProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientMetricsSamplersProvider implements MetricsSamplerProvider {
    private final LevelRenderer levelRenderer;
    private final Set<MetricSampler> samplers = new ObjectOpenHashSet<>();
    private final ProfilerSamplerAdapter samplerFactory = new ProfilerSamplerAdapter();

    public ClientMetricsSamplersProvider(LongSupplier param0, LevelRenderer param1) {
        this.levelRenderer = param1;
        this.samplers.add(ServerMetricsSamplersProvider.tickTimeSampler(param0));
        this.registerStaticSamplers();
    }

    private void registerStaticSamplers() {
        this.samplers.addAll(ServerMetricsSamplersProvider.runtimeIndependentSamplers());
        this.samplers.add(MetricSampler.create("totalChunks", MetricCategory.CHUNK_RENDERING, this.levelRenderer, LevelRenderer::getTotalSections));
        this.samplers.add(MetricSampler.create("renderedChunks", MetricCategory.CHUNK_RENDERING, this.levelRenderer, LevelRenderer::countRenderedSections));
        this.samplers.add(MetricSampler.create("lastViewDistance", MetricCategory.CHUNK_RENDERING, this.levelRenderer, LevelRenderer::getLastViewDistance));
        SectionRenderDispatcher var0 = this.levelRenderer.getSectionRenderDispatcher();
        this.samplers.add(MetricSampler.create("toUpload", MetricCategory.CHUNK_RENDERING_DISPATCHING, var0, SectionRenderDispatcher::getToUpload));
        this.samplers
            .add(MetricSampler.create("freeBufferCount", MetricCategory.CHUNK_RENDERING_DISPATCHING, var0, SectionRenderDispatcher::getFreeBufferCount));
        this.samplers.add(MetricSampler.create("toBatchCount", MetricCategory.CHUNK_RENDERING_DISPATCHING, var0, SectionRenderDispatcher::getToBatchCount));
        if (TimerQuery.getInstance().isPresent()) {
            this.samplers.add(MetricSampler.create("gpuUtilization", MetricCategory.GPU, Minecraft.getInstance(), Minecraft::getGpuUtilization));
        }

    }

    @Override
    public Set<MetricSampler> samplers(Supplier<ProfileCollector> param0) {
        this.samplers.addAll(this.samplerFactory.newSamplersFoundInProfiler(param0));
        return this.samplers;
    }
}
