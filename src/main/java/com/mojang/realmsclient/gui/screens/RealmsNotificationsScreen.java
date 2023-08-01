package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsAvailability;
import com.mojang.realmsclient.dto.RealmsNotification;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.task.DataFetcher;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsNotificationsScreen extends RealmsScreen {
    private static final ResourceLocation UNSEEN_NOTIFICATION_SPRITE = new ResourceLocation("icon/unseen_notification");
    private static final ResourceLocation NEWS_SPRITE = new ResourceLocation("icon/news");
    private static final ResourceLocation INVITE_SPRITE = new ResourceLocation("icon/invite");
    private static final ResourceLocation TRIAL_AVAILABLE_SPRITE = new ResourceLocation("icon/trial_available");
    private final CompletableFuture<Boolean> validClient = RealmsAvailability.get().thenApply(param0 -> param0.type() == RealmsAvailability.Type.SUCCESS);
    @Nullable
    private DataFetcher.Subscription realmsDataSubscription;
    @Nullable
    private RealmsNotificationsScreen.DataFetcherConfiguration currentConfiguration;
    private volatile int numberOfPendingInvites;
    private static boolean trialAvailable;
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
        boolean var0 = this.inTitleScreen() && this.validClient.getNow(false);
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

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        if (this.validClient.getNow(false)) {
            this.drawIcons(param0);
        }

    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
    }

    private void drawIcons(GuiGraphics param0) {
        int var0 = this.numberOfPendingInvites;
        int var1 = 24;
        int var2 = this.height / 4 + 48;
        int var3 = this.width / 2 + 100;
        int var4 = var2 + 48 + 2;
        int var5 = var3 - 3;
        if (hasUnseenNotifications) {
            param0.blitSprite(UNSEEN_NOTIFICATION_SPRITE, var5 - 12, var4 + 3, 10, 10);
            var5 -= 16;
        }

        if (this.currentConfiguration != null && this.currentConfiguration.showOldNotifications()) {
            if (hasUnreadNews) {
                param0.blitSprite(NEWS_SPRITE, var5 - 14, var4 + 1, 14, 14);
                var5 -= 16;
            }

            if (var0 != 0) {
                param0.blitSprite(INVITE_SPRITE, var5 - 14, var4 + 1, 14, 14);
                var5 -= 16;
            }

            if (trialAvailable) {
                param0.blitSprite(TRIAL_AVAILABLE_SPRITE, var5 - 10, var4 + 4, 8, 8);
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
