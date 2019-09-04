package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.util.RealmsTasks;
import com.mojang.realmsclient.util.RealmsTextureManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsBrokenWorldScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RealmsScreen lastScreen;
    private final RealmsMainScreen mainScreen;
    private RealmsServer serverData;
    private final long serverId;
    private String title = getLocalizedString("mco.brokenworld.title");
    private final String message = getLocalizedString("mco.brokenworld.message.line1") + "\\n" + getLocalizedString("mco.brokenworld.message.line2");
    private int left_x;
    private int right_x;
    private final int default_button_width = 80;
    private final int default_button_offset = 5;
    private static final List<Integer> playButtonIds = Arrays.asList(1, 2, 3);
    private static final List<Integer> resetButtonIds = Arrays.asList(4, 5, 6);
    private static final List<Integer> downloadButtonIds = Arrays.asList(7, 8, 9);
    private static final List<Integer> downloadConfirmationIds = Arrays.asList(10, 11, 12);
    private final List<Integer> slotsThatHasBeenDownloaded = Lists.newArrayList();
    private int animTick;

    public RealmsBrokenWorldScreen(RealmsScreen param0, RealmsMainScreen param1, long param2) {
        this.lastScreen = param0;
        this.mainScreen = param1;
        this.serverId = param2;
    }

    public void setTitle(String param0) {
        this.title = param0;
    }

    @Override
    public void init() {
        this.left_x = this.width() / 2 - 150;
        this.right_x = this.width() / 2 + 190;
        this.buttonsAdd(new RealmsButton(0, this.right_x - 80 + 8, RealmsConstants.row(13) - 5, 70, 20, getLocalizedString("gui.back")) {
            @Override
            public void onPress() {
                RealmsBrokenWorldScreen.this.backButtonClicked();
            }
        });
        if (this.serverData == null) {
            this.fetchServerData(this.serverId);
        } else {
            this.addButtons();
        }

        this.setKeyboardHandlerSendRepeatsToGui(true);
    }

    public void addButtons() {
        for(Entry<Integer, RealmsWorldOptions> var0 : this.serverData.slots.entrySet()) {
            RealmsWorldOptions var1 = var0.getValue();
            boolean var2 = var0.getKey() != this.serverData.activeSlot || this.serverData.worldType.equals(RealmsServer.WorldType.MINIGAME);
            RealmsButton var3;
            if (var2) {
                var3 = new RealmsBrokenWorldScreen.PlayButton(
                    playButtonIds.get(var0.getKey() - 1), this.getFramePositionX(var0.getKey()), getLocalizedString("mco.brokenworld.play")
                );
            } else {
                var3 = new RealmsBrokenWorldScreen.DownloadButton(
                    downloadButtonIds.get(var0.getKey() - 1), this.getFramePositionX(var0.getKey()), getLocalizedString("mco.brokenworld.download")
                );
            }

            if (this.slotsThatHasBeenDownloaded.contains(var0.getKey())) {
                var3.active(false);
                var3.setMessage(getLocalizedString("mco.brokenworld.downloaded"));
            }

            this.buttonsAdd(var3);
            this.buttonsAdd(
                new RealmsButton(
                    resetButtonIds.get(var0.getKey() - 1),
                    this.getFramePositionX(var0.getKey()),
                    RealmsConstants.row(10),
                    80,
                    20,
                    getLocalizedString("mco.brokenworld.reset")
                ) {
                    @Override
                    public void onPress() {
                        int var0 = RealmsBrokenWorldScreen.resetButtonIds.indexOf(this.id()) + 1;
                        RealmsResetWorldScreen var1 = new RealmsResetWorldScreen(
                            RealmsBrokenWorldScreen.this, RealmsBrokenWorldScreen.this.serverData, RealmsBrokenWorldScreen.this
                        );
                        if (var0 != RealmsBrokenWorldScreen.this.serverData.activeSlot
                            || RealmsBrokenWorldScreen.this.serverData.worldType.equals(RealmsServer.WorldType.MINIGAME)) {
                            var1.setSlot(var0);
                        }
    
                        var1.setConfirmationId(14);
                        Realms.setScreen(var1);
                    }
                }
            );
        }

    }

    @Override
    public void tick() {
        ++this.animTick;
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        super.render(param0, param1, param2);
        this.drawCenteredString(this.title, this.width() / 2, 17, 16777215);
        String[] var0 = this.message.split("\\\\n");

        for(int var1 = 0; var1 < var0.length; ++var1) {
            this.drawCenteredString(var0[var1], this.width() / 2, RealmsConstants.row(-1) + 3 + var1 * 12, 10526880);
        }

        if (this.serverData != null) {
            for(Entry<Integer, RealmsWorldOptions> var2 : this.serverData.slots.entrySet()) {
                if (var2.getValue().templateImage != null && var2.getValue().templateId != -1L) {
                    this.drawSlotFrame(
                        this.getFramePositionX(var2.getKey()),
                        RealmsConstants.row(1) + 5,
                        param0,
                        param1,
                        this.serverData.activeSlot == var2.getKey() && !this.isMinigame(),
                        var2.getValue().getSlotName(var2.getKey()),
                        var2.getKey(),
                        var2.getValue().templateId,
                        var2.getValue().templateImage,
                        var2.getValue().empty
                    );
                } else {
                    this.drawSlotFrame(
                        this.getFramePositionX(var2.getKey()),
                        RealmsConstants.row(1) + 5,
                        param0,
                        param1,
                        this.serverData.activeSlot == var2.getKey() && !this.isMinigame(),
                        var2.getValue().getSlotName(var2.getKey()),
                        var2.getKey(),
                        -1L,
                        null,
                        var2.getValue().empty
                    );
                }
            }

        }
    }

    private int getFramePositionX(int param0) {
        return this.left_x + (param0 - 1) * 110;
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
        Realms.setScreen(this.lastScreen);
    }

    private void fetchServerData(long param0) {
        new Thread(() -> {
            RealmsClient var0 = RealmsClient.createRealmsClient();

            try {
                this.serverData = var0.getOwnWorld(param0);
                this.addButtons();
            } catch (RealmsServiceException var5) {
                LOGGER.error("Couldn't get own world");
                Realms.setScreen(new RealmsGenericErrorScreen(var5.getMessage(), this.lastScreen));
            } catch (IOException var6) {
                LOGGER.error("Couldn't parse response getting own world");
            }

        }).start();
    }

    @Override
    public void confirmResult(boolean param0, int param1) {
        if (!param0) {
            Realms.setScreen(this);
        } else {
            if (param1 != 13 && param1 != 14) {
                if (downloadButtonIds.contains(param1)) {
                    this.downloadWorld(downloadButtonIds.indexOf(param1) + 1);
                } else if (downloadConfirmationIds.contains(param1)) {
                    this.slotsThatHasBeenDownloaded.add(downloadConfirmationIds.indexOf(param1) + 1);
                    this.childrenClear();
                    this.addButtons();
                }
            } else {
                new Thread(() -> {
                    RealmsClient var0 = RealmsClient.createRealmsClient();
                    if (this.serverData.state.equals(RealmsServer.State.CLOSED)) {
                        RealmsTasks.OpenServerTask var1 = new RealmsTasks.OpenServerTask(this.serverData, this, this.lastScreen, true);
                        RealmsLongRunningMcoTaskScreen var2x = new RealmsLongRunningMcoTaskScreen(this, var1);
                        var2x.start();
                        Realms.setScreen(var2x);
                    } else {
                        try {
                            this.mainScreen.newScreen().play(var0.getOwnWorld(this.serverId), this);
                        } catch (RealmsServiceException var41) {
                            LOGGER.error("Couldn't get own world");
                            Realms.setScreen(this.lastScreen);
                        } catch (IOException var5) {
                            LOGGER.error("Couldn't parse response getting own world");
                            Realms.setScreen(this.lastScreen);
                        }
                    }

                }).start();
            }

        }
    }

    private void downloadWorld(int param0) {
        RealmsClient var0 = RealmsClient.createRealmsClient();

        try {
            WorldDownload var1 = var0.download(this.serverData.id, param0);
            RealmsDownloadLatestWorldScreen var2 = new RealmsDownloadLatestWorldScreen(
                this, var1, this.serverData.name + " (" + this.serverData.slots.get(param0).getSlotName(param0) + ")"
            );
            var2.setConfirmationId(downloadConfirmationIds.get(param0 - 1));
            Realms.setScreen(var2);
        } catch (RealmsServiceException var5) {
            LOGGER.error("Couldn't download world data");
            Realms.setScreen(new RealmsGenericErrorScreen(var5, this));
        }

    }

    private boolean isMinigame() {
        return this.serverData != null && this.serverData.worldType.equals(RealmsServer.WorldType.MINIGAME);
    }

    private void drawSlotFrame(
        int param0, int param1, int param2, int param3, boolean param4, String param5, int param6, long param7, String param8, boolean param9
    ) {
        if (param9) {
            bind("realms:textures/gui/realms/empty_frame.png");
        } else if (param8 != null && param7 != -1L) {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(param7), param8);
        } else if (param6 == 1) {
            bind("textures/gui/title/background/panorama_0.png");
        } else if (param6 == 2) {
            bind("textures/gui/title/background/panorama_2.png");
        } else if (param6 == 3) {
            bind("textures/gui/title/background/panorama_3.png");
        } else {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(this.serverData.minigameId), this.serverData.minigameImage);
        }

        if (!param4) {
            RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
        } else if (param4) {
            float var0 = 0.9F + 0.1F * RealmsMth.cos((float)this.animTick * 0.2F);
            RenderSystem.color4f(var0, var0, var0, 1.0F);
        }

        RealmsScreen.blit(param0 + 3, param1 + 3, 0.0F, 0.0F, 74, 74, 74, 74);
        bind("realms:textures/gui/realms/slot_frame.png");
        if (param4) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
        }

        RealmsScreen.blit(param0, param1, 0.0F, 0.0F, 80, 80, 80, 80);
        this.drawCenteredString(param5, param0 + 40, param1 + 66, 16777215);
    }

    private void switchSlot(int param0) {
        RealmsTasks.SwitchSlotTask var0 = new RealmsTasks.SwitchSlotTask(this.serverData.id, param0, this, 13);
        RealmsLongRunningMcoTaskScreen var1 = new RealmsLongRunningMcoTaskScreen(this.lastScreen, var0);
        var1.start();
        Realms.setScreen(var1);
    }

    @OnlyIn(Dist.CLIENT)
    class DownloadButton extends RealmsButton {
        public DownloadButton(int param0, int param1, String param2) {
            super(param0, param1, RealmsConstants.row(8), 80, 20, param2);
        }

        @Override
        public void onPress() {
            String var0 = RealmsScreen.getLocalizedString("mco.configure.world.restore.download.question.line1");
            String var1 = RealmsScreen.getLocalizedString("mco.configure.world.restore.download.question.line2");
            Realms.setScreen(
                new RealmsLongConfirmationScreen(RealmsBrokenWorldScreen.this, RealmsLongConfirmationScreen.Type.Info, var0, var1, true, this.id())
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    class PlayButton extends RealmsButton {
        public PlayButton(int param0, int param1, String param2) {
            super(param0, param1, RealmsConstants.row(8), 80, 20, param2);
        }

        @Override
        public void onPress() {
            int var0 = RealmsBrokenWorldScreen.playButtonIds.indexOf(this.id()) + 1;
            if (RealmsBrokenWorldScreen.this.serverData.slots.get(var0).empty) {
                RealmsResetWorldScreen var1 = new RealmsResetWorldScreen(
                    RealmsBrokenWorldScreen.this,
                    RealmsBrokenWorldScreen.this.serverData,
                    RealmsBrokenWorldScreen.this,
                    RealmsScreen.getLocalizedString("mco.configure.world.switch.slot"),
                    RealmsScreen.getLocalizedString("mco.configure.world.switch.slot.subtitle"),
                    10526880,
                    RealmsScreen.getLocalizedString("gui.cancel")
                );
                var1.setSlot(var0);
                var1.setResetTitle(RealmsScreen.getLocalizedString("mco.create.world.reset.title"));
                var1.setConfirmationId(14);
                Realms.setScreen(var1);
            } else {
                RealmsBrokenWorldScreen.this.switchSlot(var0);
            }

        }
    }
}
