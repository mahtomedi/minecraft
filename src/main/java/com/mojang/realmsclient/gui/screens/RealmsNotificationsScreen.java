package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsNotificationsScreen extends RealmsScreen {
    private static final RealmsDataFetcher realmsDataFetcher = new RealmsDataFetcher();
    private volatile int numberOfPendingInvites;
    private static boolean checkedMcoAvailability;
    private static boolean trialAvailable;
    private static boolean validClient;
    private static boolean hasUnreadNews;
    private static final List<RealmsDataFetcher.Task> tasks = Arrays.asList(
        RealmsDataFetcher.Task.PENDING_INVITE, RealmsDataFetcher.Task.TRIAL_AVAILABLE, RealmsDataFetcher.Task.UNREAD_NEWS
    );

    public RealmsNotificationsScreen(RealmsScreen param0) {
    }

    @Override
    public void init() {
        this.checkIfMcoEnabled();
        this.setKeyboardHandlerSendRepeatsToGui(true);
    }

    @Override
    public void tick() {
        if ((!Realms.getRealmsNotificationsEnabled() || !Realms.inTitleScreen() || !validClient) && !realmsDataFetcher.isStopped()) {
            realmsDataFetcher.stop();
        } else if (validClient && Realms.getRealmsNotificationsEnabled()) {
            realmsDataFetcher.initWithSpecificTaskList(tasks);
            if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.PENDING_INVITE)) {
                this.numberOfPendingInvites = realmsDataFetcher.getPendingInvitesCount();
            }

            if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.TRIAL_AVAILABLE)) {
                trialAvailable = realmsDataFetcher.isTrialAvailable();
            }

            if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.UNREAD_NEWS)) {
                hasUnreadNews = realmsDataFetcher.hasUnreadNews();
            }

            realmsDataFetcher.markClean();
        }
    }

    private void checkIfMcoEnabled() {
        if (!checkedMcoAvailability) {
            checkedMcoAvailability = true;
            (new Thread("Realms Notification Availability checker #1") {
                @Override
                public void run() {
                    RealmsClient var0 = RealmsClient.createRealmsClient();

                    try {
                        RealmsClient.CompatibleVersionResponse var1 = var0.clientCompatible();
                        if (!var1.equals(RealmsClient.CompatibleVersionResponse.COMPATIBLE)) {
                            return;
                        }
                    } catch (RealmsServiceException var31) {
                        if (var31.httpResultCode != 401) {
                            RealmsNotificationsScreen.checkedMcoAvailability = false;
                        }

                        return;
                    } catch (IOException var4) {
                        RealmsNotificationsScreen.checkedMcoAvailability = false;
                        return;
                    }

                    RealmsNotificationsScreen.validClient = true;
                }
            }).start();
        }

    }

    @Override
    public void render(int param0, int param1, float param2) {
        if (validClient) {
            this.drawIcons(param0, param1);
        }

        super.render(param0, param1, param2);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        return super.mouseClicked(param0, param1, param2);
    }

    private void drawIcons(int param0, int param1) {
        int var0 = this.numberOfPendingInvites;
        int var1 = 24;
        int var2 = this.height() / 4 + 48;
        int var3 = this.width() / 2 + 80;
        int var4 = var2 + 48 + 2;
        int var5 = 0;
        if (hasUnreadNews) {
            RealmsScreen.bind("realms:textures/gui/realms/news_notification_mainscreen.png");
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.pushMatrix();
            GlStateManager.scalef(0.4F, 0.4F, 0.4F);
            RealmsScreen.blit((int)((double)(var3 + 2 - var5) * 2.5), (int)((double)var4 * 2.5), 0.0F, 0.0F, 40, 40, 40, 40);
            GlStateManager.popMatrix();
            var5 += 14;
        }

        if (var0 != 0) {
            RealmsScreen.bind("realms:textures/gui/realms/invite_icon.png");
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.pushMatrix();
            RealmsScreen.blit(var3 - var5, var4 - 6, 0.0F, 0.0F, 15, 25, 31, 25);
            GlStateManager.popMatrix();
            var5 += 16;
        }

        if (trialAvailable) {
            RealmsScreen.bind("realms:textures/gui/realms/trial_icon.png");
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.pushMatrix();
            int var6 = 0;
            if ((System.currentTimeMillis() / 800L & 1L) == 1L) {
                var6 = 8;
            }

            RealmsScreen.blit(var3 + 4 - var5, var4 + 4, 0.0F, (float)var6, 8, 8, 8, 16);
            GlStateManager.popMatrix();
        }

    }

    @Override
    public void removed() {
        realmsDataFetcher.stop();
    }
}
