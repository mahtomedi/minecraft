package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsNotification;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.task.DataFetcher;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
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
    private static final ResourceLocation UNSEEN_NOTIFICATION_ICON_LOCATION = new ResourceLocation("minecraft", "textures/gui/unseen_notification.png");
    @Nullable
    private DataFetcher.Subscription realmsDataSubscription;
    @Nullable
    private RealmsNotificationsScreen.DataFetcherConfiguration currentConfiguration;
    private volatile int numberOfPendingInvites;
    static boolean checkedMcoAvailability;
    private static boolean trialAvailable;
    static boolean validClient;
    private static boolean hasUnreadNews;
    private static boolean hasUnseenNotifications;
    private final RealmsNotificationsScreen.DataFetcherConfiguration showAll = new RealmsNotificationsScreen.DataFetcherConfiguration() {
        @Override
        public DataFetcher.Subscription initDataFetcher(RealmsDataFetcher param0) {
            DataFetcher.Subscription var0 = param0.dataFetcher.createSubscription();
            RealmsNotificationsScreen.this.addNewsAndInvitesSubscriptions(param0, var0);
            RealmsNotificationsScreen.this.addNotificationsSubscriptions(param0, var0);
            return var0;
        }

        @Override
        public boolean showOldNotifications() {
            return true;
        }
    };
    private final RealmsNotificationsScreen.DataFetcherConfiguration onlyNotifications = new RealmsNotificationsScreen.DataFetcherConfiguration() {
        @Override
        public DataFetcher.Subscription initDataFetcher(RealmsDataFetcher param0) {
            DataFetcher.Subscription var0 = param0.dataFetcher.createSubscription();
            RealmsNotificationsScreen.this.addNotificationsSubscriptions(param0, var0);
            return var0;
        }

        @Override
        public boolean showOldNotifications() {
            return false;
        }
    };

    public RealmsNotificationsScreen() {
        super(GameNarrator.NO_TITLE);
    }

    @Override
    public void init() {
        this.checkIfMcoEnabled();
        if (this.realmsDataSubscription != null) {
            this.realmsDataSubscription.forceUpdate();
        }

    }

    @Override
    public void added() {
        super.added();
        this.minecraft.realmsDataFetcher().notificationsTask.reset();
    }

    @Nullable
    private RealmsNotificationsScreen.DataFetcherConfiguration getConfiguration() {
        boolean var0 = this.inTitleScreen() && validClient;
        if (!var0) {
            return null;
        } else {
            return this.getRealmsNotificationsEnabled() ? this.showAll : this.onlyNotifications;
        }
    }

    @Override
    public void tick() {
        RealmsNotificationsScreen.DataFetcherConfiguration var0 = this.getConfiguration();
        if (!Objects.equals(this.currentConfiguration, var0)) {
            this.currentConfiguration = var0;
            if (this.currentConfiguration != null) {
                this.realmsDataSubscription = this.currentConfiguration.initDataFetcher(this.minecraft.realmsDataFetcher());
            } else {
                this.realmsDataSubscription = null;
            }
        }

        if (this.realmsDataSubscription != null) {
            this.realmsDataSubscription.tick();
        }

    }

    private boolean getRealmsNotificationsEnabled() {
        return this.minecraft.options.realmsNotifications().get();
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
        if (hasUnseenNotifications) {
            RenderSystem.setShaderTexture(0, UNSEEN_NOTIFICATION_ICON_LOCATION);
            GuiComponent.blit(param0, var3 - var5 + 5, var4 + 3, 0.0F, 0.0F, 10, 10, 10, 10);
            var5 += 14;
        }

        if (this.currentConfiguration != null && this.currentConfiguration.showOldNotifications()) {
            if (hasUnreadNews) {
                RenderSystem.setShaderTexture(0, NEWS_ICON_LOCATION);
                param0.pushPose();
                param0.scale(0.4F, 0.4F, 0.4F);
                GuiComponent.blit(param0, (int)((double)(var3 + 2 - var5) * 2.5), (int)((double)var4 * 2.5), 0.0F, 0.0F, 40, 40, 40, 40);
                param0.popPose();
                var5 += 14;
            }

            if (var0 != 0) {
                RenderSystem.setShaderTexture(0, INVITE_ICON_LOCATION);
                GuiComponent.blit(param0, var3 - var5, var4 - 6, 0.0F, 0.0F, 15, 25, 31, 25);
                var5 += 16;
            }

            if (trialAvailable) {
                RenderSystem.setShaderTexture(0, TRIAL_ICON_LOCATION);
                int var6 = 0;
                if ((Util.getMillis() / 800L & 1L) == 1L) {
                    var6 = 8;
                }

                GuiComponent.blit(param0, var3 + 4 - var5, var4 + 4, 0.0F, (float)var6, 8, 8, 8, 16);
            }
        }

    }

    void addNewsAndInvitesSubscriptions(RealmsDataFetcher param0, DataFetcher.Subscription param1) {
        param1.subscribe(param0.pendingInvitesTask, param0x -> this.numberOfPendingInvites = param0x);
        param1.subscribe(param0.trialAvailabilityTask, param0x -> trialAvailable = param0x);
        param1.subscribe(param0.newsTask, param1x -> {
            param0.newsManager.updateUnreadNews(param1x);
            hasUnreadNews = param0.newsManager.hasUnreadNews();
        });
    }

    void addNotificationsSubscriptions(RealmsDataFetcher param0, DataFetcher.Subscription param1) {
        param1.subscribe(param0.notificationsTask, param0x -> {
            hasUnseenNotifications = false;

            for(RealmsNotification var0 : param0x) {
                if (!var0.seen()) {
                    hasUnseenNotifications = true;
                    break;
                }
            }

        });
    }

    @OnlyIn(Dist.CLIENT)
    interface DataFetcherConfiguration {
        DataFetcher.Subscription initDataFetcher(RealmsDataFetcher var1);

        boolean showOldNotifications();
    }
}
