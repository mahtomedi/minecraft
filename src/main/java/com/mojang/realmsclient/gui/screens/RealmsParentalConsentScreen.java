package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.List;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsParentalConsentScreen extends RealmsScreen {
    private final RealmsScreen nextScreen;

    public RealmsParentalConsentScreen(RealmsScreen param0) {
        this.nextScreen = param0;
    }

    @Override
    public void init() {
        Realms.narrateNow(getLocalizedString("mco.account.privacyinfo"));
        String var0 = getLocalizedString("mco.account.update");
        String var1 = getLocalizedString("gui.back");
        int var2 = Math.max(this.fontWidth(var0), this.fontWidth(var1)) + 30;
        String var3 = getLocalizedString("mco.account.privacy.info");
        int var4 = (int)((double)this.fontWidth(var3) * 1.2);
        this.buttonsAdd(new RealmsButton(1, this.width() / 2 - var4 / 2, RealmsConstants.row(11), var4, 20, var3) {
            @Override
            public void onPress() {
                RealmsUtil.browseTo("https://minecraft.net/privacy/gdpr/");
            }
        });
        this.buttonsAdd(new RealmsButton(1, this.width() / 2 - (var2 + 5), RealmsConstants.row(13), var2, 20, var0) {
            @Override
            public void onPress() {
                RealmsUtil.browseTo("https://minecraft.net/update-account");
            }
        });
        this.buttonsAdd(new RealmsButton(0, this.width() / 2 + 5, RealmsConstants.row(13), var2, 20, var1) {
            @Override
            public void onPress() {
                Realms.setScreen(RealmsParentalConsentScreen.this.nextScreen);
            }
        });
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        return super.mouseClicked(param0, param1, param2);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        List<String> var0 = this.getLocalizedStringWithLineWidth("mco.account.privacyinfo", (int)Math.round((double)this.width() * 0.9));
        int var1 = 15;

        for(String var2 : var0) {
            this.drawCenteredString(var2, this.width() / 2, var1, 16777215);
            var1 += 15;
        }

        super.render(param0, param1, param2);
    }
}
