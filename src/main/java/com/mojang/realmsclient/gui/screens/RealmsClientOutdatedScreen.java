package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.RealmsConstants;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsClientOutdatedScreen extends RealmsScreen {
    private final RealmsScreen lastScreen;
    private final boolean outdated;

    public RealmsClientOutdatedScreen(RealmsScreen param0, boolean param1) {
        this.lastScreen = param0;
        this.outdated = param1;
    }

    @Override
    public void init() {
        this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 100, RealmsConstants.row(12), getLocalizedString("gui.back")) {
            @Override
            public void onPress() {
                Realms.setScreen(RealmsClientOutdatedScreen.this.lastScreen);
            }
        });
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        String var0 = getLocalizedString(this.outdated ? "mco.client.outdated.title" : "mco.client.incompatible.title");
        this.drawCenteredString(var0, this.width() / 2, RealmsConstants.row(3), 16711680);
        int var1 = this.outdated ? 2 : 3;

        for(int var2 = 0; var2 < var1; ++var2) {
            String var3 = getLocalizedString((this.outdated ? "mco.client.outdated.msg.line" : "mco.client.incompatible.msg.line") + (var2 + 1));
            this.drawCenteredString(var3, this.width() / 2, RealmsConstants.row(5) + var2 * 12, 16777215);
        }

        super.render(param0, param1, param2);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 != 257 && param0 != 335 && param0 != 256) {
            return super.keyPressed(param0, param1, param2);
        } else {
            Realms.setScreen(this.lastScreen);
            return true;
        }
    }
}
