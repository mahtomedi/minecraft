package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WinScreen extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation EDITION_LOCATION = new ResourceLocation("textures/gui/title/edition.png");
    private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
    private static final String OBFUSCATE_TOKEN = "" + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + ChatFormatting.GREEN + ChatFormatting.AQUA;
    private final boolean poem;
    private final Runnable onFinished;
    private float time;
    private List<FormattedCharSequence> lines;
    private IntSet centeredLines;
    private int totalScrollLength;
    private float scrollSpeed = 0.5F;

    public WinScreen(boolean param0, Runnable param1) {
        super(NarratorChatListener.NO_TITLE);
        this.poem = param0;
        this.onFinished = param1;
        if (!param0) {
            this.scrollSpeed = 0.75F;
        }

    }

    @Override
    public void tick() {
        this.minecraft.getMusicManager().tick();
        this.minecraft.getSoundManager().tick(false);
        float var0 = (float)(this.totalScrollLength + this.height + this.height + 24) / this.scrollSpeed;
        if (this.time > var0) {
            this.respawn();
        }

    }

    @Override
    public void onClose() {
        this.respawn();
    }

    private void respawn() {
        this.onFinished.run();
        this.minecraft.setScreen(null);
    }

    @Override
    protected void init() {
        if (this.lines == null) {
            this.lines = Lists.newArrayList();
            this.centeredLines = new IntOpenHashSet();
            Resource var0 = null;

            try {
                int var1 = 274;
                if (this.poem) {
                    var0 = this.minecraft.getResourceManager().getResource(new ResourceLocation("texts/end.txt"));
                    InputStream var2 = var0.getInputStream();
                    BufferedReader var3 = new BufferedReader(new InputStreamReader(var2, StandardCharsets.UTF_8));
                    Random var4 = new Random(8124371L);

                    String var5;
                    while((var5 = var3.readLine()) != null) {
                        int var6;
                        String var7;
                        String var8;
                        for(var5 = var5.replaceAll("PLAYERNAME", this.minecraft.getUser().getName());
                            (var6 = var5.indexOf(OBFUSCATE_TOKEN)) != -1;
                            var5 = var7 + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + "XXXXXXXX".substring(0, var4.nextInt(4) + 3) + var8
                        ) {
                            var7 = var5.substring(0, var6);
                            var8 = var5.substring(var6 + OBFUSCATE_TOKEN.length());
                        }

                        this.lines.addAll(this.minecraft.font.split(new TextComponent(var5), 274));
                        this.lines.add(FormattedCharSequence.EMPTY);
                    }

                    var2.close();

                    for(int var9 = 0; var9 < 8; ++var9) {
                        this.lines.add(FormattedCharSequence.EMPTY);
                    }
                }

                InputStream var10 = this.minecraft.getResourceManager().getResource(new ResourceLocation("texts/credits.txt")).getInputStream();
                BufferedReader var11 = new BufferedReader(new InputStreamReader(var10, StandardCharsets.UTF_8));

                String var12;
                while((var12 = var11.readLine()) != null) {
                    var12 = var12.replaceAll("PLAYERNAME", this.minecraft.getUser().getName());
                    var12 = var12.replaceAll("\t", "    ");
                    boolean var13;
                    if (var12.startsWith("[C]")) {
                        var12 = var12.substring(3);
                        var13 = true;
                    } else {
                        var13 = false;
                    }

                    for(FormattedCharSequence var16 : this.minecraft.font.split(new TextComponent(var12), 274)) {
                        if (var13) {
                            this.centeredLines.add(this.lines.size());
                        }

                        this.lines.add(var16);
                    }

                    this.lines.add(FormattedCharSequence.EMPTY);
                }

                var10.close();
                this.totalScrollLength = this.lines.size() * 12;
            } catch (Exception var13) {
                LOGGER.error("Couldn't load credits", (Throwable)var13);
            } finally {
                IOUtils.closeQuietly((Closeable)var0);
            }

        }
    }

    private void renderBg(int param0, int param1, float param2) {
        this.minecraft.getTextureManager().bind(GuiComponent.BACKGROUND_LOCATION);
        int var0 = this.width;
        float var1 = -this.time * 0.5F * this.scrollSpeed;
        float var2 = (float)this.height - this.time * 0.5F * this.scrollSpeed;
        float var3 = 0.015625F;
        float var4 = this.time * 0.02F;
        float var5 = (float)(this.totalScrollLength + this.height + this.height + 24) / this.scrollSpeed;
        float var6 = (var5 - 20.0F - this.time) * 0.005F;
        if (var6 < var4) {
            var4 = var6;
        }

        if (var4 > 1.0F) {
            var4 = 1.0F;
        }

        var4 *= var4;
        var4 = var4 * 96.0F / 255.0F;
        Tesselator var7 = Tesselator.getInstance();
        BufferBuilder var8 = var7.getBuilder();
        var8.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        var8.vertex(0.0, (double)this.height, (double)this.getBlitOffset()).uv(0.0F, var1 * 0.015625F).color(var4, var4, var4, 1.0F).endVertex();
        var8.vertex((double)var0, (double)this.height, (double)this.getBlitOffset())
            .uv((float)var0 * 0.015625F, var1 * 0.015625F)
            .color(var4, var4, var4, 1.0F)
            .endVertex();
        var8.vertex((double)var0, 0.0, (double)this.getBlitOffset()).uv((float)var0 * 0.015625F, var2 * 0.015625F).color(var4, var4, var4, 1.0F).endVertex();
        var8.vertex(0.0, 0.0, (double)this.getBlitOffset()).uv(0.0F, var2 * 0.015625F).color(var4, var4, var4, 1.0F).endVertex();
        var7.end();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBg(param1, param2, param3);
        int var0 = 274;
        int var1 = this.width / 2 - 137;
        int var2 = this.height + 50;
        this.time += param3;
        float var3 = -this.time * this.scrollSpeed;
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0.0F, var3, 0.0F);
        this.minecraft.getTextureManager().bind(LOGO_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
        this.blitOutlineBlack(var1, var2, (param1x, param2x) -> {
            this.blit(param0, param1x + 0, param2x, 0, 0, 155, 44);
            this.blit(param0, param1x + 155, param2x, 0, 45, 155, 44);
        });
        RenderSystem.disableBlend();
        this.minecraft.getTextureManager().bind(EDITION_LOCATION);
        blit(param0, var1 + 88, var2 + 37, 0.0F, 0.0F, 98, 14, 128, 16);
        RenderSystem.disableAlphaTest();
        int var4 = var2 + 100;

        for(int var5 = 0; var5 < this.lines.size(); ++var5) {
            if (var5 == this.lines.size() - 1) {
                float var6 = (float)var4 + var3 - (float)(this.height / 2 - 6);
                if (var6 < 0.0F) {
                    RenderSystem.translatef(0.0F, -var6, 0.0F);
                }
            }

            if ((float)var4 + var3 + 12.0F + 8.0F > 0.0F && (float)var4 + var3 < (float)this.height) {
                FormattedCharSequence var7 = this.lines.get(var5);
                if (this.centeredLines.contains(var5)) {
                    this.font.drawShadow(param0, var7, (float)(var1 + (274 - this.font.width(var7)) / 2), (float)var4, 16777215);
                } else {
                    this.font.random.setSeed((long)((float)((long)var5 * 4238972211L) + this.time / 4.0F));
                    this.font.drawShadow(param0, var7, (float)var1, (float)var4, 16777215);
                }
            }

            var4 += 12;
        }

        RenderSystem.popMatrix();
        this.minecraft.getTextureManager().bind(VIGNETTE_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
        int var8 = this.width;
        int var9 = this.height;
        Tesselator var10 = Tesselator.getInstance();
        BufferBuilder var11 = var10.getBuilder();
        var11.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        var11.vertex(0.0, (double)var9, (double)this.getBlitOffset()).uv(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        var11.vertex((double)var8, (double)var9, (double)this.getBlitOffset()).uv(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        var11.vertex((double)var8, 0.0, (double)this.getBlitOffset()).uv(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        var11.vertex(0.0, 0.0, (double)this.getBlitOffset()).uv(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        var10.end();
        RenderSystem.disableBlend();
        super.render(param0, param1, param2, param3);
    }
}
