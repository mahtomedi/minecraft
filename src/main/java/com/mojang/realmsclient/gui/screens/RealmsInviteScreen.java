package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.gui.RealmsConstants;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsInviteScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private RealmsEditBox profileName;
    private final RealmsServer serverData;
    private final RealmsConfigureWorldScreen configureScreen;
    private final RealmsScreen lastScreen;
    private final int BUTTON_INVITE_ID = 0;
    private final int BUTTON_CANCEL_ID = 1;
    private RealmsButton inviteButton;
    private final int PROFILENAME_EDIT_BOX = 2;
    private String errorMsg;
    private boolean showError;

    public RealmsInviteScreen(RealmsConfigureWorldScreen param0, RealmsScreen param1, RealmsServer param2) {
        this.configureScreen = param0;
        this.lastScreen = param1;
        this.serverData = param2;
    }

    @Override
    public void tick() {
        this.profileName.tick();
    }

    @Override
    public void init() {
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.buttonsAdd(
            this.inviteButton = new RealmsButton(0, this.width() / 2 - 100, RealmsConstants.row(10), getLocalizedString("mco.configure.world.buttons.invite")) {
                @Override
                public void onPress() {
                    RealmsInviteScreen.this.onInvite();
                }
            }
        );
        this.buttonsAdd(new RealmsButton(1, this.width() / 2 - 100, RealmsConstants.row(12), getLocalizedString("gui.cancel")) {
            @Override
            public void onPress() {
                Realms.setScreen(RealmsInviteScreen.this.lastScreen);
            }
        });
        this.profileName = this.newEditBox(
            2, this.width() / 2 - 100, RealmsConstants.row(2), 200, 20, getLocalizedString("mco.configure.world.invite.profile.name")
        );
        this.focusOn(this.profileName);
        this.addWidget(this.profileName);
    }

    @Override
    public void removed() {
        this.setKeyboardHandlerSendRepeatsToGui(false);
    }

    private void onInvite() {
        RealmsClient var0 = RealmsClient.createRealmsClient();
        if (this.profileName.getValue() != null && !this.profileName.getValue().isEmpty()) {
            try {
                RealmsServer var1 = var0.invite(this.serverData.id, this.profileName.getValue().trim());
                if (var1 != null) {
                    this.serverData.players = var1.players;
                    Realms.setScreen(new RealmsPlayerScreen(this.configureScreen, this.serverData));
                } else {
                    this.showError(getLocalizedString("mco.configure.world.players.error"));
                }
            } catch (Exception var3) {
                LOGGER.error("Couldn't invite user");
                this.showError(getLocalizedString("mco.configure.world.players.error"));
            }

        } else {
            this.showError(getLocalizedString("mco.configure.world.players.error"));
        }
    }

    private void showError(String param0) {
        this.showError = true;
        this.errorMsg = param0;
        Realms.narrateNow(param0);
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

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawString(getLocalizedString("mco.configure.world.invite.profile.name"), this.width() / 2 - 100, RealmsConstants.row(1), 10526880);
        if (this.showError) {
            this.drawCenteredString(this.errorMsg, this.width() / 2, RealmsConstants.row(5), 16711680);
        }

        this.profileName.render(param0, param1, param2);
        super.render(param0, param1, param2);
    }
}
