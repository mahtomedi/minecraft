package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsError;
import com.mojang.realmsclient.exception.RealmsServiceException;
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
public class RealmsGenericErrorScreen extends RealmsScreen {
    private final Screen nextScreen;
    private final RealmsGenericErrorScreen.ErrorMessage lines;
    private MultiLineLabel line2Split = MultiLineLabel.EMPTY;

    public RealmsGenericErrorScreen(RealmsServiceException param0, Screen param1) {
        super(GameNarrator.NO_TITLE);
        this.nextScreen = param1;
        this.lines = errorMessage(param0);
    }

    public RealmsGenericErrorScreen(Component param0, Screen param1) {
        super(GameNarrator.NO_TITLE);
        this.nextScreen = param1;
        this.lines = errorMessage(param0);
    }

    public RealmsGenericErrorScreen(Component param0, Component param1, Screen param2) {
        super(GameNarrator.NO_TITLE);
        this.nextScreen = param2;
        this.lines = errorMessage(param0, param1);
    }

    private static RealmsGenericErrorScreen.ErrorMessage errorMessage(RealmsServiceException param0) {
        RealmsError var0 = param0.realmsError;
        return errorMessage(Component.translatable("mco.errorMessage.realmsService.realmsError", var0.errorCode()), var0.errorMessage());
    }

    private static RealmsGenericErrorScreen.ErrorMessage errorMessage(Component param0) {
        return errorMessage(Component.translatable("mco.errorMessage.generic"), param0);
    }

    private static RealmsGenericErrorScreen.ErrorMessage errorMessage(Component param0, Component param1) {
        return new RealmsGenericErrorScreen.ErrorMessage(param0, param1);
    }

    @Override
    public void init() {
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_OK, param0 -> this.minecraft.setScreen(this.nextScreen))
                .bounds(this.width / 2 - 100, this.height - 52, 200, 20)
                .build()
        );
        this.line2Split = MultiLineLabel.create(this.font, this.lines.detail, this.width * 3 / 4);
    }

    @Override
    public Component getNarrationMessage() {
        return Component.empty().append(this.lines.title).append(": ").append(this.lines.detail);
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.lines.title, this.width / 2, 80, -1);
        this.line2Split.renderCentered(param0, this.width / 2, 100, 9, -2142128);
    }

    @OnlyIn(Dist.CLIENT)
    static record ErrorMessage(Component title, Component detail) {
    }
}
