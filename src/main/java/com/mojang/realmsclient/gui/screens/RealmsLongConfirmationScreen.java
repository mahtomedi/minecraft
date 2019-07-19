package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.RealmsConstants;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsConfirmResultListener;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsLongConfirmationScreen extends RealmsScreen {
    private final RealmsLongConfirmationScreen.Type type;
    private final String line2;
    private final String line3;
    protected final RealmsConfirmResultListener listener;
    protected final String yesButton;
    protected final String noButton;
    private final String okButton;
    protected final int id;
    private final boolean yesNoQuestion;

    public RealmsLongConfirmationScreen(
        RealmsConfirmResultListener param0, RealmsLongConfirmationScreen.Type param1, String param2, String param3, boolean param4, int param5
    ) {
        this.listener = param0;
        this.id = param5;
        this.type = param1;
        this.line2 = param2;
        this.line3 = param3;
        this.yesNoQuestion = param4;
        this.yesButton = getLocalizedString("gui.yes");
        this.noButton = getLocalizedString("gui.no");
        this.okButton = getLocalizedString("mco.gui.ok");
    }

    @Override
    public void init() {
        Realms.narrateNow(this.type.text, this.line2, this.line3);
        if (this.yesNoQuestion) {
            this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 105, RealmsConstants.row(8), 100, 20, this.yesButton) {
                @Override
                public void onPress() {
                    RealmsLongConfirmationScreen.this.listener.confirmResult(true, RealmsLongConfirmationScreen.this.id);
                }
            });
            this.buttonsAdd(new RealmsButton(1, this.width() / 2 + 5, RealmsConstants.row(8), 100, 20, this.noButton) {
                @Override
                public void onPress() {
                    RealmsLongConfirmationScreen.this.listener.confirmResult(false, RealmsLongConfirmationScreen.this.id);
                }
            });
        } else {
            this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 50, RealmsConstants.row(8), 100, 20, this.okButton) {
                @Override
                public void onPress() {
                    RealmsLongConfirmationScreen.this.listener.confirmResult(true, RealmsLongConfirmationScreen.this.id);
                }
            });
        }

    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.listener.confirmResult(false, this.id);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.type.text, this.width() / 2, RealmsConstants.row(2), this.type.colorCode);
        this.drawCenteredString(this.line2, this.width() / 2, RealmsConstants.row(4), 16777215);
        this.drawCenteredString(this.line3, this.width() / 2, RealmsConstants.row(6), 16777215);
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
