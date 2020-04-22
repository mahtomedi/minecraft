package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.realmsclient.dto.Backup;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ScrolledSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsBackupInfoScreen extends RealmsScreen {
    private final Screen lastScreen;
    private final Backup backup;
    private final List<String> keys = Lists.newArrayList();
    private RealmsBackupInfoScreen.BackupInfoList backupInfoList;

    public RealmsBackupInfoScreen(Screen param0, Backup param1) {
        this.lastScreen = param0;
        this.backup = param1;
        if (param1.changeList != null) {
            for(Entry<String, String> var0 : param1.changeList.entrySet()) {
                this.keys.add(var0.getKey());
            }
        }

    }

    @Override
    public void tick() {
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addButton(
            new Button(
                this.width / 2 - 100, this.height / 4 + 120 + 24, 200, 20, CommonComponents.GUI_BACK, param0 -> this.minecraft.setScreen(this.lastScreen)
            )
        );
        this.backupInfoList = new RealmsBackupInfoScreen.BackupInfoList(this.minecraft);
        this.addWidget(this.backupInfoList);
        this.magicalSpecialHackyFocus(this.backupInfoList);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.drawCenteredString(param0, this.font, "Changes from last backup", this.width / 2, 10, 16777215);
        this.backupInfoList.render(param0, param1, param2, param3);
        super.render(param0, param1, param2, param3);
    }

    private Component checkForSpecificMetadata(String param0, String param1) {
        String var0 = param0.toLowerCase(Locale.ROOT);
        if (var0.contains("game") && var0.contains("mode")) {
            return this.gameModeMetadata(param1);
        } else {
            return (Component)(var0.contains("game") && var0.contains("difficulty") ? this.gameDifficultyMetadata(param1) : new TextComponent(param1));
        }
    }

    private Component gameDifficultyMetadata(String param0) {
        try {
            return RealmsSlotOptionsScreen.DIFFICULTIES[Integer.parseInt(param0)];
        } catch (Exception var3) {
            return new TextComponent("UNKNOWN");
        }
    }

    private Component gameModeMetadata(String param0) {
        try {
            return RealmsSlotOptionsScreen.GAME_MODES[Integer.parseInt(param0)];
        } catch (Exception var3) {
            return new TextComponent("UNKNOWN");
        }
    }

    @OnlyIn(Dist.CLIENT)
    class BackupInfoList extends ScrolledSelectionList {
        public BackupInfoList(Minecraft param0) {
            super(param0, RealmsBackupInfoScreen.this.width, RealmsBackupInfoScreen.this.height, 32, RealmsBackupInfoScreen.this.height - 64, 36);
        }

        @Override
        public int getItemCount() {
            return RealmsBackupInfoScreen.this.backup.changeList.size();
        }

        @Override
        protected void renderItem(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, float param7) {
            String var0 = RealmsBackupInfoScreen.this.keys.get(param1);
            Font var1 = this.minecraft.font;
            this.drawString(param0, var1, var0, this.width / 2 - 40, param3, 10526880);
            String var2 = RealmsBackupInfoScreen.this.backup.changeList.get(var0);
            this.drawString(param0, var1, RealmsBackupInfoScreen.this.checkForSpecificMetadata(var0, var2), this.width / 2 - 40, param3 + 12, 16777215);
        }

        @Override
        public boolean isSelectedItem(int param0) {
            return false;
        }

        @Override
        public void renderBackground() {
        }

        @Override
        public void render(PoseStack param0, int param1, int param2, float param3) {
            if (this.visible) {
                this.renderBackground();
                int var0 = this.getScrollbarPosition();
                int var1 = var0 + 6;
                this.capYPosition();
                RenderSystem.disableFog();
                Tesselator var2 = Tesselator.getInstance();
                BufferBuilder var3 = var2.getBuilder();
                int var4 = this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
                int var5 = this.y0 + 4 - (int)this.yo;
                if (this.renderHeader) {
                    this.renderHeader(var4, var5, var2);
                }

                this.renderList(param0, var4, var5, param1, param2, param3);
                RenderSystem.disableDepthTest();
                this.renderHoleBackground(0, this.y0, 255, 255);
                this.renderHoleBackground(this.y1, this.height, 255, 255);
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ZERO,
                    GlStateManager.DestFactor.ONE
                );
                RenderSystem.disableAlphaTest();
                RenderSystem.shadeModel(7425);
                RenderSystem.disableTexture();
                int var6 = this.getMaxScroll();
                if (var6 > 0) {
                    int var7 = (this.y1 - this.y0) * (this.y1 - this.y0) / this.getMaxPosition();
                    var7 = Mth.clamp(var7, 32, this.y1 - this.y0 - 8);
                    int var8 = (int)this.yo * (this.y1 - this.y0 - var7) / var6 + this.y0;
                    if (var8 < this.y0) {
                        var8 = this.y0;
                    }

                    var3.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
                    var3.vertex((double)var0, (double)this.y1, 0.0).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                    var3.vertex((double)var1, (double)this.y1, 0.0).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                    var3.vertex((double)var1, (double)this.y0, 0.0).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                    var3.vertex((double)var0, (double)this.y0, 0.0).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                    var2.end();
                    var3.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
                    var3.vertex((double)var0, (double)(var8 + var7), 0.0).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                    var3.vertex((double)var1, (double)(var8 + var7), 0.0).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                    var3.vertex((double)var1, (double)var8, 0.0).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                    var3.vertex((double)var0, (double)var8, 0.0).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                    var2.end();
                    var3.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
                    var3.vertex((double)var0, (double)(var8 + var7 - 1), 0.0).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                    var3.vertex((double)(var1 - 1), (double)(var8 + var7 - 1), 0.0).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                    var3.vertex((double)(var1 - 1), (double)var8, 0.0).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                    var3.vertex((double)var0, (double)var8, 0.0).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                    var2.end();
                }

                this.renderDecorations(param1, param2);
                RenderSystem.enableTexture();
                RenderSystem.shadeModel(7424);
                RenderSystem.enableAlphaTest();
                RenderSystem.disableBlend();
            }
        }
    }
}
