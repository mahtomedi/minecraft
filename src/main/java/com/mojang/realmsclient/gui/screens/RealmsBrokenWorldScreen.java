package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsWorldSlotButton;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.task.OpenServerTask;
import com.mojang.realmsclient.util.task.SwitchSlotTask;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsBrokenWorldScreen extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_BUTTON_WIDTH = 80;
    private final Screen lastScreen;
    private final RealmsMainScreen mainScreen;
    @Nullable
    private RealmsServer serverData;
    private final long serverId;
    private final Component[] message = new Component[]{
        Component.translatable("mco.brokenworld.message.line1"), Component.translatable("mco.brokenworld.message.line2")
    };
    private int leftX;
    private int rightX;
    private final List<Integer> slotsThatHasBeenDownloaded = Lists.newArrayList();
    private int animTick;

    public RealmsBrokenWorldScreen(Screen param0, RealmsMainScreen param1, long param2, boolean param3) {
        super(param3 ? Component.translatable("mco.brokenworld.minigame.title") : Component.translatable("mco.brokenworld.title"));
        this.lastScreen = param0;
        this.mainScreen = param1;
        this.serverId = param2;
    }

    @Override
    public void init() {
        this.leftX = this.width / 2 - 150;
        this.rightX = this.width / 2 + 190;
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_BACK, param0 -> this.backButtonClicked()).bounds(this.rightX - 80 + 8, row(13) - 5, 70, 20).build()
        );
        if (this.serverData == null) {
            this.fetchServerData(this.serverId);
        } else {
            this.addButtons();
        }

    }

    @Override
    public Component getNarrationMessage() {
        return ComponentUtils.formatList(Stream.concat(Stream.of(this.title), Stream.of(this.message)).collect(Collectors.toList()), CommonComponents.SPACE);
    }

    private void addButtons() {
        for(Entry<Integer, RealmsWorldOptions> var0 : this.serverData.slots.entrySet()) {
            int var1 = var0.getKey();
            boolean var2 = var1 != this.serverData.activeSlot || this.serverData.worldType == RealmsServer.WorldType.MINIGAME;
            Button var3;
            if (var2) {
                var3 = Button.builder(
                        Component.translatable("mco.brokenworld.play"),
                        param1 -> {
                            if (this.serverData.slots.get(var1).empty) {
                                RealmsResetWorldScreen var0x = new RealmsResetWorldScreen(
                                    this,
                                    this.serverData,
                                    Component.translatable("mco.configure.world.switch.slot"),
                                    Component.translatable("mco.configure.world.switch.slot.subtitle"),
                                    10526880,
                                    CommonComponents.GUI_CANCEL,
                                    this::doSwitchOrReset,
                                    () -> {
                                        this.minecraft.setScreen(this);
                                        this.doSwitchOrReset();
                                    }
                                );
                                var0x.setSlot(var1);
                                var0x.setResetTitle(Component.translatable("mco.create.world.reset.title"));
                                this.minecraft.setScreen(var0x);
                            } else {
                                this.minecraft
                                    .setScreen(
                                        new RealmsLongRunningMcoTaskScreen(this.lastScreen, new SwitchSlotTask(this.serverData.id, var1, this::doSwitchOrReset))
                                    );
                            }
        
                        }
                    )
                    .bounds(this.getFramePositionX(var1), row(8), 80, 20)
                    .build();
            } else {
                var3 = Button.builder(Component.translatable("mco.brokenworld.download"), param1 -> {
                    Component var0x = Component.translatable("mco.configure.world.restore.download.question.line1");
                    Component var1x = Component.translatable("mco.configure.world.restore.download.question.line2");
                    this.minecraft.setScreen(new RealmsLongConfirmationScreen(param1x -> {
                        if (param1x) {
                            this.downloadWorld(var1);
                        } else {
                            this.minecraft.setScreen(this);
                        }

                    }, RealmsLongConfirmationScreen.Type.Info, var0x, var1x, true));
                }).bounds(this.getFramePositionX(var1), row(8), 80, 20).build();
            }

            if (this.slotsThatHasBeenDownloaded.contains(var1)) {
                var3.active = false;
                var3.setMessage(Component.translatable("mco.brokenworld.downloaded"));
            }

            this.addRenderableWidget(var3);
            this.addRenderableWidget(Button.builder(Component.translatable("mco.brokenworld.reset"), param1 -> {
                RealmsResetWorldScreen var0x = new RealmsResetWorldScreen(this, this.serverData, this::doSwitchOrReset, () -> {
                    this.minecraft.setScreen(this);
                    this.doSwitchOrReset();
                });
                if (var1 != this.serverData.activeSlot || this.serverData.worldType == RealmsServer.WorldType.MINIGAME) {
                    var0x.setSlot(var1);
                }

                this.minecraft.setScreen(var0x);
            }).bounds(this.getFramePositionX(var1), row(10), 80, 20).build());
        }

    }

    @Override
    public void tick() {
        ++this.animTick;
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 17, 16777215);

        for(int var0 = 0; var0 < this.message.length; ++var0) {
            param0.drawCenteredString(this.font, this.message[var0], this.width / 2, row(-1) + 3 + var0 * 12, 10526880);
        }

        if (this.serverData != null) {
            for(Entry<Integer, RealmsWorldOptions> var1 : this.serverData.slots.entrySet()) {
                if (var1.getValue().templateImage != null && var1.getValue().templateId != -1L) {
                    this.drawSlotFrame(
                        param0,
                        this.getFramePositionX(var1.getKey()),
                        row(1) + 5,
                        param1,
                        param2,
                        this.serverData.activeSlot == var1.getKey() && !this.isMinigame(),
                        var1.getValue().getSlotName(var1.getKey()),
                        var1.getKey(),
                        var1.getValue().templateId,
                        var1.getValue().templateImage,
                        var1.getValue().empty
                    );
                } else {
                    this.drawSlotFrame(
                        param0,
                        this.getFramePositionX(var1.getKey()),
                        row(1) + 5,
                        param1,
                        param2,
                        this.serverData.activeSlot == var1.getKey() && !this.isMinigame(),
                        var1.getValue().getSlotName(var1.getKey()),
                        var1.getKey(),
                        -1L,
                        null,
                        var1.getValue().empty
                    );
                }
            }

        }
    }

    private int getFramePositionX(int param0) {
        return this.leftX + (param0 - 1) * 110;
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
        this.minecraft.setScreen(this.lastScreen);
    }

    private void fetchServerData(long param0) {
        new Thread(() -> {
            RealmsClient var0 = RealmsClient.create();

            try {
                this.serverData = var0.getOwnWorld(param0);
                this.addButtons();
            } catch (RealmsServiceException var5) {
                LOGGER.error("Couldn't get own world");
                this.minecraft.setScreen(new RealmsGenericErrorScreen(Component.nullToEmpty(var5.getMessage()), this.lastScreen));
            }

        }).start();
    }

    public void doSwitchOrReset() {
        new Thread(
                () -> {
                    RealmsClient var0 = RealmsClient.create();
                    if (this.serverData.state == RealmsServer.State.CLOSED) {
                        this.minecraft
                            .execute(
                                () -> this.minecraft
                                        .setScreen(
                                            new RealmsLongRunningMcoTaskScreen(
                                                this, new OpenServerTask(this.serverData, this, this.mainScreen, true, this.minecraft)
                                            )
                                        )
                            );
                    } else {
                        try {
                            RealmsServer var1 = var0.getOwnWorld(this.serverId);
                            this.minecraft.execute(() -> this.mainScreen.newScreen().play(var1, this));
                        } catch (RealmsServiceException var3) {
                            LOGGER.error("Couldn't get own world");
                            this.minecraft.execute(() -> this.minecraft.setScreen(this.lastScreen));
                        }
                    }
        
                }
            )
            .start();
    }

    private void downloadWorld(int param0) {
        RealmsClient var0 = RealmsClient.create();

        try {
            WorldDownload var1 = var0.requestDownloadInfo(this.serverData.id, param0);
            RealmsDownloadLatestWorldScreen var2 = new RealmsDownloadLatestWorldScreen(this, var1, this.serverData.getWorldName(param0), param1 -> {
                if (param1) {
                    this.slotsThatHasBeenDownloaded.add(param0);
                    this.clearWidgets();
                    this.addButtons();
                } else {
                    this.minecraft.setScreen(this);
                }

            });
            this.minecraft.setScreen(var2);
        } catch (RealmsServiceException var5) {
            LOGGER.error("Couldn't download world data");
            this.minecraft.setScreen(new RealmsGenericErrorScreen(var5, this));
        }

    }

    private boolean isMinigame() {
        return this.serverData != null && this.serverData.worldType == RealmsServer.WorldType.MINIGAME;
    }

    private void drawSlotFrame(
        GuiGraphics param0,
        int param1,
        int param2,
        int param3,
        int param4,
        boolean param5,
        String param6,
        int param7,
        long param8,
        @Nullable String param9,
        boolean param10
    ) {
        ResourceLocation var0;
        if (param10) {
            var0 = RealmsWorldSlotButton.EMPTY_SLOT_LOCATION;
        } else if (param9 != null && param8 != -1L) {
            var0 = RealmsTextureManager.worldTemplate(String.valueOf(param8), param9);
        } else if (param7 == 1) {
            var0 = RealmsWorldSlotButton.DEFAULT_WORLD_SLOT_1;
        } else if (param7 == 2) {
            var0 = RealmsWorldSlotButton.DEFAULT_WORLD_SLOT_2;
        } else if (param7 == 3) {
            var0 = RealmsWorldSlotButton.DEFAULT_WORLD_SLOT_3;
        } else {
            var0 = RealmsTextureManager.worldTemplate(String.valueOf(this.serverData.minigameId), this.serverData.minigameImage);
        }

        if (!param5) {
            param0.setColor(0.56F, 0.56F, 0.56F, 1.0F);
        } else if (param5) {
            float var6 = 0.9F + 0.1F * Mth.cos((float)this.animTick * 0.2F);
            param0.setColor(var6, var6, var6, 1.0F);
        }

        param0.blit(var0, param1 + 3, param2 + 3, 0.0F, 0.0F, 74, 74, 74, 74);
        if (param5) {
            param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            param0.setColor(0.56F, 0.56F, 0.56F, 1.0F);
        }

        param0.blit(RealmsWorldSlotButton.SLOT_FRAME_LOCATION, param1, param2, 0.0F, 0.0F, 80, 80, 80, 80);
        param0.drawCenteredString(this.font, param6, param1 + 40, param2 + 66, 16777215);
        param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
