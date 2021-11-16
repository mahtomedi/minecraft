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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
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
    private static final Component SECTION_HEADING = new TextComponent("============").withStyle(ChatFormatting.WHITE);
    private static final String NAME_PREFIX = "           ";
    private static final String OBFUSCATE_TOKEN = "" + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + ChatFormatting.GREEN + ChatFormatting.AQUA;
    private static final int LOGO_WIDTH = 274;
    private static final float SPEEDUP_FACTOR = 5.0F;
    private static final float SPEEDUP_FACTOR_FAST = 15.0F;
    private final boolean poem;
    private final Runnable onFinished;
    private float scroll;
    private List<FormattedCharSequence> lines;
    private IntSet centeredLines;
    private int totalScrollLength;
    private boolean speedupActive;
    private final IntSet speedupModifiers = new IntOpenHashSet();
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

    private float calculateScrollSpeed() {
        return this.speedupActive ? this.unmodifiedScrollSpeed * (5.0F + (float)this.speedupModifiers.size() * 15.0F) : this.unmodifiedScrollSpeed;
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
        if (param0 == 341 || param0 == 345) {
            this.speedupModifiers.add(param0);
        } else if (param0 == 32) {
            this.speedupActive = true;
        }

        this.scrollSpeed = this.calculateScrollSpeed();
        return super.keyPressed(param0, param1, param2);
    }

    @Override
    public boolean keyReleased(int param0, int param1, int param2) {
        if (param0 == 32) {
            this.speedupActive = false;
        } else if (param0 == 341 || param0 == 345) {
            this.speedupModifiers.remove(param0);
        }

        this.scrollSpeed = this.calculateScrollSpeed();
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
            if (this.poem) {
                this.wrapCreditsIO("texts/end.txt", this::addPoemFile);
            }

            this.wrapCreditsIO("texts/credits.json", this::addCreditsFile);
            if (this.poem) {
                this.wrapCreditsIO("texts/postcredits.txt", this::addPoemFile);
            }

            this.totalScrollLength = this.lines.size() * 12;
        }
    }

    private void wrapCreditsIO(String param0, WinScreen.CreditsReader param1) {
        Resource var0 = null;

        try {
            var0 = this.minecraft.getResourceManager().getResource(new ResourceLocation(param0));
            InputStreamReader var1 = new InputStreamReader(var0.getInputStream(), StandardCharsets.UTF_8);
            param1.read(var1);
        } catch (Exception var8) {
            LOGGER.error("Couldn't load credits", (Throwable)var8);
        } finally {
            IOUtils.closeQuietly((Closeable)var0);
        }

    }

    private void addPoemFile(InputStreamReader param0) throws IOException {
        BufferedReader var0 = new BufferedReader(param0);
        Random var1 = new Random(8124371L);

        String var2;
        while((var2 = var0.readLine()) != null) {
            int var3;
            String var4;
            String var5;
            for(var2 = var2.replaceAll("PLAYERNAME", this.minecraft.getUser().getName());
                (var3 = var2.indexOf(OBFUSCATE_TOKEN)) != -1;
                var2 = var4 + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + "XXXXXXXX".substring(0, var1.nextInt(4) + 3) + var5
            ) {
                var4 = var2.substring(0, var3);
                var5 = var2.substring(var3 + OBFUSCATE_TOKEN.length());
            }

            this.addPoemLines(var2);
            this.addEmptyLine();
        }

        for(int var6 = 0; var6 < 8; ++var6) {
            this.addEmptyLine();
        }

    }

    private void addCreditsFile(InputStreamReader param0) {
        for(JsonElement var1 : GsonHelper.parseArray(param0)) {
            JsonObject var2 = var1.getAsJsonObject();
            String var3 = var2.get("section").getAsString();
            this.addCreditsLine(SECTION_HEADING, true);
            this.addCreditsLine(new TextComponent(var3).withStyle(ChatFormatting.YELLOW), true);
            this.addCreditsLine(SECTION_HEADING, true);
            this.addEmptyLine();
            this.addEmptyLine();

            for(JsonElement var5 : var2.getAsJsonArray("titles")) {
                JsonObject var6 = var5.getAsJsonObject();
                String var7 = var6.get("title").getAsString();
                JsonArray var8 = var6.getAsJsonArray("names");
                this.addCreditsLine(new TextComponent(var7).withStyle(ChatFormatting.GRAY), false);

                for(JsonElement var9 : var8) {
                    String var10 = var9.getAsString();
                    this.addCreditsLine(new TextComponent("           ").append(var10).withStyle(ChatFormatting.WHITE), false);
                }

                this.addEmptyLine();
                this.addEmptyLine();
            }
        }

    }

    private void addEmptyLine() {
        this.lines.add(FormattedCharSequence.EMPTY);
    }

    private void addPoemLines(String param0) {
        this.lines.addAll(this.minecraft.font.split(new TextComponent(param0), 274));
    }

    private void addCreditsLine(Component param0, boolean param1) {
        if (param1) {
            this.centeredLines.add(this.lines.size());
        }

        this.lines.add(param0.getVisualOrderText());
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

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    interface CreditsReader {
        void read(InputStreamReader var1) throws IOException;
    }
}
