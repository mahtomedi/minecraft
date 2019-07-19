package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsTasks;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsTermsScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RealmsScreen lastScreen;
    private final RealmsMainScreen mainScreen;
    private final RealmsServer realmsServer;
    private RealmsButton agreeButton;
    private boolean onLink;
    private final String realmsToSUrl = "https://minecraft.net/realms/terms";

    public RealmsTermsScreen(RealmsScreen param0, RealmsMainScreen param1, RealmsServer param2) {
        this.lastScreen = param0;
        this.mainScreen = param1;
        this.realmsServer = param2;
    }

    @Override
    public void init() {
        this.setKeyboardHandlerSendRepeatsToGui(true);
        int var0 = this.width() / 4;
        int var1 = this.width() / 4 - 2;
        int var2 = this.width() / 2 + 4;
        this.buttonsAdd(this.agreeButton = new RealmsButton(1, var0, RealmsConstants.row(12), var1, 20, getLocalizedString("mco.terms.buttons.agree")) {
            @Override
            public void onPress() {
                RealmsTermsScreen.this.agreedToTos();
            }
        });
        this.buttonsAdd(new RealmsButton(2, var2, RealmsConstants.row(12), var1, 20, getLocalizedString("mco.terms.buttons.disagree")) {
            @Override
            public void onPress() {
                Realms.setScreen(RealmsTermsScreen.this.lastScreen);
            }
        });
    }

    @Override
    public void removed() {
        this.setKeyboardHandlerSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            Realms.setScreen(this.lastScreen);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    private void agreedToTos() {
        RealmsClient var0 = RealmsClient.createRealmsClient();

        try {
            var0.agreeToTos();
            RealmsLongRunningMcoTaskScreen var1 = new RealmsLongRunningMcoTaskScreen(
                this.lastScreen, new RealmsTasks.RealmsGetServerDetailsTask(this.mainScreen, this.lastScreen, this.realmsServer, new ReentrantLock())
            );
            var1.start();
            Realms.setScreen(var1);
        } catch (RealmsServiceException var3) {
            LOGGER.error("Couldn't agree to TOS");
        }

    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.onLink) {
            Realms.setClipboard("https://minecraft.net/realms/terms");
            RealmsUtil.browseTo("https://minecraft.net/realms/terms");
            return true;
        } else {
            return super.mouseClicked(param0, param1, param2);
        }
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(getLocalizedString("mco.terms.title"), this.width() / 2, 17, 16777215);
        this.drawString(getLocalizedString("mco.terms.sentence.1"), this.width() / 2 - 120, RealmsConstants.row(5), 16777215);
        int var0 = this.fontWidth(getLocalizedString("mco.terms.sentence.1"));
        int var1 = this.width() / 2 - 121 + var0;
        int var2 = RealmsConstants.row(5);
        int var3 = var1 + this.fontWidth("mco.terms.sentence.2") + 1;
        int var4 = var2 + 1 + this.fontLineHeight();
        if (var1 <= param0 && param0 <= var3 && var2 <= param1 && param1 <= var4) {
            this.onLink = true;
            this.drawString(" " + getLocalizedString("mco.terms.sentence.2"), this.width() / 2 - 120 + var0, RealmsConstants.row(5), 7107012);
        } else {
            this.onLink = false;
            this.drawString(" " + getLocalizedString("mco.terms.sentence.2"), this.width() / 2 - 120 + var0, RealmsConstants.row(5), 3368635);
        }

        super.render(param0, param1, param2);
    }
}
