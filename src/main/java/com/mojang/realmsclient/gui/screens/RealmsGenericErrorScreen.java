package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsGenericErrorScreen extends RealmsScreen {
    private final Screen nextScreen;
    private final Pair<Component, Component> lines;
    private MultiLineLabel line2Split = MultiLineLabel.EMPTY;

    public RealmsGenericErrorScreen(RealmsServiceException param0, Screen param1) {
        super(NarratorChatListener.NO_TITLE);
        this.nextScreen = param1;
        this.lines = errorMessage(param0);
    }

    public RealmsGenericErrorScreen(Component param0, Screen param1) {
        super(NarratorChatListener.NO_TITLE);
        this.nextScreen = param1;
        this.lines = errorMessage(param0);
    }

    public RealmsGenericErrorScreen(Component param0, Component param1, Screen param2) {
        super(NarratorChatListener.NO_TITLE);
        this.nextScreen = param2;
        this.lines = errorMessage(param0, param1);
    }

    private static Pair<Component, Component> errorMessage(RealmsServiceException param0) {
        if (param0.realmsError == null) {
            return Pair.of(new TextComponent("An error occurred (" + param0.httpResultCode + "):"), new TextComponent(param0.rawResponse));
        } else {
            String var0 = "mco.errorMessage." + param0.realmsError.getErrorCode();
            return Pair.of(
                new TextComponent("Realms (" + param0.realmsError + "):"),
                (Component)(I18n.exists(var0) ? new TranslatableComponent(var0) : Component.nullToEmpty(param0.realmsError.getErrorMessage()))
            );
        }
    }

    private static Pair<Component, Component> errorMessage(Component param0) {
        return Pair.of(new TextComponent("An error occurred: "), param0);
    }

    private static Pair<Component, Component> errorMessage(Component param0, Component param1) {
        return Pair.of(param0, param1);
    }

    @Override
    public void init() {
        this.addRenderableWidget(
            new Button(this.width / 2 - 100, this.height - 52, 200, 20, new TextComponent("Ok"), param0 -> this.minecraft.setScreen(this.nextScreen))
        );
        this.line2Split = MultiLineLabel.create(this.font, this.lines.getSecond(), this.width * 3 / 4);
    }

    @Override
    public Component getNarrationMessage() {
        return new TextComponent("").append(this.lines.getFirst()).append(": ").append(this.lines.getSecond());
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.lines.getFirst(), this.width / 2, 80, 16777215);
        this.line2Split.renderCentered(param0, this.width / 2, 100, 9, 16711680);
        super.render(param0, param1, param2, param3);
    }
}
