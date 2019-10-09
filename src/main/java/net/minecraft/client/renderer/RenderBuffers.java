package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.SortedMap;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderBuffers {
    private final ChunkBufferBuilderPack fixedBufferPack = new ChunkBufferBuilderPack();
    private final SortedMap<RenderType, BufferBuilder> fixedBuffers = Util.make(new Object2ObjectLinkedOpenHashMap<>(), param0 -> {
        param0.put(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS), this.fixedBufferPack.builder(RenderType.solid()));
        param0.put(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS), this.fixedBufferPack.builder(RenderType.cutout()));
        param0.put(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), this.fixedBufferPack.builder(RenderType.translucent()));
        param0.put(RenderType.translucentNoCrumbling(), new BufferBuilder(RenderType.translucentNoCrumbling().bufferSize()));
        param0.put(RenderType.glint(), new BufferBuilder(RenderType.glint().bufferSize()));
        param0.put(RenderType.entityGlint(), new BufferBuilder(RenderType.entityGlint().bufferSize()));
        param0.put(RenderType.waterMask(), new BufferBuilder(RenderType.waterMask().bufferSize()));
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
