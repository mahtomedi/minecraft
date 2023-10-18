package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.SortedMap;
import net.minecraft.Util;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderBuffers {
    private final SectionBufferBuilderPack fixedBufferPack = new SectionBufferBuilderPack();
    private final SectionBufferBuilderPool sectionBufferPool;
    private final MultiBufferSource.BufferSource bufferSource;
    private final MultiBufferSource.BufferSource crumblingBufferSource;
    private final OutlineBufferSource outlineBufferSource;

    public RenderBuffers(int param0) {
        this.sectionBufferPool = SectionBufferBuilderPool.allocate(param0);
        SortedMap<RenderType, BufferBuilder> var0 = Util.make(new Object2ObjectLinkedOpenHashMap<>(), param0x -> {
            param0x.put(Sheets.solidBlockSheet(), this.fixedBufferPack.builder(RenderType.solid()));
            param0x.put(Sheets.cutoutBlockSheet(), this.fixedBufferPack.builder(RenderType.cutout()));
            param0x.put(Sheets.bannerSheet(), this.fixedBufferPack.builder(RenderType.cutoutMipped()));
            param0x.put(Sheets.translucentCullBlockSheet(), this.fixedBufferPack.builder(RenderType.translucent()));
            put(param0x, Sheets.shieldSheet());
            put(param0x, Sheets.bedSheet());
            put(param0x, Sheets.shulkerBoxSheet());
            put(param0x, Sheets.signSheet());
            put(param0x, Sheets.hangingSignSheet());
            param0x.put(Sheets.chestSheet(), new BufferBuilder(786432));
            put(param0x, RenderType.armorGlint());
            put(param0x, RenderType.armorEntityGlint());
            put(param0x, RenderType.glint());
            put(param0x, RenderType.glintDirect());
            put(param0x, RenderType.glintTranslucent());
            put(param0x, RenderType.entityGlint());
            put(param0x, RenderType.entityGlintDirect());
            put(param0x, RenderType.waterMask());
            ModelBakery.DESTROY_TYPES.forEach(param1 -> put(param0x, param1));
        });
        this.crumblingBufferSource = MultiBufferSource.immediate(new BufferBuilder(1536));
        this.bufferSource = MultiBufferSource.immediateWithBuffers(var0, new BufferBuilder(786432));
        this.outlineBufferSource = new OutlineBufferSource(this.bufferSource);
    }

    private static void put(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> param0, RenderType param1) {
        param0.put(param1, new BufferBuilder(param1.bufferSize()));
    }

    public SectionBufferBuilderPack fixedBufferPack() {
        return this.fixedBufferPack;
    }

    public SectionBufferBuilderPool sectionBufferPool() {
        return this.sectionBufferPool;
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
