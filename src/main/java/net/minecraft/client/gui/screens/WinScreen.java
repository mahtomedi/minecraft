package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WinScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
    private static final Component SECTION_HEADING = Component.literal("============").withStyle(ChatFormatting.WHITE);
    private static final String NAME_PREFIX = "           ";
    private static final String OBFUSCATE_TOKEN = "" + ChatFormatting.WHITE + ChatFormatting.OBFUSCATED + ChatFormatting.GREEN + ChatFormatting.AQUA;
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
    private int direction;
    private final LogoRenderer logoRenderer = new LogoRenderer(false);

    public WinScreen(boolean param0, Runnable param1) {
        super(GameNarrator.NO_TITLE);
        this.poem = param0;
        this.onFinished = param1;
        if (!param0) {
            this.unmodifiedScrollSpeed = 0.75F;
        } else {
            this.unmodifiedScrollSpeed = 0.5F;
        }

        this.direction = 1;
        this.scrollSpeed = this.unmodifiedScrollSpeed;
    }

    private float calculateScrollSpeed() {
        return this.speedupActive
            ? this.unmodifiedScrollSpeed * (5.0F + (float)this.speedupModifiers.size() * 15.0F) * (float)this.direction
            : this.unmodifiedScrollSpeed * (float)this.direction;
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
        if (param0 == 265) {
            this.direction = -1;
        } else if (param0 == 341 || param0 == 345) {
            this.speedupModifiers.add(param0);
        } else if (param0 == 32) {
            this.speedupActive = true;
        }

        this.scrollSpeed = this.calculateScrollSpeed();
        return super.keyPressed(param0, param1, param2);
    }

    @Override
    public boolean keyReleased(int param0, int param1, int param2) {
        if (param0 == 265) {
            this.direction = 1;
        }

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
        try (Reader var0 = this.minecraft.getResourceManager().openAsReader(new ResourceLocation(param0))) {
            param1.read(var0);
        } catch (Exception var8) {
            LOGGER.error("Couldn't load credits", (Throwable)var8);
        }

    }

    private void addPoemFile(Reader param0) throws IOException {
        BufferedReader var0 = new BufferedReader(param0);
        RandomSource var1 = RandomSource.create(8124371L);

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

    private void addCreditsFile(Reader param0) {
        for(JsonElement var1 : GsonHelper.parseArray(param0)) {
            JsonObject var2 = var1.getAsJsonObject();
            String var3 = var2.get("section").getAsString();
            this.addCreditsLine(SECTION_HEADING, true);
            this.addCreditsLine(Component.literal(var3).withStyle(ChatFormatting.YELLOW), true);
            this.addCreditsLine(SECTION_HEADING, true);
            this.addEmptyLine();
            this.addEmptyLine();

            for(JsonElement var5 : var2.getAsJsonArray("disciplines")) {
                JsonObject var6 = var5.getAsJsonObject();
                String var7 = var6.get("discipline").getAsString();
                if (StringUtils.isNotEmpty(var7)) {
                    this.addCreditsLine(Component.literal(var7).withStyle(ChatFormatting.YELLOW), true);
                    this.addEmptyLine();
                    this.addEmptyLine();
                }

                for(JsonElement var9 : var6.getAsJsonArray("titles")) {
                    JsonObject var10 = var9.getAsJsonObject();
                    String var11 = var10.get("title").getAsString();
                    JsonArray var12 = var10.getAsJsonArray("names");
                    this.addCreditsLine(Component.literal(var11).withStyle(ChatFormatting.GRAY), false);

                    for(JsonElement var13 : var12) {
                        String var14 = var13.getAsString();
                        this.addCreditsLine(Component.literal("           ").append(var14).withStyle(ChatFormatting.WHITE), false);
                    }

                    this.addEmptyLine();
                    this.addEmptyLine();
                }
            }
        }

    }

    private void addEmptyLine() {
        this.lines.add(FormattedCharSequence.EMPTY);
    }

    private void addPoemLines(String param0) {
        this.lines.addAll(this.minecraft.font.split(Component.literal(param0), 256));
    }

    private void addCreditsLine(Component param0, boolean param1) {
        if (param1) {
            this.centeredLines.add(this.lines.size());
        }

        this.lines.add(param0.getVisualOrderText());
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.scroll = Math.max(0.0F, this.scroll + param3 * this.scrollSpeed);
        super.render(param0, param1, param2, param3);
        int var0 = this.width / 2 - 128;
        int var1 = this.height + 50;
        float var2 = -this.scroll;
        param0.pose().pushPose();
        param0.pose().translate(0.0F, var2, 0.0F);
        this.logoRenderer.renderLogo(param0, this.width, 1.0F, var1);
        int var3 = var1 + 100;

        for(int var4 = 0; var4 < this.lines.size(); ++var4) {
            if (var4 == this.lines.size() - 1) {
                float var5 = (float)var3 + var2 - (float)(this.height / 2 - 6);
                if (var5 < 0.0F) {
                    param0.pose().translate(0.0F, -var5, 0.0F);
                }
            }

            if ((float)var3 + var2 + 12.0F + 8.0F > 0.0F && (float)var3 + var2 < (float)this.height) {
                FormattedCharSequence var6 = this.lines.get(var4);
                if (this.centeredLines.contains(var4)) {
                    param0.drawCenteredString(this.font, var6, var0 + 128, var3, 16777215);
                } else {
                    param0.drawString(this.font, var6, var0, var3, 16777215);
                }
            }

            var3 += 12;
        }

        param0.pose().popPose();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
        param0.blit(VIGNETTE_LOCATION, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
        int var0 = this.width;
        float var1 = this.scroll * 0.5F;
        int var2 = 64;
        float var3 = this.scroll / this.unmodifiedScrollSpeed;
        float var4 = var3 * 0.02F;
        float var5 = (float)(this.totalScrollLength + this.height + this.height + 24) / this.unmodifiedScrollSpeed;
        float var6 = (var5 - 20.0F - var3) * 0.005F;
        if (var6 < var4) {
            var4 = var6;
        }

        if (var4 > 1.0F) {
            var4 = 1.0F;
        }

        var4 *= var4;
        var4 = var4 * 96.0F / 255.0F;
        param0.setColor(var4, var4, var4, 1.0F);
        param0.blit(BACKGROUND_LOCATION, 0, 0, 0, 0.0F, var1, var0, this.height, 64, 64);
        param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void removed() {
        this.minecraft.getMusicManager().stopPlaying(Musics.CREDITS);
    }

    @Override
    public Music getBackgroundMusic() {
        return Musics.CREDITS;
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    interface CreditsReader {
        void read(Reader var1) throws IOException;
    }
}
