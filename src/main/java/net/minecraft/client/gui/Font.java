package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Font implements AutoCloseable {
    public final int lineHeight = 9;
    public final Random random = new Random();
    private final TextureManager textureManager;
    private final FontSet fonts;
    private boolean bidirectional;

    public Font(TextureManager param0, FontSet param1) {
        this.textureManager = param0;
        this.fonts = param1;
    }

    public void reload(List<GlyphProvider> param0) {
        this.fonts.reload(param0);
    }

    @Override
    public void close() {
        this.fonts.close();
    }

    public int drawShadow(String param0, float param1, float param2, int param3) {
        RenderSystem.enableAlphaTest();
        return this.drawInternal(param0, param1, param2, param3, Transformation.identity().getMatrix(), true);
    }

    public int draw(String param0, float param1, float param2, int param3) {
        RenderSystem.enableAlphaTest();
        return this.drawInternal(param0, param1, param2, param3, Transformation.identity().getMatrix(), false);
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

    private int drawInternal(String param0, float param1, float param2, int param3, Matrix4f param4, boolean param5) {
        if (param0 == null) {
            return 0;
        } else {
            MultiBufferSource.BufferSource var0 = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            int var1 = this.drawInBatch(param0, param1, param2, param3, param5, param4, var0, false, 0, 15728880);
            var0.endBatch();
            return var1;
        }
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
        return this.drawInternal(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
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
        int param9
    ) {
        if (this.bidirectional) {
            param0 = this.bidirectionalShaping(param0);
        }

        if ((param3 & -67108864) == 0) {
            param3 |= -16777216;
        }

        if (param4) {
            this.renderText(param0, param1, param2, param3, true, param5, param6, param7, param8, param9);
        }

        param1 = this.renderText(param0, param1, param2, param3, false, param5, param6, param7, param8, param9);
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
        float var0 = param4 ? 0.25F : 1.0F;
        float var1 = (float)(param3 >> 16 & 0xFF) / 255.0F * var0;
        float var2 = (float)(param3 >> 8 & 0xFF) / 255.0F * var0;
        float var3 = (float)(param3 & 0xFF) / 255.0F * var0;
        float var4 = param1;
        float var5 = var1;
        float var6 = var2;
        float var7 = var3;
        float var8 = (float)(param3 >> 24 & 0xFF) / 255.0F;
        boolean var9 = false;
        boolean var10 = false;
        boolean var11 = false;
        boolean var12 = false;
        boolean var13 = false;
        List<BakedGlyph.Effect> var14 = Lists.newArrayList();

        for(int var15 = 0; var15 < param0.length(); ++var15) {
            char var16 = param0.charAt(var15);
            if (var16 == 167 && var15 + 1 < param0.length()) {
                ChatFormatting var17 = ChatFormatting.getByCode(param0.charAt(var15 + 1));
                if (var17 != null) {
                    if (var17.shouldReset()) {
                        var9 = false;
                        var10 = false;
                        var13 = false;
                        var12 = false;
                        var11 = false;
                        var5 = var1;
                        var6 = var2;
                        var7 = var3;
                    }

                    if (var17.getColor() != null) {
                        int var18 = var17.getColor();
                        var5 = (float)(var18 >> 16 & 0xFF) / 255.0F * var0;
                        var6 = (float)(var18 >> 8 & 0xFF) / 255.0F * var0;
                        var7 = (float)(var18 & 0xFF) / 255.0F * var0;
                    } else if (var17 == ChatFormatting.OBFUSCATED) {
                        var9 = true;
                    } else if (var17 == ChatFormatting.BOLD) {
                        var10 = true;
                    } else if (var17 == ChatFormatting.STRIKETHROUGH) {
                        var13 = true;
                    } else if (var17 == ChatFormatting.UNDERLINE) {
                        var12 = true;
                    } else if (var17 == ChatFormatting.ITALIC) {
                        var11 = true;
                    }
                }

                ++var15;
            } else {
                GlyphInfo var19 = this.fonts.getGlyphInfo(var16);
                BakedGlyph var20 = var9 && var16 != ' ' ? this.fonts.getRandomGlyph(var19) : this.fonts.getGlyph(var16);
                ResourceLocation var21 = var20.getTexture();
                if (var21 != null) {
                    float var22 = var10 ? var19.getBoldOffset() : 0.0F;
                    float var23 = param4 ? var19.getShadowOffset() : 0.0F;
                    VertexConsumer var24 = param6.getBuffer(param7 ? RenderType.TEXT_SEE_THROUGH(var21) : RenderType.TEXT(var21));
                    this.renderChar(var20, var10, var11, var22, var4 + var23, param2 + var23, param5, var24, var5, var6, var7, var8, param9);
                }

                float var25 = var19.getAdvance(var10);
                float var26 = param4 ? 1.0F : 0.0F;
                if (var13) {
                    var14.add(
                        new BakedGlyph.Effect(
                            var4 + var26 - 1.0F, param2 + var26 + 4.5F, var4 + var26 + var25, param2 + var26 + 4.5F - 1.0F, 0.001F, var5, var6, var7, var8
                        )
                    );
                }

                if (var12) {
                    var14.add(
                        new BakedGlyph.Effect(
                            var4 + var26 - 1.0F, param2 + var26 + 9.0F, var4 + var26 + var25, param2 + var26 + 9.0F - 1.0F, 0.001F, var5, var6, var7, var8
                        )
                    );
                }

                var4 += var25;
            }
        }

        if (param8 != 0) {
            float var27 = (float)(param8 >> 24 & 0xFF) / 255.0F;
            float var28 = (float)(param8 >> 16 & 0xFF) / 255.0F;
            float var29 = (float)(param8 >> 8 & 0xFF) / 255.0F;
            float var30 = (float)(param8 & 0xFF) / 255.0F;
            var14.add(new BakedGlyph.Effect(param1 - 1.0F, param2 + 9.0F, var4 + 1.0F, param2 - 1.0F, -0.001F, var28, var29, var30, var27));
        }

        if (!var14.isEmpty()) {
            BakedGlyph var31 = this.fonts.whiteGlyph();
            ResourceLocation var32 = var31.getTexture();
            if (var32 != null) {
                VertexConsumer var33 = param6.getBuffer(param7 ? RenderType.TEXT_SEE_THROUGH(var32) : RenderType.TEXT(var32));

                for(BakedGlyph.Effect var34 : var14) {
                    var31.renderEffect(var34, param5, var33, param9);
                }
            }
        }

        return var4;
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
        if (param0 == null) {
            return 0;
        } else {
            float var0 = 0.0F;
            boolean var1 = false;

            for(int var2 = 0; var2 < param0.length(); ++var2) {
                char var3 = param0.charAt(var2);
                if (var3 == 167 && var2 < param0.length() - 1) {
                    ChatFormatting var4 = ChatFormatting.getByCode(param0.charAt(++var2));
                    if (var4 == ChatFormatting.BOLD) {
                        var1 = true;
                    } else if (var4 != null && var4.shouldReset()) {
                        var1 = false;
                    }
                } else {
                    var0 += this.fonts.getGlyphInfo(var3).getAdvance(var1);
                }
            }

            return Mth.ceil(var0);
        }
    }

    public float charWidth(char param0) {
        return param0 == 167 ? 0.0F : this.fonts.getGlyphInfo(param0).getAdvance(false);
    }

    public String substrByWidth(String param0, int param1) {
        return this.substrByWidth(param0, param1, false);
    }

    public String substrByWidth(String param0, int param1, boolean param2) {
        StringBuilder var0 = new StringBuilder();
        float var1 = 0.0F;
        int var2 = param2 ? param0.length() - 1 : 0;
        int var3 = param2 ? -1 : 1;
        boolean var4 = false;
        boolean var5 = false;

        for(int var6 = var2; var6 >= 0 && var6 < param0.length() && var1 < (float)param1; var6 += var3) {
            char var7 = param0.charAt(var6);
            if (var4) {
                var4 = false;
                ChatFormatting var8 = ChatFormatting.getByCode(var7);
                if (var8 == ChatFormatting.BOLD) {
                    var5 = true;
                } else if (var8 != null && var8.shouldReset()) {
                    var5 = false;
                }
            } else if (var7 == 167) {
                var4 = true;
            } else {
                var1 += this.charWidth(var7);
                if (var5) {
                    ++var1;
                }
            }

            if (var1 > (float)param1) {
                break;
            }

            if (param2) {
                var0.insert(0, var7);
            } else {
                var0.append(var7);
            }
        }

        return var0.toString();
    }

    private String eraseTrailingNewLines(String param0) {
        while(param0 != null && param0.endsWith("\n")) {
            param0 = param0.substring(0, param0.length() - 1);
        }

        return param0;
    }

    public void drawWordWrap(String param0, int param1, int param2, int param3, int param4) {
        param0 = this.eraseTrailingNewLines(param0);
        this.drawWordWrapInternal(param0, param1, param2, param3, param4);
    }

    private void drawWordWrapInternal(String param0, int param1, int param2, int param3, int param4) {
        List<String> var0 = this.split(param0, param3);
        Matrix4f var1 = Transformation.identity().getMatrix();

        for(String var2 : var0) {
            float var3 = (float)param1;
            if (this.bidirectional) {
                int var4 = this.width(this.bidirectionalShaping(var2));
                var3 += (float)(param3 - var4);
            }

            this.drawInternal(var2, var3, (float)param2, param4, var1, false);
            param2 += 9;
        }

    }

    public int wordWrapHeight(String param0, int param1) {
        return 9 * this.split(param0, param1).size();
    }

    public void setBidirectional(boolean param0) {
        this.bidirectional = param0;
    }

    public List<String> split(String param0, int param1) {
        return Arrays.asList(this.insertLineBreaks(param0, param1).split("\n"));
    }

    public String insertLineBreaks(String param0, int param1) {
        String var0;
        String var2;
        for(var0 = ""; !param0.isEmpty(); var0 = var0 + var2 + "\n") {
            int var1 = this.indexAtWidth(param0, param1);
            if (param0.length() <= var1) {
                return var0 + param0;
            }

            var2 = param0.substring(0, var1);
            char var3 = param0.charAt(var1);
            boolean var4 = var3 == ' ' || var3 == '\n';
            param0 = ChatFormatting.getLastColors(var2) + param0.substring(var1 + (var4 ? 1 : 0));
        }

        return var0;
    }

    public int indexAtWidth(String param0, int param1) {
        int var0 = Math.max(1, param1);
        int var1 = param0.length();
        float var2 = 0.0F;
        int var3 = 0;
        int var4 = -1;
        boolean var5 = false;

        for(boolean var6 = true; var3 < var1; ++var3) {
            char var7 = param0.charAt(var3);
            switch(var7) {
                case '\n':
                    --var3;
                    break;
                case ' ':
                    var4 = var3;
                default:
                    if (var2 != 0.0F) {
                        var6 = false;
                    }

                    var2 += this.charWidth(var7);
                    if (var5) {
                        ++var2;
                    }
                    break;
                case '\u00a7':
                    if (var3 < var1 - 1) {
                        ChatFormatting var8 = ChatFormatting.getByCode(param0.charAt(++var3));
                        if (var8 == ChatFormatting.BOLD) {
                            var5 = true;
                        } else if (var8 != null && var8.shouldReset()) {
                            var5 = false;
                        }
                    }
            }

            if (var7 == '\n') {
                var4 = ++var3;
                break;
            }

            if (var2 > (float)var0) {
                if (var6) {
                    ++var3;
                }
                break;
            }
        }

        return var3 != var1 && var4 != -1 && var4 < var3 ? var4 : var3;
    }

    public int getWordPosition(String param0, int param1, int param2, boolean param3) {
        int var0 = param2;
        boolean var1 = param1 < 0;
        int var2 = Math.abs(param1);

        for(int var3 = 0; var3 < var2; ++var3) {
            if (var1) {
                while(param3 && var0 > 0 && (param0.charAt(var0 - 1) == ' ' || param0.charAt(var0 - 1) == '\n')) {
                    --var0;
                }

                while(var0 > 0 && param0.charAt(var0 - 1) != ' ' && param0.charAt(var0 - 1) != '\n') {
                    --var0;
                }
            } else {
                int var4 = param0.length();
                int var5 = param0.indexOf(32, var0);
                int var6 = param0.indexOf(10, var0);
                if (var5 == -1 && var6 == -1) {
                    var0 = -1;
                } else if (var5 != -1 && var6 != -1) {
                    var0 = Math.min(var5, var6);
                } else if (var5 != -1) {
                    var0 = var5;
                } else {
                    var0 = var6;
                }

                if (var0 == -1) {
                    var0 = var4;
                } else {
                    while(param3 && var0 < var4 && (param0.charAt(var0) == ' ' || param0.charAt(var0) == '\n')) {
                        ++var0;
                    }
                }
            }
        }

        return var0;
    }

    public boolean isBidirectional() {
        return this.bidirectional;
    }
}
