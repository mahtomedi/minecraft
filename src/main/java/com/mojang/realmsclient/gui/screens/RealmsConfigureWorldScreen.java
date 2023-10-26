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
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
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
    private static final ResourceLocation EXPIRED_SPRITE = new ResourceLocation("realm_status/expired");
    private static final ResourceLocation EXPIRES_SOON_SPRITE = new ResourceLocation("realm_status/expires_soon");
    private static final ResourceLocation OPEN_SPRITE = new ResourceLocation("realm_status/open");
    private static final ResourceLocation CLOSED_SPRITE = new ResourceLocation("realm_status/closed");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component WORLD_LIST_TITLE = Component.translatable("mco.configure.worlds.title");
    private static final Component TITLE = Component.translatable("mco.configure.world.title");
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
                                RealmsResetWorldScreen.forResetSlot(
                                    this, this.serverData.clone(), () -> this.minecraft.execute(() -> this.minecraft.setScreen(this.getNewScreen()))
                                )
                            )
                )
                .bounds(this.leftButton(2), row(13) - 5, 90, 20)
                .build()
        );
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, param0 -> this.onClose()).bounds(this.rightX - 80 + 8, row(13) - 5, 70, 20).build());
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
        RealmsWorldSlotButton var2 = new RealmsWorldSlotButton(var0, var1, 80, 80, param0, param1 -> {
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

        });
        if (this.serverData != null) {
            var2.setServerData(this.serverData);
        }

        return this.addRenderableWidget(var2);
    }

    private int leftButton(int param0) {
        return this.leftX + param0 * 95;
    }

    private int centerButton(int param0, int param1) {
        return this.width / 2 - (param1 * 105 - 5) / 2 + param0 * 105;
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.toolTip = null;
        param0.drawCenteredString(this.font, WORLD_LIST_TITLE, this.width / 2, row(4), -1);
        if (this.serverData == null) {
            param0.drawCenteredString(this.font, this.title, this.width / 2, 17, -1);
        } else {
            String var0 = this.serverData.getName();
            int var1 = this.font.width(var0);
            int var2 = this.serverData.state == RealmsServer.State.CLOSED ? -6250336 : 8388479;
            int var3 = this.font.width(this.title);
            param0.drawCenteredString(this.font, this.title, this.width / 2, 12, -1);
            param0.drawCenteredString(this.font, var0, this.width / 2, 24, var2);
            int var4 = Math.min(this.centerButton(2, 3) + 80 - 11, this.width / 2 + var1 / 2 + var3 / 2 + 10);
            this.drawServerStatus(param0, var4, 7, param1, param2);
            if (this.isMinigame()) {
                param0.drawString(
                    this.font,
                    Component.translatable("mco.configure.world.minigame", this.serverData.getMinigameName()),
                    this.leftX + 80 + 20 + 10,
                    row(13),
                    -1,
                    false
                );
            }

        }
    }

    private int frame(int param0) {
        return this.leftX + (param0 - 1) * 98;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
        if (this.stateChanged) {
            this.lastScreen.resetScreen();
        }

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

                    for(RealmsWorldSlotButton var0x : this.slotButtonList) {
                        var0x.setServerData(var1);
                    }

                });
            } catch (RealmsServiceException var5) {
                LOGGER.error("Couldn't get own world", (Throwable)var5);
                this.minecraft.execute(() -> this.minecraft.setScreen(new RealmsGenericErrorScreen(var5, this.lastScreen)));
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
            RealmsMainScreen.play(param0, new RealmsConfigureWorldScreen(this.lastScreen, this.serverId));
        } else {
            this.openTheWorld(true, new RealmsConfigureWorldScreen(this.lastScreen, this.serverId));
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
                            this.stateChanged();
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
                    RealmsLongConfirmationScreen.Type.INFO,
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
                            this.stateChanged();
                            RealmsResetWorldScreen var0x = RealmsResetWorldScreen.forEmptySlot(
                                this, param0, param1, () -> this.minecraft.execute(() -> this.minecraft.setScreen(this.getNewScreen()))
                            );
                            this.minecraft.setScreen(var0x);
                        } else {
                            this.minecraft.setScreen(this);
                        }
            
                    },
                    RealmsLongConfirmationScreen.Type.INFO,
                    var0,
                    var1,
                    true
                )
            );
    }

    private void drawServerStatus(GuiGraphics param0, int param1, int param2, int param3, int param4) {
        if (this.serverData.expired) {
            this.drawRealmStatus(param0, param1, param2, param3, param4, EXPIRED_SPRITE, () -> SERVER_EXPIRED_TOOLTIP);
        } else if (this.serverData.state == RealmsServer.State.CLOSED) {
            this.drawRealmStatus(param0, param1, param2, param3, param4, CLOSED_SPRITE, () -> SERVER_CLOSED_TOOLTIP);
        } else if (this.serverData.state == RealmsServer.State.OPEN) {
            if (this.serverData.daysLeft < 7) {
                this.drawRealmStatus(
                    param0,
                    param1,
                    param2,
                    param3,
                    param4,
                    EXPIRES_SOON_SPRITE,
                    () -> {
                        if (this.serverData.daysLeft <= 0) {
                            return SERVER_EXPIRING_SOON_TOOLTIP;
                        } else {
                            return (Component)(this.serverData.daysLeft == 1
                                ? SERVER_EXPIRING_IN_DAY_TOOLTIP
                                : Component.translatable("mco.selectServer.expires.days", this.serverData.daysLeft));
                        }
                    }
                );
            } else {
                this.drawRealmStatus(param0, param1, param2, param3, param4, OPEN_SPRITE, () -> SERVER_OPEN_TOOLTIP);
            }
        }

    }

    private void drawRealmStatus(GuiGraphics param0, int param1, int param2, int param3, int param4, ResourceLocation param5, Supplier<Component> param6) {
        param0.blitSprite(param5, param1, param2, 10, 28);
        if (param3 >= param1 && param3 <= param1 + 9 && param4 >= param2 && param4 <= param2 + 27) {
            this.setTooltipForNextRenderPass(param6.get());
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
    }

    private void show(Button param0) {
        param0.visible = true;
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
            LOGGER.error("Couldn't save slot settings", (Throwable)var5);
            this.minecraft.setScreen(new RealmsGenericErrorScreen(var5, this));
            return;
        }

        this.minecraft.setScreen(this);
    }

    public void saveSettings(String param0, String param1) {
        String var0 = Util.isBlank(param1) ? null : param1;
        RealmsClient var1 = RealmsClient.create();

        try {
            var1.update(this.serverData.id, param0, var0);
            this.serverData.setName(param0);
            this.serverData.setDescription(var0);
            this.stateChanged();
        } catch (RealmsServiceException var6) {
            LOGGER.error("Couldn't save settings", (Throwable)var6);
            this.minecraft.setScreen(new RealmsGenericErrorScreen(var6, this));
            return;
        }

        this.minecraft.setScreen(this);
    }

    public void openTheWorld(boolean param0, Screen param1) {
        this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(param1, new OpenServerTask(this.serverData, this, param0, this.minecraft)));
    }

    public void closeTheWorld(Screen param0) {
        this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(param0, new CloseServerTask(this.serverData, this)));
    }

    public void stateChanged() {
        this.stateChanged = true;
    }

    private void templateSelectionCallback(@Nullable WorldTemplate param0) {
        if (param0 != null && WorldTemplate.WorldTemplateType.MINIGAME == param0.type) {
            this.stateChanged();
            this.minecraft
                .setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new SwitchMinigameTask(this.serverData.id, param0, this.getNewScreen())));
        } else {
            this.minecraft.setScreen(this);
        }

    }

    public RealmsConfigureWorldScreen getNewScreen() {
        RealmsConfigureWorldScreen var0 = new RealmsConfigureWorldScreen(this.lastScreen, this.serverId);
        var0.stateChanged = this.stateChanged;
        return var0;
    }
}
