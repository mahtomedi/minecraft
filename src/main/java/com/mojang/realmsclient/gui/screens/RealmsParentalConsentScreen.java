package com.mojang.realmsclient.gui.screens;

import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsParentalConsentScreen extends RealmsScreen {
    private final Screen nextScreen;

    public RealmsParentalConsentScreen(Screen param0) {
        this.nextScreen = param0;
    }

    @Override
    public void init() {
        NarrationHelper.now(I18n.get("mco.account.privacyinfo"));
        String var0 = I18n.get("mco.account.update");
        String var1 = I18n.get("gui.back");
        int var2 = Math.max(this.font.width(var0), this.font.width(var1)) + 30;
        String var3 = I18n.get("mco.account.privacy.info");
        int var4 = (int)((double)this.font.width(var3) * 1.2);
        this.addButton(
            new Button(this.width / 2 - var4 / 2, row(11), var4, 20, var3, param0 -> Util.getPlatform().openUri("https://minecraft.net/privacy/gdpr/"))
        );
        this.addButton(
            new Button(this.width / 2 - (var2 + 5), row(13), var2, 20, var0, param0 -> Util.getPlatform().openUri("https://minecraft.net/update-account"))
        );
        this.addButton(new Button(this.width / 2 + 5, row(13), var2, 20, var1, param0 -> this.minecraft.setScreen(this.nextScreen)));
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        List<String> var0 = this.minecraft.font.split(I18n.get("mco.account.privacyinfo"), (int)Math.round((double)this.width * 0.9));
        int var1 = 15;

        for(String var2 : var0) {
            this.drawCenteredString(this.font, var2, this.width / 2, var1, 16777215);
            var1 += 15;
        }

        super.render(param0, param1, param2);
    }
}
