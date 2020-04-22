package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsInviteScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private EditBox profileName;
    private final RealmsServer serverData;
    private final RealmsConfigureWorldScreen configureScreen;
    private final Screen lastScreen;
    private String errorMsg;
    private boolean showError;

    public RealmsInviteScreen(RealmsConfigureWorldScreen param0, Screen param1, RealmsServer param2) {
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
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.profileName = new EditBox(
            this.minecraft.font, this.width / 2 - 100, row(2), 200, 20, null, new TranslatableComponent("mco.configure.world.invite.profile.name")
        );
        this.addWidget(this.profileName);
        this.setInitialFocus(this.profileName);
        this.addButton(
            new Button(this.width / 2 - 100, row(10), 200, 20, new TranslatableComponent("mco.configure.world.buttons.invite"), param0 -> this.onInvite())
        );
        this.addButton(new Button(this.width / 2 - 100, row(12), 200, 20, CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.lastScreen)));
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void onInvite() {
        RealmsClient var0 = RealmsClient.create();
        if (this.profileName.getValue() != null && !this.profileName.getValue().isEmpty()) {
            try {
                RealmsServer var1 = var0.invite(this.serverData.id, this.profileName.getValue().trim());
                if (var1 != null) {
                    this.serverData.players = var1.players;
                    this.minecraft.setScreen(new RealmsPlayerScreen(this.configureScreen, this.serverData));
                } else {
                    this.showError(I18n.get("mco.configure.world.players.error"));
                }
            } catch (Exception var3) {
                LOGGER.error("Couldn't invite user");
                this.showError(I18n.get("mco.configure.world.players.error"));
            }

        } else {
            this.showError(I18n.get("mco.configure.world.players.error"));
        }
    }

    private void showError(String param0) {
        this.showError = true;
        this.errorMsg = param0;
        NarrationHelper.now(param0);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.font.draw(param0, I18n.get("mco.configure.world.invite.profile.name"), (float)(this.width / 2 - 100), (float)row(1), 10526880);
        if (this.showError) {
            this.drawCenteredString(param0, this.font, this.errorMsg, this.width / 2, row(5), 16711680);
        }

        this.profileName.render(param0, param1, param2, param3);
        super.render(param0, param1, param2, param3);
    }
}
