package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsBrokenWorldScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Screen lastScreen;
    private final RealmsMainScreen mainScreen;
    private RealmsServer serverData;
    private final long serverId;
    private String title = I18n.get("mco.brokenworld.title");
    private final String message = I18n.get("mco.brokenworld.message.line1") + "\\n" + I18n.get("mco.brokenworld.message.line2");
    private int leftX;
    private int rightX;
    private final List<Integer> slotsThatHasBeenDownloaded = Lists.newArrayList();
    private int animTick;

    public RealmsBrokenWorldScreen(Screen param0, RealmsMainScreen param1, long param2) {
        this.lastScreen = param0;
        this.mainScreen = param1;
        this.serverId = param2;
    }

    public void setTitle(String param0) {
        this.title = param0;
    }

    @Override
    public void init() {
        this.leftX = this.width / 2 - 150;
        this.rightX = this.width / 2 + 190;
        this.addButton(new Button(this.rightX - 80 + 8, row(13) - 5, 70, 20, I18n.get("gui.back"), param0 -> this.backButtonClicked()));
        if (this.serverData == null) {
            this.fetchServerData(this.serverId);
        } else {
            this.addButtons();
        }

        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
    }

    public void addButtons() {
        for(Entry<Integer, RealmsWorldOptions> var0 : this.serverData.slots.entrySet()) {
            int var1 = var0.getKey();
            boolean var2 = var1 != this.serverData.activeSlot || this.serverData.worldType == RealmsServer.WorldType.MINIGAME;
            Button var3;
            if (var2) {
                var3 = new Button(
                    this.getFramePositionX(var1),
                    row(8),
                    80,
                    20,
                    I18n.get("mco.brokenworld.play"),
                    param1 -> {
                        if (this.serverData.slots.get(var1).empty) {
                            RealmsResetWorldScreen var0x = new RealmsResetWorldScreen(
                                this,
                                this.serverData,
                                I18n.get("mco.configure.world.switch.slot"),
                                I18n.get("mco.configure.world.switch.slot.subtitle"),
                                10526880,
                                I18n.get("gui.cancel"),
                                this::doSwitchOrReset,
                                () -> {
                                    this.minecraft.setScreen(this);
                                    this.doSwitchOrReset();
                                }
                            );
                            var0x.setSlot(var1);
                            var0x.setResetTitle(I18n.get("mco.create.world.reset.title"));
                            this.minecraft.setScreen(var0x);
                        } else {
                            this.minecraft
                                .setScreen(
                                    new RealmsLongRunningMcoTaskScreen(this.lastScreen, new SwitchSlotTask(this.serverData.id, var1, this::doSwitchOrReset))
                                );
                        }
    
                    }
                );
            } else {
                var3 = new Button(this.getFramePositionX(var1), row(8), 80, 20, I18n.get("mco.brokenworld.download"), param1 -> {
                    String var0x = I18n.get("mco.configure.world.restore.download.question.line1");
                    String var1x = I18n.get("mco.configure.world.restore.download.question.line2");
                    this.minecraft.setScreen(new RealmsLongConfirmationScreen(param1x -> {
                        if (param1x) {
                            this.downloadWorld(var1);
                        } else {
                            this.minecraft.setScreen(this);
                        }

                    }, RealmsLongConfirmationScreen.Type.Info, var0x, var1x, true));
                });
            }

            if (this.slotsThatHasBeenDownloaded.contains(var1)) {
                var3.active = false;
                var3.setMessage(I18n.get("mco.brokenworld.downloaded"));
            }

            this.addButton(var3);
            this.addButton(new Button(this.getFramePositionX(var1), row(10), 80, 20, I18n.get("mco.brokenworld.reset"), param1 -> {
                RealmsResetWorldScreen var0x = new RealmsResetWorldScreen(this, this.serverData, this::doSwitchOrReset, () -> {
                    this.minecraft.setScreen(this);
                    this.doSwitchOrReset();
                });
                if (var1 != this.serverData.activeSlot || this.serverData.worldType == RealmsServer.WorldType.MINIGAME) {
                    var0x.setSlot(var1);
                }

                this.minecraft.setScreen(var0x);
            }));
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
        this.drawCenteredString(this.font, this.title, this.width / 2, 17, 16777215);
        String[] var0 = this.message.split("\\\\n");

        for(int var1 = 0; var1 < var0.length; ++var1) {
            this.drawCenteredString(this.font, var0[var1], this.width / 2, row(-1) + 3 + var1 * 12, 10526880);
        }

        if (this.serverData != null) {
            for(Entry<Integer, RealmsWorldOptions> var2 : this.serverData.slots.entrySet()) {
                if (var2.getValue().templateImage != null && var2.getValue().templateId != -1L) {
                    this.drawSlotFrame(
                        this.getFramePositionX(var2.getKey()),
                        row(1) + 5,
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
                        row(1) + 5,
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
        return this.leftX + (param0 - 1) * 110;
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
                this.minecraft.setScreen(new RealmsGenericErrorScreen(var5.getMessage(), this.lastScreen));
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
                                        .setScreen(new RealmsLongRunningMcoTaskScreen(this, new OpenServerTask(this.serverData, this, this.mainScreen, true)))
                            );
                    } else {
                        try {
                            this.mainScreen.newScreen().play(var0.getOwnWorld(this.serverId), this);
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
            WorldDownload var1 = var0.download(this.serverData.id, param0);
            RealmsDownloadLatestWorldScreen var2 = new RealmsDownloadLatestWorldScreen(this, var1, this.serverData.getWorldName(param0), param1 -> {
                if (param1) {
                    this.slotsThatHasBeenDownloaded.add(param0);
                    this.children.clear();
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
        int param0, int param1, int param2, int param3, boolean param4, String param5, int param6, long param7, String param8, boolean param9
    ) {
        if (param9) {
            this.minecraft.getTextureManager().bind(RealmsWorldSlotButton.EMPTY_SLOT_LOCATION);
        } else if (param8 != null && param7 != -1L) {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(param7), param8);
        } else if (param6 == 1) {
            this.minecraft.getTextureManager().bind(RealmsWorldSlotButton.DEFAULT_WORLD_SLOT_1);
        } else if (param6 == 2) {
            this.minecraft.getTextureManager().bind(RealmsWorldSlotButton.DEFAULT_WORLD_SLOT_2);
        } else if (param6 == 3) {
            this.minecraft.getTextureManager().bind(RealmsWorldSlotButton.DEFAULT_WORLD_SLOT_3);
        } else {
            RealmsTextureManager.bindWorldTemplate(String.valueOf(this.serverData.minigameId), this.serverData.minigameImage);
        }

        if (!param4) {
            RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
        } else if (param4) {
            float var0 = 0.9F + 0.1F * Mth.cos((float)this.animTick * 0.2F);
            RenderSystem.color4f(var0, var0, var0, 1.0F);
        }

        GuiComponent.blit(param0 + 3, param1 + 3, 0.0F, 0.0F, 74, 74, 74, 74);
        this.minecraft.getTextureManager().bind(RealmsWorldSlotButton.SLOT_FRAME_LOCATION);
        if (param4) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            RenderSystem.color4f(0.56F, 0.56F, 0.56F, 1.0F);
        }

        GuiComponent.blit(param0, param1, 0.0F, 0.0F, 80, 80, 80, 80);
        this.drawCenteredString(this.font, param5, param0 + 40, param1 + 66, 16777215);
    }
}
