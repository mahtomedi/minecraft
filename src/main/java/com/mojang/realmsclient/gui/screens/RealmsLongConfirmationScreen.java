package com.mojang.realmsclient.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsLongConfirmationScreen extends RealmsScreen {
    private final RealmsLongConfirmationScreen.Type type;
    private final String line2;
    private final String line3;
    protected final BooleanConsumer callback;
    protected final String yesButton;
    protected final String noButton;
    private final String okButton;
    private final boolean yesNoQuestion;

    public RealmsLongConfirmationScreen(BooleanConsumer param0, RealmsLongConfirmationScreen.Type param1, String param2, String param3, boolean param4) {
        this.callback = param0;
        this.type = param1;
        this.line2 = param2;
        this.line3 = param3;
        this.yesNoQuestion = param4;
        this.yesButton = I18n.get("gui.yes");
        this.noButton = I18n.get("gui.no");
        this.okButton = I18n.get("mco.gui.ok");
    }

    @Override
    public void init() {
        NarrationHelper.now(this.type.text, this.line2, this.line3);
        if (this.yesNoQuestion) {
            this.addButton(new Button(this.width / 2 - 105, row(8), 100, 20, this.yesButton, param0 -> this.callback.accept(true)));
            this.addButton(new Button(this.width / 2 + 5, row(8), 100, 20, this.noButton, param0 -> this.callback.accept(false)));
        } else {
            this.addButton(new Button(this.width / 2 - 50, row(8), 100, 20, this.okButton, param0 -> this.callback.accept(true)));
        }

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
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.type.text, this.width / 2, row(2), this.type.colorCode);
        this.drawCenteredString(this.font, this.line2, this.width / 2, row(4), 16777215);
        this.drawCenteredString(this.font, this.line3, this.width / 2, row(6), 16777215);
        super.render(param0, param1, param2);
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Type {
        Warning("Warning!", 16711680),
        Info("Info!", 8226750);

        public final int colorCode;
        public final String text;

        private Type(String param0, int param1) {
            this.text = param0;
            this.colorCode = param1;
        }
    }
}
