package com.mojang.realmsclient;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.mojang.realmsclient.client.Ping;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.RealmsNotification;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerList;
import com.mojang.realmsclient.dto.RegionPingResult;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.RealmsServerList;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsCreateRealmScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsPendingInvitesScreen;
import com.mojang.realmsclient.gui.screens.RealmsPopupScreen;
import com.mojang.realmsclient.gui.task.DataFetcher;
import com.mojang.realmsclient.util.RealmsPersistence;
import com.mojang.realmsclient.util.RealmsUtil;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.LoadingDotsWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.CommonLinks;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsMainScreen extends RealmsScreen {
    static final ResourceLocation INFO_SPRITE = new ResourceLocation("icon/info");
    static final ResourceLocation NEW_REALM_SPRITE = new ResourceLocation("icon/new_realm");
    static final ResourceLocation EXPIRED_SPRITE = new ResourceLocation("realm_status/expired");
    static final ResourceLocation EXPIRES_SOON_SPRITE = new ResourceLocation("realm_status/expires_soon");
    static final ResourceLocation OPEN_SPRITE = new ResourceLocation("realm_status/open");
    static final ResourceLocation CLOSED_SPRITE = new ResourceLocation("realm_status/closed");
    private static final ResourceLocation INVITE_SPRITE = new ResourceLocation("icon/invite");
    private static final ResourceLocation NEWS_SPRITE = new ResourceLocation("icon/news");
    static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("textures/gui/title/realms.png");
    private static final ResourceLocation NO_REALMS_LOCATION = new ResourceLocation("textures/gui/realms/no_realms.png");
    private static final Component TITLE = Component.translatable("menu.online");
    private static final Component LOADING_TEXT = Component.translatable("mco.selectServer.loading");
    static final Component SERVER_UNITIALIZED_TEXT = Component.translatable("mco.selectServer.uninitialized");
    static final Component SUBSCRIPTION_EXPIRED_TEXT = Component.translatable("mco.selectServer.expiredList");
    private static final Component SUBSCRIPTION_RENEW_TEXT = Component.translatable("mco.selectServer.expiredRenew");
    static final Component TRIAL_EXPIRED_TEXT = Component.translatable("mco.selectServer.expiredTrial");
    static final Component SELECT_MINIGAME_PREFIX = Component.translatable("mco.selectServer.minigame").append(CommonComponents.SPACE);
    private static final Component PLAY_TEXT = Component.translatable("mco.selectServer.play");
    private static final Component LEAVE_SERVER_TEXT = Component.translatable("mco.selectServer.leave");
    private static final Component CONFIGURE_SERVER_TEXT = Component.translatable("mco.selectServer.configure");
    static final Component SERVER_EXPIRED_TOOLTIP = Component.translatable("mco.selectServer.expired");
    static final Component SERVER_EXPIRES_SOON_TOOLTIP = Component.translatable("mco.selectServer.expires.soon");
    static final Component SERVER_EXPIRES_IN_DAY_TOOLTIP = Component.translatable("mco.selectServer.expires.day");
    static final Component SERVER_OPEN_TOOLTIP = Component.translatable("mco.selectServer.open");
    static final Component SERVER_CLOSED_TOOLTIP = Component.translatable("mco.selectServer.closed");
    static final Component UNITIALIZED_WORLD_NARRATION = Component.translatable("gui.narrate.button", SERVER_UNITIALIZED_TEXT);
    private static final Component NO_REALMS_TEXT = Component.translatable("mco.selectServer.noRealms");
    private static final Tooltip NO_PENDING_INVITES = Tooltip.create(Component.translatable("mco.invites.nopending"));
    private static final Tooltip PENDING_INVITES = Tooltip.create(Component.translatable("mco.invites.pending"));
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_COLUMNS = 3;
    private static final int BUTTON_SPACING = 4;
    private static final int CONTENT_WIDTH = 308;
    private static final int LOGO_WIDTH = 128;
    private static final int LOGO_HEIGHT = 34;
    private static final int LOGO_TEXTURE_WIDTH = 128;
    private static final int LOGO_TEXTURE_HEIGHT = 64;
    private static final int LOGO_PADDING = 5;
    private static final int HEADER_HEIGHT = 44;
    private static final int FOOTER_PADDING = 10;
    private final CompletableFuture<RealmsAvailability.Result> availability = RealmsAvailability.get();
    @Nullable
    private DataFetcher.Subscription dataSubscription;
    private final Set<UUID> handledSeenNotifications = new HashSet<>();
    private static boolean regionsPinged;
    private final RateLimiter inviteNarrationLimiter;
    private final Screen lastScreen;
    private Button playButton;
    private Button backButton;
    private Button renewButton;
    private Button configureButton;
    private Button leaveButton;
    private RealmsMainScreen.RealmSelectionList realmSelectionList;
    private boolean hasFetchedServers;
    private RealmsServerList serverList;
    private volatile int numberOfPendingInvites;
    int animTick;
    private volatile boolean trialsAvailable;
    private volatile boolean hasUnreadNews;
    @Nullable
    private volatile String newsLink;
    long lastClickTime;
    private ReentrantLock connectLock = new ReentrantLock();
    private final List<RealmsNotification> notifications = new ArrayList<>();
    private Button addRealmButton;
    private RealmsMainScreen.NotificationButton pendingInvitesButton;
    private RealmsMainScreen.NotificationButton newsButton;
    private RealmsMainScreen.LayoutState activeLayout;
    @Nullable
    private HeaderAndFooterLayout layout;

    public RealmsMainScreen(Screen param0) {
        super(TITLE);
        this.lastScreen = param0;
        this.inviteNarrationLimiter = RateLimiter.create(0.016666668F);
    }

    @Override
    public void init() {
        this.connectLock = new ReentrantLock();
        this.serverList = new RealmsServerList(this.minecraft);
        this.realmSelectionList = this.addRenderableWidget(new RealmsMainScreen.RealmSelectionList());
        Component var0 = Component.translatable("mco.invites.title");
        this.pendingInvitesButton = new RealmsMainScreen.NotificationButton(
            var0, INVITE_SPRITE, param1 -> this.minecraft.setScreen(new RealmsPendingInvitesScreen(this, var0))
        );
        Component var1 = Component.translatable("mco.news");
        this.newsButton = new RealmsMainScreen.NotificationButton(var1, NEWS_SPRITE, param0 -> {
            if (this.newsLink != null) {
                ConfirmLinkScreen.confirmLinkNow(this.newsLink, this, true);
                if (this.hasUnreadNews) {
                    RealmsPersistence.RealmsPersistenceData var0x = RealmsPersistence.readFile();
                    var0x.hasUnreadNews = false;
                    this.hasUnreadNews = false;
                    RealmsPersistence.writeFile(var0x);
                    this.updateButtonStates();
                }

            }
        });
        this.newsButton.setTooltip(Tooltip.create(var1));
        this.playButton = Button.builder(PLAY_TEXT, param0 -> this.play(this.getSelectedServer(), this)).width(100).build();
        this.configureButton = Button.builder(CONFIGURE_SERVER_TEXT, param0 -> this.configureClicked(this.getSelectedServer())).width(100).build();
        this.renewButton = Button.builder(SUBSCRIPTION_RENEW_TEXT, param0 -> this.onRenew(this.getSelectedServer())).width(100).build();
        this.leaveButton = Button.builder(LEAVE_SERVER_TEXT, param0 -> this.leaveClicked(this.getSelectedServer())).width(100).build();
        this.addRealmButton = Button.builder(Component.translatable("mco.selectServer.purchase"), param0 -> this.openPopup()).size(100, 20).build();
        this.backButton = Button.builder(CommonComponents.GUI_BACK, param0 -> this.minecraft.setScreen(this.lastScreen)).width(100).build();
        this.updateLayout(RealmsMainScreen.LayoutState.LOADING);
        this.updateButtonStates();
        this.availability.thenAcceptAsync(param0 -> {
            Screen var0x = param0.createErrorScreen(this.lastScreen);
            if (var0x == null) {
                this.dataSubscription = this.initDataFetcher(this.minecraft.realmsDataFetcher());
            } else {
                this.minecraft.setScreen(var0x);
            }

        }, this.screenExecutor);
    }

    @Override
    protected void repositionElements() {
        if (this.layout != null) {
            this.realmSelectionList.updateSize(this.width, this.height, this.layout.getHeaderHeight(), this.height - this.layout.getFooterHeight());
            this.layout.arrangeElements();
        }

    }

    private void updateLayout(RealmsMainScreen.LayoutState param0) {
        if (this.activeLayout != param0) {
            if (this.layout != null) {
                this.layout.visitWidgets(param1 -> this.removeWidget(param1));
            }

            this.layout = this.createLayout(param0);
            this.activeLayout = param0;
            this.layout.visitWidgets(param1 -> {
            });
            this.repositionElements();
        }
    }

    private HeaderAndFooterLayout createLayout(RealmsMainScreen.LayoutState param0) {
        HeaderAndFooterLayout var0 = new HeaderAndFooterLayout(this);
        var0.setHeaderHeight(44);
        var0.addToHeader(this.createHeader());
        Layout var1 = this.createFooter(param0);
        var1.arrangeElements();
        var0.setFooterHeight(var1.getHeight() + 20);
        var0.addToFooter(var1);
        switch(param0) {
            case LOADING:
                var0.addToContents(new LoadingDotsWidget(this.font, LOADING_TEXT));
                break;
            case NO_REALMS:
                var0.addToContents(this.createNoRealmsContent());
        }

        return var0;
    }

    private Layout createHeader() {
        int var0 = 90;
        LinearLayout var1 = LinearLayout.horizontal().spacing(4);
        var1.defaultCellSetting().alignVerticallyMiddle();
        var1.addChild(this.pendingInvitesButton);
        var1.addChild(this.newsButton);
        LinearLayout var2 = LinearLayout.horizontal();
        var2.defaultCellSetting().alignVerticallyMiddle();
        var2.addChild(SpacerElement.width(90));
        var2.addChild(ImageWidget.texture(128, 34, LOGO_LOCATION, 128, 64), LayoutSettings::alignHorizontallyCenter);
        var2.addChild(new FrameLayout(90, 44)).addChild(var1, LayoutSettings::alignHorizontallyRight);
        return var2;
    }

    private Layout createFooter(RealmsMainScreen.LayoutState param0) {
        GridLayout var0 = new GridLayout().spacing(4);
        GridLayout.RowHelper var1 = var0.createRowHelper(3);
        if (param0 == RealmsMainScreen.LayoutState.LIST) {
            var1.addChild(this.playButton);
            var1.addChild(this.configureButton);
            var1.addChild(this.renewButton);
            var1.addChild(this.leaveButton);
        }

        var1.addChild(this.addRealmButton);
        var1.addChild(this.backButton);
        return var0;
    }

    private LinearLayout createNoRealmsContent() {
        LinearLayout var0 = LinearLayout.vertical().spacing(10);
        var0.defaultCellSetting().alignHorizontallyCenter();
        var0.addChild(ImageWidget.texture(130, 64, NO_REALMS_LOCATION, 130, 64));
        FocusableTextWidget var1 = new FocusableTextWidget(308, NO_REALMS_TEXT, this.font, false);
        var0.addChild(var1);
        return var0;
    }

    void updateButtonStates() {
        RealmsServer var0 = this.getSelectedServer();
        this.addRealmButton.active = this.hasFetchedServers;
        this.playButton.active = this.shouldPlayButtonBeActive(var0);
        this.renewButton.active = this.shouldRenewButtonBeActive(var0);
        this.leaveButton.active = this.shouldLeaveButtonBeActive(var0);
        this.configureButton.active = this.shouldConfigureButtonBeActive(var0);
        this.pendingInvitesButton.setNotificationCount(this.numberOfPendingInvites);
        this.pendingInvitesButton.setTooltip(this.numberOfPendingInvites == 0 ? NO_PENDING_INVITES : PENDING_INVITES);
        this.newsButton.setNotificationCount(this.hasUnreadNews ? Integer.MAX_VALUE : 0);
    }

    boolean shouldPlayButtonBeActive(@Nullable RealmsServer param0) {
        return param0 != null && !param0.expired && param0.state == RealmsServer.State.OPEN;
    }

    private boolean shouldRenewButtonBeActive(@Nullable RealmsServer param0) {
        return param0 != null && param0.expired && this.isSelfOwnedServer(param0);
    }

    private boolean shouldConfigureButtonBeActive(@Nullable RealmsServer param0) {
        return param0 != null && this.isSelfOwnedServer(param0);
    }

    private boolean shouldLeaveButtonBeActive(@Nullable RealmsServer param0) {
        return param0 != null && !this.isSelfOwnedServer(param0);
    }

    @Override
    public void tick() {
        super.tick();
        ++this.animTick;
        if (this.dataSubscription != null) {
            this.dataSubscription.tick();
        }

    }

    private DataFetcher.Subscription initDataFetcher(RealmsDataFetcher param0) {
        DataFetcher.Subscription var0 = param0.dataFetcher.createSubscription();
        var0.subscribe(param0.serverListUpdateTask, param0x -> {
            this.serverList.updateServersList(param0x);
            boolean var0x = false;

            for(RealmsServer var1x : this.serverList) {
                if (this.isSelfOwnedNonExpiredServer(var1x)) {
                    var0x = true;
                }
            }

            this.hasFetchedServers = true;
            this.updateLayout(this.serverList.isEmpty() ? RealmsMainScreen.LayoutState.NO_REALMS : RealmsMainScreen.LayoutState.LIST);
            this.refreshRealmsSelectionList();
            if (!regionsPinged && var0x) {
                regionsPinged = true;
                this.pingRegions();
            }

        });
        callRealmsClient(RealmsClient::getNotifications, param0x -> {
            this.notifications.clear();
            this.notifications.addAll(param0x);
            this.refreshRealmsSelectionList();
        });
        var0.subscribe(param0.pendingInvitesTask, param0x -> {
            this.numberOfPendingInvites = param0x;
            this.updateButtonStates();
            if (this.numberOfPendingInvites > 0 && this.inviteNarrationLimiter.tryAcquire(1)) {
                this.minecraft.getNarrator().sayNow(Component.translatable("mco.configure.world.invite.narration", this.numberOfPendingInvites));
            }

        });
        var0.subscribe(param0.trialAvailabilityTask, param0x -> this.trialsAvailable = param0x);
        var0.subscribe(param0.liveStatsTask, param0x -> {
            for(RealmsServerPlayerList var0x : param0x.servers) {
                for(RealmsServer var1x : this.serverList) {
                    if (var1x.id == var0x.serverId) {
                        var1x.updateServerPing(var0x);
                        break;
                    }
                }
            }

        });
        var0.subscribe(param0.newsTask, param1 -> {
            param0.newsManager.updateUnreadNews(param1);
            this.hasUnreadNews = param0.newsManager.hasUnreadNews();
            this.newsLink = param0.newsManager.newsLink();
            this.updateButtonStates();
        });
        return var0;
    }

    private static <T> void callRealmsClient(RealmsMainScreen.RealmsCall<T> param0, Consumer<T> param1) {
        Minecraft var0 = Minecraft.getInstance();
        CompletableFuture.<T>supplyAsync(() -> {
            try {
                return param0.request(RealmsClient.create(var0));
            } catch (RealmsServiceException var3) {
                throw new RuntimeException(var3);
            }
        }).thenAcceptAsync(param1, var0).exceptionally(param0x -> {
            LOGGER.error("Failed to execute call to Realms Service", param0x);
            return null;
        });
    }

    private void refreshRealmsSelectionList() {
        RealmsServer var0 = this.getSelectedServer();
        this.realmSelectionList.clear();
        List<UUID> var1 = new ArrayList<>();

        for(RealmsNotification var2 : this.notifications) {
            this.addEntriesForNotification(this.realmSelectionList, var2);
            if (!var2.seen() && !this.handledSeenNotifications.contains(var2.uuid())) {
                var1.add(var2.uuid());
            }
        }

        if (!var1.isEmpty()) {
            callRealmsClient(param1 -> {
                param1.notificationsSeen(var1);
                return null;
            }, param1 -> this.handledSeenNotifications.addAll(var1));
        }

        for(RealmsServer var3 : this.serverList) {
            RealmsMainScreen.ServerEntry var4 = new RealmsMainScreen.ServerEntry(var3);
            this.realmSelectionList.addEntry(var4);
            if (var0 != null && var0.id == var3.id) {
                this.realmSelectionList.setSelected((RealmsMainScreen.Entry)var4);
            }
        }

        this.updateButtonStates();
    }

    private void addEntriesForNotification(RealmsMainScreen.RealmSelectionList param0, RealmsNotification param1) {
        if (param1 instanceof RealmsNotification.VisitUrl var0) {
            param0.addEntry(new RealmsMainScreen.NotificationMessageEntry(var0.getMessage(), var0));
            param0.addEntry(new RealmsMainScreen.ButtonEntry(var0.buildOpenLinkButton(this)));
        }

    }

    private void pingRegions() {
        new Thread(() -> {
            List<RegionPingResult> var0 = Ping.pingAllRegions();
            RealmsClient var1 = RealmsClient.create();
            PingResult var2 = new PingResult();
            var2.pingResults = var0;
            var2.worldIds = this.getOwnedNonExpiredWorldIds();

            try {
                var1.sendPingResults(var2);
            } catch (Throwable var5) {
                LOGGER.warn("Could not send ping result to Realms: ", var5);
            }

        }).start();
    }

    private List<Long> getOwnedNonExpiredWorldIds() {
        List<Long> var0 = Lists.newArrayList();

        for(RealmsServer var1 : this.serverList) {
            if (this.isSelfOwnedNonExpiredServer(var1)) {
                var0.add(var1.id);
            }
        }

        return var0;
    }

    private void onRenew(@Nullable RealmsServer param0) {
        if (param0 != null) {
            String var0 = CommonLinks.extendRealms(param0.remoteSubscriptionId, this.minecraft.getUser().getProfileId(), param0.expiredTrial);
            this.minecraft.keyboardHandler.setClipboard(var0);
            Util.getPlatform().openUri(var0);
        }

    }

    private void configureClicked(@Nullable RealmsServer param0) {
        if (param0 != null && this.minecraft.isLocalPlayer(param0.ownerUUID)) {
            this.minecraft.setScreen(new RealmsConfigureWorldScreen(this, param0.id));
        }

    }

    private void leaveClicked(@Nullable RealmsServer param0) {
        if (param0 != null && !this.minecraft.isLocalPlayer(param0.ownerUUID)) {
            Component var0 = Component.translatable("mco.configure.world.leave.question.line1");
            Component var1 = Component.translatable("mco.configure.world.leave.question.line2");
            this.minecraft
                .setScreen(
                    new RealmsLongConfirmationScreen(param1 -> this.leaveServer(param1, param0), RealmsLongConfirmationScreen.Type.INFO, var0, var1, true)
                );
        }

    }

    @Nullable
    private RealmsServer getSelectedServer() {
        if (this.realmSelectionList == null) {
            return null;
        } else {
            RealmsMainScreen.Entry var0 = this.realmSelectionList.getSelected();
            return var0 != null ? var0.getServer() : null;
        }
    }

    private void leaveServer(boolean param0, final RealmsServer param1) {
        if (param0) {
            (new Thread("Realms-leave-server") {
                    @Override
                    public void run() {
                        try {
                            RealmsClient var0 = RealmsClient.create();
                            var0.uninviteMyselfFrom(param1.id);
                            RealmsMainScreen.this.minecraft.execute(() -> RealmsMainScreen.this.removeServer(param1));
                        } catch (RealmsServiceException var2) {
                            RealmsMainScreen.LOGGER.error("Couldn't configure world", (Throwable)var2);
                            RealmsMainScreen.this.minecraft
                                .execute(() -> RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(var2, RealmsMainScreen.this)));
                        }
    
                    }
                })
                .start();
        }

        this.minecraft.setScreen(this);
    }

    void removeServer(RealmsServer param0) {
        this.serverList.removeItem(param0);
        this.realmSelectionList.children().removeIf(param1 -> {
            RealmsServer var0 = param1.getServer();
            return var0 != null && var0.id == param0.id;
        });
        this.realmSelectionList.setSelected(null);
        this.updateButtonStates();
    }

    void dismissNotification(UUID param0) {
        callRealmsClient(param1 -> {
            param1.notificationsDismiss(List.of(param0));
            return null;
        }, param1 -> {
            this.notifications.removeIf(param1x -> param1x.dismissable() && param0.equals(param1x.uuid()));
            this.refreshRealmsSelectionList();
        });
    }

    public void resetScreen() {
        if (this.realmSelectionList != null) {
            this.realmSelectionList.setSelected(null);
        }

    }

    @Override
    public Component getNarrationMessage() {
        return (Component)(switch(this.activeLayout) {
            case LOADING -> CommonComponents.joinForNarration(super.getNarrationMessage(), LOADING_TEXT);
            case NO_REALMS -> CommonComponents.joinForNarration(super.getNarrationMessage(), NO_REALMS_TEXT);
            case LIST -> super.getNarrationMessage();
        });
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        if (this.trialsAvailable && this.addRealmButton.active) {
            RealmsPopupScreen.renderDiamond(param0, this.addRealmButton);
        }

        switch(RealmsClient.ENVIRONMENT) {
            case STAGE:
                this.renderEnvironment(param0, "STAGE!", -256);
                break;
            case LOCAL:
                this.renderEnvironment(param0, "LOCAL!", 8388479);
        }

    }

    private void openPopup() {
        this.minecraft.setScreen(new RealmsPopupScreen(this, this.trialsAvailable));
    }

    public void play(@Nullable RealmsServer param0, Screen param1) {
        if (param0 != null) {
            try {
                if (!this.connectLock.tryLock(1L, TimeUnit.SECONDS)) {
                    return;
                }

                if (this.connectLock.getHoldCount() > 1) {
                    return;
                }
            } catch (InterruptedException var4) {
                return;
            }

            this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(param1, new GetServerDetailsTask(this, param1, param0, this.connectLock)));
        }

    }

    boolean isSelfOwnedServer(RealmsServer param0) {
        return this.minecraft.isLocalPlayer(param0.ownerUUID);
    }

    private boolean isSelfOwnedNonExpiredServer(RealmsServer param0) {
        return this.isSelfOwnedServer(param0) && !param0.expired;
    }

    private void renderEnvironment(GuiGraphics param0, String param1, int param2) {
        param0.pose().pushPose();
        param0.pose().translate((float)(this.width / 2 - 25), 20.0F, 0.0F);
        param0.pose().mulPose(Axis.ZP.rotationDegrees(-20.0F));
        param0.pose().scale(1.5F, 1.5F, 1.5F);
        param0.drawString(this.font, param1, 0, 0, param2, false);
        param0.pose().popPose();
    }

    public RealmsMainScreen newScreen() {
        RealmsMainScreen var0 = new RealmsMainScreen(this.lastScreen);
        var0.init(this.minecraft, this.width, this.height);
        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    class ButtonEntry extends RealmsMainScreen.Entry {
        private final Button button;
        private final int xPos = RealmsMainScreen.this.width / 2 - 75;

        public ButtonEntry(Button param0) {
            this.button = param0;
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            this.button.mouseClicked(param0, param1, param2);
            return true;
        }

        @Override
        public boolean keyPressed(int param0, int param1, int param2) {
            return this.button.keyPressed(param0, param1, param2) ? true : super.keyPressed(param0, param1, param2);
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            this.button.setPosition(this.xPos, param2 + 4);
            this.button.render(param0, param6, param7, param9);
        }

        @Override
        public Component getNarration() {
            return this.button.getMessage();
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class CrossButton extends ImageButton {
        private static final WidgetSprites SPRITES = new WidgetSprites(
            new ResourceLocation("widget/cross_button"), new ResourceLocation("widget/cross_button_highlighted")
        );

        protected CrossButton(Button.OnPress param0, Component param1) {
            super(0, 0, 14, 14, SPRITES, param0);
            this.setTooltip(Tooltip.create(param1));
        }
    }

    @OnlyIn(Dist.CLIENT)
    abstract class Entry extends ObjectSelectionList.Entry<RealmsMainScreen.Entry> {
        @Nullable
        public RealmsServer getServer() {
            return null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static enum LayoutState {
        LOADING,
        NO_REALMS,
        LIST;
    }

    @OnlyIn(Dist.CLIENT)
    static class NotificationButton extends SpriteIconButton.CenteredIcon {
        private static final ResourceLocation[] NOTIFICATION_ICONS = new ResourceLocation[]{
            new ResourceLocation("notification/1"),
            new ResourceLocation("notification/2"),
            new ResourceLocation("notification/3"),
            new ResourceLocation("notification/4"),
            new ResourceLocation("notification/5"),
            new ResourceLocation("notification/more")
        };
        private static final int UNKNOWN_COUNT = Integer.MAX_VALUE;
        private static final int SIZE = 20;
        private static final int SPRITE_SIZE = 14;
        private int notificationCount;

        public NotificationButton(Component param0, ResourceLocation param1, Button.OnPress param2) {
            super(20, 20, param0, 14, 14, param1, param2);
        }

        public void setNotificationCount(int param0) {
            this.notificationCount = param0;
        }

        @Override
        public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
            super.renderWidget(param0, param1, param2, param3);
            if (this.active && this.notificationCount != 0) {
                this.drawNotificationCounter(param0);
            }

        }

        private void drawNotificationCounter(GuiGraphics param0) {
            param0.blitSprite(NOTIFICATION_ICONS[Math.min(this.notificationCount, 6) - 1], this.getX() + this.getWidth() - 5, this.getY() - 3, 8, 8);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class NotificationMessageEntry extends RealmsMainScreen.Entry {
        private static final int SIDE_MARGINS = 40;
        private static final int ITEM_HEIGHT = 36;
        private static final int OUTLINE_COLOR = -12303292;
        private final Component text;
        private final List<AbstractWidget> children = new ArrayList<>();
        @Nullable
        private final RealmsMainScreen.CrossButton dismissButton;
        private final MultiLineTextWidget textWidget;
        private final GridLayout gridLayout;
        private final FrameLayout textFrame;
        private int lastEntryWidth = -1;

        public NotificationMessageEntry(Component param0, RealmsNotification param1) {
            this.text = param0;
            this.gridLayout = new GridLayout();
            int param2 = 7;
            this.gridLayout.addChild(ImageWidget.sprite(20, 20, RealmsMainScreen.INFO_SPRITE), 0, 0, this.gridLayout.newCellSettings().padding(7, 7, 0, 0));
            this.gridLayout.addChild(SpacerElement.width(40), 0, 0);
            this.textFrame = this.gridLayout.addChild(new FrameLayout(0, 9 * 3), 0, 1, this.gridLayout.newCellSettings().paddingTop(7));
            this.textWidget = this.textFrame
                .addChild(
                    new MultiLineTextWidget(param0, RealmsMainScreen.this.font).setCentered(true).setMaxRows(3),
                    this.textFrame.newChildLayoutSettings().alignHorizontallyCenter().alignVerticallyTop()
                );
            this.gridLayout.addChild(SpacerElement.width(40), 0, 2);
            if (param1.dismissable()) {
                this.dismissButton = this.gridLayout
                    .addChild(
                        new RealmsMainScreen.CrossButton(
                            param1x -> RealmsMainScreen.this.dismissNotification(param1.uuid()), Component.translatable("mco.notification.dismiss")
                        ),
                        0,
                        2,
                        this.gridLayout.newCellSettings().alignHorizontallyRight().padding(0, 7, 7, 0)
                    );
            } else {
                this.dismissButton = null;
            }

            this.gridLayout.visitWidgets(this.children::add);
        }

        @Override
        public boolean keyPressed(int param0, int param1, int param2) {
            return this.dismissButton != null && this.dismissButton.keyPressed(param0, param1, param2) ? true : super.keyPressed(param0, param1, param2);
        }

        private void updateEntryWidth(int param0) {
            if (this.lastEntryWidth != param0) {
                this.refreshLayout(param0);
                this.lastEntryWidth = param0;
            }

        }

        private void refreshLayout(int param0) {
            int var0 = param0 - 80;
            this.textFrame.setMinWidth(var0);
            this.textWidget.setMaxWidth(var0);
            this.gridLayout.arrangeElements();
        }

        @Override
        public void renderBack(
            GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9
        ) {
            super.renderBack(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
            param0.renderOutline(param3 - 2, param2 - 2, param4, 70, -12303292);
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            this.gridLayout.setPosition(param3, param2);
            this.updateEntryWidth(param4 - 4);
            this.children.forEach(param4x -> param4x.render(param0, param6, param7, param9));
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (this.dismissButton != null) {
                this.dismissButton.mouseClicked(param0, param1, param2);
            }

            return true;
        }

        @Override
        public Component getNarration() {
            return this.text;
        }
    }

    @OnlyIn(Dist.CLIENT)
    class RealmSelectionList extends RealmsObjectSelectionList<RealmsMainScreen.Entry> {
        public RealmSelectionList() {
            super(RealmsMainScreen.this.width, RealmsMainScreen.this.height, 0, RealmsMainScreen.this.height, 36);
        }

        public void setSelected(@Nullable RealmsMainScreen.Entry param0) {
            super.setSelected(param0);
            RealmsMainScreen.this.updateButtonStates();
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 36;
        }

        @Override
        public int getRowWidth() {
            return 300;
        }
    }

    @OnlyIn(Dist.CLIENT)
    interface RealmsCall<T> {
        T request(RealmsClient var1) throws RealmsServiceException;
    }

    @OnlyIn(Dist.CLIENT)
    class ServerEntry extends RealmsMainScreen.Entry {
        private static final int SKIN_HEAD_LARGE_WIDTH = 36;
        private final RealmsServer serverData;

        public ServerEntry(RealmsServer param0) {
            this.serverData = param0;
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            this.renderMcoServerItem(this.serverData, param0, param3, param2, param6, param7);
        }

        private void playRealm() {
            RealmsMainScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            RealmsMainScreen.this.play(this.serverData, RealmsMainScreen.this);
        }

        private void createUnitializedRealm() {
            RealmsMainScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            RealmsCreateRealmScreen var0 = new RealmsCreateRealmScreen(this.serverData, RealmsMainScreen.this);
            RealmsMainScreen.this.minecraft.setScreen(var0);
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
                this.createUnitializedRealm();
            } else if (RealmsMainScreen.this.shouldPlayButtonBeActive(this.serverData)) {
                if (Util.getMillis() - RealmsMainScreen.this.lastClickTime < 250L && this.isFocused()) {
                    this.playRealm();
                }

                RealmsMainScreen.this.lastClickTime = Util.getMillis();
            }

            return true;
        }

        @Override
        public boolean keyPressed(int param0, int param1, int param2) {
            if (CommonInputs.selected(param0)) {
                if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
                    this.createUnitializedRealm();
                    return true;
                }

                if (RealmsMainScreen.this.shouldPlayButtonBeActive(this.serverData)) {
                    this.playRealm();
                    return true;
                }
            }

            return super.keyPressed(param0, param1, param2);
        }

        private void renderMcoServerItem(RealmsServer param0, GuiGraphics param1, int param2, int param3, int param4, int param5) {
            this.renderLegacy(param0, param1, param2 + 36, param3, param4, param5);
        }

        private void renderLegacy(RealmsServer param0, GuiGraphics param1, int param2, int param3, int param4, int param5) {
            if (param0.state == RealmsServer.State.UNINITIALIZED) {
                param1.blitSprite(RealmsMainScreen.NEW_REALM_SPRITE, param2 + 10, param3 + 6, 40, 20);
                float var0 = 0.5F + (1.0F + Mth.sin((float)RealmsMainScreen.this.animTick * 0.25F)) * 0.25F;
                int var1 = 0xFF000000 | (int)(127.0F * var0) << 16 | (int)(255.0F * var0) << 8 | (int)(127.0F * var0);
                param1.drawCenteredString(RealmsMainScreen.this.font, RealmsMainScreen.SERVER_UNITIALIZED_TEXT, param2 + 10 + 40 + 75, param3 + 12, var1);
            } else {
                int var2 = 225;
                int var3 = 2;
                this.renderStatusLights(param0, param1, param2, param3, param4, param5, 225, 2);
                if (!"0".equals(param0.serverPing.nrOfPlayers)) {
                    String var4 = ChatFormatting.GRAY + param0.serverPing.nrOfPlayers;
                    param1.drawString(RealmsMainScreen.this.font, var4, param2 + 207 - RealmsMainScreen.this.font.width(var4), param3 + 3, -8355712, false);
                    if (param4 >= param2 + 207 - RealmsMainScreen.this.font.width(var4)
                        && param4 <= param2 + 207
                        && param5 >= param3 + 1
                        && param5 <= param3 + 10
                        && param5 < RealmsMainScreen.this.height - 40
                        && param5 > 32) {
                        RealmsMainScreen.this.setTooltipForNextRenderPass(Component.literal(param0.serverPing.playerList));
                    }
                }

                if (RealmsMainScreen.this.isSelfOwnedServer(param0) && param0.expired) {
                    Component var5 = param0.expiredTrial ? RealmsMainScreen.TRIAL_EXPIRED_TEXT : RealmsMainScreen.SUBSCRIPTION_EXPIRED_TEXT;
                    int var6 = param3 + 11 + 5;
                    param1.drawString(RealmsMainScreen.this.font, var5, param2 + 2, var6 + 1, 15553363, false);
                } else {
                    if (param0.worldType == RealmsServer.WorldType.MINIGAME) {
                        int var7 = 13413468;
                        int var8 = RealmsMainScreen.this.font.width(RealmsMainScreen.SELECT_MINIGAME_PREFIX);
                        param1.drawString(RealmsMainScreen.this.font, RealmsMainScreen.SELECT_MINIGAME_PREFIX, param2 + 2, param3 + 12, 13413468, false);
                        param1.drawString(RealmsMainScreen.this.font, param0.getMinigameName(), param2 + 2 + var8, param3 + 12, 7105644, false);
                    } else {
                        param1.drawString(RealmsMainScreen.this.font, param0.getDescription(), param2 + 2, param3 + 12, 7105644, false);
                    }

                    if (!RealmsMainScreen.this.isSelfOwnedServer(param0)) {
                        param1.drawString(RealmsMainScreen.this.font, param0.owner, param2 + 2, param3 + 12 + 11, 5000268, false);
                    }
                }

                param1.drawString(RealmsMainScreen.this.font, param0.getName(), param2 + 2, param3 + 1, -1, false);
                RealmsUtil.renderPlayerFace(param1, param2 - 36, param3, 32, param0.ownerUUID);
            }
        }

        private void renderStatusLights(RealmsServer param0, GuiGraphics param1, int param2, int param3, int param4, int param5, int param6, int param7) {
            int var0 = param2 + param6 + 22;
            if (param0.expired) {
                this.drawRealmStatus(
                    param1, var0, param3 + param7, param4, param5, RealmsMainScreen.EXPIRED_SPRITE, () -> RealmsMainScreen.SERVER_EXPIRED_TOOLTIP
                );
            } else if (param0.state == RealmsServer.State.CLOSED) {
                this.drawRealmStatus(
                    param1, var0, param3 + param7, param4, param5, RealmsMainScreen.CLOSED_SPRITE, () -> RealmsMainScreen.SERVER_CLOSED_TOOLTIP
                );
            } else if (RealmsMainScreen.this.isSelfOwnedServer(param0) && param0.daysLeft < 7) {
                this.drawRealmStatus(
                    param1,
                    var0,
                    param3 + param7,
                    param4,
                    param5,
                    RealmsMainScreen.EXPIRES_SOON_SPRITE,
                    () -> {
                        if (param0.daysLeft <= 0) {
                            return RealmsMainScreen.SERVER_EXPIRES_SOON_TOOLTIP;
                        } else {
                            return (Component)(param0.daysLeft == 1
                                ? RealmsMainScreen.SERVER_EXPIRES_IN_DAY_TOOLTIP
                                : Component.translatable("mco.selectServer.expires.days", param0.daysLeft));
                        }
                    }
                );
            } else if (param0.state == RealmsServer.State.OPEN) {
                this.drawRealmStatus(param1, var0, param3 + param7, param4, param5, RealmsMainScreen.OPEN_SPRITE, () -> RealmsMainScreen.SERVER_OPEN_TOOLTIP);
            }

        }

        private void drawRealmStatus(GuiGraphics param0, int param1, int param2, int param3, int param4, ResourceLocation param5, Supplier<Component> param6) {
            param0.blitSprite(param5, param1, param2, 10, 28);
            if (param3 >= param1
                && param3 <= param1 + 9
                && param4 >= param2
                && param4 <= param2 + 27
                && param4 < RealmsMainScreen.this.height - 40
                && param4 > 32) {
                RealmsMainScreen.this.setTooltipForNextRenderPass(param6.get());
            }

        }

        @Override
        public Component getNarration() {
            return (Component)(this.serverData.state == RealmsServer.State.UNINITIALIZED
                ? RealmsMainScreen.UNITIALIZED_WORLD_NARRATION
                : Component.translatable("narrator.select", this.serverData.name));
        }

        @Nullable
        @Override
        public RealmsServer getServer() {
            return this.serverData;
        }
    }
}
