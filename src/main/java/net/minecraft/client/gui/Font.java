package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.StringDecomposer;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Font {
    private static final Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);
    public final int lineHeight = 9;
    public final Random random = new Random();
    private final Function<ResourceLocation, FontSet> fonts;
    private boolean bidirectional;
    private final StringSplitter splitter;

    public Font(Function<ResourceLocation, FontSet> param0) {
        this.fonts = param0;
        this.splitter = new StringSplitter((param0x, param1) -> this.getFontSet(param1.getFont()).getGlyphInfo(param0x).getAdvance(param1.isBold()));
    }

    private FontSet getFontSet(ResourceLocation param0) {
        return this.fonts.apply(param0);
    }

    public int drawShadow(PoseStack param0, String param1, float param2, float param3, int param4) {
        return this.drawInternal(param1, param2, param3, param4, param0.last().pose(), true, this.bidirectional);
    }

    public int draw(PoseStack param0, String param1, float param2, float param3, int param4) {
        RenderSystem.enableAlphaTest();
        return this.drawInternal(param1, param2, param3, param4, param0.last().pose(), false, this.bidirectional);
    }

    public int drawShadow(PoseStack param0, Component param1, float param2, float param3, int param4) {
        RenderSystem.enableAlphaTest();
        return this.drawInternal(param1, param2, param3, param4, param0.last().pose(), true);
    }

    public int draw(PoseStack param0, Component param1, float param2, float param3, int param4) {
        RenderSystem.enableAlphaTest();
        return this.drawInternal(param1, param2, param3, param4, param0.last().pose(), false);
    }

    public String bidirectionalShaping(String param0) {
        try {
            Bidi var0 = new Bidi(new ArabicShaping(8).shape(param0), 127);
            var0.setReorderingMode(0);
            return var0.writeReordered(2);
        } catch (ArabicShapingException var3) {
            return param0;
        }
    }

    private int drawInternal(String param0, float param1, float param2, int param3, Matrix4f param4, boolean param5, boolean param6) {
        if (param0 == null) {
            return 0;
        } else {
            MultiBufferSource.BufferSource var0 = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            int var1 = this.drawInBatch(param0, param1, param2, param3, param5, param4, var0, false, 0, 15728880, param6);
            var0.endBatch();
            return var1;
        }
    }

    private int drawInternal(Component param0, float param1, float param2, int param3, Matrix4f param4, boolean param5) {
        MultiBufferSource.BufferSource var0 = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        int var1 = this.drawInBatch(param0, param1, param2, param3, param5, param4, var0, false, 0, 15728880);
        var0.endBatch();
        return var1;
    }

    public int drawInBatch(
        String param0,
        float param1,
        float param2,
        int param3,
        boolean param4,
        Matrix4f param5,
        MultiBufferSource param6,
        boolean param7,
        int param8,
        int param9
    ) {
        return this.drawInBatch(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, this.bidirectional);
    }

    public int drawInBatch(
        String param0,
        float param1,
        float param2,
        int param3,
        boolean param4,
        Matrix4f param5,
        MultiBufferSource param6,
        boolean param7,
        int param8,
        int param9,
        boolean param10
    ) {
        return this.drawInternal(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
    }

    public int drawInBatch(
        Component param0,
        float param1,
        float param2,
        int param3,
        boolean param4,
        Matrix4f param5,
        MultiBufferSource param6,
        boolean param7,
        int param8,
        int param9
    ) {
        return this.drawInternal(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    private static int adjustColor(int param0) {
        return (param0 & -67108864) == 0 ? param0 | 0xFF000000 : param0;
    }

    private int drawInternal(
        String param0,
        float param1,
        float param2,
        int param3,
        boolean param4,
        Matrix4f param5,
        MultiBufferSource param6,
        boolean param7,
        int param8,
        int param9,
        boolean param10
    ) {
        if (param10) {
            param0 = this.bidirectionalShaping(param0);
        }

        param3 = adjustColor(param3);
        if (param4) {
            this.renderText(param0, param1, param2, param3, true, param5, param6, param7, param8, param9);
        }

        Matrix4f var0 = param5.copy();
        var0.translate(SHADOW_OFFSET);
        param1 = this.renderText(param0, param1, param2, param3, false, var0, param6, param7, param8, param9);
        return (int)param1 + (param4 ? 1 : 0);
    }

    private int drawInternal(
        Component param0,
        float param1,
        float param2,
        int param3,
        boolean param4,
        Matrix4f param5,
        MultiBufferSource param6,
        boolean param7,
        int param8,
        int param9
    ) {
        param3 = adjustColor(param3);
        if (param4) {
            this.renderText(param0, param1, param2, param3, true, param5, param6, param7, param8, param9);
        }

        Matrix4f var0 = param5.copy();
        var0.translate(SHADOW_OFFSET);
        param1 = this.renderText(param0, param1, param2, param3, false, var0, param6, param7, param8, param9);
        return (int)param1 + (param4 ? 1 : 0);
    }

    private float renderText(
        String param0,
        float param1,
        float param2,
        int param3,
        boolean param4,
        Matrix4f param5,
        MultiBufferSource param6,
        boolean param7,
        int param8,
        int param9
    ) {
        Font.StringRenderOutput var0 = new Font.StringRenderOutput(param6, param1, param2, param3, param4, param5, param7, param9);
        StringDecomposer.iterateFormatted(param0, Style.EMPTY, var0);
        return var0.finish(param8, param1);
    }

    private float renderText(
        Component param0,
        float param1,
        float param2,
        int param3,
        boolean param4,
        Matrix4f param5,
        MultiBufferSource param6,
        boolean param7,
        int param8,
        int param9
    ) {
        Font.StringRenderOutput var0 = new Font.StringRenderOutput(param6, param1, param2, param3, param4, param5, param7, param9);
        StringDecomposer.iterateFormatted(param0, Style.EMPTY, var0);
        return var0.finish(param8, param1);
    }

    private void renderChar(
        BakedGlyph param0,
        boolean param1,
        boolean param2,
        float param3,
        float param4,
        float param5,
        Matrix4f param6,
        VertexConsumer param7,
        float param8,
        float param9,
        float param10,
        float param11,
        int param12
    ) {
        param0.render(param2, param4, param5, param6, param7, param8, param9, param10, param11, param12);
        if (param1) {
            param0.render(param2, param4 + param3, param5, param6, param7, param8, param9, param10, param11, param12);
        }

    }

    public int width(String param0) {
        return Mth.ceil(this.splitter.stringWidth(param0));
    }

    public int width(Component param0) {
        return Mth.ceil(this.splitter.stringWidth(param0));
    }

    public String plainSubstrByWidth(String param0, int param1, boolean param2) {
        return param2 ? this.splitter.plainTailByWidth(param0, param1, Style.EMPTY) : this.splitter.plainHeadByWidth(param0, param1, Style.EMPTY);
    }

    public String plainSubstrByWidth(String param0, int param1) {
        return this.splitter.plainHeadByWidth(param0, param1, Style.EMPTY);
    }

    public MutableComponent substrByWidth(Component param0, int param1) {
        return this.splitter.headByWidth(param0, param1, Style.EMPTY);
    }

    public void drawWordWrap(Component param0, int param1, int param2, int param3, int param4) {
        Matrix4f var0 = Transformation.identity().getMatrix();

        for(Component var1 : this.split(param0, param3)) {
            this.drawInternal(var1, (float)param1, (float)param2, param4, var0, false);
            param2 += 9;
        }

    }

    public int wordWrapHeight(String param0, int param1) {
        return 9 * this.splitter.splitLines(param0, param1, Style.EMPTY).size();
    }

    public void setBidirectional(boolean param0) {
        this.bidirectional = param0;
    }

    public List<Component> split(Component param0, int param1) {
        return this.splitter.splitLines(param0, param1, Style.EMPTY);
    }

    public boolean isBidirectional() {
        return this.bidirectional;
    }

    public StringSplitter getSplitter() {
        return this.splitter;
    }

    @OnlyIn(Dist.CLIENT)
    class StringRenderOutput implements StringDecomposer.Output {
        final MultiBufferSource bufferSource;
        private final boolean dropShadow;
        private final float dimFactor;
        private final float r;
        private final float g;
        private final float b;
        private final float a;
        private final Matrix4f pose;
        private final boolean seeThrough;
        private final int packedLightCoords;
        private float x;
        private float y;
        @Nullable
        private List<BakedGlyph.Effect> effects;

        private void addEffect(BakedGlyph.Effect param0) {
            if (this.effects == null) {
                this.effects = Lists.newArrayList();
            }

            this.effects.add(param0);
        }

        public StringRenderOutput(MultiBufferSource param0, float param1, float param2, int param3, boolean param4, Matrix4f param5, boolean param6, int param7) {
            this.bufferSource = param0;
            this.x = param1;
            this.y = param2;
            this.dropShadow = param4;
            this.dimFactor = param4 ? 0.25F : 1.0F;
            this.r = (float)(param3 >> 16 & 0xFF) / 255.0F * this.dimFactor;
            this.g = (float)(param3 >> 8 & 0xFF) / 255.0F * this.dimFactor;
            this.b = (float)(param3 & 0xFF) / 255.0F * this.dimFactor;
            this.a = (float)(param3 >> 24 & 0xFF) / 255.0F;
            this.pose = param5;
            this.seeThrough = param6;
            this.packedLightCoords = param7;
        }

        @Override
        public boolean onChar(int param0, Style param1, int param2) {
            FontSet var0 = Font.this.getFontSet(param1.getFont());
            GlyphInfo var1 = var0.getGlyphInfo(param2);
            BakedGlyph var2 = param1.isObfuscated() && param2 != 32 ? var0.getRandomGlyph(var1) : var0.getGlyph(param2);
            boolean var3 = param1.isBold();
            float var4 = this.a;
            TextColor var5 = param1.getColor();
            float var7;
            float var8;
            float var9;
            if (var5 != null) {
                int var6 = var5.getValue();
                var7 = (float)(var6 >> 16 & 0xFF) / 255.0F * this.dimFactor;
                var8 = (float)(var6 >> 8 & 0xFF) / 255.0F * this.dimFactor;
                var9 = (float)(var6 & 0xFF) / 255.0F * this.dimFactor;
            } else {
                var7 = this.r;
                var8 = this.g;
                var9 = this.b;
            }

            if (!(var2 instanceof EmptyGlyph)) {
                float var13 = var3 ? var1.getBoldOffset() : 0.0F;
                float var14 = this.dropShadow ? var1.getShadowOffset() : 0.0F;
                VertexConsumer var15 = this.bufferSource.getBuffer(var2.renderType(this.seeThrough));
                Font.this.renderChar(
                    var2, var3, param1.isItalic(), var13, this.x + var14, this.y + var14, this.pose, var15, var7, var8, var9, var4, this.packedLightCoords
                );
            }

            float var16 = var1.getAdvance(var3);
            float var17 = this.dropShadow ? 1.0F : 0.0F;
            if (param1.isStrikethrough()) {
                this.addEffect(
                    new BakedGlyph.Effect(
                        this.x + var17 - 1.0F, this.y + var17 + 4.5F, this.x + var17 + var16, this.y + var17 + 4.5F - 1.0F, 0.01F, var7, var8, var9, var4
                    )
                );
            }

            if (param1.isUnderlined()) {
                this.addEffect(
                    new BakedGlyph.Effect(
                        this.x + var17 - 1.0F, this.y + var17 + 9.0F, this.x + var17 + var16, this.y + var17 + 9.0F - 1.0F, 0.01F, var7, var8, var9, var4
                    )
                );
            }

            this.x += var16;
            return true;
        }

        public float finish(int param0, float param1) {
            if (param0 != 0) {
                float var0 = (float)(param0 >> 24 & 0xFF) / 255.0F;
                float var1 = (float)(param0 >> 16 & 0xFF) / 255.0F;
                float var2 = (float)(param0 >> 8 & 0xFF) / 255.0F;
                float var3 = (float)(param0 & 0xFF) / 255.0F;
                this.addEffect(new BakedGlyph.Effect(param1 - 1.0F, this.y + 9.0F, this.x + 1.0F, this.y - 1.0F, -0.01F, var1, var2, var3, var0));
            }

            if (this.effects != null) {
                BakedGlyph var4 = Font.this.getFontSet(Style.DEFAULT_FONT).whiteGlyph();
                VertexConsumer var5 = this.bufferSource.getBuffer(var4.renderType(this.seeThrough));

                for(BakedGlyph.Effect var6 : this.effects) {
                    var4.renderEffect(var6, this.pose, var5, this.packedLightCoords);
                }
            }

            return this.x;
        }
    }
}
