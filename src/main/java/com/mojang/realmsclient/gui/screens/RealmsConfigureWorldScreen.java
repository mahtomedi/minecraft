package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.gui.RealmsWorldSlotButton;
import com.mojang.realmsclient.util.RealmsTasks;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.annotation.Nullable;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsConfigureWorldScreen extends RealmsScreenWithCallback<WorldTemplate> implements RealmsWorldSlotButton.Listener {
    private static final Logger LOGGER = LogManager.getLogger();
    private String toolTip;
    private final RealmsMainScreen lastScreen;
    @Nullable
    private RealmsServer serverData;
    private final long serverId;
    private int left_x;
    private int right_x;
    private final int default_button_width = 80;
    private final int default_button_offset = 5;
    private RealmsButton playersButton;
    private RealmsButton settingsButton;
    private RealmsButton subscriptionButton;
    private RealmsButton optionsButton;
    private RealmsButton backupButton;
    private RealmsButton resetWorldButton;
    private RealmsButton switchMinigameButton;
    private boolean stateChanged;
    private int animTick;
    private int clicks;

    public RealmsConfigureWorldScreen(RealmsMainScreen param0, long param1) {
        this.lastScreen = param0;
        this.serverId = param1;
    }

    @Override
    public void init() {
        if (this.serverData == null) {
            this.fetchServerData(this.serverId);
        }

        this.left_x = this.width() / 2 - 187;
        this.right_x = this.width() / 2 + 190;
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.buttonsAdd(
            this.playersButton = new RealmsButton(
                2, this.centerButton(0, 3), RealmsConstants.row(0), 100, 20, getLocalizedString("mco.configure.world.buttons.players")
            ) {
                @Override
                public void onPress() {
                    Realms.setScreen(new RealmsPlayerScreen(RealmsConfigureWorldScreen.this, RealmsConfigureWorldScreen.this.serverData));
                }
            }
        );
        this.buttonsAdd(
            this.settingsButton = new RealmsButton(
                3, this.centerButton(1, 3), RealmsConstants.row(0), 100, 20, getLocalizedString("mco.configure.world.buttons.settings")
            ) {
                @Override
                public void onPress() {
                    Realms.setScreen(new RealmsSettingsScreen(RealmsConfigureWorldScreen.this, RealmsConfigureWorldScreen.this.serverData.clone()));
                }
            }
        );
        this.buttonsAdd(
            this.subscriptionButton = new RealmsButton(
                4, this.centerButton(2, 3), RealmsConstants.row(0), 100, 20, getLocalizedString("mco.configure.world.buttons.subscription")
            ) {
                @Override
                public void onPress() {
                    Realms.setScreen(
                        new RealmsSubscriptionInfoScreen(
                            RealmsConfigureWorldScreen.this, RealmsConfigureWorldScreen.this.serverData.clone(), RealmsConfigureWorldScreen.this.lastScreen
                        )
                    );
                }
            }
        );

        for(int var0 = 1; var0 < 5; ++var0) {
            this.addSlotButton(var0);
        }

        this.buttonsAdd(
            this.switchMinigameButton = new RealmsButton(
                8, this.leftButton(0), RealmsConstants.row(13) - 5, 100, 20, getLocalizedString("mco.configure.world.buttons.switchminigame")
            ) {
                @Override
                public void onPress() {
                    RealmsSelectWorldTemplateScreen var0 = new RealmsSelectWorldTemplateScreen(RealmsConfigureWorldScreen.this, RealmsServer.WorldType.MINIGAME);
                    var0.setTitle(RealmsScreen.getLocalizedString("mco.template.title.minigame"));
                    Realms.setScreen(var0);
                }
            }
        );
        this.buttonsAdd(
            this.optionsButton = new RealmsButton(
                5, this.leftButton(0), RealmsConstants.row(13) - 5, 90, 20, getLocalizedString("mco.configure.world.buttons.options")
            ) {
                @Override
                public void onPress() {
                    Realms.setScreen(
                        new RealmsSlotOptionsScreen(
                            RealmsConfigureWorldScreen.this,
                            RealmsConfigureWorldScreen.this.serverData.slots.get(RealmsConfigureWorldScreen.this.serverData.activeSlot).clone(),
                            RealmsConfigureWorldScreen.this.serverData.worldType,
                            RealmsConfigureWorldScreen.this.serverData.activeSlot
                        )
                    );
                }
            }
        );
        this.buttonsAdd(
            this.backupButton = new RealmsButton(6, this.leftButton(1), RealmsConstants.row(13) - 5, 90, 20, getLocalizedString("mco.configure.world.backup")) {
                @Override
                public void onPress() {
                    Realms.setScreen(
                        new RealmsBackupScreen(
                            RealmsConfigureWorldScreen.this,
                            RealmsConfigureWorldScreen.this.serverData.clone(),
                            RealmsConfigureWorldScreen.this.serverData.activeSlot
                        )
                    );
                }
            }
        );
        this.buttonsAdd(
            this.resetWorldButton = new RealmsButton(
                7, this.leftButton(2), RealmsConstants.row(13) - 5, 90, 20, getLocalizedString("mco.configure.world.buttons.resetworld")
            ) {
                @Override
                public void onPress() {
                    Realms.setScreen(
                        new RealmsResetWorldScreen(
                            RealmsConfigureWorldScreen.this, RealmsConfigureWorldScreen.this.serverData.clone(), RealmsConfigureWorldScreen.this.getNewScreen()
                        )
                    );
                }
            }
        );
        this.buttonsAdd(new RealmsButton(0, this.right_x - 80 + 8, RealmsConstants.row(13) - 5, 70, 20, getLocalizedString("gui.back")) {
            @Override
            public void onPress() {
                RealmsConfigureWorldScreen.this.backButtonClicked();
            }
        });
        this.backupButton.active(true);
        if (this.serverData == null) {
            this.hideMinigameButtons();
            this.hideRegularButtons();
            this.playersButton.active(false);
            this.settingsButton.active(false);
            this.subscriptionButton.active(false);
        } else {
            this.disableButtons();
            if (this.isMinigame()) {
                this.hideRegularButtons();
            } else {
                this.hideMinigameButtons();
            }
        }

    }

    private void addSlotButton(int param0) {
        int var0 = this.frame(param0);
        int var1 = RealmsConstants.row(5) + 5;
        int var2 = 100 + param0;
        RealmsWorldSlotButton var3 = new RealmsWorldSlotButton(var0, var1, 80, 80, () -> this.serverData, param0x -> this.toolTip = param0x, var2, param0, this);
        this.getProxy().buttonsAdd(var3);
    }

    private int leftButton(int param0) {
        return this.left_x + param0 * 95;
    }

    private int centerButton(int param0, int param1) {
        return this.width() / 2 - (param1 * 105 - 5) / 2 + param0 * 105;
    }

    @Override
    public void tick() {
        this.tickButtons();
        ++this.animTick;
        --this.clicks;
        if (this.clicks < 0) {
            this.clicks = 0;
        }

    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.toolTip = null;
        this.renderBackground();
        this.drawCenteredString(getLocalizedString("mco.configure.worlds.title"), this.width() / 2, RealmsConstants.row(4), 16777215);
        super.render(param0, param1, param2);
        if (this.serverData == null) {
            this.drawCenteredString(getLocalizedString("mco.configure.world.title"), this.width() / 2, 17, 16777215);
        } else {
            String var0 = this.serverData.getName();
            int var1 = this.fontWidth(var0);
            int var2 = this.serverData.state == RealmsServer.State.CLOSED ? 10526880 : 8388479;
            int var3 = this.fontWidth(getLocalizedString("mco.configure.world.title"));
            this.drawCenteredString(getLocalizedString("mco.configure.world.title"), this.width() / 2, 12, 16777215);
            this.drawCenteredString(var0, this.width() / 2, 24, var2);
            int var4 = Math.min(this.centerButton(2, 3) + 80 - 11, this.width() / 2 + var1 / 2 + var3 / 2 + 10);
            this.drawServerStatus(var4, 7, param0, param1);
            if (this.isMinigame()) {
                this.drawString(
                    getLocalizedString("mco.configure.current.minigame") + ": " + this.serverData.getMinigameName(),
                    this.left_x + 80 + 20 + 10,
                    RealmsConstants.row(13),
                    16777215
                );
            }

            if (this.toolTip != null) {
                this.renderMousehoverTooltip(this.toolTip, param0, param1);
            }

        }
    }

    private int frame(int param0) {
        return this.left_x + (param0 - 1) * 98;
    }

    @Override
    public void removed() {
        this.setKeyboardHandlerSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.backButtonClicked();
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    private void backButtonClicked() {
        if (this.stateChanged) {
            this.lastScreen.removeSelection();
        }

        Realms.setScreen(this.lastScreen);
    }

    private void fetchServerData(long param0) {
        new Thread(() -> {
            RealmsClient var0 = RealmsClient.createRealmsClient();

            try {
                this.serverData = var0.getOwnWorld(param0);
                this.disableButtons();
                if (this.isMinigame()) {
                    this.showMinigameButtons();
                } else {
                    this.showRegularButtons();
                }
            } catch (RealmsServiceException var5) {
                LOGGER.error("Couldn't get own world");
                Realms.setScreen(new RealmsGenericErrorScreen(var5.getMessage(), this.lastScreen));
            } catch (IOException var6) {
                LOGGER.error("Couldn't parse response getting own world");
            }

        }).start();
    }

    private void disableButtons() {
        this.playersButton.active(!this.serverData.expired);
        this.settingsButton.active(!this.serverData.expired);
        this.subscriptionButton.active(true);
        this.switchMinigameButton.active(!this.serverData.expired);
        this.optionsButton.active(!this.serverData.expired);
        this.resetWorldButton.active(!this.serverData.expired);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        return super.mouseClicked(param0, param1, param2);
    }

    private void joinRealm(RealmsServer param0) {
        if (this.serverData.state == RealmsServer.State.OPEN) {
            this.lastScreen.play(param0, new RealmsConfigureWorldScreen(this.lastScreen.newScreen(), this.serverId));
        } else {
            this.openTheWorld(true, new RealmsConfigureWorldScreen(this.lastScreen.newScreen(), this.serverId));
        }

    }

    @Override
    public void onSlotClick(int param0, RealmsWorldSlotButton.Action param1, boolean param2, boolean param3) {
        switch(param1) {
            case NOTHING:
                break;
            case JOIN:
                this.joinRealm(this.serverData);
                break;
            case SWITCH_SLOT:
                if (param2) {
                    this.switchToMinigame();
                } else if (param3) {
                    this.switchToEmptySlot(param0, this.serverData);
                } else {
                    this.switchToFullSlot(param0, this.serverData);
                }
                break;
            default:
                throw new IllegalStateException("Unknown action " + param1);
        }

    }

    private void switchToMinigame() {
        RealmsSelectWorldTemplateScreen var0 = new RealmsSelectWorldTemplateScreen(this, RealmsServer.WorldType.MINIGAME);
        var0.setTitle(getLocalizedString("mco.template.title.minigame"));
        var0.setWarning(getLocalizedString("mco.minigame.world.info.line1") + "\\n" + getLocalizedString("mco.minigame.world.info.line2"));
        Realms.setScreen(var0);
    }

    private void switchToFullSlot(int param0, RealmsServer param1) {
        String var0 = getLocalizedString("mco.configure.world.slot.switch.question.line1");
        String var1 = getLocalizedString("mco.configure.world.slot.switch.question.line2");
        Realms.setScreen(new RealmsLongConfirmationScreen((param2, param3) -> {
            if (param2) {
                this.switchSlot(param1.id, param0);
            } else {
                Realms.setScreen(this);
            }

        }, RealmsLongConfirmationScreen.Type.Info, var0, var1, true, 9));
    }

    private void switchToEmptySlot(int param0, RealmsServer param1) {
        String var0 = getLocalizedString("mco.configure.world.slot.switch.question.line1");
        String var1 = getLocalizedString("mco.configure.world.slot.switch.question.line2");
        Realms.setScreen(
            new RealmsLongConfirmationScreen(
                (param2, param3) -> {
                    if (param2) {
                        RealmsResetWorldScreen var0x = new RealmsResetWorldScreen(
                            this,
                            param1,
                            this.getNewScreen(),
                            getLocalizedString("mco.configure.world.switch.slot"),
                            getLocalizedString("mco.configure.world.switch.slot.subtitle"),
                            10526880,
                            getLocalizedString("gui.cancel")
                        );
                        var0x.setSlot(param0);
                        var0x.setResetTitle(getLocalizedString("mco.create.world.reset.title"));
                        Realms.setScreen(var0x);
                    } else {
                        Realms.setScreen(this);
                    }
        
                },
                RealmsLongConfirmationScreen.Type.Info,
                var0,
                var1,
                true,
                10
            )
        );
    }

    protected void renderMousehoverTooltip(String param0, int param1, int param2) {
        if (param0 != null) {
            int var0 = param1 + 12;
            int var1 = param2 - 12;
            int var2 = this.fontWidth(param0);
            if (var0 + var2 + 3 > this.right_x) {
                var0 = var0 - var2 - 20;
            }

            this.fillGradient(var0 - 3, var1 - 3, var0 + var2 + 3, var1 + 8 + 3, -1073741824, -1073741824);
            this.fontDrawShadow(param0, var0, var1, 16777215);
        }
    }

    private void drawServerStatus(int param0, int param1, int param2, int param3) {
        if (this.serverData.expired) {
            this.drawExpired(param0, param1, param2, param3);
        } else if (this.serverData.state == RealmsServer.State.CLOSED) {
            this.drawClose(param0, param1, param2, param3);
        } else if (this.serverData.state == RealmsServer.State.OPEN) {
            if (this.serverData.daysLeft < 7) {
                this.drawExpiring(param0, param1, param2, param3, this.serverData.daysLeft);
            } else {
                this.drawOpen(param0, param1, param2, param3);
            }
        }

    }

    private void drawExpired(int param0, int param1, int param2, int param3) {
        RealmsScreen.bind("realms:textures/gui/realms/expired_icon.png");
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        RealmsScreen.blit(param0, param1, 0.0F, 0.0F, 10, 28, 10, 28);
        RenderSystem.popMatrix();
        if (param2 >= param0 && param2 <= param0 + 9 && param3 >= param1 && param3 <= param1 + 27) {
            this.toolTip = getLocalizedString("mco.selectServer.expired");
        }

    }

    private void drawExpiring(int param0, int param1, int param2, int param3, int param4) {
        RealmsScreen.bind("realms:textures/gui/realms/expires_soon_icon.png");
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        if (this.animTick % 20 < 10) {
            RealmsScreen.blit(param0, param1, 0.0F, 0.0F, 10, 28, 20, 28);
        } else {
            RealmsScreen.blit(param0, param1, 10.0F, 0.0F, 10, 28, 20, 28);
        }

        RenderSystem.popMatrix();
        if (param2 >= param0 && param2 <= param0 + 9 && param3 >= param1 && param3 <= param1 + 27) {
            if (param4 <= 0) {
                this.toolTip = getLocalizedString("mco.selectServer.expires.soon");
            } else if (param4 == 1) {
                this.toolTip = getLocalizedString("mco.selectServer.expires.day");
            } else {
                this.toolTip = getLocalizedString("mco.selectServer.expires.days", new Object[]{param4});
            }
        }

    }

    private void drawOpen(int param0, int param1, int param2, int param3) {
        RealmsScreen.bind("realms:textures/gui/realms/on_icon.png");
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        RealmsScreen.blit(param0, param1, 0.0F, 0.0F, 10, 28, 10, 28);
        RenderSystem.popMatrix();
        if (param2 >= param0 && param2 <= param0 + 9 && param3 >= param1 && param3 <= param1 + 27) {
            this.toolTip = getLocalizedString("mco.selectServer.open");
        }

    }

    private void drawClose(int param0, int param1, int param2, int param3) {
        RealmsScreen.bind("realms:textures/gui/realms/off_icon.png");
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        RealmsScreen.blit(param0, param1, 0.0F, 0.0F, 10, 28, 10, 28);
        RenderSystem.popMatrix();
        if (param2 >= param0 && param2 <= param0 + 9 && param3 >= param1 && param3 <= param1 + 27) {
            this.toolTip = getLocalizedString("mco.selectServer.closed");
        }

    }

    private boolean isMinigame() {
        return this.serverData != null && this.serverData.worldType.equals(RealmsServer.WorldType.MINIGAME);
    }

    private void hideRegularButtons() {
        this.hide(this.optionsButton);
        this.hide(this.backupButton);
        this.hide(this.resetWorldButton);
    }

    private void hide(RealmsButton param0) {
        param0.setVisible(false);
        this.removeButton(param0);
    }

    private void showRegularButtons() {
        this.show(this.optionsButton);
        this.show(this.backupButton);
        this.show(this.resetWorldButton);
    }

    private void show(RealmsButton param0) {
        param0.setVisible(true);
        this.buttonsAdd(param0);
    }

    private void hideMinigameButtons() {
        this.hide(this.switchMinigameButton);
    }

    private void showMinigameButtons() {
        this.show(this.switchMinigameButton);
    }

    public void saveSlotSettings(RealmsWorldOptions param0) {
        RealmsWorldOptions var0 = this.serverData.slots.get(this.serverData.activeSlot);
        param0.templateId = var0.templateId;
        param0.templateImage = var0.templateImage;
        RealmsClient var1 = RealmsClient.createRealmsClient();

        try {
            var1.updateSlot(this.serverData.id, this.serverData.activeSlot, param0);
            this.serverData.slots.put(this.serverData.activeSlot, param0);
        } catch (RealmsServiceException var5) {
            LOGGER.error("Couldn't save slot settings");
            Realms.setScreen(new RealmsGenericErrorScreen(var5, this));
            return;
        } catch (UnsupportedEncodingException var6) {
            LOGGER.error("Couldn't save slot settings");
        }

        Realms.setScreen(this);
    }

    public void saveSettings(String param0, String param1) {
        String var0 = param1 != null && !param1.trim().isEmpty() ? param1 : null;
        RealmsClient var1 = RealmsClient.createRealmsClient();

        try {
            var1.update(this.serverData.id, param0, var0);
            this.serverData.setName(param0);
            this.serverData.setDescription(var0);
        } catch (RealmsServiceException var6) {
            LOGGER.error("Couldn't save settings");
            Realms.setScreen(new RealmsGenericErrorScreen(var6, this));
            return;
        } catch (UnsupportedEncodingException var7) {
            LOGGER.error("Couldn't save settings");
        }

        Realms.setScreen(this);
    }

    public void openTheWorld(boolean param0, RealmsScreen param1) {
        RealmsTasks.OpenServerTask var0 = new RealmsTasks.OpenServerTask(this.serverData, this, this.lastScreen, param0);
        RealmsLongRunningMcoTaskScreen var1 = new RealmsLongRunningMcoTaskScreen(param1, var0);
        var1.start();
        Realms.setScreen(var1);
    }

    public void closeTheWorld(RealmsScreen param0) {
        RealmsTasks.CloseServerTask var0 = new RealmsTasks.CloseServerTask(this.serverData, this);
        RealmsLongRunningMcoTaskScreen var1 = new RealmsLongRunningMcoTaskScreen(param0, var0);
        var1.start();
        Realms.setScreen(var1);
    }

    public void stateChanged() {
        this.stateChanged = true;
    }

    void callback(WorldTemplate param0) {
        if (param0 != null) {
            if (WorldTemplate.WorldTemplateType.MINIGAME.equals(param0.type)) {
                this.switchMinigame(param0);
            }

        }
    }

    private void switchSlot(long param0, int param1) {
        RealmsConfigureWorldScreen var0 = this.getNewScreen();
        RealmsTasks.SwitchSlotTask var1 = new RealmsTasks.SwitchSlotTask(param0, param1, (param1x, param2) -> Realms.setScreen(var0), 11);
        RealmsLongRunningMcoTaskScreen var2 = new RealmsLongRunningMcoTaskScreen(this.lastScreen, var1);
        var2.start();
        Realms.setScreen(var2);
    }

    private void switchMinigame(WorldTemplate param0) {
        RealmsTasks.SwitchMinigameTask var0 = new RealmsTasks.SwitchMinigameTask(this.serverData.id, param0, this.getNewScreen());
        RealmsLongRunningMcoTaskScreen var1 = new RealmsLongRunningMcoTaskScreen(this.lastScreen, var0);
        var1.start();
        Realms.setScreen(var1);
    }

    public RealmsConfigureWorldScreen getNewScreen() {
        return new RealmsConfigureWorldScreen(this.lastScreen, this.serverId);
    }
}
