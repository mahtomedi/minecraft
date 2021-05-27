package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
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
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
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
    private static final FormattedText SECTION_HEADING = new TextComponent("============").withStyle(ChatFormatting.WHITE);
    private static final String NAME_PREFIX = "           ";
    private static final String OBFUSCATE_TOKEN = "" + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + ChatFormatting.GREEN + ChatFormatting.AQUA;
    private static final int LOGO_WIDTH = 274;
    private static final float SPEEDUP_FACTOR = 5.0F;
    private final boolean poem;
    private final Runnable onFinished;
    private float scroll;
    private List<FormattedCharSequence> lines;
    private IntSet centeredLines;
    private int totalScrollLength;
    private float scrollSpeed;
    private final float unmodifiedScrollSpeed;

    public WinScreen(boolean param0, Runnable param1) {
        super(NarratorChatListener.NO_TITLE);
        this.poem = param0;
        this.onFinished = param1;
        if (!param0) {
            this.unmodifiedScrollSpeed = 0.75F;
        } else {
            this.unmodifiedScrollSpeed = 0.5F;
        }

        this.scrollSpeed = this.unmodifiedScrollSpeed;
    }

    @Override
    public void tick() {
        this.minecraft.getMusicManager().tick();
        this.minecraft.getSoundManager().tick(false);
        float var0 = (float)(this.totalScrollLength + this.height + this.height + 24);
        if (this.scroll > var0) {
            this.respawn();
        }

    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 32) {
            this.scrollSpeed = this.unmodifiedScrollSpeed * 5.0F;
        }

        return super.keyPressed(param0, param1, param2);
    }

    @Override
    public boolean keyReleased(int param0, int param1, int param2) {
        if (param0 == 32) {
            this.scrollSpeed = this.unmodifiedScrollSpeed;
        }

        return super.keyReleased(param0, param1, param2);
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
                if (this.poem) {
                    var0 = this.minecraft.getResourceManager().getResource(new ResourceLocation("texts/end.txt"));
                    InputStream var1 = var0.getInputStream();
                    BufferedReader var2 = new BufferedReader(new InputStreamReader(var1, StandardCharsets.UTF_8));
                    Random var3 = new Random(8124371L);

                    String var4;
                    while((var4 = var2.readLine()) != null) {
                        int var5;
                        String var6;
                        String var7;
                        for(var4 = var4.replaceAll("PLAYERNAME", this.minecraft.getUser().getName());
                            (var5 = var4.indexOf(OBFUSCATE_TOKEN)) != -1;
                            var4 = var6 + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + "XXXXXXXX".substring(0, var3.nextInt(4) + 3) + var7
                        ) {
                            var6 = var4.substring(0, var5);
                            var7 = var4.substring(var5 + OBFUSCATE_TOKEN.length());
                        }

                        this.addLines(var4, false);
                        this.addEmptyLine();
                    }

                    var1.close();

                    for(int var8 = 0; var8 < 8; ++var8) {
                        this.addEmptyLine();
                    }
                }

                var0 = this.minecraft.getResourceManager().getResource(new ResourceLocation("texts/credits.json"));
                JsonArray var9 = GsonHelper.parseArray(new InputStreamReader(var0.getInputStream(), StandardCharsets.UTF_8));

                for(JsonElement var11 : var9.getAsJsonArray()) {
                    JsonObject var12 = var11.getAsJsonObject();
                    String var13 = var12.get("section").getAsString();
                    this.addLines(SECTION_HEADING, true);
                    this.addEmptyLine();
                    this.addLines(new TextComponent(var13).withStyle(ChatFormatting.YELLOW), true);
                    this.addEmptyLine();
                    this.addLines(SECTION_HEADING, true);
                    this.addEmptyLine();
                    this.addEmptyLine();
                    this.addEmptyLine();

                    for(JsonElement var15 : var12.getAsJsonArray("titles")) {
                        JsonObject var16 = var15.getAsJsonObject();
                        String var17 = var16.get("title").getAsString();
                        JsonArray var18 = var16.getAsJsonArray("names");
                        this.addLines(new TextComponent(var17).withStyle(ChatFormatting.GRAY), false);
                        this.addEmptyLine();

                        for(JsonElement var19 : var18) {
                            String var20 = var19.getAsString();
                            this.addLines(new TextComponent("           ").append(new TextComponent(var20)).withStyle(ChatFormatting.WHITE), false);
                            this.addEmptyLine();
                        }

                        this.addEmptyLine();
                        this.addEmptyLine();
                    }
                }

                this.totalScrollLength = this.lines.size() * 12;
            } catch (Exception var201) {
                LOGGER.error("Couldn't load credits", (Throwable)var201);
            } finally {
                IOUtils.closeQuietly((Closeable)var0);
            }

        }
    }

    private void addEmptyLine() {
        this.lines.add(FormattedCharSequence.EMPTY);
    }

    private void addLines(String param0, boolean param1) {
        this.addLines(new TextComponent(param0), param1);
    }

    private void addLines(FormattedText param0, boolean param1) {
        for(FormattedCharSequence var1 : this.minecraft.font.split(param0, 274)) {
            if (param1) {
                this.centeredLines.add(this.lines.size());
            }

            this.lines.add(var1);
        }

    }

    private void renderBg() {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
        int var0 = this.width;
        float var1 = -this.scroll * 0.5F;
        float var2 = (float)this.height - 0.5F * this.scroll;
        float var3 = 0.015625F;
        float var4 = this.scroll / this.unmodifiedScrollSpeed;
        float var5 = var4 * 0.02F;
        float var6 = (float)(this.totalScrollLength + this.height + this.height + 24) / this.unmodifiedScrollSpeed;
        float var7 = (var6 - 20.0F - var4) * 0.005F;
        if (var7 < var5) {
            var5 = var7;
        }

        if (var5 > 1.0F) {
            var5 = 1.0F;
        }

        var5 *= var5;
        var5 = var5 * 96.0F / 255.0F;
        Tesselator var8 = Tesselator.getInstance();
        BufferBuilder var9 = var8.getBuilder();
        var9.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        var9.vertex(0.0, (double)this.height, (double)this.getBlitOffset()).uv(0.0F, var1 * 0.015625F).color(var5, var5, var5, 1.0F).endVertex();
        var9.vertex((double)var0, (double)this.height, (double)this.getBlitOffset())
            .uv((float)var0 * 0.015625F, var1 * 0.015625F)
            .color(var5, var5, var5, 1.0F)
            .endVertex();
        var9.vertex((double)var0, 0.0, (double)this.getBlitOffset()).uv((float)var0 * 0.015625F, var2 * 0.015625F).color(var5, var5, var5, 1.0F).endVertex();
        var9.vertex(0.0, 0.0, (double)this.getBlitOffset()).uv(0.0F, var2 * 0.015625F).color(var5, var5, var5, 1.0F).endVertex();
        var8.end();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.scroll += param3 * this.scrollSpeed;
        this.renderBg();
        int var0 = this.width / 2 - 137;
        int var1 = this.height + 50;
        float var2 = -this.scroll;
        param0.pushPose();
        param0.translate(0.0, (double)var2, 0.0);
        RenderSystem.setShaderTexture(0, LOGO_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        this.blitOutlineBlack(var0, var1, (param1x, param2x) -> {
            this.blit(param0, param1x + 0, param2x, 0, 0, 155, 44);
            this.blit(param0, param1x + 155, param2x, 0, 45, 155, 44);
        });
        RenderSystem.disableBlend();
        RenderSystem.setShaderTexture(0, EDITION_LOCATION);
        blit(param0, var0 + 88, var1 + 37, 0.0F, 0.0F, 98, 14, 128, 16);
        int var3 = var1 + 100;

        for(int var4 = 0; var4 < this.lines.size(); ++var4) {
            if (var4 == this.lines.size() - 1) {
                float var5 = (float)var3 + var2 - (float)(this.height / 2 - 6);
                if (var5 < 0.0F) {
                    param0.translate(0.0, (double)(-var5), 0.0);
                }
            }

            if ((float)var3 + var2 + 12.0F + 8.0F > 0.0F && (float)var3 + var2 < (float)this.height) {
                FormattedCharSequence var6 = this.lines.get(var4);
                if (this.centeredLines.contains(var4)) {
                    this.font.drawShadow(param0, var6, (float)(var0 + (274 - this.font.width(var6)) / 2), (float)var3, 16777215);
                } else {
                    this.font.drawShadow(param0, var6, (float)var0, (float)var3, 16777215);
                }
            }

            var3 += 12;
        }

        param0.popPose();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, VIGNETTE_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
        int var7 = this.width;
        int var8 = this.height;
        Tesselator var9 = Tesselator.getInstance();
        BufferBuilder var10 = var9.getBuilder();
        var10.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        var10.vertex(0.0, (double)var8, (double)this.getBlitOffset()).uv(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        var10.vertex((double)var7, (double)var8, (double)this.getBlitOffset()).uv(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        var10.vertex((double)var7, 0.0, (double)this.getBlitOffset()).uv(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        var10.vertex(0.0, 0.0, (double)this.getBlitOffset()).uv(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        var9.end();
        RenderSystem.disableBlend();
        super.render(param0, param1, param2, param3);
    }
}
