package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsParentalConsentScreen extends RealmsScreen {
    private static final Component MESSAGE = new TranslatableComponent("mco.account.privacyinfo");
    private final Screen nextScreen;
    private MultiLineLabel messageLines = MultiLineLabel.EMPTY;

    public RealmsParentalConsentScreen(Screen param0) {
        super(NarratorChatListener.NO_TITLE);
        this.nextScreen = param0;
    }

    @Override
    public void init() {
        Component var0 = new TranslatableComponent("mco.account.update");
        Component var1 = CommonComponents.GUI_BACK;
        int var2 = Math.max(this.font.width(var0), this.font.width(var1)) + 30;
        Component var3 = new TranslatableComponent("mco.account.privacy.info");
        int var4 = (int)((double)this.font.width(var3) * 1.2);
        this.addRenderableWidget(
            new Button(this.width / 2 - var4 / 2, row(11), var4, 20, var3, param0 -> Util.getPlatform().openUri("https://aka.ms/MinecraftGDPR"))
        );
        this.addRenderableWidget(
            new Button(this.width / 2 - (var2 + 5), row(13), var2, 20, var0, param0 -> Util.getPlatform().openUri("https://aka.ms/UpdateMojangAccount"))
        );
        this.addRenderableWidget(new Button(this.width / 2 + 5, row(13), var2, 20, var1, param0 -> this.minecraft.setScreen(this.nextScreen)));
        this.messageLines = MultiLineLabel.create(this.font, MESSAGE, (int)Math.round((double)this.width * 0.9));
    }

    @Override
    public Component getNarrationMessage() {
        return MESSAGE;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.messageLines.renderCentered(param0, this.width / 2, 15, 15, 16777215);
        super.render(param0, param1, param2, param3);
    }
}
