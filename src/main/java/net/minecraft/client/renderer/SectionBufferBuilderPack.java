package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SectionBufferBuilderPack implements AutoCloseable {
    public static final int TOTAL_BUFFERS_SIZE = RenderType.chunkBufferLayers().stream().mapToInt(RenderType::bufferSize).sum();
    private final Map<RenderType, BufferBuilder> builders = RenderType.chunkBufferLayers()
        .stream()
        .collect(Collectors.toMap(param0 -> param0, param0 -> new BufferBuilder(param0.bufferSize())));

    public BufferBuilder builder(RenderType param0) {
        return this.builders.get(param0);
    }

    public void clearAll() {
        this.builders.values().forEach(BufferBuilder::clear);
    }

    public void discardAll() {
        this.builders.values().forEach(BufferBuilder::discard);
    }

    @Override
    public void close() {
        this.builders.values().forEach(BufferBuilder::release);
    }
}
