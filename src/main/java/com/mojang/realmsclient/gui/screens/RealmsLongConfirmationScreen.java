package com.mojang.realmsclient.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsLongConfirmationScreen extends RealmsScreen {
    private final RealmsLongConfirmationScreen.Type type;
    private final Component line2;
    private final Component line3;
    protected final BooleanConsumer callback;
    private final boolean yesNoQuestion;

    public RealmsLongConfirmationScreen(BooleanConsumer param0, RealmsLongConfirmationScreen.Type param1, Component param2, Component param3, boolean param4) {
        super(GameNarrator.NO_TITLE);
        this.callback = param0;
        this.type = param1;
        this.line2 = param2;
        this.line3 = param3;
        this.yesNoQuestion = param4;
    }

    @Override
    public void init() {
        if (this.yesNoQuestion) {
            this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_YES, param0 -> this.callback.accept(true)).bounds(this.width / 2 - 105, row(8), 100, 20).build()
            );
            this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_NO, param0 -> this.callback.accept(false)).bounds(this.width / 2 + 5, row(8), 100, 20).build()
            );
        } else {
            this.addRenderableWidget(
                Button.builder(Component.translatable("mco.gui.ok"), param0 -> this.callback.accept(true)).bounds(this.width / 2 - 50, row(8), 100, 20).build()
            );
        }

    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinLines(this.type.text, this.line2, this.line3);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.callback.accept(false);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        param0.drawCenteredString(this.font, this.type.text, this.width / 2, row(2), this.type.colorCode);
        param0.drawCenteredString(this.font, this.line2, this.width / 2, row(4), 16777215);
        param0.drawCenteredString(this.font, this.line3, this.width / 2, row(6), 16777215);
        super.render(param0, param1, param2, param3);
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Type {
        Warning("Warning!", 16711680),
        Info("Info!", 8226750);

        public final int colorCode;
        public final Component text;

        private Type(String param0, int param1) {
            this.text = Component.literal(param0);
            this.colorCode = param1;
        }
    }
}
