package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.SortedMap;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderBuffers {
    private final ChunkBufferBuilderPack fixedBufferPack = new ChunkBufferBuilderPack();
    private final SortedMap<RenderType, BufferBuilder> fixedBuffers = Util.make(new Object2ObjectLinkedOpenHashMap<>(), param0 -> {
        for(RenderType var0 : RenderType.chunkBufferLayers()) {
            param0.put(var0, this.fixedBufferPack.builder(var0));
        }

        param0.put(RenderType.TRANSLUCENT_NO_CRUMBLING, new BufferBuilder(RenderType.TRANSLUCENT_NO_CRUMBLING.bufferSize()));
        param0.put(RenderType.GLINT, new BufferBuilder(RenderType.GLINT.bufferSize()));
        param0.put(RenderType.ENTITY_GLINT, new BufferBuilder(RenderType.ENTITY_GLINT.bufferSize()));
        param0.put(RenderType.WATER_MASK, new BufferBuilder(RenderType.WATER_MASK.bufferSize()));
    });
    private final MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediateWithBuffers(this.fixedBuffers, new BufferBuilder(256));
    private final MultiBufferSource.BufferSource effectBufferSource = MultiBufferSource.immediate(new BufferBuilder(256));
    private final OutlineBufferSource outlineBufferSource = new OutlineBufferSource(this.bufferSource);

    public ChunkBufferBuilderPack fixedBufferPack() {
        return this.fixedBufferPack;
    }

    public MultiBufferSource.BufferSource bufferSource() {
        return this.bufferSource;
    }

    public MultiBufferSource.BufferSource effectBufferSource() {
        return this.effectBufferSource;
    }

    public OutlineBufferSource outlineBufferSource() {
        return this.outlineBufferSource;
    }
}
