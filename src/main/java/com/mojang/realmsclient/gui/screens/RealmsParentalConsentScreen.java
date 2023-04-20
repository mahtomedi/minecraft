package com.mojang.realmsclient.gui.screens;

import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsParentalConsentScreen extends RealmsScreen {
    private static final Component MESSAGE = Component.translatable("mco.account.privacyinfo");
    private final Screen nextScreen;
    private MultiLineLabel messageLines = MultiLineLabel.EMPTY;

    public RealmsParentalConsentScreen(Screen param0) {
        super(GameNarrator.NO_TITLE);
        this.nextScreen = param0;
    }

    @Override
    public void init() {
        Component var0 = Component.translatable("mco.account.update");
        Component var1 = CommonComponents.GUI_BACK;
        int var2 = Math.max(this.font.width(var0), this.font.width(var1)) + 30;
        Component var3 = Component.translatable("mco.account.privacy.info");
        int var4 = (int)((double)this.font.width(var3) * 1.2);
        this.addRenderableWidget(
            Button.builder(var3, param0 -> Util.getPlatform().openUri("https://aka.ms/MinecraftGDPR"))
                .bounds(this.width / 2 - var4 / 2, row(11), var4, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(var0, param0 -> Util.getPlatform().openUri("https://aka.ms/UpdateMojangAccount"))
                .bounds(this.width / 2 - (var2 + 5), row(13), var2, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(var1, param0 -> this.minecraft.setScreen(this.nextScreen)).bounds(this.width / 2 + 5, row(13), var2, 20).build()
        );
        this.messageLines = MultiLineLabel.create(this.font, MESSAGE, (int)Math.round((double)this.width * 0.9));
    }

    @Override
    public Component getNarrationMessage() {
        return MESSAGE;
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.messageLines.renderCentered(param0, this.width / 2, 15, 15, 16777215);
        super.render(param0, param1, param2, param3);
    }
}
