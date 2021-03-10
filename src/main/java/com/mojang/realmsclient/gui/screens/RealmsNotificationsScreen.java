package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsNotificationsScreen extends RealmsScreen {
    private static final ResourceLocation INVITE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invite_icon.png");
    private static final ResourceLocation TRIAL_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/trial_icon.png");
    private static final ResourceLocation NEWS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/news_notification_mainscreen.png");
    private static final RealmsDataFetcher REALMS_DATA_FETCHER = new RealmsDataFetcher(Minecraft.getInstance(), RealmsClient.create());
    private volatile int numberOfPendingInvites;
    private static boolean checkedMcoAvailability;
    private static boolean trialAvailable;
    private static boolean validClient;
    private static boolean hasUnreadNews;

    @Override
    public void init() {
        this.checkIfMcoEnabled();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
    }

    @Override
    public void tick() {
        if ((!this.getRealmsNotificationsEnabled() || !this.inTitleScreen() || !validClient) && !REALMS_DATA_FETCHER.isStopped()) {
            REALMS_DATA_FETCHER.stop();
        } else if (validClient && this.getRealmsNotificationsEnabled()) {
            REALMS_DATA_FETCHER.initWithSpecificTaskList();
            if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.PENDING_INVITE)) {
                this.numberOfPendingInvites = REALMS_DATA_FETCHER.getPendingInvitesCount();
            }

            if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.TRIAL_AVAILABLE)) {
                trialAvailable = REALMS_DATA_FETCHER.isTrialAvailable();
            }

            if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.UNREAD_NEWS)) {
                hasUnreadNews = REALMS_DATA_FETCHER.hasUnreadNews();
            }

            REALMS_DATA_FETCHER.markClean();
        }
    }

    private boolean getRealmsNotificationsEnabled() {
        return this.minecraft.options.realmsNotifications;
    }

    private boolean inTitleScreen() {
        return this.minecraft.screen instanceof TitleScreen;
    }

    private void checkIfMcoEnabled() {
        if (!checkedMcoAvailability) {
            checkedMcoAvailability = true;
            (new Thread("Realms Notification Availability checker #1") {
                @Override
                public void run() {
                    RealmsClient var0 = RealmsClient.create();

                    try {
                        RealmsClient.CompatibleVersionResponse var1 = var0.clientCompatible();
                        if (var1 != RealmsClient.CompatibleVersionResponse.COMPATIBLE) {
                            return;
                        }
                    } catch (RealmsServiceException var3) {
                        if (var3.httpResultCode != 401) {
                            RealmsNotificationsScreen.checkedMcoAvailability = false;
                        }

                        return;
                    }

                    RealmsNotificationsScreen.validClient = true;
                }
            }).start();
        }

    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        if (validClient) {
            this.drawIcons(param0, param1, param2);
        }

        super.render(param0, param1, param2, param3);
    }

    private void drawIcons(PoseStack param0, int param1, int param2) {
        int var0 = this.numberOfPendingInvites;
        int var1 = 24;
        int var2 = this.height / 4 + 48;
        int var3 = this.width / 2 + 80;
        int var4 = var2 + 48 + 2;
        int var5 = 0;
        if (hasUnreadNews) {
            RenderSystem.setShaderTexture(0, NEWS_ICON_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            param0.pushPose();
            param0.scale(0.4F, 0.4F, 0.4F);
            GuiComponent.blit(param0, (int)((double)(var3 + 2 - var5) * 2.5), (int)((double)var4 * 2.5), 0.0F, 0.0F, 40, 40, 40, 40);
            param0.popPose();
            var5 += 14;
        }

        if (var0 != 0) {
            RenderSystem.setShaderTexture(0, INVITE_ICON_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(param0, var3 - var5, var4 - 6, 0.0F, 0.0F, 15, 25, 31, 25);
            var5 += 16;
        }

        if (trialAvailable) {
            RenderSystem.setShaderTexture(0, TRIAL_ICON_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int var6 = 0;
            if ((Util.getMillis() / 800L & 1L) == 1L) {
                var6 = 8;
            }

            GuiComponent.blit(param0, var3 + 4 - var5, var4 + 4, 0.0F, (float)var6, 8, 8, 8, 16);
        }

    }

    @Override
    public void removed() {
        REALMS_DATA_FETCHER.stop();
    }
}
