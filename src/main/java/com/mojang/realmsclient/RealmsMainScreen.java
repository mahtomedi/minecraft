package com.mojang.realmsclient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.realmsclient.client.Ping;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerList;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.dto.RegionPingResult;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.screens.RealmsClientOutdatedScreen;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsCreateRealmScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsParentalConsentScreen;
import com.mojang.realmsclient.gui.screens.RealmsPendingInvitesScreen;
import com.mojang.realmsclient.util.RealmsPersistence;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsMainScreen extends RealmsScreen {
    static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation ON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/on_icon.png");
    private static final ResourceLocation OFF_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/off_icon.png");
    private static final ResourceLocation EXPIRED_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expired_icon.png");
    private static final ResourceLocation EXPIRES_SOON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expires_soon_icon.png");
    private static final ResourceLocation LEAVE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/leave_icon.png");
    private static final ResourceLocation INVITATION_ICONS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invitation_icons.png");
    private static final ResourceLocation INVITE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invite_icon.png");
    static final ResourceLocation WORLDICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/world_icon.png");
    private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("realms", "textures/gui/title/realms.png");
    private static final ResourceLocation CONFIGURE_LOCATION = new ResourceLocation("realms", "textures/gui/realms/configure_icon.png");
    private static final ResourceLocation QUESTIONMARK_LOCATION = new ResourceLocation("realms", "textures/gui/realms/questionmark.png");
    private static final ResourceLocation NEWS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/news_icon.png");
    private static final ResourceLocation POPUP_LOCATION = new ResourceLocation("realms", "textures/gui/realms/popup.png");
    private static final ResourceLocation DARKEN_LOCATION = new ResourceLocation("realms", "textures/gui/realms/darken.png");
    static final ResourceLocation CROSS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/cross_icon.png");
    private static final ResourceLocation TRIAL_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/trial_icon.png");
    static final ResourceLocation BUTTON_LOCATION = new ResourceLocation("minecraft", "textures/gui/widgets.png");
    static final Component NO_PENDING_INVITES_TEXT = new TranslatableComponent("mco.invites.nopending");
    static final Component PENDING_INVITES_TEXT = new TranslatableComponent("mco.invites.pending");
    static final List<Component> TRIAL_MESSAGE_LINES = ImmutableList.of(
        new TranslatableComponent("mco.trial.message.line1"), new TranslatableComponent("mco.trial.message.line2")
    );
    static final Component SERVER_UNITIALIZED_TEXT = new TranslatableComponent("mco.selectServer.uninitialized");
    static final Component SUBSCRIPTION_EXPIRED_TEXT = new TranslatableComponent("mco.selectServer.expiredList");
    static final Component SUBSCRIPTION_RENEW_TEXT = new TranslatableComponent("mco.selectServer.expiredRenew");
    static final Component TRIAL_EXPIRED_TEXT = new TranslatableComponent("mco.selectServer.expiredTrial");
    static final Component SUBSCRIPTION_CREATE_TEXT = new TranslatableComponent("mco.selectServer.expiredSubscribe");
    static final Component SELECT_MINIGAME_PREFIX = new TranslatableComponent("mco.selectServer.minigame").append(" ");
    private static final Component POPUP_TEXT = new TranslatableComponent("mco.selectServer.popup");
    private static final Component SERVER_EXPIRED_TOOLTIP = new TranslatableComponent("mco.selectServer.expired");
    private static final Component SERVER_EXPIRES_SOON_TOOLTIP = new TranslatableComponent("mco.selectServer.expires.soon");
    private static final Component SERVER_EXPIRES_IN_DAY_TOOLTIP = new TranslatableComponent("mco.selectServer.expires.day");
    private static final Component SERVER_OPEN_TOOLTIP = new TranslatableComponent("mco.selectServer.open");
    private static final Component SERVER_CLOSED_TOOLTIP = new TranslatableComponent("mco.selectServer.closed");
    private static final Component LEAVE_SERVER_TOOLTIP = new TranslatableComponent("mco.selectServer.leave");
    private static final Component CONFIGURE_SERVER_TOOLTIP = new TranslatableComponent("mco.selectServer.configure");
    private static final Component SERVER_INFO_TOOLTIP = new TranslatableComponent("mco.selectServer.info");
    private static final Component NEWS_TOOLTIP = new TranslatableComponent("mco.news");
    static final Component UNITIALIZED_WORLD_NARRATION = new TranslatableComponent("gui.narrate.button", SERVER_UNITIALIZED_TEXT);
    static final Component TRIAL_TEXT = CommonComponents.joinLines(TRIAL_MESSAGE_LINES);
    private static List<ResourceLocation> teaserImages = ImmutableList.of();
    static final RealmsDataFetcher REALMS_DATA_FETCHER = new RealmsDataFetcher(Minecraft.getInstance(), RealmsClient.create());
    static boolean overrideConfigure;
    private static int lastScrollYPosition = -1;
    static volatile boolean hasParentalConsent;
    static volatile boolean checkedParentalConsent;
    static volatile boolean checkedClientCompatability;
    static Screen realmsGenericErrorScreen;
    private static boolean regionsPinged;
    private final RateLimiter inviteNarrationLimiter;
    private boolean dontSetConnectedToRealms;
    final Screen lastScreen;
    RealmsMainScreen.RealmSelectionList realmSelectionList;
    private boolean realmsSelectionListAdded;
    private Button playButton;
    private Button backButton;
    private Button renewButton;
    private Button configureButton;
    private Button leaveButton;
    private List<Component> toolTip;
    private List<RealmsServer> realmsServers = ImmutableList.of();
    volatile int numberOfPendingInvites;
    int animTick;
    private boolean hasFetchedServers;
    boolean popupOpenedByUser;
    private boolean justClosedPopup;
    private volatile boolean trialsAvailable;
    private volatile boolean createdTrial;
    private volatile boolean showingPopup;
    volatile boolean hasUnreadNews;
    volatile String newsLink;
    private int carouselIndex;
    private int carouselTick;
    private boolean hasSwitchedCarouselImage;
    private List<KeyCombo> keyCombos;
    int clicks;
    private ReentrantLock connectLock = new ReentrantLock();
    private MultiLineLabel formattedPopup = MultiLineLabel.EMPTY;
    RealmsMainScreen.HoveredElement hoveredElement;
    private Button showPopupButton;
    @Nullable
    private RealmsMainScreen.PendingInvitesButton pendingInvitesButton;
    private Button newsButton;
    private Button createTrialButton;
    private Button buyARealmButton;
    private Button closeButton;

    public RealmsMainScreen(Screen param0) {
        super(NarratorChatListener.NO_TITLE);
        this.lastScreen = param0;
        this.inviteNarrationLimiter = RateLimiter.create(0.016666668F);
    }

    private boolean shouldShowMessageInList() {
        if (hasParentalConsent() && this.hasFetchedServers) {
            if (this.trialsAvailable && !this.createdTrial) {
                return true;
            } else {
                for(RealmsServer var0 : this.realmsServers) {
                    if (var0.ownerUUID.equals(this.minecraft.getUser().getUuid())) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }

    public boolean shouldShowPopup() {
        if (!hasParentalConsent() || !this.hasFetchedServers) {
            return false;
        } else {
            return this.popupOpenedByUser ? true : this.realmsServers.isEmpty();
        }
    }

    @Override
    public void init() {
        this.keyCombos = Lists.newArrayList(
            new KeyCombo(new char[]{'3', '2', '1', '4', '5', '6'}, () -> overrideConfigure = !overrideConfigure),
            new KeyCombo(new char[]{'9', '8', '7', '1', '2', '3'}, () -> {
                if (RealmsClient.currentEnvironment == RealmsClient.Environment.STAGE) {
                    this.switchToProd();
                } else {
                    this.switchToStage();
                }
    
            }),
            new KeyCombo(new char[]{'9', '8', '7', '4', '5', '6'}, () -> {
                if (RealmsClient.currentEnvironment == RealmsClient.Environment.LOCAL) {
                    this.switchToProd();
                } else {
                    this.switchToLocal();
                }
    
            })
        );
        if (realmsGenericErrorScreen != null) {
            this.minecraft.setScreen(realmsGenericErrorScreen);
        } else {
            this.connectLock = new ReentrantLock();
            if (checkedClientCompatability && !hasParentalConsent()) {
                this.checkParentalConsent();
            }

            this.checkClientCompatability();
            if (!this.dontSetConnectedToRealms) {
                this.minecraft.setConnectedToRealms(false);
            }

            this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
            if (hasParentalConsent()) {
                REALMS_DATA_FETCHER.forceUpdate();
            }

            this.showingPopup = false;
            if (hasParentalConsent() && this.hasFetchedServers) {
                this.addButtons();
            }

            this.realmSelectionList = new RealmsMainScreen.RealmSelectionList();
            if (lastScrollYPosition != -1) {
                this.realmSelectionList.setScrollAmount((double)lastScrollYPosition);
            }

            this.addWidget(this.realmSelectionList);
            this.realmsSelectionListAdded = true;
            this.magicalSpecialHackyFocus(this.realmSelectionList);
            this.formattedPopup = MultiLineLabel.create(this.font, POPUP_TEXT, 100);
        }
    }

    private static boolean hasParentalConsent() {
        return checkedParentalConsent && hasParentalConsent;
    }

    public void addButtons() {
        this.leaveButton = this.addRenderableWidget(
            new Button(
                this.width / 2 - 202,
                this.height - 32,
                90,
                20,
                new TranslatableComponent("mco.selectServer.leave"),
                param0 -> this.leaveClicked(this.getSelectedServer())
            )
        );
        this.configureButton = this.addRenderableWidget(
            new Button(
                this.width / 2 - 190,
                this.height - 32,
                90,
                20,
                new TranslatableComponent("mco.selectServer.configure"),
                param0 -> this.configureClicked(this.getSelectedServer())
            )
        );
        this.playButton = this.addRenderableWidget(
            new Button(
                this.width / 2 - 93,
                this.height - 32,
                90,
                20,
                new TranslatableComponent("mco.selectServer.play"),
                param0 -> this.play(this.getSelectedServer(), this)
            )
        );
        this.backButton = this.addRenderableWidget(new Button(this.width / 2 + 4, this.height - 32, 90, 20, CommonComponents.GUI_BACK, param0 -> {
            if (!this.justClosedPopup) {
                this.minecraft.setScreen(this.lastScreen);
            }

        }));
        this.renewButton = this.addRenderableWidget(
            new Button(
                this.width / 2 + 100,
                this.height - 32,
                90,
                20,
                new TranslatableComponent("mco.selectServer.expiredRenew"),
                param0 -> this.onRenew(this.getSelectedServer())
            )
        );
        this.pendingInvitesButton = this.addRenderableWidget(new RealmsMainScreen.PendingInvitesButton());
        this.newsButton = this.addRenderableWidget(new RealmsMainScreen.NewsButton());
        this.showPopupButton = this.addRenderableWidget(new RealmsMainScreen.ShowPopupButton());
        this.closeButton = this.addRenderableWidget(new RealmsMainScreen.CloseButton());
        this.createTrialButton = this.addRenderableWidget(
            new Button(this.width / 2 + 52, this.popupY0() + 137 - 20, 98, 20, new TranslatableComponent("mco.selectServer.trial"), param0 -> {
                if (this.trialsAvailable && !this.createdTrial) {
                    Util.getPlatform().openUri("https://aka.ms/startjavarealmstrial");
                    this.minecraft.setScreen(this.lastScreen);
                }
            })
        );
        this.buyARealmButton = this.addRenderableWidget(
            new Button(
                this.width / 2 + 52,
                this.popupY0() + 160 - 20,
                98,
                20,
                new TranslatableComponent("mco.selectServer.buy"),
                param0 -> Util.getPlatform().openUri("https://aka.ms/BuyJavaRealms")
            )
        );
        this.updateButtonStates(null);
    }

    void updateButtonStates(@Nullable RealmsServer param0) {
        this.playButton.active = this.shouldPlayButtonBeActive(param0) && !this.shouldShowPopup();
        this.renewButton.visible = this.shouldRenewButtonBeActive(param0);
        this.configureButton.visible = this.shouldConfigureButtonBeVisible(param0);
        this.leaveButton.visible = this.shouldLeaveButtonBeVisible(param0);
        boolean var0 = this.shouldShowPopup() && this.trialsAvailable && !this.createdTrial;
        this.createTrialButton.visible = var0;
        this.createTrialButton.active = var0;
        this.buyARealmButton.visible = this.shouldShowPopup();
        this.closeButton.visible = this.shouldShowPopup() && this.popupOpenedByUser;
        this.renewButton.active = !this.shouldShowPopup();
        this.configureButton.active = !this.shouldShowPopup();
        this.leaveButton.active = !this.shouldShowPopup();
        this.newsButton.active = true;
        this.pendingInvitesButton.active = true;
        this.backButton.active = true;
        this.showPopupButton.active = !this.shouldShowPopup();
    }

    private boolean shouldShowPopupButton() {
        return (!this.shouldShowPopup() || this.popupOpenedByUser) && hasParentalConsent() && this.hasFetchedServers;
    }

    boolean shouldPlayButtonBeActive(@Nullable RealmsServer param0) {
        return param0 != null && !param0.expired && param0.state == RealmsServer.State.OPEN;
    }

    private boolean shouldRenewButtonBeActive(@Nullable RealmsServer param0) {
        return param0 != null && param0.expired && this.isSelfOwnedServer(param0);
    }

    private boolean shouldConfigureButtonBeVisible(@Nullable RealmsServer param0) {
        return param0 != null && this.isSelfOwnedServer(param0);
    }

    private boolean shouldLeaveButtonBeVisible(@Nullable RealmsServer param0) {
        return param0 != null && !this.isSelfOwnedServer(param0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.pendingInvitesButton != null) {
            this.pendingInvitesButton.tick();
        }

        this.justClosedPopup = false;
        ++this.animTick;
        --this.clicks;
        if (this.clicks < 0) {
            this.clicks = 0;
        }

        if (hasParentalConsent()) {
            REALMS_DATA_FETCHER.init();
            if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.SERVER_LIST)) {
                List<RealmsServer> var0 = REALMS_DATA_FETCHER.getServers();
                RealmsServer var1 = this.getSelectedServer();
                RealmsMainScreen.Entry var2 = null;
                this.realmSelectionList.clear();
                boolean var3 = !this.hasFetchedServers;
                if (var3) {
                    this.hasFetchedServers = true;
                }

                if (var0 != null) {
                    boolean var4 = false;

                    for(RealmsServer var5 : var0) {
                        if (this.isSelfOwnedNonExpiredServer(var5)) {
                            var4 = true;
                        }
                    }

                    this.realmsServers = var0;
                    if (this.shouldShowMessageInList()) {
                        this.realmSelectionList.addEntry(new RealmsMainScreen.TrialEntry());
                    }

                    for(RealmsServer var6 : this.realmsServers) {
                        RealmsMainScreen.ServerEntry var7 = new RealmsMainScreen.ServerEntry(var6);
                        this.realmSelectionList.addEntry(var7);
                        if (var1 != null && var1.id == var6.id) {
                            var2 = var7;
                        }
                    }

                    if (!regionsPinged && var4) {
                        regionsPinged = true;
                        this.pingRegions();
                    }
                }

                if (var3) {
                    this.addButtons();
                } else {
                    this.realmSelectionList.setSelected(var2);
                }
            }

            if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.PENDING_INVITE)) {
                this.numberOfPendingInvites = REALMS_DATA_FETCHER.getPendingInvitesCount();
                if (this.numberOfPendingInvites > 0 && this.inviteNarrationLimiter.tryAcquire(1)) {
                    NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("mco.configure.world.invite.narration", this.numberOfPendingInvites));
                }
            }

            if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.TRIAL_AVAILABLE) && !this.createdTrial) {
                boolean var8 = REALMS_DATA_FETCHER.isTrialAvailable();
                if (var8 != this.trialsAvailable && this.shouldShowPopup()) {
                    this.trialsAvailable = var8;
                    this.showingPopup = false;
                } else {
                    this.trialsAvailable = var8;
                }
            }

            if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.LIVE_STATS)) {
                RealmsServerPlayerLists var9 = REALMS_DATA_FETCHER.getLivestats();

                for(RealmsServerPlayerList var10 : var9.servers) {
                    for(RealmsServer var11 : this.realmsServers) {
                        if (var11.id == var10.serverId) {
                            var11.updateServerPing(var10);
                            break;
                        }
                    }
                }
            }

            if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.UNREAD_NEWS)) {
                this.hasUnreadNews = REALMS_DATA_FETCHER.hasUnreadNews();
                this.newsLink = REALMS_DATA_FETCHER.newsLink();
            }

            REALMS_DATA_FETCHER.markClean();
            if (this.shouldShowPopup()) {
                ++this.carouselTick;
            }

            if (this.showPopupButton != null) {
                this.showPopupButton.visible = this.shouldShowPopupButton();
            }

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

        for(RealmsServer var1 : this.realmsServers) {
            if (this.isSelfOwnedNonExpiredServer(var1)) {
                var0.add(var1.id);
            }
        }

        return var0;
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.stopRealmsFetcher();
    }

    public void setCreatedTrial(boolean param0) {
        this.createdTrial = param0;
    }

    void onRenew(@Nullable RealmsServer param0) {
        if (param0 != null) {
            String var0 = "https://aka.ms/ExtendJavaRealms?subscriptionId="
                + param0.remoteSubscriptionId
                + "&profileId="
                + this.minecraft.getUser().getUuid()
                + "&ref="
                + (param0.expiredTrial ? "expiredTrial" : "expiredRealm");
            this.minecraft.keyboardHandler.setClipboard(var0);
            Util.getPlatform().openUri(var0);
        }

    }

    private void checkClientCompatability() {
        if (!checkedClientCompatability) {
            checkedClientCompatability = true;
            (new Thread("MCO Compatability Checker #1") {
                    @Override
                    public void run() {
                        RealmsClient var0 = RealmsClient.create();
    
                        try {
                            RealmsClient.CompatibleVersionResponse var1 = var0.clientCompatible();
                            if (var1 == RealmsClient.CompatibleVersionResponse.OUTDATED) {
                                RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen, true);
                                RealmsMainScreen.this.minecraft
                                    .execute(() -> RealmsMainScreen.this.minecraft.setScreen(RealmsMainScreen.realmsGenericErrorScreen));
                                return;
                            }
    
                            if (var1 == RealmsClient.CompatibleVersionResponse.OTHER) {
                                RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen, false);
                                RealmsMainScreen.this.minecraft
                                    .execute(() -> RealmsMainScreen.this.minecraft.setScreen(RealmsMainScreen.realmsGenericErrorScreen));
                                return;
                            }
    
                            RealmsMainScreen.this.checkParentalConsent();
                        } catch (RealmsServiceException var3) {
                            RealmsMainScreen.checkedClientCompatability = false;
                            RealmsMainScreen.LOGGER.error("Couldn't connect to realms", (Throwable)var3);
                            if (var3.httpResultCode == 401) {
                                RealmsMainScreen.realmsGenericErrorScreen = new RealmsGenericErrorScreen(
                                    new TranslatableComponent("mco.error.invalid.session.title"),
                                    new TranslatableComponent("mco.error.invalid.session.message"),
                                    RealmsMainScreen.this.lastScreen
                                );
                                RealmsMainScreen.this.minecraft
                                    .execute(() -> RealmsMainScreen.this.minecraft.setScreen(RealmsMainScreen.realmsGenericErrorScreen));
                            } else {
                                RealmsMainScreen.this.minecraft
                                    .execute(
                                        () -> RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.lastScreen))
                                    );
                            }
                        }
    
                    }
                })
                .start();
        }

    }

    void checkParentalConsent() {
        (new Thread("MCO Compatability Checker #1") {
                @Override
                public void run() {
                    RealmsClient var0 = RealmsClient.create();
    
                    try {
                        Boolean var1 = var0.mcoEnabled();
                        if (var1) {
                            RealmsMainScreen.LOGGER.info("Realms is available for this user");
                            RealmsMainScreen.hasParentalConsent = true;
                        } else {
                            RealmsMainScreen.LOGGER.info("Realms is not available for this user");
                            RealmsMainScreen.hasParentalConsent = false;
                            RealmsMainScreen.this.minecraft
                                .execute(() -> RealmsMainScreen.this.minecraft.setScreen(new RealmsParentalConsentScreen(RealmsMainScreen.this.lastScreen)));
                        }
    
                        RealmsMainScreen.checkedParentalConsent = true;
                    } catch (RealmsServiceException var3) {
                        RealmsMainScreen.LOGGER.error("Couldn't connect to realms", (Throwable)var3);
                        RealmsMainScreen.this.minecraft
                            .execute(() -> RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this.lastScreen)));
                    }
    
                }
            })
            .start();
    }

    private void switchToStage() {
        if (RealmsClient.currentEnvironment != RealmsClient.Environment.STAGE) {
            (new Thread("MCO Stage Availability Checker #1") {
                @Override
                public void run() {
                    RealmsClient var0 = RealmsClient.create();

                    try {
                        Boolean var1 = var0.stageAvailable();
                        if (var1) {
                            RealmsClient.switchToStage();
                            RealmsMainScreen.LOGGER.info("Switched to stage");
                            RealmsMainScreen.REALMS_DATA_FETCHER.forceUpdate();
                        }
                    } catch (RealmsServiceException var3) {
                        RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: {}", var3.toString());
                    }

                }
            }).start();
        }

    }

    private void switchToLocal() {
        if (RealmsClient.currentEnvironment != RealmsClient.Environment.LOCAL) {
            (new Thread("MCO Local Availability Checker #1") {
                @Override
                public void run() {
                    RealmsClient var0 = RealmsClient.create();

                    try {
                        Boolean var1 = var0.stageAvailable();
                        if (var1) {
                            RealmsClient.switchToLocal();
                            RealmsMainScreen.LOGGER.info("Switched to local");
                            RealmsMainScreen.REALMS_DATA_FETCHER.forceUpdate();
                        }
                    } catch (RealmsServiceException var3) {
                        RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: {}", var3.toString());
                    }

                }
            }).start();
        }

    }

    private void switchToProd() {
        RealmsClient.switchToProd();
        REALMS_DATA_FETCHER.forceUpdate();
    }

    private void stopRealmsFetcher() {
        REALMS_DATA_FETCHER.stop();
    }

    void configureClicked(@Nullable RealmsServer param0) {
        if (param0 != null && (this.minecraft.getUser().getUuid().equals(param0.ownerUUID) || overrideConfigure)) {
            this.saveListScrollPosition();
            this.minecraft.setScreen(new RealmsConfigureWorldScreen(this, param0.id));
        }

    }

    void leaveClicked(@Nullable RealmsServer param0) {
        if (param0 != null && !this.minecraft.getUser().getUuid().equals(param0.ownerUUID)) {
            this.saveListScrollPosition();
            Component var0 = new TranslatableComponent("mco.configure.world.leave.question.line1");
            Component var1 = new TranslatableComponent("mco.configure.world.leave.question.line2");
            this.minecraft
                .setScreen(
                    new RealmsLongConfirmationScreen(param1 -> this.leaveServer(param1, param0), RealmsLongConfirmationScreen.Type.Info, var0, var1, true)
                );
        }

    }

    private void saveListScrollPosition() {
        lastScrollYPosition = (int)this.realmSelectionList.getScrollAmount();
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
                            RealmsMainScreen.LOGGER.error("Couldn't configure world");
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
        REALMS_DATA_FETCHER.removeItem(param0);
        this.realmsServers.remove(param0);
        this.realmSelectionList.children().removeIf(param1 -> {
            RealmsServer var0 = param1.getServer();
            return var0 != null && var0.id == param0.id;
        });
        this.realmSelectionList.setSelected(null);
        this.updateButtonStates(null);
        this.playButton.active = false;
    }

    public void resetScreen() {
        if (this.realmSelectionList != null) {
            this.realmSelectionList.setSelected(null);
        }

    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.keyCombos.forEach(KeyCombo::reset);
            this.onClosePopup();
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    void onClosePopup() {
        if (this.shouldShowPopup() && this.popupOpenedByUser) {
            this.popupOpenedByUser = false;
        } else {
            this.minecraft.setScreen(this.lastScreen);
        }

    }

    @Override
    public boolean charTyped(char param0, int param1) {
        this.keyCombos.forEach(param1x -> param1x.keyPressed(param0));
        return true;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.hoveredElement = RealmsMainScreen.HoveredElement.NONE;
        this.toolTip = null;
        this.renderBackground(param0);
        this.realmSelectionList.render(param0, param1, param2, param3);
        this.drawRealmsLogo(param0, this.width / 2 - 50, 7);
        if (RealmsClient.currentEnvironment == RealmsClient.Environment.STAGE) {
            this.renderStage(param0);
        }

        if (RealmsClient.currentEnvironment == RealmsClient.Environment.LOCAL) {
            this.renderLocal(param0);
        }

        if (this.shouldShowPopup()) {
            this.drawPopup(param0, param1, param2);
        } else {
            if (this.showingPopup) {
                this.updateButtonStates(null);
                if (!this.realmsSelectionListAdded) {
                    this.addWidget(this.realmSelectionList);
                    this.realmsSelectionListAdded = true;
                }

                this.playButton.active = this.shouldPlayButtonBeActive(this.getSelectedServer());
            }

            this.showingPopup = false;
        }

        super.render(param0, param1, param2, param3);
        if (this.toolTip != null) {
            this.renderMousehoverTooltip(param0, this.toolTip, param1, param2);
        }

        if (this.trialsAvailable && !this.createdTrial && this.shouldShowPopup()) {
            RenderSystem.setShaderTexture(0, TRIAL_ICON_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int var0 = 8;
            int var1 = 8;
            int var2 = 0;
            if ((Util.getMillis() / 800L & 1L) == 1L) {
                var2 = 8;
            }

            GuiComponent.blit(
                param0,
                this.createTrialButton.x + this.createTrialButton.getWidth() - 8 - 4,
                this.createTrialButton.y + this.createTrialButton.getHeight() / 2 - 4,
                0.0F,
                (float)var2,
                8,
                8,
                8,
                16
            );
        }

    }

    private void drawRealmsLogo(PoseStack param0, int param1, int param2) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, LOGO_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        param0.pushPose();
        param0.scale(0.5F, 0.5F, 0.5F);
        GuiComponent.blit(param0, param1 * 2, param2 * 2 - 5, 0.0F, 0.0F, 200, 50, 200, 50);
        param0.popPose();
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.isOutsidePopup(param0, param1) && this.popupOpenedByUser) {
            this.popupOpenedByUser = false;
            this.justClosedPopup = true;
            return true;
        } else {
            return super.mouseClicked(param0, param1, param2);
        }
    }

    private boolean isOutsidePopup(double param0, double param1) {
        int var0 = this.popupX0();
        int var1 = this.popupY0();
        return param0 < (double)(var0 - 5) || param0 > (double)(var0 + 315) || param1 < (double)(var1 - 5) || param1 > (double)(var1 + 171);
    }

    private void drawPopup(PoseStack param0, int param1, int param2) {
        int var0 = this.popupX0();
        int var1 = this.popupY0();
        if (!this.showingPopup) {
            this.carouselIndex = 0;
            this.carouselTick = 0;
            this.hasSwitchedCarouselImage = true;
            this.updateButtonStates(null);
            if (this.realmsSelectionListAdded) {
                this.removeWidget(this.realmSelectionList);
                this.realmsSelectionListAdded = false;
            }

            NarratorChatListener.INSTANCE.sayNow(POPUP_TEXT);
        }

        if (this.hasFetchedServers) {
            this.showingPopup = true;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.7F);
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, DARKEN_LOCATION);
        int var2 = 0;
        int var3 = 32;
        GuiComponent.blit(param0, 0, 32, 0.0F, 0.0F, this.width, this.height - 40 - 32, 310, 166);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, POPUP_LOCATION);
        GuiComponent.blit(param0, var0, var1, 0.0F, 0.0F, 310, 166, 310, 166);
        if (!teaserImages.isEmpty()) {
            RenderSystem.setShaderTexture(0, teaserImages.get(this.carouselIndex));
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(param0, var0 + 7, var1 + 7, 0.0F, 0.0F, 195, 152, 195, 152);
            if (this.carouselTick % 95 < 5) {
                if (!this.hasSwitchedCarouselImage) {
                    this.carouselIndex = (this.carouselIndex + 1) % teaserImages.size();
                    this.hasSwitchedCarouselImage = true;
                }
            } else {
                this.hasSwitchedCarouselImage = false;
            }
        }

        this.formattedPopup.renderLeftAlignedNoShadow(param0, this.width / 2 + 52, var1 + 7, 10, 5000268);
    }

    int popupX0() {
        return (this.width - 310) / 2;
    }

    int popupY0() {
        return this.height / 2 - 80;
    }

    void drawInvitationPendingIcon(PoseStack param0, int param1, int param2, int param3, int param4, boolean param5, boolean param6) {
        int var0 = this.numberOfPendingInvites;
        boolean var1 = this.inPendingInvitationArea((double)param1, (double)param2);
        boolean var2 = param6 && param5;
        if (var2) {
            float var3 = 0.25F + (1.0F + Mth.sin((float)this.animTick * 0.5F)) * 0.25F;
            int var4 = 0xFF000000 | (int)(var3 * 64.0F) << 16 | (int)(var3 * 64.0F) << 8 | (int)(var3 * 64.0F) << 0;
            this.fillGradient(param0, param3 - 2, param4 - 2, param3 + 18, param4 + 18, var4, var4);
            var4 = 0xFF000000 | (int)(var3 * 255.0F) << 16 | (int)(var3 * 255.0F) << 8 | (int)(var3 * 255.0F) << 0;
            this.fillGradient(param0, param3 - 2, param4 - 2, param3 + 18, param4 - 1, var4, var4);
            this.fillGradient(param0, param3 - 2, param4 - 2, param3 - 1, param4 + 18, var4, var4);
            this.fillGradient(param0, param3 + 17, param4 - 2, param3 + 18, param4 + 18, var4, var4);
            this.fillGradient(param0, param3 - 2, param4 + 17, param3 + 18, param4 + 18, var4, var4);
        }

        RenderSystem.setShaderTexture(0, INVITE_ICON_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        boolean var5 = param6 && param5;
        float var6 = var5 ? 16.0F : 0.0F;
        GuiComponent.blit(param0, param3, param4 - 6, var6, 0.0F, 15, 25, 31, 25);
        boolean var7 = param6 && var0 != 0;
        if (var7) {
            int var8 = (Math.min(var0, 6) - 1) * 8;
            int var9 = (int)(Math.max(0.0F, Math.max(Mth.sin((float)(10 + this.animTick) * 0.57F), Mth.cos((float)this.animTick * 0.35F))) * -6.0F);
            RenderSystem.setShaderTexture(0, INVITATION_ICONS_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            float var10 = var1 ? 8.0F : 0.0F;
            GuiComponent.blit(param0, param3 + 4, param4 + 4 + var9, (float)var8, var10, 8, 8, 48, 16);
        }

        int var11 = param1 + 12;
        boolean var13 = param6 && var1;
        if (var13) {
            Component var14 = var0 == 0 ? NO_PENDING_INVITES_TEXT : PENDING_INVITES_TEXT;
            int var15 = this.font.width(var14);
            this.fillGradient(param0, var11 - 3, param2 - 3, var11 + var15 + 3, param2 + 8 + 3, -1073741824, -1073741824);
            this.font.drawShadow(param0, var14, (float)var11, (float)param2, -1);
        }

    }

    private boolean inPendingInvitationArea(double param0, double param1) {
        int var0 = this.width / 2 + 50;
        int var1 = this.width / 2 + 66;
        int var2 = 11;
        int var3 = 23;
        if (this.numberOfPendingInvites != 0) {
            var0 -= 3;
            var1 += 3;
            var2 -= 5;
            var3 += 5;
        }

        return (double)var0 <= param0 && param0 <= (double)var1 && (double)var2 <= param1 && param1 <= (double)var3;
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

            this.dontSetConnectedToRealms = true;
            this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(param1, new GetServerDetailsTask(this, param1, param0, this.connectLock)));
        }

    }

    boolean isSelfOwnedServer(RealmsServer param0) {
        return param0.ownerUUID != null && param0.ownerUUID.equals(this.minecraft.getUser().getUuid());
    }

    private boolean isSelfOwnedNonExpiredServer(RealmsServer param0) {
        return this.isSelfOwnedServer(param0) && !param0.expired;
    }

    void drawExpired(PoseStack param0, int param1, int param2, int param3, int param4) {
        RenderSystem.setShaderTexture(0, EXPIRED_ICON_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.blit(param0, param1, param2, 0.0F, 0.0F, 10, 28, 10, 28);
        if (param3 >= param1
            && param3 <= param1 + 9
            && param4 >= param2
            && param4 <= param2 + 27
            && param4 < this.height - 40
            && param4 > 32
            && !this.shouldShowPopup()) {
            this.setTooltip(SERVER_EXPIRED_TOOLTIP);
        }

    }

    void drawExpiring(PoseStack param0, int param1, int param2, int param3, int param4, int param5) {
        RenderSystem.setShaderTexture(0, EXPIRES_SOON_ICON_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.animTick % 20 < 10) {
            GuiComponent.blit(param0, param1, param2, 0.0F, 0.0F, 10, 28, 20, 28);
        } else {
            GuiComponent.blit(param0, param1, param2, 10.0F, 0.0F, 10, 28, 20, 28);
        }

        if (param3 >= param1
            && param3 <= param1 + 9
            && param4 >= param2
            && param4 <= param2 + 27
            && param4 < this.height - 40
            && param4 > 32
            && !this.shouldShowPopup()) {
            if (param5 <= 0) {
                this.setTooltip(SERVER_EXPIRES_SOON_TOOLTIP);
            } else if (param5 == 1) {
                this.setTooltip(SERVER_EXPIRES_IN_DAY_TOOLTIP);
            } else {
                this.setTooltip(new TranslatableComponent("mco.selectServer.expires.days", param5));
            }
        }

    }

    void drawOpen(PoseStack param0, int param1, int param2, int param3, int param4) {
        RenderSystem.setShaderTexture(0, ON_ICON_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.blit(param0, param1, param2, 0.0F, 0.0F, 10, 28, 10, 28);
        if (param3 >= param1
            && param3 <= param1 + 9
            && param4 >= param2
            && param4 <= param2 + 27
            && param4 < this.height - 40
            && param4 > 32
            && !this.shouldShowPopup()) {
            this.setTooltip(SERVER_OPEN_TOOLTIP);
        }

    }

    void drawClose(PoseStack param0, int param1, int param2, int param3, int param4) {
        RenderSystem.setShaderTexture(0, OFF_ICON_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.blit(param0, param1, param2, 0.0F, 0.0F, 10, 28, 10, 28);
        if (param3 >= param1
            && param3 <= param1 + 9
            && param4 >= param2
            && param4 <= param2 + 27
            && param4 < this.height - 40
            && param4 > 32
            && !this.shouldShowPopup()) {
            this.setTooltip(SERVER_CLOSED_TOOLTIP);
        }

    }

    void drawLeave(PoseStack param0, int param1, int param2, int param3, int param4) {
        boolean var0 = false;
        if (param3 >= param1
            && param3 <= param1 + 28
            && param4 >= param2
            && param4 <= param2 + 28
            && param4 < this.height - 40
            && param4 > 32
            && !this.shouldShowPopup()) {
            var0 = true;
        }

        RenderSystem.setShaderTexture(0, LEAVE_ICON_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        float var1 = var0 ? 28.0F : 0.0F;
        GuiComponent.blit(param0, param1, param2, var1, 0.0F, 28, 28, 56, 28);
        if (var0) {
            this.setTooltip(LEAVE_SERVER_TOOLTIP);
            this.hoveredElement = RealmsMainScreen.HoveredElement.LEAVE;
        }

    }

    void drawConfigure(PoseStack param0, int param1, int param2, int param3, int param4) {
        boolean var0 = false;
        if (param3 >= param1
            && param3 <= param1 + 28
            && param4 >= param2
            && param4 <= param2 + 28
            && param4 < this.height - 40
            && param4 > 32
            && !this.shouldShowPopup()) {
            var0 = true;
        }

        RenderSystem.setShaderTexture(0, CONFIGURE_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        float var1 = var0 ? 28.0F : 0.0F;
        GuiComponent.blit(param0, param1, param2, var1, 0.0F, 28, 28, 56, 28);
        if (var0) {
            this.setTooltip(CONFIGURE_SERVER_TOOLTIP);
            this.hoveredElement = RealmsMainScreen.HoveredElement.CONFIGURE;
        }

    }

    protected void renderMousehoverTooltip(PoseStack param0, List<Component> param1, int param2, int param3) {
        if (!param1.isEmpty()) {
            int var0 = 0;
            int var1 = 0;

            for(Component var2 : param1) {
                int var3 = this.font.width(var2);
                if (var3 > var1) {
                    var1 = var3;
                }
            }

            int var4 = param2 - var1 - 5;
            int var5 = param3;
            if (var4 < 0) {
                var4 = param2 + 12;
            }

            for(Component var6 : param1) {
                int var7 = var5 - (var0 == 0 ? 3 : 0) + var0;
                this.fillGradient(param0, var4 - 3, var7, var4 + var1 + 3, var5 + 8 + 3 + var0, -1073741824, -1073741824);
                this.font.drawShadow(param0, var6, (float)var4, (float)(var5 + var0), 16777215);
                var0 += 10;
            }

        }
    }

    void renderMoreInfo(PoseStack param0, int param1, int param2, int param3, int param4, boolean param5) {
        boolean var0 = false;
        if (param1 >= param3 && param1 <= param3 + 20 && param2 >= param4 && param2 <= param4 + 20) {
            var0 = true;
        }

        RenderSystem.setShaderTexture(0, QUESTIONMARK_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        float var1 = param5 ? 20.0F : 0.0F;
        GuiComponent.blit(param0, param3, param4, var1, 0.0F, 20, 20, 40, 20);
        if (var0) {
            this.setTooltip(SERVER_INFO_TOOLTIP);
        }

    }

    void renderNews(PoseStack param0, int param1, int param2, boolean param3, int param4, int param5, boolean param6, boolean param7) {
        boolean var0 = false;
        if (param1 >= param4 && param1 <= param4 + 20 && param2 >= param5 && param2 <= param5 + 20) {
            var0 = true;
        }

        RenderSystem.setShaderTexture(0, NEWS_LOCATION);
        if (param7) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        }

        boolean var1 = param7 && param6;
        float var2 = var1 ? 20.0F : 0.0F;
        GuiComponent.blit(param0, param4, param5, var2, 0.0F, 20, 20, 40, 20);
        if (var0 && param7) {
            this.setTooltip(NEWS_TOOLTIP);
        }

        if (param3 && param7) {
            int var3 = var0 ? 0 : (int)(Math.max(0.0F, Math.max(Mth.sin((float)(10 + this.animTick) * 0.57F), Mth.cos((float)this.animTick * 0.35F))) * -6.0F);
            RenderSystem.setShaderTexture(0, INVITATION_ICONS_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(param0, param4 + 10, param5 + 2 + var3, 40.0F, 0.0F, 8, 8, 48, 16);
        }

    }

    private void renderLocal(PoseStack param0) {
        String var0 = "LOCAL!";
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        param0.pushPose();
        param0.translate((double)(this.width / 2 - 25), 20.0, 0.0);
        param0.mulPose(Vector3f.ZP.rotationDegrees(-20.0F));
        param0.scale(1.5F, 1.5F, 1.5F);
        this.font.draw(param0, "LOCAL!", 0.0F, 0.0F, 8388479);
        param0.popPose();
    }

    private void renderStage(PoseStack param0) {
        String var0 = "STAGE!";
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        param0.pushPose();
        param0.translate((double)(this.width / 2 - 25), 20.0, 0.0);
        param0.mulPose(Vector3f.ZP.rotationDegrees(-20.0F));
        param0.scale(1.5F, 1.5F, 1.5F);
        this.font.draw(param0, "STAGE!", 0.0F, 0.0F, -256);
        param0.popPose();
    }

    public RealmsMainScreen newScreen() {
        RealmsMainScreen var0 = new RealmsMainScreen(this.lastScreen);
        var0.init(this.minecraft, this.width, this.height);
        return var0;
    }

    public static void updateTeaserImages(ResourceManager param0) {
        Collection<ResourceLocation> var0 = param0.listResources("textures/gui/images", param0x -> param0x.endsWith(".png"));
        teaserImages = var0.stream().filter(param0x -> param0x.getNamespace().equals("realms")).collect(ImmutableList.toImmutableList());
    }

    void setTooltip(Component... param0) {
        this.toolTip = Arrays.asList(param0);
    }

    private void pendingButtonPress(Button param0) {
        this.minecraft.setScreen(new RealmsPendingInvitesScreen(this.lastScreen));
    }

    @OnlyIn(Dist.CLIENT)
    class CloseButton extends Button {
        public CloseButton() {
            super(
                RealmsMainScreen.this.popupX0() + 4,
                RealmsMainScreen.this.popupY0() + 4,
                12,
                12,
                new TranslatableComponent("mco.selectServer.close"),
                param1 -> RealmsMainScreen.this.onClosePopup()
            );
        }

        @Override
        public void renderButton(PoseStack param0, int param1, int param2, float param3) {
            RenderSystem.setShaderTexture(0, RealmsMainScreen.CROSS_ICON_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            float var0 = this.isHovered() ? 12.0F : 0.0F;
            blit(param0, this.x, this.y, 0.0F, var0, 12, 12, 12, 24);
            if (this.isMouseOver((double)param1, (double)param2)) {
                RealmsMainScreen.this.setTooltip(this.getMessage());
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    abstract class Entry extends ObjectSelectionList.Entry<RealmsMainScreen.Entry> {
        @Nullable
        public abstract RealmsServer getServer();
    }

    @OnlyIn(Dist.CLIENT)
    static enum HoveredElement {
        NONE,
        EXPIRED,
        LEAVE,
        CONFIGURE;
    }

    @OnlyIn(Dist.CLIENT)
    class NewsButton extends Button {
        public NewsButton() {
            super(RealmsMainScreen.this.width - 62, 6, 20, 20, new TranslatableComponent("mco.news"), param1 -> {
                if (RealmsMainScreen.this.newsLink != null) {
                    Util.getPlatform().openUri(RealmsMainScreen.this.newsLink);
                    if (RealmsMainScreen.this.hasUnreadNews) {
                        RealmsPersistence.RealmsPersistenceData var0 = RealmsPersistence.readFile();
                        var0.hasUnreadNews = false;
                        RealmsMainScreen.this.hasUnreadNews = false;
                        RealmsPersistence.writeFile(var0);
                    }

                }
            });
        }

        @Override
        public void renderButton(PoseStack param0, int param1, int param2, float param3) {
            RealmsMainScreen.this.renderNews(param0, param1, param2, RealmsMainScreen.this.hasUnreadNews, this.x, this.y, this.isHovered(), this.active);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class PendingInvitesButton extends Button {
        public PendingInvitesButton() {
            super(RealmsMainScreen.this.width / 2 + 47, 6, 22, 22, TextComponent.EMPTY, RealmsMainScreen.this::pendingButtonPress);
        }

        public void tick() {
            this.setMessage(
                RealmsMainScreen.this.numberOfPendingInvites == 0 ? RealmsMainScreen.NO_PENDING_INVITES_TEXT : RealmsMainScreen.PENDING_INVITES_TEXT
            );
        }

        @Override
        public void renderButton(PoseStack param0, int param1, int param2, float param3) {
            RealmsMainScreen.this.drawInvitationPendingIcon(param0, param1, param2, this.x, this.y, this.isHovered(), this.active);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class RealmSelectionList extends RealmsObjectSelectionList<RealmsMainScreen.Entry> {
        public RealmSelectionList() {
            super(RealmsMainScreen.this.width, RealmsMainScreen.this.height, 32, RealmsMainScreen.this.height - 40, 36);
        }

        @Override
        public boolean isFocused() {
            return RealmsMainScreen.this.getFocused() == this;
        }

        @Override
        public boolean keyPressed(int param0, int param1, int param2) {
            if (param0 != 257 && param0 != 32 && param0 != 335) {
                return super.keyPressed(param0, param1, param2);
            } else {
                RealmsMainScreen.Entry var0 = this.getSelected();
                return var0 == null ? super.keyPressed(param0, param1, param2) : var0.mouseClicked(0.0, 0.0, 0);
            }
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (param2 == 0 && param0 < (double)this.getScrollbarPosition() && param1 >= (double)this.y0 && param1 <= (double)this.y1) {
                int var0 = RealmsMainScreen.this.realmSelectionList.getRowLeft();
                int var1 = this.getScrollbarPosition();
                int var2 = (int)Math.floor(param1 - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
                int var3 = var2 / this.itemHeight;
                if (param0 >= (double)var0 && param0 <= (double)var1 && var3 >= 0 && var2 >= 0 && var3 < this.getItemCount()) {
                    this.itemClicked(var2, var3, param0, param1, this.width);
                    RealmsMainScreen.this.clicks += 7;
                    this.selectItem(var3);
                }

                return true;
            } else {
                return super.mouseClicked(param0, param1, param2);
            }
        }

        public void setSelected(@Nullable RealmsMainScreen.Entry param0) {
            super.setSelected(param0);
            if (param0 != null) {
                RealmsMainScreen.this.updateButtonStates(param0.getServer());
            } else {
                RealmsMainScreen.this.updateButtonStates(null);
            }

        }

        @Override
        public void itemClicked(int param0, int param1, double param2, double param3, int param4) {
            RealmsMainScreen.Entry var0 = this.getEntry(param1);
            if (var0 instanceof RealmsMainScreen.TrialEntry) {
                RealmsMainScreen.this.popupOpenedByUser = true;
            } else {
                RealmsServer var1 = var0.getServer();
                if (var1 != null) {
                    if (var1.state == RealmsServer.State.UNINITIALIZED) {
                        Minecraft.getInstance().setScreen(new RealmsCreateRealmScreen(var1, RealmsMainScreen.this));
                    } else {
                        if (RealmsMainScreen.this.hoveredElement == RealmsMainScreen.HoveredElement.CONFIGURE) {
                            RealmsMainScreen.this.configureClicked(var1);
                        } else if (RealmsMainScreen.this.hoveredElement == RealmsMainScreen.HoveredElement.LEAVE) {
                            RealmsMainScreen.this.leaveClicked(var1);
                        } else if (RealmsMainScreen.this.hoveredElement == RealmsMainScreen.HoveredElement.EXPIRED) {
                            RealmsMainScreen.this.onRenew(var1);
                        } else if (RealmsMainScreen.this.clicks >= 10 && RealmsMainScreen.this.shouldPlayButtonBeActive(var1)) {
                            RealmsMainScreen.this.play(var1, RealmsMainScreen.this);
                        }

                    }
                }
            }
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
    class ServerEntry extends RealmsMainScreen.Entry {
        private static final int SKIN_HEAD_LARGE_WIDTH = 36;
        private final RealmsServer serverData;

        public ServerEntry(RealmsServer param0) {
            this.serverData = param0;
        }

        @Override
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            this.renderMcoServerItem(this.serverData, param0, param3, param2, param6, param7);
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
                RealmsMainScreen.this.minecraft.setScreen(new RealmsCreateRealmScreen(this.serverData, RealmsMainScreen.this));
            }

            return true;
        }

        private void renderMcoServerItem(RealmsServer param0, PoseStack param1, int param2, int param3, int param4, int param5) {
            this.renderLegacy(param0, param1, param2 + 36, param3, param4, param5);
        }

        private void renderLegacy(RealmsServer param0, PoseStack param1, int param2, int param3, int param4, int param5) {
            if (param0.state == RealmsServer.State.UNINITIALIZED) {
                RenderSystem.setShaderTexture(0, RealmsMainScreen.WORLDICON_LOCATION);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                GuiComponent.blit(param1, param2 + 10, param3 + 6, 0.0F, 0.0F, 40, 20, 40, 20);
                float var0 = 0.5F + (1.0F + Mth.sin((float)RealmsMainScreen.this.animTick * 0.25F)) * 0.25F;
                int var1 = 0xFF000000 | (int)(127.0F * var0) << 16 | (int)(255.0F * var0) << 8 | (int)(127.0F * var0);
                GuiComponent.drawCenteredString(
                    param1, RealmsMainScreen.this.font, RealmsMainScreen.SERVER_UNITIALIZED_TEXT, param2 + 10 + 40 + 75, param3 + 12, var1
                );
            } else {
                int var2 = 225;
                int var3 = 2;
                if (param0.expired) {
                    RealmsMainScreen.this.drawExpired(param1, param2 + 225 - 14, param3 + 2, param4, param5);
                } else if (param0.state == RealmsServer.State.CLOSED) {
                    RealmsMainScreen.this.drawClose(param1, param2 + 225 - 14, param3 + 2, param4, param5);
                } else if (RealmsMainScreen.this.isSelfOwnedServer(param0) && param0.daysLeft < 7) {
                    RealmsMainScreen.this.drawExpiring(param1, param2 + 225 - 14, param3 + 2, param4, param5, param0.daysLeft);
                } else if (param0.state == RealmsServer.State.OPEN) {
                    RealmsMainScreen.this.drawOpen(param1, param2 + 225 - 14, param3 + 2, param4, param5);
                }

                if (!RealmsMainScreen.this.isSelfOwnedServer(param0) && !RealmsMainScreen.overrideConfigure) {
                    RealmsMainScreen.this.drawLeave(param1, param2 + 225, param3 + 2, param4, param5);
                } else {
                    RealmsMainScreen.this.drawConfigure(param1, param2 + 225, param3 + 2, param4, param5);
                }

                if (!"0".equals(param0.serverPing.nrOfPlayers)) {
                    String var4 = ChatFormatting.GRAY + param0.serverPing.nrOfPlayers;
                    RealmsMainScreen.this.font.draw(param1, var4, (float)(param2 + 207 - RealmsMainScreen.this.font.width(var4)), (float)(param3 + 3), 8421504);
                    if (param4 >= param2 + 207 - RealmsMainScreen.this.font.width(var4)
                        && param4 <= param2 + 207
                        && param5 >= param3 + 1
                        && param5 <= param3 + 10
                        && param5 < RealmsMainScreen.this.height - 40
                        && param5 > 32
                        && !RealmsMainScreen.this.shouldShowPopup()) {
                        RealmsMainScreen.this.setTooltip(new TextComponent(param0.serverPing.playerList));
                    }
                }

                if (RealmsMainScreen.this.isSelfOwnedServer(param0) && param0.expired) {
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.enableBlend();
                    RenderSystem.setShaderTexture(0, RealmsMainScreen.BUTTON_LOCATION);
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    Component var5;
                    Component var6;
                    if (param0.expiredTrial) {
                        var5 = RealmsMainScreen.TRIAL_EXPIRED_TEXT;
                        var6 = RealmsMainScreen.SUBSCRIPTION_CREATE_TEXT;
                    } else {
                        var5 = RealmsMainScreen.SUBSCRIPTION_EXPIRED_TEXT;
                        var6 = RealmsMainScreen.SUBSCRIPTION_RENEW_TEXT;
                    }

                    int var9 = RealmsMainScreen.this.font.width(var6) + 17;
                    int var10 = 16;
                    int var11 = param2 + RealmsMainScreen.this.font.width(var5) + 8;
                    int var12 = param3 + 13;
                    boolean var13 = false;
                    if (param4 >= var11
                        && param4 < var11 + var9
                        && param5 > var12
                        && param5 <= var12 + 16
                        && param5 < RealmsMainScreen.this.height - 40
                        && param5 > 32
                        && !RealmsMainScreen.this.shouldShowPopup()) {
                        var13 = true;
                        RealmsMainScreen.this.hoveredElement = RealmsMainScreen.HoveredElement.EXPIRED;
                    }

                    int var14 = var13 ? 2 : 1;
                    GuiComponent.blit(param1, var11, var12, 0.0F, (float)(46 + var14 * 20), var9 / 2, 8, 256, 256);
                    GuiComponent.blit(param1, var11 + var9 / 2, var12, (float)(200 - var9 / 2), (float)(46 + var14 * 20), var9 / 2, 8, 256, 256);
                    GuiComponent.blit(param1, var11, var12 + 8, 0.0F, (float)(46 + var14 * 20 + 12), var9 / 2, 8, 256, 256);
                    GuiComponent.blit(param1, var11 + var9 / 2, var12 + 8, (float)(200 - var9 / 2), (float)(46 + var14 * 20 + 12), var9 / 2, 8, 256, 256);
                    RenderSystem.disableBlend();
                    int var15 = param3 + 11 + 5;
                    int var16 = var13 ? 16777120 : 16777215;
                    RealmsMainScreen.this.font.draw(param1, var5, (float)(param2 + 2), (float)(var15 + 1), 15553363);
                    GuiComponent.drawCenteredString(param1, RealmsMainScreen.this.font, var6, var11 + var9 / 2, var15 + 1, var16);
                } else {
                    if (param0.worldType == RealmsServer.WorldType.MINIGAME) {
                        int var17 = 13413468;
                        int var18 = RealmsMainScreen.this.font.width(RealmsMainScreen.SELECT_MINIGAME_PREFIX);
                        RealmsMainScreen.this.font.draw(param1, RealmsMainScreen.SELECT_MINIGAME_PREFIX, (float)(param2 + 2), (float)(param3 + 12), 13413468);
                        RealmsMainScreen.this.font.draw(param1, param0.getMinigameName(), (float)(param2 + 2 + var18), (float)(param3 + 12), 7105644);
                    } else {
                        RealmsMainScreen.this.font.draw(param1, param0.getDescription(), (float)(param2 + 2), (float)(param3 + 12), 7105644);
                    }

                    if (!RealmsMainScreen.this.isSelfOwnedServer(param0)) {
                        RealmsMainScreen.this.font.draw(param1, param0.owner, (float)(param2 + 2), (float)(param3 + 12 + 11), 5000268);
                    }
                }

                RealmsMainScreen.this.font.draw(param1, param0.getName(), (float)(param2 + 2), (float)(param3 + 1), 16777215);
                RealmsTextureManager.withBoundFace(param0.ownerUUID, () -> {
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    GuiComponent.blit(param1, param2 - 36, param3, 32, 32, 8.0F, 8.0F, 8, 8, 64, 64);
                    GuiComponent.blit(param1, param2 - 36, param3, 32, 32, 40.0F, 8.0F, 8, 8, 64, 64);
                });
            }
        }

        @Override
        public Component getNarration() {
            return (Component)(this.serverData.state == RealmsServer.State.UNINITIALIZED
                ? RealmsMainScreen.UNITIALIZED_WORLD_NARRATION
                : new TranslatableComponent("narrator.select", this.serverData.name));
        }

        @Nullable
        @Override
        public RealmsServer getServer() {
            return this.serverData;
        }
    }

    @OnlyIn(Dist.CLIENT)
    class ShowPopupButton extends Button {
        public ShowPopupButton() {
            super(
                RealmsMainScreen.this.width - 37,
                6,
                20,
                20,
                new TranslatableComponent("mco.selectServer.info"),
                param1 -> RealmsMainScreen.this.popupOpenedByUser = !RealmsMainScreen.this.popupOpenedByUser
            );
        }

        @Override
        public void renderButton(PoseStack param0, int param1, int param2, float param3) {
            RealmsMainScreen.this.renderMoreInfo(param0, param1, param2, this.x, this.y, this.isHovered());
        }
    }

    @OnlyIn(Dist.CLIENT)
    class TrialEntry extends RealmsMainScreen.Entry {
        @Override
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            this.renderTrialItem(param0, param1, param3, param2, param6, param7);
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            RealmsMainScreen.this.popupOpenedByUser = true;
            return true;
        }

        private void renderTrialItem(PoseStack param0, int param1, int param2, int param3, int param4, int param5) {
            int var0 = param3 + 8;
            int var1 = 0;
            boolean var2 = false;
            if (param2 <= param4 && param4 <= (int)RealmsMainScreen.this.realmSelectionList.getScrollAmount() && param3 <= param5 && param5 <= param3 + 32) {
                var2 = true;
            }

            int var3 = 8388479;
            if (var2 && !RealmsMainScreen.this.shouldShowPopup()) {
                var3 = 6077788;
            }

            for(Component var4 : RealmsMainScreen.TRIAL_MESSAGE_LINES) {
                GuiComponent.drawCenteredString(param0, RealmsMainScreen.this.font, var4, RealmsMainScreen.this.width / 2, var0 + var1, var3);
                var1 += 10;
            }

        }

        @Override
        public Component getNarration() {
            return RealmsMainScreen.TRIAL_TEXT;
        }

        @Nullable
        @Override
        public RealmsServer getServer() {
            return null;
        }
    }
}
