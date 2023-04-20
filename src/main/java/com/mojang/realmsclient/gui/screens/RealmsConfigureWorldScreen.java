package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
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
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsConfigureWorldScreen extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation ON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/on_icon.png");
    private static final ResourceLocation OFF_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/off_icon.png");
    private static final ResourceLocation EXPIRED_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expired_icon.png");
    private static final ResourceLocation EXPIRES_SOON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expires_soon_icon.png");
    private static final Component WORLD_LIST_TITLE = Component.translatable("mco.configure.worlds.title");
    private static final Component TITLE = Component.translatable("mco.configure.world.title");
    private static final Component MINIGAME_PREFIX = Component.translatable("mco.configure.current.minigame").append(": ");
    private static final Component SERVER_EXPIRED_TOOLTIP = Component.translatable("mco.selectServer.expired");
    private static final Component SERVER_EXPIRING_SOON_TOOLTIP = Component.translatable("mco.selectServer.expires.soon");
    private static final Component SERVER_EXPIRING_IN_DAY_TOOLTIP = Component.translatable("mco.selectServer.expires.day");
    private static final Component SERVER_OPEN_TOOLTIP = Component.translatable("mco.selectServer.open");
    private static final Component SERVER_CLOSED_TOOLTIP = Component.translatable("mco.selectServer.closed");
    private static final int DEFAULT_BUTTON_WIDTH = 80;
    private static final int DEFAULT_BUTTON_OFFSET = 5;
    @Nullable
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
    private final List<RealmsWorldSlotButton> slotButtonList = Lists.newArrayList();

    public RealmsConfigureWorldScreen(RealmsMainScreen param0, long param1) {
        super(TITLE);
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
        this.playersButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("mco.configure.world.buttons.players"),
                    param0 -> this.minecraft.setScreen(new RealmsPlayerScreen(this, this.serverData))
                )
                .bounds(this.centerButton(0, 3), row(0), 100, 20)
                .build()
        );
        this.settingsButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("mco.configure.world.buttons.settings"),
                    param0 -> this.minecraft.setScreen(new RealmsSettingsScreen(this, this.serverData.clone()))
                )
                .bounds(this.centerButton(1, 3), row(0), 100, 20)
                .build()
        );
        this.subscriptionButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("mco.configure.world.buttons.subscription"),
                    param0 -> this.minecraft.setScreen(new RealmsSubscriptionInfoScreen(this, this.serverData.clone(), this.lastScreen))
                )
                .bounds(this.centerButton(2, 3), row(0), 100, 20)
                .build()
        );
        this.slotButtonList.clear();

        for(int var0 = 1; var0 < 5; ++var0) {
            this.slotButtonList.add(this.addSlotButton(var0));
        }

        this.switchMinigameButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("mco.configure.world.buttons.switchminigame"),
                    param0 -> this.minecraft
                            .setScreen(
                                new RealmsSelectWorldTemplateScreen(
                                    Component.translatable("mco.template.title.minigame"), this::templateSelectionCallback, RealmsServer.WorldType.MINIGAME
                                )
                            )
                )
                .bounds(this.leftButton(0), row(13) - 5, 100, 20)
                .build()
        );
        this.optionsButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("mco.configure.world.buttons.options"),
                    param0 -> this.minecraft
                            .setScreen(
                                new RealmsSlotOptionsScreen(
                                    this, this.serverData.slots.get(this.serverData.activeSlot).clone(), this.serverData.worldType, this.serverData.activeSlot
                                )
                            )
                )
                .bounds(this.leftButton(0), row(13) - 5, 90, 20)
                .build()
        );
        this.backupButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("mco.configure.world.backup"),
                    param0 -> this.minecraft.setScreen(new RealmsBackupScreen(this, this.serverData.clone(), this.serverData.activeSlot))
                )
                .bounds(this.leftButton(1), row(13) - 5, 90, 20)
                .build()
        );
        this.resetWorldButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("mco.configure.world.buttons.resetworld"),
                    param0 -> this.minecraft
                            .setScreen(
                                new RealmsResetWorldScreen(
                                    this,
                                    this.serverData.clone(),
                                    () -> this.minecraft.execute(() -> this.minecraft.setScreen(this.getNewScreen())),
                                    () -> this.minecraft.setScreen(this.getNewScreen())
                                )
                            )
                )
                .bounds(this.leftButton(2), row(13) - 5, 90, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_BACK, param0 -> this.backButtonClicked()).bounds(this.rightX - 80 + 8, row(13) - 5, 70, 20).build()
        );
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

    private RealmsWorldSlotButton addSlotButton(int param0) {
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
        return this.addRenderableWidget(var2);
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

        this.slotButtonList.forEach(RealmsWorldSlotButton::tick);
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.toolTip = null;
        this.renderBackground(param0);
        param0.drawCenteredString(this.font, WORLD_LIST_TITLE, this.width / 2, row(4), 16777215);
        super.render(param0, param1, param2, param3);
        if (this.serverData == null) {
            param0.drawCenteredString(this.font, this.title, this.width / 2, 17, 16777215);
        } else {
            String var0 = this.serverData.getName();
            int var1 = this.font.width(var0);
            int var2 = this.serverData.state == RealmsServer.State.CLOSED ? 10526880 : 8388479;
            int var3 = this.font.width(this.title);
            param0.drawCenteredString(this.font, this.title, this.width / 2, 12, 16777215);
            param0.drawCenteredString(this.font, var0, this.width / 2, 24, var2);
            int var4 = Math.min(this.centerButton(2, 3) + 80 - 11, this.width / 2 + var1 / 2 + var3 / 2 + 10);
            this.drawServerStatus(param0, var4, 7, param1, param2);
            if (this.isMinigame()) {
                param0.drawString(
                    this.font, MINIGAME_PREFIX.copy().append(this.serverData.getMinigameName()), this.leftX + 80 + 20 + 10, row(13), 16777215, false
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
            this.lastScreen.resetScreen();
        }

        this.minecraft.setScreen(this.lastScreen);
    }

    private void fetchServerData(long param0) {
        new Thread(() -> {
            RealmsClient var0 = RealmsClient.create();

            try {
                RealmsServer var1 = var0.getOwnWorld(param0);
                this.minecraft.execute(() -> {
                    this.serverData = var1;
                    this.disableButtons();
                    if (this.isMinigame()) {
                        this.show(this.switchMinigameButton);
                    } else {
                        this.show(this.optionsButton);
                        this.show(this.backupButton);
                        this.show(this.resetWorldButton);
                    }

                });
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
        RealmsSelectWorldTemplateScreen var0 = new RealmsSelectWorldTemplateScreen(
            Component.translatable("mco.template.title.minigame"), this::templateSelectionCallback, RealmsServer.WorldType.MINIGAME
        );
        var0.setWarning(Component.translatable("mco.minigame.world.info.line1"), Component.translatable("mco.minigame.world.info.line2"));
        this.minecraft.setScreen(var0);
    }

    private void switchToFullSlot(int param0, RealmsServer param1) {
        Component var0 = Component.translatable("mco.configure.world.slot.switch.question.line1");
        Component var1 = Component.translatable("mco.configure.world.slot.switch.question.line2");
        this.minecraft
            .setScreen(
                new RealmsLongConfirmationScreen(
                    param2 -> {
                        if (param2) {
                            this.minecraft
                                .setScreen(
                                    new RealmsLongRunningMcoTaskScreen(
                                        this.lastScreen,
                                        new SwitchSlotTask(param1.id, param0, () -> this.minecraft.execute(() -> this.minecraft.setScreen(this.getNewScreen())))
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
        Component var0 = Component.translatable("mco.configure.world.slot.switch.question.line1");
        Component var1 = Component.translatable("mco.configure.world.slot.switch.question.line2");
        this.minecraft
            .setScreen(
                new RealmsLongConfirmationScreen(
                    param2 -> {
                        if (param2) {
                            RealmsResetWorldScreen var0x = new RealmsResetWorldScreen(
                                this,
                                param1,
                                Component.translatable("mco.configure.world.switch.slot"),
                                Component.translatable("mco.configure.world.switch.slot.subtitle"),
                                10526880,
                                CommonComponents.GUI_CANCEL,
                                () -> this.minecraft.execute(() -> this.minecraft.setScreen(this.getNewScreen())),
                                () -> this.minecraft.setScreen(this.getNewScreen())
                            );
                            var0x.setSlot(param0);
                            var0x.setResetTitle(Component.translatable("mco.create.world.reset.title"));
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

    protected void renderMousehoverTooltip(GuiGraphics param0, @Nullable Component param1, int param2, int param3) {
        int var0 = param2 + 12;
        int var1 = param3 - 12;
        int var2 = this.font.width(param1);
        if (var0 + var2 + 3 > this.rightX) {
            var0 = var0 - var2 - 20;
        }

        param0.fillGradient(var0 - 3, var1 - 3, var0 + var2 + 3, var1 + 8 + 3, -1073741824, -1073741824);
        param0.drawString(this.font, param1, var0, var1, 16777215);
    }

    private void drawServerStatus(GuiGraphics param0, int param1, int param2, int param3, int param4) {
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

    private void drawExpired(GuiGraphics param0, int param1, int param2, int param3, int param4) {
        param0.blit(EXPIRED_ICON_LOCATION, param1, param2, 0.0F, 0.0F, 10, 28, 10, 28);
        if (param3 >= param1 && param3 <= param1 + 9 && param4 >= param2 && param4 <= param2 + 27) {
            this.toolTip = SERVER_EXPIRED_TOOLTIP;
        }

    }

    private void drawExpiring(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5) {
        if (this.animTick % 20 < 10) {
            param0.blit(EXPIRES_SOON_ICON_LOCATION, param1, param2, 0.0F, 0.0F, 10, 28, 20, 28);
        } else {
            param0.blit(EXPIRES_SOON_ICON_LOCATION, param1, param2, 10.0F, 0.0F, 10, 28, 20, 28);
        }

        if (param3 >= param1 && param3 <= param1 + 9 && param4 >= param2 && param4 <= param2 + 27) {
            if (param5 <= 0) {
                this.toolTip = SERVER_EXPIRING_SOON_TOOLTIP;
            } else if (param5 == 1) {
                this.toolTip = SERVER_EXPIRING_IN_DAY_TOOLTIP;
            } else {
                this.toolTip = Component.translatable("mco.selectServer.expires.days", param5);
            }
        }

    }

    private void drawOpen(GuiGraphics param0, int param1, int param2, int param3, int param4) {
        param0.blit(ON_ICON_LOCATION, param1, param2, 0.0F, 0.0F, 10, 28, 10, 28);
        if (param3 >= param1 && param3 <= param1 + 9 && param4 >= param2 && param4 <= param2 + 27) {
            this.toolTip = SERVER_OPEN_TOOLTIP;
        }

    }

    private void drawClose(GuiGraphics param0, int param1, int param2, int param3, int param4) {
        param0.blit(OFF_ICON_LOCATION, param1, param2, 0.0F, 0.0F, 10, 28, 10, 28);
        if (param3 >= param1 && param3 <= param1 + 9 && param4 >= param2 && param4 <= param2 + 27) {
            this.toolTip = SERVER_CLOSED_TOOLTIP;
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
        this.removeWidget(param0);
    }

    private void show(Button param0) {
        param0.visible = true;
        this.addRenderableWidget(param0);
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
        this.minecraft
            .setScreen(new RealmsLongRunningMcoTaskScreen(param1, new OpenServerTask(this.serverData, this, this.lastScreen, param0, this.minecraft)));
    }

    public void closeTheWorld(Screen param0) {
        this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(param0, new CloseServerTask(this.serverData, this)));
    }

    public void stateChanged() {
        this.stateChanged = true;
    }

    private void templateSelectionCallback(@Nullable WorldTemplate param0) {
        if (param0 != null && WorldTemplate.WorldTemplateType.MINIGAME == param0.type) {
            this.minecraft
                .setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new SwitchMinigameTask(this.serverData.id, param0, this.getNewScreen())));
        } else {
            this.minecraft.setScreen(this);
        }

    }

    public RealmsConfigureWorldScreen getNewScreen() {
        return new RealmsConfigureWorldScreen(this.lastScreen, this.serverId);
    }
}
