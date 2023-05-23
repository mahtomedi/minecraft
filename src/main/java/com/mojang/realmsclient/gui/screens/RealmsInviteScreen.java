package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsInviteScreen extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component NAME_LABEL = Component.translatable("mco.configure.world.invite.profile.name")
        .withStyle(param0 -> param0.withColor(-6250336));
    private static final Component INVITING_PLAYER_TEXT = Component.translatable("mco.configure.world.players.inviting")
        .withStyle(param0 -> param0.withColor(-6250336));
    private static final Component NO_SUCH_PLAYER_ERROR_TEXT = Component.translatable("mco.configure.world.players.error")
        .withStyle(param0 -> param0.withColor(-65536));
    private EditBox profileName;
    private Button inviteButton;
    private final RealmsServer serverData;
    private final RealmsConfigureWorldScreen configureScreen;
    private final Screen lastScreen;
    @Nullable
    private Component message;

    public RealmsInviteScreen(RealmsConfigureWorldScreen param0, Screen param1, RealmsServer param2) {
        super(GameNarrator.NO_TITLE);
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
        this.profileName = new EditBox(
            this.minecraft.font, this.width / 2 - 100, row(2), 200, 20, null, Component.translatable("mco.configure.world.invite.profile.name")
        );
        this.addWidget(this.profileName);
        this.setInitialFocus(this.profileName);
        this.inviteButton = this.addRenderableWidget(
            Button.builder(Component.translatable("mco.configure.world.buttons.invite"), param0 -> this.onInvite())
                .bounds(this.width / 2 - 100, row(10), 200, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 - 100, row(12), 200, 20)
                .build()
        );
    }

    private void onInvite() {
        if (Util.isBlank(this.profileName.getValue())) {
            this.showMessage(NO_SUCH_PLAYER_ERROR_TEXT);
        } else {
            long var0 = this.serverData.id;
            String var1 = this.profileName.getValue().trim();
            this.inviteButton.active = false;
            this.profileName.setEditable(false);
            this.showMessage(INVITING_PLAYER_TEXT);
            CompletableFuture.<RealmsServer>supplyAsync(() -> {
                try {
                    return RealmsClient.create().invite(var0, var1);
                } catch (Exception var4) {
                    LOGGER.error("Couldn't invite user");
                    return null;
                }
            }, Util.ioPool()).thenAcceptAsync(param0 -> {
                if (param0 != null) {
                    this.serverData.players = param0.players;
                    this.minecraft.setScreen(new RealmsPlayerScreen(this.configureScreen, this.serverData));
                } else {
                    this.showMessage(NO_SUCH_PLAYER_ERROR_TEXT);
                }

                this.profileName.setEditable(true);
                this.inviteButton.active = true;
            }, this.screenExecutor);
        }
    }

    private void showMessage(Component param0) {
        this.message = param0;
        this.minecraft.getNarrator().sayNow(param0);
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
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        param0.drawString(this.font, NAME_LABEL, this.width / 2 - 100, row(1), -1, false);
        if (this.message != null) {
            param0.drawCenteredString(this.font, this.message, this.width / 2, row(5), -1);
        }

        this.profileName.render(param0, param1, param2, param3);
        super.render(param0, param1, param2, param3);
    }
}
