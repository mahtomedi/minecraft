package com.mojang.realmsclient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
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
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation ON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/on_icon.png");
    private static final ResourceLocation OFF_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/off_icon.png");
    private static final ResourceLocation EXPIRED_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expired_icon.png");
    private static final ResourceLocation EXPIRES_SOON_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/expires_soon_icon.png");
    private static final ResourceLocation LEAVE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/leave_icon.png");
    private static final ResourceLocation INVITATION_ICONS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invitation_icons.png");
    private static final ResourceLocation INVITE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/invite_icon.png");
    private static final ResourceLocation WORLDICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/world_icon.png");
    private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("realms", "textures/gui/title/realms.png");
    private static final ResourceLocation CONFIGURE_LOCATION = new ResourceLocation("realms", "textures/gui/realms/configure_icon.png");
    private static final ResourceLocation QUESTIONMARK_LOCATION = new ResourceLocation("realms", "textures/gui/realms/questionmark.png");
    private static final ResourceLocation NEWS_LOCATION = new ResourceLocation("realms", "textures/gui/realms/news_icon.png");
    private static final ResourceLocation POPUP_LOCATION = new ResourceLocation("realms", "textures/gui/realms/popup.png");
    private static final ResourceLocation DARKEN_LOCATION = new ResourceLocation("realms", "textures/gui/realms/darken.png");
    private static final ResourceLocation CROSS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/cross_icon.png");
    private static final ResourceLocation TRIAL_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/trial_icon.png");
    private static final ResourceLocation BUTTON_LOCATION = new ResourceLocation("minecraft", "textures/gui/widgets.png");
    private static List<ResourceLocation> teaserImages = ImmutableList.of();
    private static final RealmsDataFetcher REALMS_DATA_FETCHER = new RealmsDataFetcher();
    private static boolean overrideConfigure;
    private static int lastScrollYPosition = -1;
    private static volatile boolean hasParentalConsent;
    private static volatile boolean checkedParentalConsent;
    private static volatile boolean checkedClientCompatability;
    private static Screen realmsGenericErrorScreen;
    private static boolean regionsPinged;
    private final RateLimiter inviteNarrationLimiter;
    private boolean dontSetConnectedToRealms;
    private final Screen lastScreen;
    private volatile RealmsMainScreen.RealmSelectionList realmSelectionList;
    private long selectedServerId = -1L;
    private Button playButton;
    private Button backButton;
    private Button renewButton;
    private Button configureButton;
    private Button leaveButton;
    private String toolTip;
    private List<RealmsServer> realmsServers = Lists.newArrayList();
    private volatile int numberOfPendingInvites;
    private int animTick;
    private boolean hasFetchedServers;
    private boolean popupOpenedByUser;
    private boolean justClosedPopup;
    private volatile boolean trialsAvailable;
    private volatile boolean createdTrial;
    private volatile boolean showingPopup;
    private volatile boolean hasUnreadNews;
    private volatile String newsLink;
    private int carouselIndex;
    private int carouselTick;
    private boolean hasSwitchedCarouselImage;
    private List<KeyCombo> keyCombos;
    private int clicks;
    private ReentrantLock connectLock = new ReentrantLock();
    private boolean expiredHover;
    private Button showPopupButton;
    private Button pendingInvitesButton;
    private Button newsButton;
    private Button createTrialButton;
    private Button buyARealmButton;
    private Button closeButton;

    public RealmsMainScreen(Screen param0) {
        this.lastScreen = param0;
        this.inviteNarrationLimiter = RateLimiter.create(0.016666668F);
    }

    public boolean shouldShowMessageInList() {
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
        } else if (this.popupOpenedByUser) {
            return true;
        } else {
            return this.trialsAvailable && !this.createdTrial && this.realmsServers.isEmpty() ? true : this.realmsServers.isEmpty();
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
            this.checkUnreadNews();
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
            this.magicalSpecialHackyFocus(this.realmSelectionList);
        }
    }

    private static boolean hasParentalConsent() {
        return checkedParentalConsent && hasParentalConsent;
    }

    public void addButtons() {
        this.configureButton = this.addButton(
            new Button(
                this.width / 2 - 190,
                this.height - 32,
                90,
                20,
                I18n.get("mco.selectServer.configure"),
                param0 -> this.configureClicked(this.findServer(this.selectedServerId))
            )
        );
        this.playButton = this.addButton(new Button(this.width / 2 - 93, this.height - 32, 90, 20, I18n.get("mco.selectServer.play"), param0 -> {
            RealmsServer var0x = this.findServer(this.selectedServerId);
            if (var0x != null) {
                this.play(var0x, this);
            }
        }));
        this.backButton = this.addButton(new Button(this.width / 2 + 4, this.height - 32, 90, 20, I18n.get("gui.back"), param0 -> {
            if (!this.justClosedPopup) {
                this.minecraft.setScreen(this.lastScreen);
            }

        }));
        this.renewButton = this.addButton(
            new Button(this.width / 2 + 100, this.height - 32, 90, 20, I18n.get("mco.selectServer.expiredRenew"), param0 -> this.onRenew())
        );
        this.leaveButton = this.addButton(
            new Button(
                this.width / 2 - 202,
                this.height - 32,
                90,
                20,
                I18n.get("mco.selectServer.leave"),
                param0 -> this.leaveClicked(this.findServer(this.selectedServerId))
            )
        );
        this.pendingInvitesButton = this.addButton(new RealmsMainScreen.PendingInvitesButton());
        this.newsButton = this.addButton(new RealmsMainScreen.NewsButton());
        this.showPopupButton = this.addButton(new RealmsMainScreen.ShowPopupButton());
        this.closeButton = this.addButton(new RealmsMainScreen.CloseButton());
        this.createTrialButton = this.addButton(
            new Button(this.width / 2 + 52, this.popupY0() + 137 - 20, 98, 20, I18n.get("mco.selectServer.trial"), param0 -> {
                if (this.trialsAvailable && !this.createdTrial) {
                    Util.getPlatform().openUri("https://aka.ms/startjavarealmstrial");
                    this.minecraft.setScreen(this.lastScreen);
                }
            })
        );
        this.buyARealmButton = this.addButton(
            new Button(
                this.width / 2 + 52,
                this.popupY0() + 160 - 20,
                98,
                20,
                I18n.get("mco.selectServer.buy"),
                param0 -> Util.getPlatform().openUri("https://aka.ms/BuyJavaRealms")
            )
        );
        RealmsServer var0 = this.findServer(this.selectedServerId);
        this.updateButtonStates(var0);
    }

    private void updateButtonStates(@Nullable RealmsServer param0) {
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

    private boolean shouldPlayButtonBeActive(@Nullable RealmsServer param0) {
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
                this.realmSelectionList.clear();
                boolean var1 = !this.hasFetchedServers;
                if (var1) {
                    this.hasFetchedServers = true;
                }

                if (var0 != null) {
                    boolean var2 = false;

                    for(RealmsServer var3 : var0) {
                        if (this.isSelfOwnedNonExpiredServer(var3)) {
                            var2 = true;
                        }
                    }

                    this.realmsServers = var0;
                    if (this.shouldShowMessageInList()) {
                        this.realmSelectionList.addEntry(new RealmsMainScreen.TrialEntry());
                    }

                    for(RealmsServer var4 : this.realmsServers) {
                        this.realmSelectionList.addEntry(new RealmsMainScreen.ServerEntry(var4));
                    }

                    if (!regionsPinged && var2) {
                        regionsPinged = true;
                        this.pingRegions();
                    }
                }

                if (var1) {
                    this.addButtons();
                }
            }

            if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.PENDING_INVITE)) {
                this.numberOfPendingInvites = REALMS_DATA_FETCHER.getPendingInvitesCount();
                if (this.numberOfPendingInvites > 0 && this.inviteNarrationLimiter.tryAcquire(1)) {
                    NarrationHelper.now(I18n.get("mco.configure.world.invite.narration", this.numberOfPendingInvites));
                }
            }

            if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.TRIAL_AVAILABLE) && !this.createdTrial) {
                boolean var5 = REALMS_DATA_FETCHER.isTrialAvailable();
                if (var5 != this.trialsAvailable && this.shouldShowPopup()) {
                    this.trialsAvailable = var5;
                    this.showingPopup = false;
                } else {
                    this.trialsAvailable = var5;
                }
            }

            if (REALMS_DATA_FETCHER.isFetchedSinceLastTry(RealmsDataFetcher.Task.LIVE_STATS)) {
                RealmsServerPlayerLists var6 = REALMS_DATA_FETCHER.getLivestats();

                for(RealmsServerPlayerList var7 : var6.servers) {
                    for(RealmsServer var8 : this.realmsServers) {
                        if (var8.id == var7.serverId) {
                            var8.updateServerPing(var7);
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

    private void onRenew() {
        RealmsServer var0 = this.findServer(this.selectedServerId);
        if (var0 != null) {
            String var1 = "https://aka.ms/ExtendJavaRealms?subscriptionId="
                + var0.remoteSubscriptionId
                + "&profileId="
                + this.minecraft.getUser().getUuid()
                + "&ref="
                + (var0.expiredTrial ? "expiredTrial" : "expiredRealm");
            this.minecraft.keyboardHandler.setClipboard(var1);
            Util.getPlatform().openUri(var1);
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
                                    I18n.get("mco.error.invalid.session.title"),
                                    I18n.get("mco.error.invalid.session.message"),
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

    private void checkUnreadNews() {
    }

    private void checkParentalConsent() {
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
                        RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: " + var3);
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
                        RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: " + var3);
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

    private void configureClicked(RealmsServer param0) {
        if (this.minecraft.getUser().getUuid().equals(param0.ownerUUID) || overrideConfigure) {
            this.saveListScrollPosition();
            this.minecraft.setScreen(new RealmsConfigureWorldScreen(this, param0.id));
        }

    }

    private void leaveClicked(@Nullable RealmsServer param0) {
        if (param0 != null && !this.minecraft.getUser().getUuid().equals(param0.ownerUUID)) {
            this.saveListScrollPosition();
            String var0 = I18n.get("mco.configure.world.leave.question.line1");
            String var1 = I18n.get("mco.configure.world.leave.question.line2");
            this.minecraft.setScreen(new RealmsLongConfirmationScreen(this::leaveServer, RealmsLongConfirmationScreen.Type.Info, var0, var1, true));
        }

    }

    private void saveListScrollPosition() {
        lastScrollYPosition = (int)this.realmSelectionList.getScrollAmount();
    }

    @Nullable
    private RealmsServer findServer(long param0) {
        for(RealmsServer var0 : this.realmsServers) {
            if (var0.id == param0) {
                return var0;
            }
        }

        return null;
    }

    private void leaveServer(boolean param0x) {
        if (param0x) {
            (new Thread("Realms-leave-server") {
                    @Override
                    public void run() {
                        try {
                            RealmsServer var0 = RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId);
                            if (var0 != null) {
                                RealmsClient var1 = RealmsClient.create();
                                var1.uninviteMyselfFrom(var0.id);
                                RealmsMainScreen.REALMS_DATA_FETCHER.removeItem(var0);
                                RealmsMainScreen.this.realmsServers.remove(var0);
                                RealmsMainScreen.this.realmSelectionList
                                    .children()
                                    .removeIf(
                                        param0 -> param0 instanceof RealmsMainScreen.ServerEntry
                                                && ((RealmsMainScreen.ServerEntry)param0).serverData.id == RealmsMainScreen.this.selectedServerId
                                    );
                                RealmsMainScreen.this.realmSelectionList.setSelected(null);
                                RealmsMainScreen.this.updateButtonStates(null);
                                RealmsMainScreen.this.selectedServerId = -1L;
                                RealmsMainScreen.this.playButton.active = false;
                            }
                        } catch (RealmsServiceException var3) {
                            RealmsMainScreen.LOGGER.error("Couldn't configure world");
                            RealmsMainScreen.this.minecraft
                                .execute(() -> RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this)));
                        }
    
                    }
                })
                .start();
        }

        this.minecraft.setScreen(this);
    }

    public void removeSelection() {
        this.selectedServerId = -1L;
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

    private void onClosePopup() {
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
    public void render(int param0, int param1, float param2) {
        this.expiredHover = false;
        this.toolTip = null;
        this.renderBackground();
        this.realmSelectionList.render(param0, param1, param2);
        this.drawRealmsLogo(this.width / 2 - 50, 7);
        if (RealmsClient.currentEnvironment == RealmsClient.Environment.STAGE) {
            this.renderStage();
        }

        if (RealmsClient.currentEnvironment == RealmsClient.Environment.LOCAL) {
            this.renderLocal();
        }

        if (this.shouldShowPopup()) {
            this.drawPopup(param0, param1);
        } else {
            if (this.showingPopup) {
                this.updateButtonStates(null);
                if (!this.children.contains(this.realmSelectionList)) {
                    this.children.add(this.realmSelectionList);
                }

                RealmsServer var0 = this.findServer(this.selectedServerId);
                this.playButton.active = this.shouldPlayButtonBeActive(var0);
            }

            this.showingPopup = false;
        }

        super.render(param0, param1, param2);
        if (this.toolTip != null) {
            this.renderMousehoverTooltip(this.toolTip, param0, param1);
        }

        if (this.trialsAvailable && !this.createdTrial && this.shouldShowPopup()) {
            this.minecraft.getTextureManager().bind(TRIAL_ICON_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            int var1 = 8;
            int var2 = 8;
            int var3 = 0;
            if ((Util.getMillis() / 800L & 1L) == 1L) {
                var3 = 8;
            }

            GuiComponent.blit(
                this.createTrialButton.x + this.createTrialButton.getWidth() - 8 - 4,
                this.createTrialButton.y + this.createTrialButton.getHeight() / 2 - 4,
                0.0F,
                (float)var3,
                8,
                8,
                8,
                16
            );
        }

    }

    private void drawRealmsLogo(int param0, int param1) {
        this.minecraft.getTextureManager().bind(LOGO_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        RenderSystem.scalef(0.5F, 0.5F, 0.5F);
        GuiComponent.blit(param0 * 2, param1 * 2 - 5, 0.0F, 0.0F, 200, 50, 200, 50);
        RenderSystem.popMatrix();
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

    private void drawPopup(int param0, int param1) {
        int var0 = this.popupX0();
        int var1 = this.popupY0();
        String var2 = I18n.get("mco.selectServer.popup");
        List<String> var3 = this.font.split(var2, 100);
        if (!this.showingPopup) {
            this.carouselIndex = 0;
            this.carouselTick = 0;
            this.hasSwitchedCarouselImage = true;
            this.updateButtonStates(null);
            if (this.children.contains(this.realmSelectionList)) {
                GuiEventListener var4 = this.realmSelectionList;
                if (!this.children.remove(var4)) {
                    LOGGER.error("Unable to remove widget: " + var4);
                }
            }

            NarrationHelper.now(var2);
        }

        if (this.hasFetchedServers) {
            this.showingPopup = true;
        }

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.7F);
        RenderSystem.enableBlend();
        this.minecraft.getTextureManager().bind(DARKEN_LOCATION);
        int var5 = 0;
        int var6 = 32;
        GuiComponent.blit(0, 32, 0.0F, 0.0F, this.width, this.height - 40 - 32, 310, 166);
        RenderSystem.disableBlend();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(POPUP_LOCATION);
        GuiComponent.blit(var0, var1, 0.0F, 0.0F, 310, 166, 310, 166);
        if (!teaserImages.isEmpty()) {
            this.minecraft.getTextureManager().bind(teaserImages.get(this.carouselIndex));
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(var0 + 7, var1 + 7, 0.0F, 0.0F, 195, 152, 195, 152);
            if (this.carouselTick % 95 < 5) {
                if (!this.hasSwitchedCarouselImage) {
                    this.carouselIndex = (this.carouselIndex + 1) % teaserImages.size();
                    this.hasSwitchedCarouselImage = true;
                }
            } else {
                this.hasSwitchedCarouselImage = false;
            }
        }

        int var7 = 0;

        for(String var8 : var3) {
            ++var7;
            int var9 = var1 + 10 * var7 - 3;
            this.font.draw(var8, (float)(this.width / 2 + 52), (float)var9, 5000268);
        }

    }

    private int popupX0() {
        return (this.width - 310) / 2;
    }

    private int popupY0() {
        return this.height / 2 - 80;
    }

    private void drawInvitationPendingIcon(int param0, int param1, int param2, int param3, boolean param4, boolean param5) {
        int var0 = this.numberOfPendingInvites;
        boolean var1 = this.inPendingInvitationArea((double)param0, (double)param1);
        boolean var2 = param5 && param4;
        if (var2) {
            float var3 = 0.25F + (1.0F + Mth.sin((float)this.animTick * 0.5F)) * 0.25F;
            int var4 = 0xFF000000 | (int)(var3 * 64.0F) << 16 | (int)(var3 * 64.0F) << 8 | (int)(var3 * 64.0F) << 0;
            this.fillGradient(param2 - 2, param3 - 2, param2 + 18, param3 + 18, var4, var4);
            var4 = 0xFF000000 | (int)(var3 * 255.0F) << 16 | (int)(var3 * 255.0F) << 8 | (int)(var3 * 255.0F) << 0;
            this.fillGradient(param2 - 2, param3 - 2, param2 + 18, param3 - 1, var4, var4);
            this.fillGradient(param2 - 2, param3 - 2, param2 - 1, param3 + 18, var4, var4);
            this.fillGradient(param2 + 17, param3 - 2, param2 + 18, param3 + 18, var4, var4);
            this.fillGradient(param2 - 2, param3 + 17, param2 + 18, param3 + 18, var4, var4);
        }

        this.minecraft.getTextureManager().bind(INVITE_ICON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        boolean var5 = param5 && param4;
        float var6 = var5 ? 16.0F : 0.0F;
        GuiComponent.blit(param2, param3 - 6, var6, 0.0F, 15, 25, 31, 25);
        boolean var7 = param5 && var0 != 0;
        if (var7) {
            int var8 = (Math.min(var0, 6) - 1) * 8;
            int var9 = (int)(Math.max(0.0F, Math.max(Mth.sin((float)(10 + this.animTick) * 0.57F), Mth.cos((float)this.animTick * 0.35F))) * -6.0F);
            this.minecraft.getTextureManager().bind(INVITATION_ICONS_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            float var10 = var1 ? 8.0F : 0.0F;
            GuiComponent.blit(param2 + 4, param3 + 4 + var9, (float)var8, var10, 8, 8, 48, 16);
        }

        int var11 = param0 + 12;
        boolean var13 = param5 && var1;
        if (var13) {
            String var14 = var0 == 0 ? "mco.invites.nopending" : "mco.invites.pending";
            String var15 = I18n.get(var14);
            int var16 = this.font.width(var15);
            this.fillGradient(var11 - 3, param1 - 3, var11 + var16 + 3, param1 + 8 + 3, -1073741824, -1073741824);
            this.font.drawShadow(var15, (float)var11, (float)param1, -1);
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

    public void play(RealmsServer param0, Screen param1) {
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

    private boolean isSelfOwnedServer(RealmsServer param0) {
        return param0.ownerUUID != null && param0.ownerUUID.equals(this.minecraft.getUser().getUuid());
    }

    private boolean isSelfOwnedNonExpiredServer(RealmsServer param0) {
        return this.isSelfOwnedServer(param0) && !param0.expired;
    }

    private void drawExpired(int param0, int param1, int param2, int param3) {
        this.minecraft.getTextureManager().bind(EXPIRED_ICON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.blit(param0, param1, 0.0F, 0.0F, 10, 28, 10, 28);
        if (param2 >= param0
            && param2 <= param0 + 9
            && param3 >= param1
            && param3 <= param1 + 27
            && param3 < this.height - 40
            && param3 > 32
            && !this.shouldShowPopup()) {
            this.toolTip = I18n.get("mco.selectServer.expired");
        }

    }

    private void drawExpiring(int param0, int param1, int param2, int param3, int param4) {
        this.minecraft.getTextureManager().bind(EXPIRES_SOON_ICON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.animTick % 20 < 10) {
            GuiComponent.blit(param0, param1, 0.0F, 0.0F, 10, 28, 20, 28);
        } else {
            GuiComponent.blit(param0, param1, 10.0F, 0.0F, 10, 28, 20, 28);
        }

        if (param2 >= param0
            && param2 <= param0 + 9
            && param3 >= param1
            && param3 <= param1 + 27
            && param3 < this.height - 40
            && param3 > 32
            && !this.shouldShowPopup()) {
            if (param4 <= 0) {
                this.toolTip = I18n.get("mco.selectServer.expires.soon");
            } else if (param4 == 1) {
                this.toolTip = I18n.get("mco.selectServer.expires.day");
            } else {
                this.toolTip = I18n.get("mco.selectServer.expires.days", param4);
            }
        }

    }

    private void drawOpen(int param0, int param1, int param2, int param3) {
        this.minecraft.getTextureManager().bind(ON_ICON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.blit(param0, param1, 0.0F, 0.0F, 10, 28, 10, 28);
        if (param2 >= param0
            && param2 <= param0 + 9
            && param3 >= param1
            && param3 <= param1 + 27
            && param3 < this.height - 40
            && param3 > 32
            && !this.shouldShowPopup()) {
            this.toolTip = I18n.get("mco.selectServer.open");
        }

    }

    private void drawClose(int param0, int param1, int param2, int param3) {
        this.minecraft.getTextureManager().bind(OFF_ICON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.blit(param0, param1, 0.0F, 0.0F, 10, 28, 10, 28);
        if (param2 >= param0
            && param2 <= param0 + 9
            && param3 >= param1
            && param3 <= param1 + 27
            && param3 < this.height - 40
            && param3 > 32
            && !this.shouldShowPopup()) {
            this.toolTip = I18n.get("mco.selectServer.closed");
        }

    }

    private void drawLeave(int param0, int param1, int param2, int param3) {
        boolean var0 = false;
        if (param2 >= param0
            && param2 <= param0 + 28
            && param3 >= param1
            && param3 <= param1 + 28
            && param3 < this.height - 40
            && param3 > 32
            && !this.shouldShowPopup()) {
            var0 = true;
        }

        this.minecraft.getTextureManager().bind(LEAVE_ICON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var1 = var0 ? 28.0F : 0.0F;
        GuiComponent.blit(param0, param1, var1, 0.0F, 28, 28, 56, 28);
        if (var0) {
            this.toolTip = I18n.get("mco.selectServer.leave");
        }

    }

    private void drawConfigure(int param0, int param1, int param2, int param3) {
        boolean var0 = false;
        if (param2 >= param0
            && param2 <= param0 + 28
            && param3 >= param1
            && param3 <= param1 + 28
            && param3 < this.height - 40
            && param3 > 32
            && !this.shouldShowPopup()) {
            var0 = true;
        }

        this.minecraft.getTextureManager().bind(CONFIGURE_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var1 = var0 ? 28.0F : 0.0F;
        GuiComponent.blit(param0, param1, var1, 0.0F, 28, 28, 56, 28);
        if (var0) {
            this.toolTip = I18n.get("mco.selectServer.configure");
        }

    }

    protected void renderMousehoverTooltip(String param0, int param1, int param2) {
        if (param0 != null) {
            int var0 = 0;
            int var1 = 0;

            for(String var2 : param0.split("\n")) {
                int var3 = this.font.width(var2);
                if (var3 > var1) {
                    var1 = var3;
                }
            }

            int var4 = param1 - var1 - 5;
            int var5 = param2;
            if (var4 < 0) {
                var4 = param1 + 12;
            }

            for(String var6 : param0.split("\n")) {
                int var7 = var5 - (var0 == 0 ? 3 : 0) + var0;
                this.fillGradient(var4 - 3, var7, var4 + var1 + 3, var5 + 8 + 3 + var0, -1073741824, -1073741824);
                this.font.drawShadow(var6, (float)var4, (float)(var5 + var0), 16777215);
                var0 += 10;
            }

        }
    }

    private void renderMoreInfo(int param0, int param1, int param2, int param3, boolean param4) {
        boolean var0 = false;
        if (param0 >= param2 && param0 <= param2 + 20 && param1 >= param3 && param1 <= param3 + 20) {
            var0 = true;
        }

        this.minecraft.getTextureManager().bind(QUESTIONMARK_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float var1 = param4 ? 20.0F : 0.0F;
        GuiComponent.blit(param2, param3, var1, 0.0F, 20, 20, 40, 20);
        if (var0) {
            this.toolTip = I18n.get("mco.selectServer.info");
        }

    }

    private void renderNews(int param0, int param1, boolean param2, int param3, int param4, boolean param5, boolean param6) {
        boolean var0 = false;
        if (param0 >= param3 && param0 <= param3 + 20 && param1 >= param4 && param1 <= param4 + 20) {
            var0 = true;
        }

        this.minecraft.getTextureManager().bind(NEWS_LOCATION);
        if (param6) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);
        }

        boolean var1 = param6 && param5;
        float var2 = var1 ? 20.0F : 0.0F;
        GuiComponent.blit(param3, param4, var2, 0.0F, 20, 20, 40, 20);
        if (var0 && param6) {
            this.toolTip = I18n.get("mco.news");
        }

        if (param2 && param6) {
            int var3 = var0 ? 0 : (int)(Math.max(0.0F, Math.max(Mth.sin((float)(10 + this.animTick) * 0.57F), Mth.cos((float)this.animTick * 0.35F))) * -6.0F);
            this.minecraft.getTextureManager().bind(INVITATION_ICONS_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(param3 + 10, param4 + 2 + var3, 40.0F, 0.0F, 8, 8, 48, 16);
        }

    }

    private void renderLocal() {
        String var0 = "LOCAL!";
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)(this.width / 2 - 25), 20.0F, 0.0F);
        RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.scalef(1.5F, 1.5F, 1.5F);
        this.font.draw("LOCAL!", 0.0F, 0.0F, 8388479);
        RenderSystem.popMatrix();
    }

    private void renderStage() {
        String var0 = "STAGE!";
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)(this.width / 2 - 25), 20.0F, 0.0F);
        RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.scalef(1.5F, 1.5F, 1.5F);
        this.font.draw("STAGE!", 0.0F, 0.0F, -256);
        RenderSystem.popMatrix();
    }

    public RealmsMainScreen newScreen() {
        return new RealmsMainScreen(this.lastScreen);
    }

    public static void updateTeaserImages(ResourceManager param0) {
        Collection<ResourceLocation> var0 = param0.listResources("textures/gui/images", param0x -> param0x.endsWith(".png"));
        teaserImages = var0.stream().filter(param0x -> param0x.getNamespace().equals("realms")).collect(ImmutableList.toImmutableList());
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
                I18n.get("mco.selectServer.close"),
                param1 -> RealmsMainScreen.this.onClosePopup()
            );
        }

        @Override
        public void renderButton(int param0, int param1, float param2) {
            RealmsMainScreen.this.minecraft.getTextureManager().bind(RealmsMainScreen.CROSS_ICON_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            float var0 = this.isHovered() ? 12.0F : 0.0F;
            blit(this.x, this.y, 0.0F, var0, 12, 12, 12, 24);
            if (this.isMouseOver((double)param0, (double)param1)) {
                RealmsMainScreen.this.toolTip = this.getMessage();
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    abstract class Entry extends ObjectSelectionList.Entry<RealmsMainScreen.Entry> {
        private Entry() {
        }
    }

    @OnlyIn(Dist.CLIENT)
    class NewsButton extends Button {
        public NewsButton() {
            super(RealmsMainScreen.this.width - 62, 6, 20, 20, "", param1 -> {
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
            this.setMessage(I18n.get("mco.news"));
        }

        @Override
        public void renderButton(int param0, int param1, float param2) {
            RealmsMainScreen.this.renderNews(param0, param1, RealmsMainScreen.this.hasUnreadNews, this.x, this.y, this.isHovered(), this.active);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class PendingInvitesButton extends Button implements TickableWidget {
        public PendingInvitesButton() {
            super(RealmsMainScreen.this.width / 2 + 47, 6, 22, 22, "", param1 -> RealmsMainScreen.this.pendingButtonPress(param1));
        }

        @Override
        public void tick() {
            this.setMessage(I18n.get(RealmsMainScreen.this.numberOfPendingInvites == 0 ? "mco.invites.nopending" : "mco.invites.pending"));
        }

        @Override
        public void renderButton(int param0, int param1, float param2) {
            RealmsMainScreen.this.drawInvitationPendingIcon(param0, param1, this.x, this.y, this.isHovered(), this.active);
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
                ObjectSelectionList.Entry var0 = this.getSelected();
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
                    RealmsMainScreen.this.clicks = RealmsMainScreen.this.clicks + 7;
                    this.selectItem(var3);
                }

                return true;
            } else {
                return super.mouseClicked(param0, param1, param2);
            }
        }

        @Override
        public void selectItem(int param0) {
            this.setSelectedItem(param0);
            if (param0 != -1) {
                RealmsServer var0;
                if (RealmsMainScreen.this.shouldShowMessageInList()) {
                    if (param0 == 0) {
                        NarrationHelper.now(I18n.get("mco.trial.message.line1"), I18n.get("mco.trial.message.line2"));
                        var0 = null;
                    } else {
                        if (param0 - 1 >= RealmsMainScreen.this.realmsServers.size()) {
                            RealmsMainScreen.this.selectedServerId = -1L;
                            return;
                        }

                        var0 = RealmsMainScreen.this.realmsServers.get(param0 - 1);
                    }
                } else {
                    if (param0 >= RealmsMainScreen.this.realmsServers.size()) {
                        RealmsMainScreen.this.selectedServerId = -1L;
                        return;
                    }

                    var0 = RealmsMainScreen.this.realmsServers.get(param0);
                }

                RealmsMainScreen.this.updateButtonStates(var0);
                if (var0 == null) {
                    RealmsMainScreen.this.selectedServerId = -1L;
                } else if (var0.state == RealmsServer.State.UNINITIALIZED) {
                    NarrationHelper.now(I18n.get("mco.selectServer.uninitialized") + I18n.get("mco.gui.button"));
                    RealmsMainScreen.this.selectedServerId = -1L;
                } else {
                    RealmsMainScreen.this.selectedServerId = var0.id;
                    if (RealmsMainScreen.this.clicks >= 10 && RealmsMainScreen.this.playButton.active) {
                        RealmsMainScreen.this.play(RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId), RealmsMainScreen.this);
                    }

                    NarrationHelper.now(I18n.get("narrator.select", var0.name));
                }
            }
        }

        public void setSelected(@Nullable RealmsMainScreen.Entry param0) {
            super.setSelected(param0);
            RealmsServer var0 = RealmsMainScreen.this.realmsServers
                .get(this.children().indexOf(param0) - (RealmsMainScreen.this.shouldShowMessageInList() ? 1 : 0));
            RealmsMainScreen.this.selectedServerId = var0.id;
            RealmsMainScreen.this.updateButtonStates(var0);
        }

        @Override
        public void itemClicked(int param0, int param1, double param2, double param3, int param4) {
            if (RealmsMainScreen.this.shouldShowMessageInList()) {
                if (param1 == 0) {
                    RealmsMainScreen.this.popupOpenedByUser = true;
                    return;
                }

                --param1;
            }

            if (param1 < RealmsMainScreen.this.realmsServers.size()) {
                RealmsServer var0 = RealmsMainScreen.this.realmsServers.get(param1);
                if (var0 != null) {
                    if (var0.state == RealmsServer.State.UNINITIALIZED) {
                        RealmsMainScreen.this.selectedServerId = -1L;
                        Minecraft.getInstance().setScreen(new RealmsCreateRealmScreen(var0, RealmsMainScreen.this));
                    } else {
                        RealmsMainScreen.this.selectedServerId = var0.id;
                    }

                    if (RealmsMainScreen.this.toolTip != null && RealmsMainScreen.this.toolTip.equals(I18n.get("mco.selectServer.configure"))) {
                        RealmsMainScreen.this.selectedServerId = var0.id;
                        RealmsMainScreen.this.configureClicked(var0);
                    } else if (RealmsMainScreen.this.toolTip != null && RealmsMainScreen.this.toolTip.equals(I18n.get("mco.selectServer.leave"))) {
                        RealmsMainScreen.this.selectedServerId = var0.id;
                        RealmsMainScreen.this.leaveClicked(var0);
                    } else if (RealmsMainScreen.this.isSelfOwnedServer(var0) && var0.expired && RealmsMainScreen.this.expiredHover) {
                        RealmsMainScreen.this.onRenew();
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
        private final RealmsServer serverData;

        public ServerEntry(RealmsServer param0) {
            this.serverData = param0;
        }

        @Override
        public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
            this.renderMcoServerItem(this.serverData, param2, param1, param5, param6);
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
                RealmsMainScreen.this.selectedServerId = -1L;
                RealmsMainScreen.this.minecraft.setScreen(new RealmsCreateRealmScreen(this.serverData, RealmsMainScreen.this));
            } else {
                RealmsMainScreen.this.selectedServerId = this.serverData.id;
            }

            return true;
        }

        private void renderMcoServerItem(RealmsServer param0, int param1, int param2, int param3, int param4) {
            this.renderLegacy(param0, param1 + 36, param2, param3, param4);
        }

        private void renderLegacy(RealmsServer param0, int param1, int param2, int param3, int param4) {
            if (param0.state == RealmsServer.State.UNINITIALIZED) {
                RealmsMainScreen.this.minecraft.getTextureManager().bind(RealmsMainScreen.WORLDICON_LOCATION);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableAlphaTest();
                GuiComponent.blit(param1 + 10, param2 + 6, 0.0F, 0.0F, 40, 20, 40, 20);
                float var0 = 0.5F + (1.0F + Mth.sin((float)RealmsMainScreen.this.animTick * 0.25F)) * 0.25F;
                int var1 = 0xFF000000 | (int)(127.0F * var0) << 16 | (int)(255.0F * var0) << 8 | (int)(127.0F * var0);
                RealmsMainScreen.this.drawCenteredString(
                    RealmsMainScreen.this.font, I18n.get("mco.selectServer.uninitialized"), param1 + 10 + 40 + 75, param2 + 12, var1
                );
            } else {
                int var2 = 225;
                int var3 = 2;
                if (param0.expired) {
                    RealmsMainScreen.this.drawExpired(param1 + 225 - 14, param2 + 2, param3, param4);
                } else if (param0.state == RealmsServer.State.CLOSED) {
                    RealmsMainScreen.this.drawClose(param1 + 225 - 14, param2 + 2, param3, param4);
                } else if (RealmsMainScreen.this.isSelfOwnedServer(param0) && param0.daysLeft < 7) {
                    RealmsMainScreen.this.drawExpiring(param1 + 225 - 14, param2 + 2, param3, param4, param0.daysLeft);
                } else if (param0.state == RealmsServer.State.OPEN) {
                    RealmsMainScreen.this.drawOpen(param1 + 225 - 14, param2 + 2, param3, param4);
                }

                if (!RealmsMainScreen.this.isSelfOwnedServer(param0) && !RealmsMainScreen.overrideConfigure) {
                    RealmsMainScreen.this.drawLeave(param1 + 225, param2 + 2, param3, param4);
                } else {
                    RealmsMainScreen.this.drawConfigure(param1 + 225, param2 + 2, param3, param4);
                }

                if (!"0".equals(param0.serverPing.nrOfPlayers)) {
                    String var4 = ChatFormatting.GRAY + "" + param0.serverPing.nrOfPlayers;
                    RealmsMainScreen.this.font.draw(var4, (float)(param1 + 207 - RealmsMainScreen.this.font.width(var4)), (float)(param2 + 3), 8421504);
                    if (param3 >= param1 + 207 - RealmsMainScreen.this.font.width(var4)
                        && param3 <= param1 + 207
                        && param4 >= param2 + 1
                        && param4 <= param2 + 10
                        && param4 < RealmsMainScreen.this.height - 40
                        && param4 > 32
                        && !RealmsMainScreen.this.shouldShowPopup()) {
                        RealmsMainScreen.this.toolTip = param0.serverPing.playerList;
                    }
                }

                if (RealmsMainScreen.this.isSelfOwnedServer(param0) && param0.expired) {
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.enableBlend();
                    RealmsMainScreen.this.minecraft.getTextureManager().bind(RealmsMainScreen.BUTTON_LOCATION);
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    String var5 = I18n.get("mco.selectServer.expiredList");
                    String var6 = I18n.get("mco.selectServer.expiredRenew");
                    if (param0.expiredTrial) {
                        var5 = I18n.get("mco.selectServer.expiredTrial");
                        var6 = I18n.get("mco.selectServer.expiredSubscribe");
                    }

                    int var7 = RealmsMainScreen.this.font.width(var6) + 17;
                    int var8 = 16;
                    int var9 = param1 + RealmsMainScreen.this.font.width(var5) + 8;
                    int var10 = param2 + 13;
                    boolean var11 = false;
                    if (param3 >= var9
                        && param3 < var9 + var7
                        && param4 > var10
                        && param4 <= var10 + 16 & param4 < RealmsMainScreen.this.height - 40
                        && param4 > 32
                        && !RealmsMainScreen.this.shouldShowPopup()) {
                        var11 = true;
                        RealmsMainScreen.this.expiredHover = true;
                    }

                    int var12 = var11 ? 2 : 1;
                    GuiComponent.blit(var9, var10, 0.0F, (float)(46 + var12 * 20), var7 / 2, 8, 256, 256);
                    GuiComponent.blit(var9 + var7 / 2, var10, (float)(200 - var7 / 2), (float)(46 + var12 * 20), var7 / 2, 8, 256, 256);
                    GuiComponent.blit(var9, var10 + 8, 0.0F, (float)(46 + var12 * 20 + 12), var7 / 2, 8, 256, 256);
                    GuiComponent.blit(var9 + var7 / 2, var10 + 8, (float)(200 - var7 / 2), (float)(46 + var12 * 20 + 12), var7 / 2, 8, 256, 256);
                    RenderSystem.disableBlend();
                    int var13 = param2 + 11 + 5;
                    int var14 = var11 ? 16777120 : 16777215;
                    RealmsMainScreen.this.font.draw(var5, (float)(param1 + 2), (float)(var13 + 1), 15553363);
                    RealmsMainScreen.this.drawCenteredString(RealmsMainScreen.this.font, var6, var9 + var7 / 2, var13 + 1, var14);
                } else {
                    if (param0.worldType == RealmsServer.WorldType.MINIGAME) {
                        int var15 = 13413468;
                        String var16 = I18n.get("mco.selectServer.minigame") + " ";
                        int var17 = RealmsMainScreen.this.font.width(var16);
                        RealmsMainScreen.this.font.draw(var16, (float)(param1 + 2), (float)(param2 + 12), 13413468);
                        RealmsMainScreen.this.font.draw(param0.getMinigameName(), (float)(param1 + 2 + var17), (float)(param2 + 12), 7105644);
                    } else {
                        RealmsMainScreen.this.font.draw(param0.getDescription(), (float)(param1 + 2), (float)(param2 + 12), 7105644);
                    }

                    if (!RealmsMainScreen.this.isSelfOwnedServer(param0)) {
                        RealmsMainScreen.this.font.draw(param0.owner, (float)(param1 + 2), (float)(param2 + 12 + 11), 5000268);
                    }
                }

                RealmsMainScreen.this.font.draw(param0.getName(), (float)(param1 + 2), (float)(param2 + 1), 16777215);
                RealmsTextureManager.withBoundFace(param0.ownerUUID, () -> {
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GuiComponent.blit(param1 - 36, param2, 32, 32, 8.0F, 8.0F, 8, 8, 64, 64);
                    GuiComponent.blit(param1 - 36, param2, 32, 32, 40.0F, 8.0F, 8, 8, 64, 64);
                });
            }
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
                I18n.get("mco.selectServer.info"),
                param1 -> RealmsMainScreen.this.popupOpenedByUser = !RealmsMainScreen.this.popupOpenedByUser
            );
        }

        @Override
        public void renderButton(int param0, int param1, float param2) {
            RealmsMainScreen.this.renderMoreInfo(param0, param1, this.x, this.y, this.isHovered());
        }
    }

    @OnlyIn(Dist.CLIENT)
    class TrialEntry extends RealmsMainScreen.Entry {
        private TrialEntry() {
        }

        @Override
        public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
            this.renderTrialItem(param0, param2, param1, param5, param6);
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            RealmsMainScreen.this.popupOpenedByUser = true;
            return true;
        }

        private void renderTrialItem(int param0, int param1, int param2, int param3, int param4) {
            int var0 = param2 + 8;
            int var1 = 0;
            String var2 = I18n.get("mco.trial.message.line1") + "\\n" + I18n.get("mco.trial.message.line2");
            boolean var3 = false;
            if (param1 <= param3 && param3 <= (int)RealmsMainScreen.this.realmSelectionList.getScrollAmount() && param2 <= param4 && param4 <= param2 + 32) {
                var3 = true;
            }

            int var4 = 8388479;
            if (var3 && !RealmsMainScreen.this.shouldShowPopup()) {
                var4 = 6077788;
            }

            for(String var5 : var2.split("\\\\n")) {
                RealmsMainScreen.this.drawCenteredString(RealmsMainScreen.this.font, var5, RealmsMainScreen.this.width / 2, var0 + var1, var4);
                var1 += 10;
            }

        }
    }
}
