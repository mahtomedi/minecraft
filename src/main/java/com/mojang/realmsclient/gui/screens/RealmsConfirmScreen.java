package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.RealmsConstants;
import net.minecraft.realms.AbstractRealmsButton;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsConfirmScreen extends RealmsScreen {
    protected RealmsScreen parent;
    protected String title1;
    private final String title2;
    protected String yesButton;
    protected String noButton;
    protected int id;
    private int delayTicker;

    public RealmsConfirmScreen(RealmsScreen param0, String param1, String param2, int param3) {
        this.parent = param0;
        this.title1 = param1;
        this.title2 = param2;
        this.id = param3;
        this.yesButton = getLocalizedString("gui.yes");
        this.noButton = getLocalizedString("gui.no");
    }

    @Override
    public void init() {
        this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 105, RealmsConstants.row(9), 100, 20, this.yesButton) {
            @Override
            public void onPress() {
                RealmsConfirmScreen.this.parent.confirmResult(true, RealmsConfirmScreen.this.id);
            }
        });
        this.buttonsAdd(new RealmsButton(1, this.width() / 2 + 5, RealmsConstants.row(9), 100, 20, this.noButton) {
            @Override
            public void onPress() {
                RealmsConfirmScreen.this.parent.confirmResult(false, RealmsConfirmScreen.this.id);
            }
        });
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.title1, this.width() / 2, RealmsConstants.row(3), 16777215);
        this.drawCenteredString(this.title2, this.width() / 2, RealmsConstants.row(5), 16777215);
        super.render(param0, param1, param2);
    }

    @Override
    public void tick() {
        super.tick();
        if (--this.delayTicker == 0) {
            for(AbstractRealmsButton<?> var0 : this.buttons()) {
                var0.active(true);
            }
        }

    }
}
