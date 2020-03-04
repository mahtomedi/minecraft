package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsGenericErrorScreen extends RealmsScreen {
    private final Screen nextScreen;
    private String line1;
    private String line2;

    public RealmsGenericErrorScreen(RealmsServiceException param0, Screen param1) {
        this.nextScreen = param1;
        this.errorMessage(param0);
    }

    public RealmsGenericErrorScreen(String param0, Screen param1) {
        this.nextScreen = param1;
        this.errorMessage(param0);
    }

    public RealmsGenericErrorScreen(String param0, String param1, Screen param2) {
        this.nextScreen = param2;
        this.errorMessage(param0, param1);
    }

    private void errorMessage(RealmsServiceException param0) {
        if (param0.errorCode == -1) {
            this.line1 = "An error occurred (" + param0.httpResultCode + "):";
            this.line2 = param0.httpResponseContent;
        } else {
            this.line1 = "Realms (" + param0.errorCode + "):";
            String var0 = "mco.errorMessage." + param0.errorCode;
            String var1 = I18n.get(var0);
            this.line2 = var1.equals(var0) ? param0.errorMsg : var1;
        }

    }

    private void errorMessage(String param0) {
        this.line1 = "An error occurred: ";
        this.line2 = param0;
    }

    private void errorMessage(String param0, String param1) {
        this.line1 = param0;
        this.line2 = param1;
    }

    @Override
    public void init() {
        NarrationHelper.now(this.line1 + ": " + this.line2);
        this.addButton(new Button(this.width / 2 - 100, this.height - 52, 200, 20, "Ok", param0 -> this.minecraft.setScreen(this.nextScreen)));
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.line1, this.width / 2, 80, 16777215);
        this.drawCenteredString(this.font, this.line2, this.width / 2, 100, 16711680);
        super.render(param0, param1, param2);
    }
}
