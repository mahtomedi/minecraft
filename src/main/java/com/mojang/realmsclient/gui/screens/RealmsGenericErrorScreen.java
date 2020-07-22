package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsGenericErrorScreen extends RealmsScreen {
    private final Screen nextScreen;
    private Component line1;
    private Component line2;

    public RealmsGenericErrorScreen(RealmsServiceException param0, Screen param1) {
        this.nextScreen = param1;
        this.errorMessage(param0);
    }

    public RealmsGenericErrorScreen(Component param0, Screen param1) {
        this.nextScreen = param1;
        this.errorMessage(param0);
    }

    public RealmsGenericErrorScreen(Component param0, Component param1, Screen param2) {
        this.nextScreen = param2;
        this.errorMessage(param0, param1);
    }

    private void errorMessage(RealmsServiceException param0) {
        if (param0.errorCode == -1) {
            this.line1 = new TextComponent("An error occurred (" + param0.httpResultCode + "):");
            this.line2 = new TextComponent(param0.httpResponseContent);
        } else {
            this.line1 = new TextComponent("Realms (" + param0.errorCode + "):");
            String var0 = "mco.errorMessage." + param0.errorCode;
            this.line2 = (Component)(I18n.exists(var0) ? new TranslatableComponent(var0) : Component.nullToEmpty(param0.errorMsg));
        }

    }

    private void errorMessage(Component param0) {
        this.line1 = new TextComponent("An error occurred: ");
        this.line2 = param0;
    }

    private void errorMessage(Component param0, Component param1) {
        this.line1 = param0;
        this.line2 = param1;
    }

    @Override
    public void init() {
        NarrationHelper.now(this.line1.getString() + ": " + this.line2.getString());
        this.addButton(
            new Button(this.width / 2 - 100, this.height - 52, 200, 20, new TextComponent("Ok"), param0 -> this.minecraft.setScreen(this.nextScreen))
        );
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.line1, this.width / 2, 80, 16777215);
        drawCenteredString(param0, this.font, this.line2, this.width / 2, 100, 16711680);
        super.render(param0, param1, param2, param3);
    }
}
