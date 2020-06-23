package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsWorldSlotButton;
import com.mojang.realmsclient.util.task.CloseServerTask;
import com.mojang.realmsclient.util.task.OpenServerTask;
import com.mojang.realmsclient.util.task.SwitchMinigameTask;
import com.mojang.realmsclient.util.task.SwitchSlotTask;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsConfigureWorldScreen extends RealmsScreenWithCallback {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation ON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/on_icon.png");
    private static final ResourceLocation OFF_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/off_icon.png");
    private static final ResourceLocation EXPIRED_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expired_icon.png");
    private static final ResourceLocation EXPIRES_SOON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expires_soon_icon.png");
    private Component toolTip;
    private final RealmsMainScreen lastScreen;
    @Nullable
    private RealmsServer serverData;
    private final long serverId;
    private int leftX;
    private int rightX;
    private Button playersButton;
    private Button settingsButton;
    private Button subscriptionButton;
    private Button optionsButton;
    private Button backupButton;
    private Button resetWorldButton;
    private Button switchMinigameButton;
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

        this.leftX = this.width / 2 - 187;
        this.rightX = this.width / 2 + 190;
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.playersButton = this.addButton(
            new Button(
                this.centerButton(0, 3),
                row(0),
                100,
                20,
                new TranslatableComponent("mco.configure.world.buttons.players"),
                param0 -> this.minecraft.setScreen(new RealmsPlayerScreen(this, this.serverData))
            )
        );
        this.settingsButton = this.addButton(
            new Button(
                this.centerButton(1, 3),
                row(0),
                100,
                20,
                new TranslatableComponent("mco.configure.world.buttons.settings"),
                param0 -> this.minecraft.setScreen(new RealmsSettingsScreen(this, this.serverData.clone()))
            )
        );
        this.subscriptionButton = this.addButton(
            new Button(
                this.centerButton(2, 3),
                row(0),
                100,
                20,
                new TranslatableComponent("mco.configure.world.buttons.subscription"),
                param0 -> this.minecraft.setScreen(new RealmsSubscriptionInfoScreen(this, this.serverData.clone(), this.lastScreen))
            )
        );

        for(int var0 = 1; var0 < 5; ++var0) {
            this.addSlotButton(var0);
        }

        this.switchMinigameButton = this.addButton(
            new Button(this.leftButton(0), row(13) - 5, 100, 20, new TranslatableComponent("mco.configure.world.buttons.switchminigame"), param0 -> {
                RealmsSelectWorldTemplateScreen var0x = new RealmsSelectWorldTemplateScreen(this, RealmsServer.WorldType.MINIGAME);
                var0x.setTitle(new TranslatableComponent("mco.template.title.minigame"));
                this.minecraft.setScreen(var0x);
            })
        );
        this.optionsButton = this.addButton(
            new Button(
                this.leftButton(0),
                row(13) - 5,
                90,
                20,
                new TranslatableComponent("mco.configure.world.buttons.options"),
                param0 -> this.minecraft
                        .setScreen(
                            new RealmsSlotOptionsScreen(
                                this, this.serverData.slots.get(this.serverData.activeSlot).clone(), this.serverData.worldType, this.serverData.activeSlot
                            )
                        )
            )
        );
        this.backupButton = this.addButton(
            new Button(
                this.leftButton(1),
                row(13) - 5,
                90,
                20,
                new TranslatableComponent("mco.configure.world.backup"),
                param0 -> this.minecraft.setScreen(new RealmsBackupScreen(this, this.serverData.clone(), this.serverData.activeSlot))
            )
        );
        this.resetWorldButton = this.addButton(
            new Button(
                this.leftButton(2),
                row(13) - 5,
                90,
                20,
                new TranslatableComponent("mco.configure.world.buttons.resetworld"),
                param0 -> this.minecraft
                        .setScreen(
                            new RealmsResetWorldScreen(
                                this,
                                this.serverData.clone(),
                                () -> this.minecraft.setScreen(this.getNewScreen()),
                                () -> this.minecraft.setScreen(this.getNewScreen())
                            )
                        )
            )
        );
        this.addButton(new Button(this.rightX - 80 + 8, row(13) - 5, 70, 20, CommonComponents.GUI_BACK, param0 -> this.backButtonClicked()));
        this.backupButton.active = true;
        if (this.serverData == null) {
            this.hideMinigameButtons();
            this.hideRegularButtons();
            this.playersButton.active = false;
            this.settingsButton.active = false;
            this.subscriptionButton.active = false;
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
        int var1 = row(5) + 5;
        RealmsWorldSlotButton var2 = new RealmsWorldSlotButton(
            var0, var1, 80, 80, () -> this.serverData, param0x -> this.toolTip = param0x, param0, param1 -> {
                RealmsWorldSlotButton.State var0x = ((RealmsWorldSlotButton)param1).getState();
                if (var0x != null) {
                    switch(var0x.action) {
                        case NOTHING:
                            break;
                        case JOIN:
                            this.joinRealm(this.serverData);
                            break;
                        case SWITCH_SLOT:
                            if (var0x.minigame) {
                                this.switchToMinigame();
                            } else if (var0x.empty) {
                                this.switchToEmptySlot(param0, this.serverData);
                            } else {
                                this.switchToFullSlot(param0, this.serverData);
                            }
                            break;
                        default:
                            throw new IllegalStateException("Unknown action " + var0x.action);
                    }
                }
    
            }
        );
        this.addButton(var2);
    }

    private int leftButton(int param0) {
        return this.leftX + param0 * 95;
    }

    private int centerButton(int param0, int param1) {
        return this.width / 2 - (param1 * 105 - 5) / 2 + param0 * 105;
    }

    @Override
    public void tick() {
        super.tick();
        ++this.animTick;
        --this.clicks;
        if (this.clicks < 0) {
            this.clicks = 0;
        }

    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.toolTip = null;
        this.renderBackground(param0);
        this.drawCenteredString(param0, this.font, I18n.get("mco.configure.worlds.title"), this.width / 2, row(4), 16777215);
        super.render(param0, param1, param2, param3);
        if (this.serverData == null) {
            this.drawCenteredString(param0, this.font, I18n.get("mco.configure.world.title"), this.width / 2, 17, 16777215);
        } else {
            String var0 = this.serverData.getName();
            int var1 = this.font.width(var0);
            int var2 = this.serverData.state == RealmsServer.State.CLOSED ? 10526880 : 8388479;
            int var3 = this.font.width(I18n.get("mco.configure.world.title"));
            this.drawCenteredString(param0, this.font, I18n.get("mco.configure.world.title"), this.width / 2, 12, 16777215);
            this.drawCenteredString(param0, this.font, var0, this.width / 2, 24, var2);
            int var4 = Math.min(this.centerButton(2, 3) + 80 - 11, this.width / 2 + var1 / 2 + var3 / 2 + 10);
            this.drawServerStatus(param0, var4, 7, param1, param2);
            if (this.isMinigame()) {
                this.font
                    .draw(
                        param0,
                        I18n.get("mco.configure.current.minigame") + ": " + this.serverData.getMinigameName(),
                        (float)(this.leftX + 80 + 20 + 10),
                        (float)row(13),
                        16777215
                    );
            }

            if (this.toolTip != null) {
                this.renderMousehoverTooltip(param0, this.toolTip, param1, param2);
            }

        }
    }

    private int frame(int param0) {
        return this.leftX + (param0 - 1) * 98;
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
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

        this.minecraft.setScreen(this.lastScreen);
    }

    private void fetchServerData(long param0) {
        new Thread(() -> {
            RealmsClient var0 = RealmsClient.create();

            try {
                this.serverData = var0.getOwnWorld(param0);
                this.disableButtons();
                if (this.isMinigame()) {
                    this.show(this.switchMinigameButton);
                } else {
                    this.show(this.optionsButton);
                    this.show(this.backupButton);
                    this.show(this.resetWorldButton);
                }
            } catch (RealmsServiceException var5) {
                LOGGER.error("Couldn't get own world");
                this.minecraft.execute(() -> this.minecraft.setScreen(new RealmsGenericErrorScreen(Component.nullToEmpty(var5.getMessage()), this.lastScreen)));
            }

        }).start();
    }

    private void disableButtons() {
        this.playersButton.active = !this.serverData.expired;
        this.settingsButton.active = !this.serverData.expired;
        this.subscriptionButton.active = true;
        this.switchMinigameButton.active = !this.serverData.expired;
        this.optionsButton.active = !this.serverData.expired;
        this.resetWorldButton.active = !this.serverData.expired;
    }

    private void joinRealm(RealmsServer param0) {
        if (this.serverData.state == RealmsServer.State.OPEN) {
            this.lastScreen.play(param0, new RealmsConfigureWorldScreen(this.lastScreen.newScreen(), this.serverId));
        } else {
            this.openTheWorld(true, new RealmsConfigureWorldScreen(this.lastScreen.newScreen(), this.serverId));
        }

    }

    private void switchToMinigame() {
        RealmsSelectWorldTemplateScreen var0 = new RealmsSelectWorldTemplateScreen(this, RealmsServer.WorldType.MINIGAME);
        var0.setTitle(new TranslatableComponent("mco.template.title.minigame"));
        var0.setWarning(new TranslatableComponent("mco.minigame.world.info.line1"), new TranslatableComponent("mco.minigame.world.info.line2"));
        this.minecraft.setScreen(var0);
    }

    private void switchToFullSlot(int param0, RealmsServer param1) {
        Component var0 = new TranslatableComponent("mco.configure.world.slot.switch.question.line1");
        Component var1 = new TranslatableComponent("mco.configure.world.slot.switch.question.line2");
        this.minecraft
            .setScreen(
                new RealmsLongConfirmationScreen(
                    param2 -> {
                        if (param2) {
                            this.minecraft
                                .setScreen(
                                    new RealmsLongRunningMcoTaskScreen(
                                        this.lastScreen, new SwitchSlotTask(param1.id, param0, () -> this.minecraft.setScreen(this.getNewScreen()))
                                    )
                                );
                        } else {
                            this.minecraft.setScreen(this);
                        }
            
                    },
                    RealmsLongConfirmationScreen.Type.Info,
                    var0,
                    var1,
                    true
                )
            );
    }

    private void switchToEmptySlot(int param0, RealmsServer param1) {
        Component var0 = new TranslatableComponent("mco.configure.world.slot.switch.question.line1");
        Component var1 = new TranslatableComponent("mco.configure.world.slot.switch.question.line2");
        this.minecraft
            .setScreen(
                new RealmsLongConfirmationScreen(
                    param2 -> {
                        if (param2) {
                            RealmsResetWorldScreen var0x = new RealmsResetWorldScreen(
                                this,
                                param1,
                                new TranslatableComponent("mco.configure.world.switch.slot"),
                                new TranslatableComponent("mco.configure.world.switch.slot.subtitle"),
                                10526880,
                                CommonComponents.GUI_CANCEL,
                                () -> this.minecraft.setScreen(this.getNewScreen()),
                                () -> this.minecraft.setScreen(this.getNewScreen())
                            );
                            var0x.setSlot(param0);
                            var0x.setResetTitle(I18n.get("mco.create.world.reset.title"));
                            this.minecraft.setScreen(var0x);
                        } else {
                            this.minecraft.setScreen(this);
                        }
            
                    },
                    RealmsLongConfirmationScreen.Type.Info,
                    var0,
                    var1,
                    true
                )
            );
    }

    protected void renderMousehoverTooltip(PoseStack param0, Component param1, int param2, int param3) {
        int var0 = param2 + 12;
        int var1 = param3 - 12;
        int var2 = this.font.width(param1);
        if (var0 + var2 + 3 > this.rightX) {
            var0 = var0 - var2 - 20;
        }

        this.fillGradient(param0, var0 - 3, var1 - 3, var0 + var2 + 3, var1 + 8 + 3, -1073741824, -1073741824);
        this.font.drawShadow(param0, param1, (float)var0, (float)var1, 16777215);
    }

    private void drawServerStatus(PoseStack param0, int param1, int param2, int param3, int param4) {
        if (this.serverData.expired) {
            this.drawExpired(param0, param1, param2, param3, param4);
        } else if (this.serverData.state == RealmsServer.State.CLOSED) {
            this.drawClose(param0, param1, param2, param3, param4);
        } else if (this.serverData.state == RealmsServer.State.OPEN) {
            if (this.serverData.daysLeft < 7) {
                this.drawExpiring(param0, param1, param2, param3, param4, this.serverData.daysLeft);
            } else {
                this.drawOpen(param0, param1, param2, param3, param4);
            }
        }

    }

    private void drawExpired(PoseStack param0, int param1, int param2, int param3, int param4) {
        this.minecraft.getTextureManager().bind(EXPIRED_ICON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.blit(param0, param1, param2, 0.0F, 0.0F, 10, 28, 10, 28);
        if (param3 >= param1 && param3 <= param1 + 9 && param4 >= param2 && param4 <= param2 + 27) {
            this.toolTip = new TranslatableComponent("mco.selectServer.expired");
        }

    }

    private void drawExpiring(PoseStack param0, int param1, int param2, int param3, int param4, int param5) {
        this.minecraft.getTextureManager().bind(EXPIRES_SOON_ICON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.animTick % 20 < 10) {
            GuiComponent.blit(param0, param1, param2, 0.0F, 0.0F, 10, 28, 20, 28);
        } else {
            GuiComponent.blit(param0, param1, param2, 10.0F, 0.0F, 10, 28, 20, 28);
        }

        if (param3 >= param1 && param3 <= param1 + 9 && param4 >= param2 && param4 <= param2 + 27) {
            if (param5 <= 0) {
                this.toolTip = new TranslatableComponent("mco.selectServer.expires.soon");
            } else if (param5 == 1) {
                this.toolTip = new TranslatableComponent("mco.selectServer.expires.day");
            } else {
                this.toolTip = new TranslatableComponent("mco.selectServer.expires.days", param5);
            }
        }

    }

    private void drawOpen(PoseStack param0, int param1, int param2, int param3, int param4) {
        this.minecraft.getTextureManager().bind(ON_ICON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.blit(param0, param1, param2, 0.0F, 0.0F, 10, 28, 10, 28);
        if (param3 >= param1 && param3 <= param1 + 9 && param4 >= param2 && param4 <= param2 + 27) {
            this.toolTip = new TranslatableComponent("mco.selectServer.open");
        }

    }

    private void drawClose(PoseStack param0, int param1, int param2, int param3, int param4) {
        this.minecraft.getTextureManager().bind(OFF_ICON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.blit(param0, param1, param2, 0.0F, 0.0F, 10, 28, 10, 28);
        if (param3 >= param1 && param3 <= param1 + 9 && param4 >= param2 && param4 <= param2 + 27) {
            this.toolTip = new TranslatableComponent("mco.selectServer.closed");
        }

    }

    private boolean isMinigame() {
        return this.serverData != null && this.serverData.worldType == RealmsServer.WorldType.MINIGAME;
    }

    private void hideRegularButtons() {
        this.hide(this.optionsButton);
        this.hide(this.backupButton);
        this.hide(this.resetWorldButton);
    }

    private void hide(Button param0) {
        param0.visible = false;
        this.children.remove(param0);
        this.buttons.remove(param0);
    }

    private void show(Button param0) {
        param0.visible = true;
        this.addButton(param0);
    }

    private void hideMinigameButtons() {
        this.hide(this.switchMinigameButton);
    }

    public void saveSlotSettings(RealmsWorldOptions param0) {
        RealmsWorldOptions var0 = this.serverData.slots.get(this.serverData.activeSlot);
        param0.templateId = var0.templateId;
        param0.templateImage = var0.templateImage;
        RealmsClient var1 = RealmsClient.create();

        try {
            var1.updateSlot(this.serverData.id, this.serverData.activeSlot, param0);
            this.serverData.slots.put(this.serverData.activeSlot, param0);
        } catch (RealmsServiceException var5) {
            LOGGER.error("Couldn't save slot settings");
            this.minecraft.setScreen(new RealmsGenericErrorScreen(var5, this));
            return;
        }

        this.minecraft.setScreen(this);
    }

    public void saveSettings(String param0, String param1) {
        String var0 = param1.trim().isEmpty() ? null : param1;
        RealmsClient var1 = RealmsClient.create();

        try {
            var1.update(this.serverData.id, param0, var0);
            this.serverData.setName(param0);
            this.serverData.setDescription(var0);
        } catch (RealmsServiceException var6) {
            LOGGER.error("Couldn't save settings");
            this.minecraft.setScreen(new RealmsGenericErrorScreen(var6, this));
            return;
        }

        this.minecraft.setScreen(this);
    }

    public void openTheWorld(boolean param0, Screen param1) {
        this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(param1, new OpenServerTask(this.serverData, this, this.lastScreen, param0)));
    }

    public void closeTheWorld(Screen param0) {
        this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(param0, new CloseServerTask(this.serverData, this)));
    }

    public void stateChanged() {
        this.stateChanged = true;
    }

    @Override
    protected void callback(@Nullable WorldTemplate param0) {
        if (param0 != null) {
            if (WorldTemplate.WorldTemplateType.MINIGAME == param0.type) {
                this.minecraft
                    .setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new SwitchMinigameTask(this.serverData.id, param0, this.getNewScreen())));
            }

        }
    }

    public RealmsConfigureWorldScreen getNewScreen() {
        return new RealmsConfigureWorldScreen(this.lastScreen, this.serverId);
    }
}
