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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.LoadingDotsWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PopupScreen;
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
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
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
import org.apache.commons.lang3.StringUtils;
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
    private static final Component NO_PENDING_INVITES = Component.translatable("mco.invites.nopending");
    private static final Component PENDING_INVITES = Component.translatable("mco.invites.pending");
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
    private static final int FOOTER_PADDING = 11;
    private static final int NEW_REALM_SPRITE_WIDTH = 40;
    private static final int NEW_REALM_SPRITE_HEIGHT = 20;
    private static final int ENTRY_WIDTH = 216;
    private static final int ITEM_HEIGHT = 36;
    private static final boolean SNAPSHOT = !SharedConstants.getCurrentVersion().isStable();
    private static boolean snapshotToggle = SNAPSHOT;
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
    RealmsMainScreen.RealmSelectionList realmSelectionList;
    private RealmsServerList serverList;
    private List<RealmsServer> availableSnapshotServers = List.of();
    private volatile boolean trialsAvailable;
    @Nullable
    private volatile String newsLink;
    long lastClickTime;
    private final List<RealmsNotification> notifications = new ArrayList<>();
    private Button addRealmButton;
    private RealmsMainScreen.NotificationButton pendingInvitesButton;
    private RealmsMainScreen.NotificationButton newsButton;
    private RealmsMainScreen.LayoutState activeLayoutState;
    @Nullable
    private HeaderAndFooterLayout layout;

    public RealmsMainScreen(Screen param0) {
        super(TITLE);
        this.lastScreen = param0;
        this.inviteNarrationLimiter = RateLimiter.create(0.016666668F);
    }

    @Override
    public void init() {
        this.serverList = new RealmsServerList(this.minecraft);
        this.realmSelectionList = this.addRenderableWidget(new RealmsMainScreen.RealmSelectionList());
        Component var0 = Component.translatable("mco.invites.title");
        this.pendingInvitesButton = new RealmsMainScreen.NotificationButton(
            var0, INVITE_SPRITE, param1 -> this.minecraft.setScreen(new RealmsPendingInvitesScreen(this, var0))
        );
        Component var1 = Component.translatable("mco.news");
        this.newsButton = new RealmsMainScreen.NotificationButton(var1, NEWS_SPRITE, param0 -> {
            String var0x = this.newsLink;
            if (var0x != null) {
                ConfirmLinkScreen.confirmLinkNow(this, var0x);
                if (this.newsButton.notificationCount() != 0) {
                    RealmsPersistence.RealmsPersistenceData var1x = RealmsPersistence.readFile();
                    var1x.hasUnreadNews = false;
                    RealmsPersistence.writeFile(var1x);
                    this.newsButton.setNotificationCount(0);
                }

            }
        });
        this.newsButton.setTooltip(Tooltip.create(var1));
        this.playButton = Button.builder(PLAY_TEXT, param0 -> play(this.getSelectedServer(), this)).width(100).build();
        this.configureButton = Button.builder(CONFIGURE_SERVER_TEXT, param0 -> this.configureClicked(this.getSelectedServer())).width(100).build();
        this.renewButton = Button.builder(SUBSCRIPTION_RENEW_TEXT, param0 -> this.onRenew(this.getSelectedServer())).width(100).build();
        this.leaveButton = Button.builder(LEAVE_SERVER_TEXT, param0 -> this.leaveClicked(this.getSelectedServer())).width(100).build();
        this.addRealmButton = Button.builder(Component.translatable("mco.selectServer.purchase"), param0 -> this.openTrialAvailablePopup())
            .size(100, 20)
            .build();
        this.backButton = Button.builder(CommonComponents.GUI_BACK, param0 -> this.minecraft.setScreen(this.lastScreen)).width(100).build();
        if (RealmsClient.ENVIRONMENT == RealmsClient.Environment.STAGE) {
            this.addRenderableWidget(
                CycleButton.booleanBuilder(Component.literal("Snapshot"), Component.literal("Release"))
                    .create(5, 5, 100, 20, Component.literal("Realm"), (param0, param1) -> {
                        snapshotToggle = param1;
                        this.availableSnapshotServers = List.of();
                        this.debugRefreshDataFetchers();
                    })
            );
        }

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

    public static boolean isSnapshot() {
        return SNAPSHOT && snapshotToggle;
    }

    @Override
    protected void repositionElements() {
        if (this.layout != null) {
            this.realmSelectionList.updateSize(this.width, this.height, this.layout.getHeaderHeight(), this.height - this.layout.getFooterHeight());
            this.layout.arrangeElements();
        }

    }

    private void updateLayout() {
        if (this.serverList.isEmpty() && this.availableSnapshotServers.isEmpty() && this.notifications.isEmpty()) {
            this.updateLayout(RealmsMainScreen.LayoutState.NO_REALMS);
        } else {
            this.updateLayout(RealmsMainScreen.LayoutState.LIST);
        }

    }

    private void updateLayout(RealmsMainScreen.LayoutState param0) {
        if (this.activeLayoutState != param0) {
            if (this.layout != null) {
                this.layout.visitWidgets(param1 -> this.removeWidget(param1));
            }

            this.layout = this.createLayout(param0);
            this.activeLayoutState = param0;
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
        var0.setFooterHeight(var1.getHeight() + 22);
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
        this.addRealmButton.active = this.activeLayoutState != RealmsMainScreen.LayoutState.LOADING;
        this.playButton.active = var0 != null && this.shouldPlayButtonBeActive(var0);
        this.renewButton.active = var0 != null && this.shouldRenewButtonBeActive(var0);
        this.leaveButton.active = var0 != null && this.shouldLeaveButtonBeActive(var0);
        this.configureButton.active = var0 != null && this.shouldConfigureButtonBeActive(var0);
    }

    boolean shouldPlayButtonBeActive(RealmsServer param0) {
        boolean var0 = !param0.expired && param0.state == RealmsServer.State.OPEN;
        return var0 && (param0.isCompatible() || this.isSelfOwnedServer(param0));
    }

    private boolean shouldRenewButtonBeActive(RealmsServer param0) {
        return param0.expired && this.isSelfOwnedServer(param0);
    }

    private boolean shouldConfigureButtonBeActive(RealmsServer param0) {
        return this.isSelfOwnedServer(param0);
    }

    private boolean shouldLeaveButtonBeActive(RealmsServer param0) {
        return !this.isSelfOwnedServer(param0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.dataSubscription != null) {
            this.dataSubscription.tick();
        }

    }

    public static void refreshPendingInvites() {
        Minecraft.getInstance().realmsDataFetcher().pendingInvitesTask.reset();
    }

    public static void refreshServerList() {
        Minecraft.getInstance().realmsDataFetcher().serverListUpdateTask.reset();
    }

    private void debugRefreshDataFetchers() {
        for(DataFetcher.Task<?> var0 : this.minecraft.realmsDataFetcher().getTasks()) {
            var0.reset();
        }

    }

    private DataFetcher.Subscription initDataFetcher(RealmsDataFetcher param0) {
        DataFetcher.Subscription var0 = param0.dataFetcher.createSubscription();
        var0.subscribe(param0.serverListUpdateTask, param0x -> {
            this.serverList.updateServersList(param0x.serverList());
            this.availableSnapshotServers = param0x.availableSnapshotServers();
            this.refreshListAndLayout();
            boolean var0x = false;

            for(RealmsServer var1x : this.serverList) {
                if (this.isSelfOwnedNonExpiredServer(var1x)) {
                    var0x = true;
                }
            }

            if (!regionsPinged && var0x) {
                regionsPinged = true;
                this.pingRegions();
            }

        });
        callRealmsClient(RealmsClient::getNotifications, param0x -> {
            this.notifications.clear();
            this.notifications.addAll(param0x);

            for(RealmsNotification var0x : param0x) {
                if (var0x instanceof RealmsNotification.InfoPopup var1x) {
                    PopupScreen var2x = var1x.buildScreen(this, this::dismissNotification);
                    if (var2x != null) {
                        this.minecraft.setScreen(var2x);
                        this.markNotificationsAsSeen(List.of(var0x));
                        break;
                    }
                }
            }

            if (!this.notifications.isEmpty() && this.activeLayoutState != RealmsMainScreen.LayoutState.LOADING) {
                this.refreshListAndLayout();
            }

        });
        var0.subscribe(param0.pendingInvitesTask, param0x -> {
            this.pendingInvitesButton.setNotificationCount(param0x);
            this.pendingInvitesButton.setTooltip(param0x == 0 ? Tooltip.create(NO_PENDING_INVITES) : Tooltip.create(PENDING_INVITES));
            if (param0x > 0 && this.inviteNarrationLimiter.tryAcquire(1)) {
                this.minecraft.getNarrator().sayNow(Component.translatable("mco.configure.world.invite.narration", param0x));
            }

        });
        var0.subscribe(param0.trialAvailabilityTask, param0x -> this.trialsAvailable = param0x);
        var0.subscribe(param0.newsTask, param1 -> {
            param0.newsManager.updateUnreadNews(param1);
            this.newsLink = param0.newsManager.newsLink();
            this.newsButton.setNotificationCount(param0.newsManager.hasUnreadNews() ? Integer.MAX_VALUE : 0);
        });
        return var0;
    }

    private void markNotificationsAsSeen(Collection<RealmsNotification> param0) {
        List<UUID> var0 = new ArrayList<>(param0.size());

        for(RealmsNotification var1 : param0) {
            if (!var1.seen() && !this.handledSeenNotifications.contains(var1.uuid())) {
                var0.add(var1.uuid());
            }
        }

        if (!var0.isEmpty()) {
            callRealmsClient(param1 -> {
                param1.notificationsSeen(var0);
                return null;
            }, param1 -> this.handledSeenNotifications.addAll(var0));
        }

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

    private void refreshListAndLayout() {
        RealmsServer var0 = this.getSelectedServer();
        this.realmSelectionList.clear();

        for(RealmsNotification var1 : this.notifications) {
            if (this.addListEntriesForNotification(var1)) {
                this.markNotificationsAsSeen(List.of(var1));
                break;
            }
        }

        for(RealmsServer var2 : this.availableSnapshotServers) {
            this.realmSelectionList.addEntry(new RealmsMainScreen.AvailableSnapshotEntry(var2));
        }

        Iterator var6 = this.serverList.iterator();

        while(true) {
            RealmsMainScreen.Entry var4;
            RealmsServer var3;
            while(true) {
                if (!var6.hasNext()) {
                    this.updateLayout();
                    this.updateButtonStates();
                    return;
                }

                var3 = (RealmsServer)var6.next();
                if (isSnapshot() && !var3.isSnapshotRealm()) {
                    if (var3.state == RealmsServer.State.UNINITIALIZED) {
                        continue;
                    }

                    var4 = new RealmsMainScreen.ParentEntry(var3);
                    break;
                }

                var4 = new RealmsMainScreen.ServerEntry(var3);
                break;
            }

            this.realmSelectionList.addEntry(var4);
            if (var0 != null && var0.id == var3.id) {
                this.realmSelectionList.setSelected(var4);
            }
        }
    }

    private boolean addListEntriesForNotification(RealmsNotification param0) {
        if (!(param0 instanceof RealmsNotification.VisitUrl)) {
            return false;
        } else {
            RealmsNotification.VisitUrl var0 = (RealmsNotification.VisitUrl)param0;
            Component var1 = var0.getMessage();
            int var2 = this.font.wordWrapHeight(var1, 216);
            int var3 = Mth.positiveCeilDiv(var2 + 7, 36) - 1;
            this.realmSelectionList.addEntry(new RealmsMainScreen.NotificationMessageEntry(var1, var3 + 2, var0));

            for(int var4 = 0; var4 < var3; ++var4) {
                this.realmSelectionList.addEntry(new RealmsMainScreen.EmptyEntry());
            }

            this.realmSelectionList.addEntry(new RealmsMainScreen.ButtonEntry(var0.buildOpenLinkButton(this)));
            return true;
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
        AbstractSelectionList.Entry var2 = this.realmSelectionList.getSelected();
        return var2 instanceof RealmsMainScreen.ServerEntry var0 ? var0.getServer() : null;
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
            if (param1 instanceof RealmsMainScreen.ServerEntry var0) {
                RealmsServer var1x = var0.getServer();
                return var1x.id == param0.id;
            } else {
                return false;
            }
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
            this.refreshListAndLayout();
        });
    }

    public void resetScreen() {
        this.realmSelectionList.setSelected(null);
        refreshServerList();
    }

    @Override
    public Component getNarrationMessage() {
        return (Component)(switch(this.activeLayoutState) {
            case LOADING -> CommonComponents.joinForNarration(super.getNarrationMessage(), LOADING_TEXT);
            case NO_REALMS -> CommonComponents.joinForNarration(super.getNarrationMessage(), NO_REALMS_TEXT);
            case LIST -> super.getNarrationMessage();
        });
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        if (isSnapshot()) {
            param0.drawString(this.font, "Minecraft " + SharedConstants.getCurrentVersion().getName(), 2, this.height - 10, -1);
        }

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

    private void openTrialAvailablePopup() {
        this.minecraft.setScreen(new RealmsPopupScreen(this, this.trialsAvailable));
    }

    public static void play(@Nullable RealmsServer param0, Screen param1) {
        play(param0, param1, false);
    }

    public static void play(@Nullable RealmsServer param0, Screen param1, boolean param2) {
        if (param0 != null) {
            if (!isSnapshot() || param2) {
                Minecraft.getInstance().setScreen(new RealmsLongRunningMcoTaskScreen(param1, new GetServerDetailsTask(param1, param0)));
                return;
            }

            switch(param0.compatibility) {
                case COMPATIBLE:
                    Minecraft.getInstance().setScreen(new RealmsLongRunningMcoTaskScreen(param1, new GetServerDetailsTask(param1, param0)));
                    break;
                case UNVERIFIABLE:
                    confirmToPlay(
                        param0,
                        param1,
                        Component.translatable("mco.compatibility.unverifiable.title").withColor(-171),
                        Component.translatable("mco.compatibility.unverifiable.message"),
                        CommonComponents.GUI_CONTINUE
                    );
                    break;
                case NEEDS_DOWNGRADE:
                    confirmToPlay(
                        param0,
                        param1,
                        Component.translatable("selectWorld.backupQuestion.downgrade").withColor(-2142128),
                        Component.translatable(
                            "mco.compatibility.downgrade.description",
                            Component.literal(param0.activeVersion).withColor(-171),
                            Component.literal(SharedConstants.getCurrentVersion().getName()).withColor(-171)
                        ),
                        Component.translatable("mco.compatibility.downgrade")
                    );
                    break;
                case NEEDS_UPGRADE:
                    confirmToPlay(
                        param0,
                        param1,
                        Component.translatable("mco.compatibility.upgrade.title").withColor(-171),
                        Component.translatable(
                            "mco.compatibility.upgrade.description",
                            Component.literal(param0.activeVersion).withColor(-171),
                            Component.literal(SharedConstants.getCurrentVersion().getName()).withColor(-171)
                        ),
                        Component.translatable("mco.compatibility.upgrade")
                    );
            }
        }

    }

    private static void confirmToPlay(RealmsServer param0, Screen param1, Component param2, Component param3, Component param4) {
        Minecraft.getInstance().setScreen(new ConfirmScreen(param2x -> {
            Object var1x;
            if (param2x) {
                var1x = new RealmsLongRunningMcoTaskScreen(param1, new GetServerDetailsTask(param1, param0));
                refreshServerList();
            } else {
                var1x = param1;
            }

            Minecraft.getInstance().setScreen(var1x);
        }, param2, param3, param4, CommonComponents.GUI_CANCEL));
    }

    public static Component getVersionComponent(String param0, boolean param1) {
        return getVersionComponent(param0, param1 ? -8355712 : -2142128);
    }

    public static Component getVersionComponent(String param0, int param1) {
        return (Component)(StringUtils.isBlank(param0)
            ? CommonComponents.EMPTY
            : Component.translatable("mco.version", Component.literal(param0).withColor(param1)));
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

    @OnlyIn(Dist.CLIENT)
    class AvailableSnapshotEntry extends RealmsMainScreen.Entry {
        private static final Component START_SNAPSHOT_REALM = Component.translatable("mco.snapshot.start");
        private static final int TEXT_PADDING = 5;
        private final Tooltip tooltip;
        private final RealmsServer parent;

        public AvailableSnapshotEntry(RealmsServer param0) {
            this.parent = param0;
            this.tooltip = Tooltip.create(Component.translatable("mco.snapshot.tooltip"));
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            param0.blitSprite(RealmsMainScreen.NEW_REALM_SPRITE, param3 - 5, param2 + param5 / 2 - 10, 40, 20);
            int var0 = param2 + param5 / 2 - 9 / 2;
            param0.drawString(RealmsMainScreen.this.font, START_SNAPSHOT_REALM, param3 + 40 - 2, var0 - 5, 8388479);
            param0.drawString(
                RealmsMainScreen.this.font, Component.translatable("mco.snapshot.description", this.parent.name), param3 + 40 - 2, var0 + 5, -8355712
            );
            this.tooltip.refreshTooltipForNextRenderPass(param8, this.isFocused(), new ScreenRectangle(param3, param2, param4, param5));
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            this.addSnapshotRealm();
            return true;
        }

        @Override
        public boolean keyPressed(int param0, int param1, int param2) {
            if (CommonInputs.selected(param0)) {
                this.addSnapshotRealm();
                return true;
            } else {
                return super.keyPressed(param0, param1, param2);
            }
        }

        private void addSnapshotRealm() {
            RealmsMainScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            RealmsMainScreen.this.minecraft
                .setScreen(
                    new PopupScreen.Builder(RealmsMainScreen.this, Component.translatable("mco.snapshot.createSnapshotPopup.title"))
                        .setMessage(Component.translatable("mco.snapshot.createSnapshotPopup.text"))
                        .addButton(
                            Component.translatable("mco.selectServer.create"),
                            param0 -> RealmsMainScreen.this.minecraft.setScreen(new RealmsCreateRealmScreen(RealmsMainScreen.this, this.parent.id))
                        )
                        .addButton(CommonComponents.GUI_CANCEL, PopupScreen::onClose)
                        .build()
                );
        }

        @Override
        public Component getNarration() {
            return Component.translatable(
                "gui.narrate.button",
                CommonComponents.joinForNarration(START_SNAPSHOT_REALM, Component.translatable("mco.snapshot.description", this.parent.name))
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    class ButtonEntry extends RealmsMainScreen.Entry {
        private final Button button;

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
            this.button.setPosition(RealmsMainScreen.this.width / 2 - 75, param2 + 4);
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
    class EmptyEntry extends RealmsMainScreen.Entry {
        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
        }

        @Override
        public Component getNarration() {
            return Component.empty();
        }
    }

    @OnlyIn(Dist.CLIENT)
    abstract class Entry extends ObjectSelectionList.Entry<RealmsMainScreen.Entry> {
        private static final int STATUS_LIGHT_WIDTH = 10;
        private static final int STATUS_LIGHT_HEIGHT = 28;
        private static final int PADDING = 7;

        protected void renderStatusLights(RealmsServer param0, GuiGraphics param1, int param2, int param3, int param4, int param5) {
            int var0 = param2 - 10 - 7;
            int var1 = param3 + 2;
            if (param0.expired) {
                this.drawRealmStatus(param1, var0, var1, param4, param5, RealmsMainScreen.EXPIRED_SPRITE, () -> RealmsMainScreen.SERVER_EXPIRED_TOOLTIP);
            } else if (param0.state == RealmsServer.State.CLOSED) {
                this.drawRealmStatus(param1, var0, var1, param4, param5, RealmsMainScreen.CLOSED_SPRITE, () -> RealmsMainScreen.SERVER_CLOSED_TOOLTIP);
            } else if (RealmsMainScreen.this.isSelfOwnedServer(param0) && param0.daysLeft < 7) {
                this.drawRealmStatus(
                    param1,
                    var0,
                    var1,
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
                this.drawRealmStatus(param1, var0, var1, param4, param5, RealmsMainScreen.OPEN_SPRITE, () -> RealmsMainScreen.SERVER_OPEN_TOOLTIP);
            }

        }

        private void drawRealmStatus(GuiGraphics param0, int param1, int param2, int param3, int param4, ResourceLocation param5, Supplier<Component> param6) {
            param0.blitSprite(param5, param1, param2, 10, 28);
            if (RealmsMainScreen.this.realmSelectionList.isMouseOver((double)param3, (double)param4)
                && param3 >= param1
                && param3 <= param1 + 10
                && param4 >= param2
                && param4 <= param2 + 28) {
                RealmsMainScreen.this.setTooltipForNextRenderPass(param6.get());
            }

        }

        protected void renderThirdLine(GuiGraphics param0, int param1, int param2, RealmsServer param3) {
            int var0 = this.textX(param2);
            int var1 = this.firstLineY(param1);
            int var2 = this.thirdLineY(var1);
            if (!RealmsMainScreen.this.isSelfOwnedServer(param3)) {
                param0.drawString(RealmsMainScreen.this.font, param3.owner, var0, this.thirdLineY(var1), -8355712, false);
            } else if (param3.expired) {
                Component var3 = param3.expiredTrial ? RealmsMainScreen.TRIAL_EXPIRED_TEXT : RealmsMainScreen.SUBSCRIPTION_EXPIRED_TEXT;
                param0.drawString(RealmsMainScreen.this.font, var3, var0, var2, -2142128, false);
            }

        }

        protected void renderClampedName(GuiGraphics param0, String param1, int param2, int param3, int param4, int param5) {
            int var0 = param4 - param2;
            if (RealmsMainScreen.this.font.width(param1) > var0) {
                String var1 = RealmsMainScreen.this.font.plainSubstrByWidth(param1, var0 - RealmsMainScreen.this.font.width("... "));
                param0.drawString(RealmsMainScreen.this.font, var1 + "...", param2, param3, param5, false);
            } else {
                param0.drawString(RealmsMainScreen.this.font, param1, param2, param3, param5, false);
            }

        }

        protected int versionTextX(int param0, int param1, Component param2) {
            return param0 + param1 - RealmsMainScreen.this.font.width(param2) - 20;
        }

        protected int firstLineY(int param0) {
            return param0 + 1;
        }

        protected int lineHeight() {
            return 2 + 9;
        }

        protected int textX(int param0) {
            return param0 + 36 + 2;
        }

        protected int secondLineY(int param0) {
            return param0 + this.lineHeight();
        }

        protected int thirdLineY(int param0) {
            return param0 + this.lineHeight() * 2;
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

        int notificationCount() {
            return this.notificationCount;
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
        private static final int OUTLINE_COLOR = -12303292;
        private final Component text;
        private final int frameItemHeight;
        private final List<AbstractWidget> children = new ArrayList<>();
        @Nullable
        private final RealmsMainScreen.CrossButton dismissButton;
        private final MultiLineTextWidget textWidget;
        private final GridLayout gridLayout;
        private final FrameLayout textFrame;
        private int lastEntryWidth = -1;

        public NotificationMessageEntry(Component param0, int param1, RealmsNotification param2) {
            this.text = param0;
            this.frameItemHeight = param1;
            this.gridLayout = new GridLayout();
            int param3 = 7;
            this.gridLayout.addChild(ImageWidget.sprite(20, 20, RealmsMainScreen.INFO_SPRITE), 0, 0, this.gridLayout.newCellSettings().padding(7, 7, 0, 0));
            this.gridLayout.addChild(SpacerElement.width(40), 0, 0);
            this.textFrame = this.gridLayout.addChild(new FrameLayout(0, 9 * 3 * (param1 - 1)), 0, 1, this.gridLayout.newCellSettings().paddingTop(7));
            this.textWidget = this.textFrame
                .addChild(
                    new MultiLineTextWidget(param0, RealmsMainScreen.this.font).setCentered(true),
                    this.textFrame.newChildLayoutSettings().alignHorizontallyCenter().alignVerticallyTop()
                );
            this.gridLayout.addChild(SpacerElement.width(40), 0, 2);
            if (param2.dismissable()) {
                this.dismissButton = this.gridLayout
                    .addChild(
                        new RealmsMainScreen.CrossButton(
                            param1x -> RealmsMainScreen.this.dismissNotification(param2.uuid()), Component.translatable("mco.notification.dismiss")
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
            param0.renderOutline(param3 - 2, param2 - 2, param4, 36 * this.frameItemHeight - 2, -12303292);
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
    class ParentEntry extends RealmsMainScreen.Entry {
        private final RealmsServer server;
        private final Tooltip tooltip;

        public ParentEntry(RealmsServer param0) {
            this.server = param0;
            this.tooltip = Tooltip.create(Component.translatable("mco.snapshot.parent.tooltip"));
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            return true;
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            int var0 = this.textX(param3);
            int var1 = this.firstLineY(param2);
            RealmsUtil.renderPlayerFace(param0, param3, param2, 32, this.server.ownerUUID);
            Component var2 = RealmsMainScreen.getVersionComponent(this.server.activeVersion, -8355712);
            int var3 = this.versionTextX(param3, param4, var2);
            this.renderClampedName(param0, this.server.getName(), var0, var1, var3, -8355712);
            if (var2 != CommonComponents.EMPTY) {
                param0.drawString(RealmsMainScreen.this.font, var2, var3, var1, -8355712, false);
            }

            param0.drawString(RealmsMainScreen.this.font, this.server.getDescription(), var0, this.secondLineY(var1), -8355712, false);
            this.renderThirdLine(param0, param2, param3, this.server);
            this.renderStatusLights(this.server, param0, param3 + param4, param2, param6, param7);
            this.tooltip.refreshTooltipForNextRenderPass(param8, this.isFocused(), new ScreenRectangle(param3, param2, param4, param5));
        }

        @Override
        public Component getNarration() {
            return Component.literal(this.server.name);
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
        @Nullable
        private final Tooltip tooltip;

        public ServerEntry(RealmsServer param0) {
            this.serverData = param0;
            boolean param1 = RealmsMainScreen.this.isSelfOwnedServer(param0);
            if (RealmsMainScreen.isSnapshot() && param1 && param0.isSnapshotRealm()) {
                this.tooltip = Tooltip.create(Component.translatable("mco.snapshot.paired", param0.parentWorldName));
            } else if (RealmsMainScreen.isSnapshot() && !param1 && param0.needsUpgrade()) {
                this.tooltip = Tooltip.create(Component.translatable("mco.snapshot.friendsRealm.upgrade", param0.owner));
            } else if (RealmsMainScreen.isSnapshot() && !param1 && param0.needsDowngrade()) {
                this.tooltip = Tooltip.create(Component.translatable("mco.snapshot.friendsRealm.downgrade", param0.activeVersion));
            } else {
                this.tooltip = null;
            }

        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
                param0.blitSprite(RealmsMainScreen.NEW_REALM_SPRITE, param3 - 5, param2 + param5 / 2 - 10, 40, 20);
                int var0 = param2 + param5 / 2 - 9 / 2;
                param0.drawString(RealmsMainScreen.this.font, RealmsMainScreen.SERVER_UNITIALIZED_TEXT, param3 + 40 - 2, var0, 8388479);
            } else {
                RealmsUtil.renderPlayerFace(param0, param3, param2, 32, this.serverData.ownerUUID);
                this.renderFirstLine(param0, param2, param3, param4);
                this.renderSecondLine(param0, param2, param3);
                this.renderThirdLine(param0, param2, param3, this.serverData);
                this.renderStatusLights(this.serverData, param0, param3 + param4, param2, param6, param7);
                if (this.tooltip != null) {
                    this.tooltip.refreshTooltipForNextRenderPass(param8, this.isFocused(), new ScreenRectangle(param3, param2, param4, param5));
                }

            }
        }

        private void renderFirstLine(GuiGraphics param0, int param1, int param2, int param3) {
            int var0 = this.textX(param2);
            int var1 = this.firstLineY(param1);
            Component var2 = RealmsMainScreen.getVersionComponent(this.serverData.activeVersion, this.serverData.isCompatible());
            int var3 = this.versionTextX(param2, param3, var2);
            this.renderClampedName(param0, this.serverData.getName(), var0, var1, var3, -1);
            if (var2 != CommonComponents.EMPTY) {
                param0.drawString(RealmsMainScreen.this.font, var2, var3, var1, -8355712, false);
            }

        }

        private void renderSecondLine(GuiGraphics param0, int param1, int param2) {
            int var0 = this.textX(param2);
            int var1 = this.firstLineY(param1);
            int var2 = this.secondLineY(var1);
            if (this.serverData.worldType == RealmsServer.WorldType.MINIGAME) {
                Component var3 = Component.literal(this.serverData.getMinigameName()).withStyle(ChatFormatting.GRAY);
                param0.drawString(
                    RealmsMainScreen.this.font, Component.translatable("mco.selectServer.minigameName", var3).withColor(-171), var0, var2, -1, false
                );
            } else {
                param0.drawString(RealmsMainScreen.this.font, this.serverData.getDescription(), var0, this.secondLineY(var1), -8355712, false);
            }

        }

        private void playRealm() {
            RealmsMainScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            RealmsMainScreen.play(this.serverData, RealmsMainScreen.this);
        }

        private void createUnitializedRealm() {
            RealmsMainScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            RealmsCreateRealmScreen var0 = new RealmsCreateRealmScreen(RealmsMainScreen.this, this.serverData);
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

        @Override
        public Component getNarration() {
            return (Component)(this.serverData.state == RealmsServer.State.UNINITIALIZED
                ? RealmsMainScreen.UNITIALIZED_WORLD_NARRATION
                : Component.translatable("narrator.select", this.serverData.name));
        }

        public RealmsServer getServer() {
            return this.serverData;
        }
    }
}
