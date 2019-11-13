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
        param0.put(RenderType.blockentitySolid(), this.fixedBufferPack.builder(RenderType.solid()));
        param0.put(RenderType.blockentityCutout(), this.fixedBufferPack.builder(RenderType.cutout()));
        param0.put(RenderType.blockentityNoOutline(), this.fixedBufferPack.builder(RenderType.cutoutMipped()));
        param0.put(RenderType.blockentityTranslucent(), this.fixedBufferPack.builder(RenderType.translucent()));
        put(param0, RenderType.translucentNoCrumbling());
        put(param0, RenderType.glint());
        put(param0, RenderType.entityGlint());
        put(param0, RenderType.waterMask());

        for(int var0 = 0; var0 < 10; ++var0) {
            RenderType var1 = RenderType.crumbling(var0);
            put(param0, var1);
        }

    });
    private final MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediateWithBuffers(this.fixedBuffers, new BufferBuilder(256));
    private final MultiBufferSource.BufferSource crumblingBufferSource = MultiBufferSource.immediate(new BufferBuilder(256));
    private final OutlineBufferSource outlineBufferSource = new OutlineBufferSource(this.bufferSource);

    private static void put(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> param0, RenderType param1) {
        param0.put(param1, new BufferBuilder(param1.bufferSize()));
    }

    public ChunkBufferBuilderPack fixedBufferPack() {
        return this.fixedBufferPack;
    }

    public MultiBufferSource.BufferSource bufferSource() {
        return this.bufferSource;
    }

    public MultiBufferSource.BufferSource crumblingBufferSource() {
        return this.crumblingBufferSource;
    }

    public OutlineBufferSource outlineBufferSource() {
        return this.outlineBufferSource;
    }
}
