package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.EntityOutlineGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
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
    });
    private final MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediateWithBuffers(this.fixedBuffers, new BufferBuilder(256));
    private final MultiBufferSource.BufferSource effectBufferSource = MultiBufferSource.immediate(new BufferBuilder(256));
    private final BufferBuilder outlineBuilder = new BufferBuilder(RenderType.OUTLINE.bufferSize());
    private final EntityOutlineGenerator outlineBuffer = new EntityOutlineGenerator(this.outlineBuilder);
    private final MultiBufferSource outlineBufferSource = param0 -> {
        VertexConsumer var0 = this.bufferSource.getBuffer(param0);
        return (VertexConsumer)(param0.affectsEntityOutline() ? new VertexMultiConsumer(ImmutableList.of(this.outlineBuffer, var0)) : var0);
    };

    public ChunkBufferBuilderPack fixedBufferPack() {
        return this.fixedBufferPack;
    }

    public MultiBufferSource.BufferSource bufferSource() {
        return this.bufferSource;
    }

    public MultiBufferSource.BufferSource effectBufferSource() {
        return this.effectBufferSource;
    }

    public BufferBuilder outlineBuilder() {
        return this.outlineBuilder;
    }

    public EntityOutlineGenerator outlineBuffer() {
        return this.outlineBuffer;
    }

    public MultiBufferSource outlineBufferSource() {
        return this.outlineBufferSource;
    }
}
