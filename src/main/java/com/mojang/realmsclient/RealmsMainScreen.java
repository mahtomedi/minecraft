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
import com.mojang.realmsclient.gui.ChatFormatting;
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
import com.mojang.realmsclient.util.RealmsTasks;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.RealmsUtil;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsMth;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsMainScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean overrideConfigure;
    private final RateLimiter inviteNarrationLimiter;
    private boolean dontSetConnectedToRealms;
    private static List<ResourceLocation> teaserImages = ImmutableList.of();
    private static final RealmsDataFetcher realmsDataFetcher = new RealmsDataFetcher();
    private static int lastScrollYPosition = -1;
    private final RealmsScreen lastScreen;
    private volatile RealmsMainScreen.RealmSelectionList realmSelectionList;
    private long selectedServerId = -1L;
    private RealmsButton playButton;
    private RealmsButton backButton;
    private RealmsButton renewButton;
    private RealmsButton configureButton;
    private RealmsButton leaveButton;
    private String toolTip;
    private List<RealmsServer> realmsServers = Lists.newArrayList();
    private volatile int numberOfPendingInvites;
    private int animTick;
    private static volatile boolean hasParentalConsent;
    private static volatile boolean checkedParentalConsent;
    private static volatile boolean checkedClientCompatability;
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
    private static RealmsScreen realmsGenericErrorScreen;
    private static boolean regionsPinged;
    private List<KeyCombo> keyCombos;
    private int clicks;
    private ReentrantLock connectLock = new ReentrantLock();
    private boolean expiredHover;
    private RealmsMainScreen.ShowPopupButton showPopupButton;
    private RealmsMainScreen.PendingInvitesButton pendingInvitesButton;
    private RealmsMainScreen.NewsButton newsButton;
    private RealmsButton createTrialButton;
    private RealmsButton buyARealmButton;
    private RealmsButton closeButton;

    public RealmsMainScreen(RealmsScreen param0) {
        this.lastScreen = param0;
        this.inviteNarrationLimiter = RateLimiter.create(0.016666668F);
    }

    public boolean shouldShowMessageInList() {
        if (this.hasParentalConsent() && this.hasFetchedServers) {
            if (this.trialsAvailable && !this.createdTrial) {
                return true;
            } else {
                for(RealmsServer var0 : this.realmsServers) {
                    if (var0.ownerUUID.equals(Realms.getUUID())) {
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
        if (!this.hasParentalConsent() || !this.hasFetchedServers) {
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
                if (RealmsClient.currentEnvironment.equals(RealmsClient.Environment.STAGE)) {
                    this.switchToProd();
                } else {
                    this.switchToStage();
                }
    
            }),
            new KeyCombo(new char[]{'9', '8', '7', '4', '5', '6'}, () -> {
                if (RealmsClient.currentEnvironment.equals(RealmsClient.Environment.LOCAL)) {
                    this.switchToProd();
                } else {
                    this.switchToLocal();
                }
    
            })
        );
        if (realmsGenericErrorScreen != null) {
            Realms.setScreen(realmsGenericErrorScreen);
        } else {
            this.connectLock = new ReentrantLock();
            if (checkedClientCompatability && !this.hasParentalConsent()) {
                this.checkParentalConsent();
            }

            this.checkClientCompatability();
            this.checkUnreadNews();
            if (!this.dontSetConnectedToRealms) {
                Realms.setConnectedToRealms(false);
            }

            this.setKeyboardHandlerSendRepeatsToGui(true);
            if (this.hasParentalConsent()) {
                realmsDataFetcher.forceUpdate();
            }

            this.showingPopup = false;
            this.postInit();
        }
    }

    private boolean hasParentalConsent() {
        return checkedParentalConsent && hasParentalConsent;
    }

    public void addButtons() {
        this.buttonsAdd(
            this.configureButton = new RealmsButton(1, this.width() / 2 - 190, this.height() - 32, 90, 20, getLocalizedString("mco.selectServer.configure")) {
                @Override
                public void onPress() {
                    RealmsMainScreen.this.configureClicked(RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId));
                }
            }
        );
        this.buttonsAdd(this.playButton = new RealmsButton(3, this.width() / 2 - 93, this.height() - 32, 90, 20, getLocalizedString("mco.selectServer.play")) {
            @Override
            public void onPress() {
                RealmsMainScreen.this.onPlay();
            }
        });
        this.buttonsAdd(this.backButton = new RealmsButton(2, this.width() / 2 + 4, this.height() - 32, 90, 20, getLocalizedString("gui.back")) {
            @Override
            public void onPress() {
                if (!RealmsMainScreen.this.justClosedPopup) {
                    Realms.setScreen(RealmsMainScreen.this.lastScreen);
                }

            }
        });
        this.buttonsAdd(
            this.renewButton = new RealmsButton(0, this.width() / 2 + 100, this.height() - 32, 90, 20, getLocalizedString("mco.selectServer.expiredRenew")) {
                @Override
                public void onPress() {
                    RealmsMainScreen.this.onRenew();
                }
            }
        );
        this.buttonsAdd(
            this.leaveButton = new RealmsButton(7, this.width() / 2 - 202, this.height() - 32, 90, 20, getLocalizedString("mco.selectServer.leave")) {
                @Override
                public void onPress() {
                    RealmsMainScreen.this.leaveClicked(RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId));
                }
            }
        );
        this.buttonsAdd(this.pendingInvitesButton = new RealmsMainScreen.PendingInvitesButton());
        this.buttonsAdd(this.newsButton = new RealmsMainScreen.NewsButton());
        this.buttonsAdd(this.showPopupButton = new RealmsMainScreen.ShowPopupButton());
        this.buttonsAdd(this.closeButton = new RealmsMainScreen.CloseButton());
        this.buttonsAdd(
            this.createTrialButton = new RealmsButton(6, this.width() / 2 + 52, this.popupY0() + 137 - 20, 98, 20, getLocalizedString("mco.selectServer.trial")) {
                @Override
                public void onPress() {
                    RealmsMainScreen.this.createTrial();
                }
            }
        );
        this.buttonsAdd(
            this.buyARealmButton = new RealmsButton(5, this.width() / 2 + 52, this.popupY0() + 160 - 20, 98, 20, getLocalizedString("mco.selectServer.buy")) {
                @Override
                public void onPress() {
                    RealmsUtil.browseTo("https://aka.ms/BuyJavaRealms");
                }
            }
        );
        RealmsServer var0 = this.findServer(this.selectedServerId);
        this.updateButtonStates(var0);
    }

    private void updateButtonStates(RealmsServer param0) {
        this.playButton.active(this.shouldPlayButtonBeActive(param0) && !this.shouldShowPopup());
        this.renewButton.setVisible(this.shouldRenewButtonBeActive(param0));
        this.configureButton.setVisible(this.shouldConfigureButtonBeVisible(param0));
        this.leaveButton.setVisible(this.shouldLeaveButtonBeVisible(param0));
        boolean var0 = this.shouldShowPopup() && this.trialsAvailable && !this.createdTrial;
        this.createTrialButton.setVisible(var0);
        this.createTrialButton.active(var0);
        this.buyARealmButton.setVisible(this.shouldShowPopup());
        this.closeButton.setVisible(this.shouldShowPopup() && this.popupOpenedByUser);
        this.renewButton.active(!this.shouldShowPopup());
        this.configureButton.active(!this.shouldShowPopup());
        this.leaveButton.active(!this.shouldShowPopup());
        this.newsButton.active(true);
        this.pendingInvitesButton.active(true);
        this.backButton.active(true);
        this.showPopupButton.active(!this.shouldShowPopup());
    }

    private boolean shouldShowPopupButton() {
        return (!this.shouldShowPopup() || this.popupOpenedByUser) && this.hasParentalConsent() && this.hasFetchedServers;
    }

    private boolean shouldPlayButtonBeActive(RealmsServer param0) {
        return param0 != null && !param0.expired && param0.state == RealmsServer.State.OPEN;
    }

    private boolean shouldRenewButtonBeActive(RealmsServer param0) {
        return param0 != null && param0.expired && this.isSelfOwnedServer(param0);
    }

    private boolean shouldConfigureButtonBeVisible(RealmsServer param0) {
        return param0 != null && this.isSelfOwnedServer(param0);
    }

    private boolean shouldLeaveButtonBeVisible(RealmsServer param0) {
        return param0 != null && !this.isSelfOwnedServer(param0);
    }

    public void postInit() {
        if (this.hasParentalConsent() && this.hasFetchedServers) {
            this.addButtons();
        }

        this.realmSelectionList = new RealmsMainScreen.RealmSelectionList();
        if (lastScrollYPosition != -1) {
            this.realmSelectionList.scroll(lastScrollYPosition);
        }

        this.addWidget(this.realmSelectionList);
        this.focusOn(this.realmSelectionList);
    }

    @Override
    public void tick() {
        this.tickButtons();
        this.justClosedPopup = false;
        ++this.animTick;
        --this.clicks;
        if (this.clicks < 0) {
            this.clicks = 0;
        }

        if (this.hasParentalConsent()) {
            realmsDataFetcher.init();
            if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.SERVER_LIST)) {
                List<RealmsServer> var0 = realmsDataFetcher.getServers();
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
                        this.realmSelectionList.addEntry(new RealmsMainScreen.RealmSelectionListTrialEntry());
                    }

                    for(RealmsServer var4 : this.realmsServers) {
                        this.realmSelectionList.addEntry(new RealmsMainScreen.RealmSelectionListEntry(var4));
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

            if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.PENDING_INVITE)) {
                this.numberOfPendingInvites = realmsDataFetcher.getPendingInvitesCount();
                if (this.numberOfPendingInvites > 0 && this.inviteNarrationLimiter.tryAcquire(1)) {
                    Realms.narrateNow(getLocalizedString("mco.configure.world.invite.narration", new Object[]{this.numberOfPendingInvites}));
                }
            }

            if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.TRIAL_AVAILABLE) && !this.createdTrial) {
                boolean var5 = realmsDataFetcher.isTrialAvailable();
                if (var5 != this.trialsAvailable && this.shouldShowPopup()) {
                    this.trialsAvailable = var5;
                    this.showingPopup = false;
                } else {
                    this.trialsAvailable = var5;
                }
            }

            if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.LIVE_STATS)) {
                RealmsServerPlayerLists var6 = realmsDataFetcher.getLivestats();

                for(RealmsServerPlayerList var7 : var6.servers) {
                    for(RealmsServer var8 : this.realmsServers) {
                        if (var8.id == var7.serverId) {
                            var8.updateServerPing(var7);
                            break;
                        }
                    }
                }
            }

            if (realmsDataFetcher.isFetchedSinceLastTry(RealmsDataFetcher.Task.UNREAD_NEWS)) {
                this.hasUnreadNews = realmsDataFetcher.hasUnreadNews();
                this.newsLink = realmsDataFetcher.newsLink();
            }

            realmsDataFetcher.markClean();
            if (this.shouldShowPopup()) {
                ++this.carouselTick;
            }

            if (this.showPopupButton != null) {
                this.showPopupButton.setVisible(this.shouldShowPopupButton());
            }

        }
    }

    private void browseURL(String param0) {
        Realms.setClipboard(param0);
        RealmsUtil.browseTo(param0);
    }

    private void pingRegions() {
        new Thread(() -> {
            List<RegionPingResult> var0 = Ping.pingAllRegions();
            RealmsClient var1 = RealmsClient.createRealmsClient();
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
        this.setKeyboardHandlerSendRepeatsToGui(false);
        this.stopRealmsFetcher();
    }

    private void onPlay() {
        RealmsServer var0 = this.findServer(this.selectedServerId);
        if (var0 != null) {
            this.play(var0, this);
        }
    }

    private void onRenew() {
        RealmsServer var0 = this.findServer(this.selectedServerId);
        if (var0 != null) {
            String var1 = "https://aka.ms/ExtendJavaRealms?subscriptionId="
                + var0.remoteSubscriptionId
                + "&profileId="
                + Realms.getUUID()
                + "&ref="
                + (var0.expiredTrial ? "expiredTrial" : "expiredRealm");
            this.browseURL(var1);
        }
    }

    private void createTrial() {
        if (this.trialsAvailable && !this.createdTrial) {
            RealmsUtil.browseTo("https://aka.ms/startjavarealmstrial");
            Realms.setScreen(this.lastScreen);
        }
    }

    private void checkClientCompatability() {
        if (!checkedClientCompatability) {
            checkedClientCompatability = true;
            (new Thread("MCO Compatability Checker #1") {
                    @Override
                    public void run() {
                        RealmsClient var0 = RealmsClient.createRealmsClient();
    
                        try {
                            RealmsClient.CompatibleVersionResponse var1 = var0.clientCompatible();
                            if (var1.equals(RealmsClient.CompatibleVersionResponse.OUTDATED)) {
                                RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen, true);
                                Realms.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
                            } else if (var1.equals(RealmsClient.CompatibleVersionResponse.OTHER)) {
                                RealmsMainScreen.realmsGenericErrorScreen = new RealmsClientOutdatedScreen(RealmsMainScreen.this.lastScreen, false);
                                Realms.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
                            } else {
                                RealmsMainScreen.this.checkParentalConsent();
                            }
                        } catch (RealmsServiceException var31) {
                            RealmsMainScreen.checkedClientCompatability = false;
                            RealmsMainScreen.LOGGER.error("Couldn't connect to realms: ", var31.toString());
                            if (var31.httpResultCode == 401) {
                                RealmsMainScreen.realmsGenericErrorScreen = new RealmsGenericErrorScreen(
                                    RealmsScreen.getLocalizedString("mco.error.invalid.session.title"),
                                    RealmsScreen.getLocalizedString("mco.error.invalid.session.message"),
                                    RealmsMainScreen.this.lastScreen
                                );
                                Realms.setScreen(RealmsMainScreen.realmsGenericErrorScreen);
                            } else {
                                Realms.setScreen(new RealmsGenericErrorScreen(var31, RealmsMainScreen.this.lastScreen));
                            }
                        } catch (IOException var4) {
                            RealmsMainScreen.checkedClientCompatability = false;
                            RealmsMainScreen.LOGGER.error("Couldn't connect to realms: ", var4.getMessage());
                            Realms.setScreen(new RealmsGenericErrorScreen(var4.getMessage(), RealmsMainScreen.this.lastScreen));
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
                RealmsClient var0 = RealmsClient.createRealmsClient();

                try {
                    Boolean var1 = var0.mcoEnabled();
                    if (var1) {
                        RealmsMainScreen.LOGGER.info("Realms is available for this user");
                        RealmsMainScreen.hasParentalConsent = true;
                    } else {
                        RealmsMainScreen.LOGGER.info("Realms is not available for this user");
                        RealmsMainScreen.hasParentalConsent = false;
                        Realms.setScreen(new RealmsParentalConsentScreen(RealmsMainScreen.this.lastScreen));
                    }

                    RealmsMainScreen.checkedParentalConsent = true;
                } catch (RealmsServiceException var31) {
                    RealmsMainScreen.LOGGER.error("Couldn't connect to realms: ", var31.toString());
                    Realms.setScreen(new RealmsGenericErrorScreen(var31, RealmsMainScreen.this.lastScreen));
                } catch (IOException var4) {
                    RealmsMainScreen.LOGGER.error("Couldn't connect to realms: ", var4.getMessage());
                    Realms.setScreen(new RealmsGenericErrorScreen(var4.getMessage(), RealmsMainScreen.this.lastScreen));
                }

            }
        }).start();
    }

    private void switchToStage() {
        if (!RealmsClient.currentEnvironment.equals(RealmsClient.Environment.STAGE)) {
            (new Thread("MCO Stage Availability Checker #1") {
                @Override
                public void run() {
                    RealmsClient var0 = RealmsClient.createRealmsClient();

                    try {
                        Boolean var1 = var0.stageAvailable();
                        if (var1) {
                            RealmsClient.switchToStage();
                            RealmsMainScreen.LOGGER.info("Switched to stage");
                            RealmsMainScreen.realmsDataFetcher.forceUpdate();
                        }
                    } catch (RealmsServiceException var31) {
                        RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: " + var31);
                    } catch (IOException var4) {
                        RealmsMainScreen.LOGGER.error("Couldn't parse response connecting to Realms: " + var4.getMessage());
                    }

                }
            }).start();
        }

    }

    private void switchToLocal() {
        if (!RealmsClient.currentEnvironment.equals(RealmsClient.Environment.LOCAL)) {
            (new Thread("MCO Local Availability Checker #1") {
                @Override
                public void run() {
                    RealmsClient var0 = RealmsClient.createRealmsClient();

                    try {
                        Boolean var1 = var0.stageAvailable();
                        if (var1) {
                            RealmsClient.switchToLocal();
                            RealmsMainScreen.LOGGER.info("Switched to local");
                            RealmsMainScreen.realmsDataFetcher.forceUpdate();
                        }
                    } catch (RealmsServiceException var31) {
                        RealmsMainScreen.LOGGER.error("Couldn't connect to Realms: " + var31);
                    } catch (IOException var4) {
                        RealmsMainScreen.LOGGER.error("Couldn't parse response connecting to Realms: " + var4.getMessage());
                    }

                }
            }).start();
        }

    }

    private void switchToProd() {
        RealmsClient.switchToProd();
        realmsDataFetcher.forceUpdate();
    }

    private void stopRealmsFetcher() {
        realmsDataFetcher.stop();
    }

    private void configureClicked(RealmsServer param0) {
        if (Realms.getUUID().equals(param0.ownerUUID) || overrideConfigure) {
            this.saveListScrollPosition();
            Minecraft var0 = Minecraft.getInstance();
            var0.execute(() -> var0.setScreen(new RealmsConfigureWorldScreen(this, param0.id).getProxy()));
        }

    }

    private void leaveClicked(@Nullable RealmsServer param0) {
        if (param0 != null && !Realms.getUUID().equals(param0.ownerUUID)) {
            this.saveListScrollPosition();
            String var0 = getLocalizedString("mco.configure.world.leave.question.line1");
            String var1 = getLocalizedString("mco.configure.world.leave.question.line2");
            Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Info, var0, var1, true, 4));
        }

    }

    private void saveListScrollPosition() {
        lastScrollYPosition = this.realmSelectionList.getScroll();
    }

    private RealmsServer findServer(long param0) {
        for(RealmsServer var0 : this.realmsServers) {
            if (var0.id == param0) {
                return var0;
            }
        }

        return null;
    }

    @Override
    public void confirmResult(boolean param0, int param1) {
        if (param1 == 4) {
            if (param0) {
                (new Thread("Realms-leave-server") {
                        @Override
                        public void run() {
                            try {
                                RealmsServer var0 = RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId);
                                if (var0 != null) {
                                    RealmsClient var1 = RealmsClient.createRealmsClient();
                                    var1.uninviteMyselfFrom(var0.id);
                                    RealmsMainScreen.realmsDataFetcher.removeItem(var0);
                                    RealmsMainScreen.this.realmsServers.remove(var0);
                                    RealmsMainScreen.this.realmSelectionList
                                        .children()
                                        .removeIf(
                                            param0 -> param0 instanceof RealmsMainScreen.RealmSelectionListEntry
                                                    && ((RealmsMainScreen.RealmSelectionListEntry)param0).mServerData.id
                                                        == RealmsMainScreen.this.selectedServerId
                                        );
                                    RealmsMainScreen.this.realmSelectionList.setSelected(-1);
                                    RealmsMainScreen.this.updateButtonStates(null);
                                    RealmsMainScreen.this.selectedServerId = -1L;
                                    RealmsMainScreen.this.playButton.active(false);
                                }
                            } catch (RealmsServiceException var3) {
                                RealmsMainScreen.LOGGER.error("Couldn't configure world");
                                Realms.setScreen(new RealmsGenericErrorScreen(var3, RealmsMainScreen.this));
                            }
    
                        }
                    })
                    .start();
            }

            Realms.setScreen(this);
        }

    }

    public void removeSelection() {
        this.selectedServerId = -1L;
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        switch(param0) {
            case 256:
                this.keyCombos.forEach(KeyCombo::reset);
                this.onClosePopup();
                return true;
            default:
                return super.keyPressed(param0, param1, param2);
        }
    }

    private void onClosePopup() {
        if (this.shouldShowPopup() && this.popupOpenedByUser) {
            this.popupOpenedByUser = false;
        } else {
            Realms.setScreen(this.lastScreen);
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
        this.drawRealmsLogo(this.width() / 2 - 50, 7);
        if (RealmsClient.currentEnvironment.equals(RealmsClient.Environment.STAGE)) {
            this.renderStage();
        }

        if (RealmsClient.currentEnvironment.equals(RealmsClient.Environment.LOCAL)) {
            this.renderLocal();
        }

        if (this.shouldShowPopup()) {
            this.drawPopup(param0, param1);
        } else {
            if (this.showingPopup) {
                this.updateButtonStates(null);
                if (!this.hasWidget(this.realmSelectionList)) {
                    this.addWidget(this.realmSelectionList);
                }

                RealmsServer var0 = this.findServer(this.selectedServerId);
                this.playButton.active(this.shouldPlayButtonBeActive(var0));
            }

            this.showingPopup = false;
        }

        super.render(param0, param1, param2);
        if (this.toolTip != null) {
            this.renderMousehoverTooltip(this.toolTip, param0, param1);
        }

        if (this.trialsAvailable && !this.createdTrial && this.shouldShowPopup()) {
            RealmsScreen.bind("realms:textures/gui/realms/trial_icon.png");
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.pushMatrix();
            int var1 = 8;
            int var2 = 8;
            int var3 = 0;
            if ((System.currentTimeMillis() / 800L & 1L) == 1L) {
                var3 = 8;
            }

            RealmsScreen.blit(
                this.createTrialButton.x() + this.createTrialButton.getWidth() - 8 - 4,
                this.createTrialButton.y() + this.createTrialButton.getHeight() / 2 - 4,
                0.0F,
                (float)var3,
                8,
                8,
                8,
                16
            );
            RenderSystem.popMatrix();
        }

    }

    private void drawRealmsLogo(int param0, int param1) {
        RealmsScreen.bind("realms:textures/gui/title/realms.png");
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        RenderSystem.scalef(0.5F, 0.5F, 0.5F);
        RealmsScreen.blit(param0 * 2, param1 * 2 - 5, 0.0F, 0.0F, 200, 50, 200, 50);
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
        String var2 = getLocalizedString("mco.selectServer.popup");
        List<String> var3 = this.fontSplit(var2, 100);
        if (!this.showingPopup) {
            this.carouselIndex = 0;
            this.carouselTick = 0;
            this.hasSwitchedCarouselImage = true;
            this.updateButtonStates(null);
            if (this.hasWidget(this.realmSelectionList)) {
                this.removeWidget(this.realmSelectionList);
            }

            Realms.narrateNow(var2);
        }

        if (this.hasFetchedServers) {
            this.showingPopup = true;
        }

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.7F);
        RenderSystem.enableBlend();
        RealmsScreen.bind("realms:textures/gui/realms/darken.png");
        RenderSystem.pushMatrix();
        int var4 = 0;
        int var5 = 32;
        RealmsScreen.blit(0, 32, 0.0F, 0.0F, this.width(), this.height() - 40 - 32, 310, 166);
        RenderSystem.popMatrix();
        RenderSystem.disableBlend();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RealmsScreen.bind("realms:textures/gui/realms/popup.png");
        RenderSystem.pushMatrix();
        RealmsScreen.blit(var0, var1, 0.0F, 0.0F, 310, 166, 310, 166);
        RenderSystem.popMatrix();
        if (!teaserImages.isEmpty()) {
            RealmsScreen.bind(teaserImages.get(this.carouselIndex).toString());
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.pushMatrix();
            RealmsScreen.blit(var0 + 7, var1 + 7, 0.0F, 0.0F, 195, 152, 195, 152);
            RenderSystem.popMatrix();
            if (this.carouselTick % 95 < 5) {
                if (!this.hasSwitchedCarouselImage) {
                    this.carouselIndex = (this.carouselIndex + 1) % teaserImages.size();
                    this.hasSwitchedCarouselImage = true;
                }
            } else {
                this.hasSwitchedCarouselImage = false;
            }
        }

        int var6 = 0;

        for(String var7 : var3) {
            int var10002 = this.width() / 2 + 52;
            ++var6;
            this.drawString(var7, var10002, var1 + 10 * var6 - 3, 8421504, false);
        }

    }

    private int popupX0() {
        return (this.width() - 310) / 2;
    }

    private int popupY0() {
        return this.height() / 2 - 80;
    }

    private void drawInvitationPendingIcon(int param0, int param1, int param2, int param3, boolean param4, boolean param5) {
        int var0 = this.numberOfPendingInvites;
        boolean var1 = this.inPendingInvitationArea((double)param0, (double)param1);
        boolean var2 = param5 && param4;
        if (var2) {
            float var3 = 0.25F + (1.0F + RealmsMth.sin((float)this.animTick * 0.5F)) * 0.25F;
            int var4 = 0xFF000000 | (int)(var3 * 64.0F) << 16 | (int)(var3 * 64.0F) << 8 | (int)(var3 * 64.0F) << 0;
            this.fillGradient(param2 - 2, param3 - 2, param2 + 18, param3 + 18, var4, var4);
            var4 = 0xFF000000 | (int)(var3 * 255.0F) << 16 | (int)(var3 * 255.0F) << 8 | (int)(var3 * 255.0F) << 0;
            this.fillGradient(param2 - 2, param3 - 2, param2 + 18, param3 - 1, var4, var4);
            this.fillGradient(param2 - 2, param3 - 2, param2 - 1, param3 + 18, var4, var4);
            this.fillGradient(param2 + 17, param3 - 2, param2 + 18, param3 + 18, var4, var4);
            this.fillGradient(param2 - 2, param3 + 17, param2 + 18, param3 + 18, var4, var4);
        }

        RealmsScreen.bind("realms:textures/gui/realms/invite_icon.png");
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        boolean var5 = param5 && param4;
        RealmsScreen.blit(param2, param3 - 6, var5 ? 16.0F : 0.0F, 0.0F, 15, 25, 31, 25);
        RenderSystem.popMatrix();
        boolean var6 = param5 && var0 != 0;
        if (var6) {
            int var7 = (Math.min(var0, 6) - 1) * 8;
            int var8 = (int)(Math.max(0.0F, Math.max(RealmsMth.sin((float)(10 + this.animTick) * 0.57F), RealmsMth.cos((float)this.animTick * 0.35F))) * -6.0F);
            RealmsScreen.bind("realms:textures/gui/realms/invitation_icons.png");
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.pushMatrix();
            RealmsScreen.blit(param2 + 4, param3 + 4 + var8, (float)var7, var1 ? 8.0F : 0.0F, 8, 8, 48, 16);
            RenderSystem.popMatrix();
        }

        int var9 = param0 + 12;
        boolean var11 = param5 && var1;
        if (var11) {
            String var12 = getLocalizedString(var0 == 0 ? "mco.invites.nopending" : "mco.invites.pending");
            int var13 = this.fontWidth(var12);
            this.fillGradient(var9 - 3, param1 - 3, var9 + var13 + 3, param1 + 8 + 3, -1073741824, -1073741824);
            this.fontDrawShadow(var12, var9, param1, -1);
        }

    }

    private boolean inPendingInvitationArea(double param0, double param1) {
        int var0 = this.width() / 2 + 50;
        int var1 = this.width() / 2 + 66;
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

    public void play(RealmsServer param0, RealmsScreen param1) {
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
            this.connectToServer(param0, param1);
        }

    }

    private void connectToServer(RealmsServer param0, RealmsScreen param1) {
        RealmsLongRunningMcoTaskScreen var0 = new RealmsLongRunningMcoTaskScreen(
            param1, new RealmsTasks.RealmsGetServerDetailsTask(this, param1, param0, this.connectLock)
        );
        var0.start();
        Realms.setScreen(var0);
    }

    private boolean isSelfOwnedServer(RealmsServer param0) {
        return param0.ownerUUID != null && param0.ownerUUID.equals(Realms.getUUID());
    }

    private boolean isSelfOwnedNonExpiredServer(RealmsServer param0) {
        return param0.ownerUUID != null && param0.ownerUUID.equals(Realms.getUUID()) && !param0.expired;
    }

    private void drawExpired(int param0, int param1, int param2, int param3) {
        RealmsScreen.bind("realms:textures/gui/realms/expired_icon.png");
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        RealmsScreen.blit(param0, param1, 0.0F, 0.0F, 10, 28, 10, 28);
        RenderSystem.popMatrix();
        if (param2 >= param0
            && param2 <= param0 + 9
            && param3 >= param1
            && param3 <= param1 + 27
            && param3 < this.height() - 40
            && param3 > 32
            && !this.shouldShowPopup()) {
            this.toolTip = getLocalizedString("mco.selectServer.expired");
        }

    }

    private void drawExpiring(int param0, int param1, int param2, int param3, int param4) {
        RealmsScreen.bind("realms:textures/gui/realms/expires_soon_icon.png");
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        if (this.animTick % 20 < 10) {
            RealmsScreen.blit(param0, param1, 0.0F, 0.0F, 10, 28, 20, 28);
        } else {
            RealmsScreen.blit(param0, param1, 10.0F, 0.0F, 10, 28, 20, 28);
        }

        RenderSystem.popMatrix();
        if (param2 >= param0
            && param2 <= param0 + 9
            && param3 >= param1
            && param3 <= param1 + 27
            && param3 < this.height() - 40
            && param3 > 32
            && !this.shouldShowPopup()) {
            if (param4 <= 0) {
                this.toolTip = getLocalizedString("mco.selectServer.expires.soon");
            } else if (param4 == 1) {
                this.toolTip = getLocalizedString("mco.selectServer.expires.day");
            } else {
                this.toolTip = getLocalizedString("mco.selectServer.expires.days", new Object[]{param4});
            }
        }

    }

    private void drawOpen(int param0, int param1, int param2, int param3) {
        RealmsScreen.bind("realms:textures/gui/realms/on_icon.png");
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        RealmsScreen.blit(param0, param1, 0.0F, 0.0F, 10, 28, 10, 28);
        RenderSystem.popMatrix();
        if (param2 >= param0
            && param2 <= param0 + 9
            && param3 >= param1
            && param3 <= param1 + 27
            && param3 < this.height() - 40
            && param3 > 32
            && !this.shouldShowPopup()) {
            this.toolTip = getLocalizedString("mco.selectServer.open");
        }

    }

    private void drawClose(int param0, int param1, int param2, int param3) {
        RealmsScreen.bind("realms:textures/gui/realms/off_icon.png");
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        RealmsScreen.blit(param0, param1, 0.0F, 0.0F, 10, 28, 10, 28);
        RenderSystem.popMatrix();
        if (param2 >= param0
            && param2 <= param0 + 9
            && param3 >= param1
            && param3 <= param1 + 27
            && param3 < this.height() - 40
            && param3 > 32
            && !this.shouldShowPopup()) {
            this.toolTip = getLocalizedString("mco.selectServer.closed");
        }

    }

    private void drawLeave(int param0, int param1, int param2, int param3) {
        boolean var0 = false;
        if (param2 >= param0
            && param2 <= param0 + 28
            && param3 >= param1
            && param3 <= param1 + 28
            && param3 < this.height() - 40
            && param3 > 32
            && !this.shouldShowPopup()) {
            var0 = true;
        }

        RealmsScreen.bind("realms:textures/gui/realms/leave_icon.png");
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        RealmsScreen.blit(param0, param1, var0 ? 28.0F : 0.0F, 0.0F, 28, 28, 56, 28);
        RenderSystem.popMatrix();
        if (var0) {
            this.toolTip = getLocalizedString("mco.selectServer.leave");
        }

    }

    private void drawConfigure(int param0, int param1, int param2, int param3) {
        boolean var0 = false;
        if (param2 >= param0
            && param2 <= param0 + 28
            && param3 >= param1
            && param3 <= param1 + 28
            && param3 < this.height() - 40
            && param3 > 32
            && !this.shouldShowPopup()) {
            var0 = true;
        }

        RealmsScreen.bind("realms:textures/gui/realms/configure_icon.png");
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        RealmsScreen.blit(param0, param1, var0 ? 28.0F : 0.0F, 0.0F, 28, 28, 56, 28);
        RenderSystem.popMatrix();
        if (var0) {
            this.toolTip = getLocalizedString("mco.selectServer.configure");
        }

    }

    protected void renderMousehoverTooltip(String param0, int param1, int param2) {
        if (param0 != null) {
            int var0 = 0;
            int var1 = 0;

            for(String var2 : param0.split("\n")) {
                int var3 = this.fontWidth(var2);
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
                this.fillGradient(var4 - 3, var5 - (var0 == 0 ? 3 : 0) + var0, var4 + var1 + 3, var5 + 8 + 3 + var0, -1073741824, -1073741824);
                this.fontDrawShadow(var6, var4, var5 + var0, 16777215);
                var0 += 10;
            }

        }
    }

    private void renderMoreInfo(int param0, int param1, int param2, int param3, boolean param4) {
        boolean var0 = false;
        if (param0 >= param2 && param0 <= param2 + 20 && param1 >= param3 && param1 <= param3 + 20) {
            var0 = true;
        }

        RealmsScreen.bind("realms:textures/gui/realms/questionmark.png");
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        RealmsScreen.blit(param2, param3, param4 ? 20.0F : 0.0F, 0.0F, 20, 20, 40, 20);
        RenderSystem.popMatrix();
        if (var0) {
            this.toolTip = getLocalizedString("mco.selectServer.info");
        }

    }

    private void renderNews(int param0, int param1, boolean param2, int param3, int param4, boolean param5, boolean param6) {
        boolean var0 = false;
        if (param0 >= param3 && param0 <= param3 + 20 && param1 >= param4 && param1 <= param4 + 20) {
            var0 = true;
        }

        RealmsScreen.bind("realms:textures/gui/realms/news_icon.png");
        if (param6) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);
        }

        RenderSystem.pushMatrix();
        boolean var1 = param6 && param5;
        RealmsScreen.blit(param3, param4, var1 ? 20.0F : 0.0F, 0.0F, 20, 20, 40, 20);
        RenderSystem.popMatrix();
        if (var0 && param6) {
            this.toolTip = getLocalizedString("mco.news");
        }

        if (param2 && param6) {
            int var2 = var0
                ? 0
                : (int)(Math.max(0.0F, Math.max(RealmsMth.sin((float)(10 + this.animTick) * 0.57F), RealmsMth.cos((float)this.animTick * 0.35F))) * -6.0F);
            RealmsScreen.bind("realms:textures/gui/realms/invitation_icons.png");
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.pushMatrix();
            RealmsScreen.blit(param3 + 10, param4 + 2 + var2, 40.0F, 0.0F, 8, 8, 48, 16);
            RenderSystem.popMatrix();
        }

    }

    private void renderLocal() {
        String var0 = "LOCAL!";
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)(this.width() / 2 - 25), 20.0F, 0.0F);
        RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.scalef(1.5F, 1.5F, 1.5F);
        this.drawString("LOCAL!", 0, 0, 8388479);
        RenderSystem.popMatrix();
    }

    private void renderStage() {
        String var0 = "STAGE!";
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)(this.width() / 2 - 25), 20.0F, 0.0F);
        RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.scalef(1.5F, 1.5F, 1.5F);
        this.drawString("STAGE!", 0, 0, -256);
        RenderSystem.popMatrix();
    }

    public RealmsMainScreen newScreen() {
        return new RealmsMainScreen(this.lastScreen);
    }

    public static void updateTeaserImages(ResourceManager param0) {
        Collection<ResourceLocation> var0 = param0.listResources("textures/gui/images", param0x -> param0x.endsWith(".png"));
        teaserImages = var0.stream().filter(param0x -> param0x.getNamespace().equals("realms")).collect(ImmutableList.toImmutableList());
    }

    @OnlyIn(Dist.CLIENT)
    class CloseButton extends RealmsButton {
        public CloseButton() {
            super(
                11, RealmsMainScreen.this.popupX0() + 4, RealmsMainScreen.this.popupY0() + 4, 12, 12, RealmsScreen.getLocalizedString("mco.selectServer.close")
            );
        }

        @Override
        public void tick() {
            super.tick();
        }

        @Override
        public void render(int param0, int param1, float param2) {
            super.render(param0, param1, param2);
        }

        @Override
        public void renderButton(int param0, int param1, float param2) {
            RealmsScreen.bind("realms:textures/gui/realms/cross_icon.png");
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.pushMatrix();
            RealmsScreen.blit(this.x(), this.y(), 0.0F, this.getProxy().isHovered() ? 12.0F : 0.0F, 12, 12, 12, 24);
            RenderSystem.popMatrix();
            if (this.getProxy().isMouseOver((double)param0, (double)param1)) {
                RealmsMainScreen.this.toolTip = this.getProxy().getMessage();
            }

        }

        @Override
        public void onPress() {
            RealmsMainScreen.this.onClosePopup();
        }
    }

    @OnlyIn(Dist.CLIENT)
    class NewsButton extends RealmsButton {
        public NewsButton() {
            super(9, RealmsMainScreen.this.width() - 62, 6, 20, 20, "");
        }

        @Override
        public void tick() {
            this.setMessage(Realms.getLocalizedString("mco.news"));
        }

        @Override
        public void render(int param0, int param1, float param2) {
            super.render(param0, param1, param2);
        }

        @Override
        public void onPress() {
            if (RealmsMainScreen.this.newsLink != null) {
                RealmsUtil.browseTo(RealmsMainScreen.this.newsLink);
                if (RealmsMainScreen.this.hasUnreadNews) {
                    RealmsPersistence.RealmsPersistenceData var0 = RealmsPersistence.readFile();
                    var0.hasUnreadNews = false;
                    RealmsMainScreen.this.hasUnreadNews = false;
                    RealmsPersistence.writeFile(var0);
                }

            }
        }

        @Override
        public void renderButton(int param0, int param1, float param2) {
            RealmsMainScreen.this.renderNews(
                param0, param1, RealmsMainScreen.this.hasUnreadNews, this.x(), this.y(), this.getProxy().isHovered(), this.active()
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    class PendingInvitesButton extends RealmsButton {
        public PendingInvitesButton() {
            super(8, RealmsMainScreen.this.width() / 2 + 47, 6, 22, 22, "");
        }

        @Override
        public void tick() {
            this.setMessage(Realms.getLocalizedString(RealmsMainScreen.this.numberOfPendingInvites == 0 ? "mco.invites.nopending" : "mco.invites.pending"));
        }

        @Override
        public void render(int param0, int param1, float param2) {
            super.render(param0, param1, param2);
        }

        @Override
        public void onPress() {
            RealmsPendingInvitesScreen var0 = new RealmsPendingInvitesScreen(RealmsMainScreen.this.lastScreen);
            Realms.setScreen(var0);
        }

        @Override
        public void renderButton(int param0, int param1, float param2) {
            RealmsMainScreen.this.drawInvitationPendingIcon(param0, param1, this.x(), this.y(), this.getProxy().isHovered(), this.active());
        }
    }

    @OnlyIn(Dist.CLIENT)
    class RealmSelectionList extends RealmsObjectSelectionList<RealmListEntry> {
        public RealmSelectionList() {
            super(RealmsMainScreen.this.width(), RealmsMainScreen.this.height(), 32, RealmsMainScreen.this.height() - 40, 36);
        }

        @Override
        public boolean isFocused() {
            return RealmsMainScreen.this.isFocused(this);
        }

        @Override
        public boolean keyPressed(int param0, int param1, int param2) {
            if (param0 != 257 && param0 != 32 && param0 != 335) {
                return false;
            } else {
                RealmListEntry var0 = this.getSelected();
                return var0 == null ? super.keyPressed(param0, param1, param2) : var0.mouseClicked(0.0, 0.0, 0);
            }
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (param2 == 0 && param0 < (double)this.getScrollbarPosition() && param1 >= (double)this.y0() && param1 <= (double)this.y1()) {
                int var0 = RealmsMainScreen.this.realmSelectionList.getRowLeft();
                int var1 = this.getScrollbarPosition();
                int var2 = (int)Math.floor(param1 - (double)this.y0()) - this.headerHeight() + this.getScroll() - 4;
                int var3 = var2 / this.itemHeight();
                if (param0 >= (double)var0 && param0 <= (double)var1 && var3 >= 0 && var2 >= 0 && var3 < this.getItemCount()) {
                    this.itemClicked(var2, var3, param0, param1, this.width());
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
            this.setSelected(param0);
            if (param0 != -1) {
                RealmsServer var0;
                if (RealmsMainScreen.this.shouldShowMessageInList()) {
                    if (param0 == 0) {
                        Realms.narrateNow(
                            RealmsScreen.getLocalizedString("mco.trial.message.line1"), RealmsScreen.getLocalizedString("mco.trial.message.line2")
                        );
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
                    Realms.narrateNow(RealmsScreen.getLocalizedString("mco.selectServer.uninitialized") + RealmsScreen.getLocalizedString("mco.gui.button"));
                    RealmsMainScreen.this.selectedServerId = -1L;
                } else {
                    RealmsMainScreen.this.selectedServerId = var0.id;
                    if (RealmsMainScreen.this.clicks >= 10 && RealmsMainScreen.this.playButton.active()) {
                        RealmsMainScreen.this.play(RealmsMainScreen.this.findServer(RealmsMainScreen.this.selectedServerId), RealmsMainScreen.this);
                    }

                    Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", var0.name));
                }
            }
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
                        Realms.setScreen(new RealmsCreateRealmScreen(var0, RealmsMainScreen.this));
                    } else {
                        RealmsMainScreen.this.selectedServerId = var0.id;
                    }

                    if (RealmsMainScreen.this.toolTip != null
                        && RealmsMainScreen.this.toolTip.equals(RealmsScreen.getLocalizedString("mco.selectServer.configure"))) {
                        RealmsMainScreen.this.selectedServerId = var0.id;
                        RealmsMainScreen.this.configureClicked(var0);
                    } else if (RealmsMainScreen.this.toolTip != null
                        && RealmsMainScreen.this.toolTip.equals(RealmsScreen.getLocalizedString("mco.selectServer.leave"))) {
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
    class RealmSelectionListEntry extends RealmListEntry {
        final RealmsServer mServerData;

        public RealmSelectionListEntry(RealmsServer param0) {
            this.mServerData = param0;
        }

        @Override
        public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
            this.renderMcoServerItem(this.mServerData, param2, param1, param5, param6);
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (this.mServerData.state == RealmsServer.State.UNINITIALIZED) {
                RealmsMainScreen.this.selectedServerId = -1L;
                Realms.setScreen(new RealmsCreateRealmScreen(this.mServerData, RealmsMainScreen.this));
            } else {
                RealmsMainScreen.this.selectedServerId = this.mServerData.id;
            }

            return true;
        }

        private void renderMcoServerItem(RealmsServer param0, int param1, int param2, int param3, int param4) {
            this.renderLegacy(param0, param1 + 36, param2, param3, param4);
        }

        private void renderLegacy(RealmsServer param0, int param1, int param2, int param3, int param4) {
            if (param0.state == RealmsServer.State.UNINITIALIZED) {
                RealmsScreen.bind("realms:textures/gui/realms/world_icon.png");
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableAlphaTest();
                RenderSystem.pushMatrix();
                RealmsScreen.blit(param1 + 10, param2 + 6, 0.0F, 0.0F, 40, 20, 40, 20);
                RenderSystem.popMatrix();
                float var0 = 0.5F + (1.0F + RealmsMth.sin((float)RealmsMainScreen.this.animTick * 0.25F)) * 0.25F;
                int var1 = 0xFF000000 | (int)(127.0F * var0) << 16 | (int)(255.0F * var0) << 8 | (int)(127.0F * var0);
                RealmsMainScreen.this.drawCenteredString(
                    RealmsScreen.getLocalizedString("mco.selectServer.uninitialized"), param1 + 10 + 40 + 75, param2 + 12, var1
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
                    RealmsMainScreen.this.drawString(var4, param1 + 207 - RealmsMainScreen.this.fontWidth(var4), param2 + 3, 8421504);
                    if (param3 >= param1 + 207 - RealmsMainScreen.this.fontWidth(var4)
                        && param3 <= param1 + 207
                        && param4 >= param2 + 1
                        && param4 <= param2 + 10
                        && param4 < RealmsMainScreen.this.height() - 40
                        && param4 > 32
                        && !RealmsMainScreen.this.shouldShowPopup()) {
                        RealmsMainScreen.this.toolTip = param0.serverPing.playerList;
                    }
                }

                if (RealmsMainScreen.this.isSelfOwnedServer(param0) && param0.expired) {
                    boolean var5 = false;
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.enableBlend();
                    RealmsScreen.bind("minecraft:textures/gui/widgets.png");
                    RenderSystem.pushMatrix();
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    String var6 = RealmsScreen.getLocalizedString("mco.selectServer.expiredList");
                    String var7 = RealmsScreen.getLocalizedString("mco.selectServer.expiredRenew");
                    if (param0.expiredTrial) {
                        var6 = RealmsScreen.getLocalizedString("mco.selectServer.expiredTrial");
                        var7 = RealmsScreen.getLocalizedString("mco.selectServer.expiredSubscribe");
                    }

                    int var8 = RealmsMainScreen.this.fontWidth(var7) + 17;
                    int var9 = 16;
                    int var10 = param1 + RealmsMainScreen.this.fontWidth(var6) + 8;
                    int var11 = param2 + 13;
                    if (param3 >= var10
                        && param3 < var10 + var8
                        && param4 > var11
                        && param4 <= var11 + 16 & param4 < RealmsMainScreen.this.height() - 40
                        && param4 > 32
                        && !RealmsMainScreen.this.shouldShowPopup()) {
                        var5 = true;
                        RealmsMainScreen.this.expiredHover = true;
                    }

                    int var12 = var5 ? 2 : 1;
                    RealmsScreen.blit(var10, var11, 0.0F, (float)(46 + var12 * 20), var8 / 2, 8, 256, 256);
                    RealmsScreen.blit(var10 + var8 / 2, var11, (float)(200 - var8 / 2), (float)(46 + var12 * 20), var8 / 2, 8, 256, 256);
                    RealmsScreen.blit(var10, var11 + 8, 0.0F, (float)(46 + var12 * 20 + 12), var8 / 2, 8, 256, 256);
                    RealmsScreen.blit(var10 + var8 / 2, var11 + 8, (float)(200 - var8 / 2), (float)(46 + var12 * 20 + 12), var8 / 2, 8, 256, 256);
                    RenderSystem.popMatrix();
                    RenderSystem.disableBlend();
                    int var13 = param2 + 11 + 5;
                    int var14 = var5 ? 16777120 : 16777215;
                    RealmsMainScreen.this.drawString(var6, param1 + 2, var13 + 1, 15553363);
                    RealmsMainScreen.this.drawCenteredString(var7, var10 + var8 / 2, var13 + 1, var14);
                } else {
                    if (param0.worldType.equals(RealmsServer.WorldType.MINIGAME)) {
                        int var15 = 13413468;
                        String var16 = RealmsScreen.getLocalizedString("mco.selectServer.minigame") + " ";
                        int var17 = RealmsMainScreen.this.fontWidth(var16);
                        RealmsMainScreen.this.drawString(var16, param1 + 2, param2 + 12, 13413468);
                        RealmsMainScreen.this.drawString(param0.getMinigameName(), param1 + 2 + var17, param2 + 12, 8421504);
                    } else {
                        RealmsMainScreen.this.drawString(param0.getDescription(), param1 + 2, param2 + 12, 8421504);
                    }

                    if (!RealmsMainScreen.this.isSelfOwnedServer(param0)) {
                        RealmsMainScreen.this.drawString(param0.owner, param1 + 2, param2 + 12 + 11, 8421504);
                    }
                }

                RealmsMainScreen.this.drawString(param0.getName(), param1 + 2, param2 + 1, 16777215);
                RealmsTextureManager.withBoundFace(param0.ownerUUID, () -> {
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    RealmsScreen.blit(param1 - 36, param2, 8.0F, 8.0F, 8, 8, 32, 32, 64, 64);
                    RealmsScreen.blit(param1 - 36, param2, 40.0F, 8.0F, 8, 8, 32, 32, 64, 64);
                });
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class RealmSelectionListTrialEntry extends RealmListEntry {
        public RealmSelectionListTrialEntry() {
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
            String var2 = RealmsScreen.getLocalizedString("mco.trial.message.line1") + "\\n" + RealmsScreen.getLocalizedString("mco.trial.message.line2");
            boolean var3 = false;
            if (param1 <= param3 && param3 <= RealmsMainScreen.this.realmSelectionList.getScroll() && param2 <= param4 && param4 <= param2 + 32) {
                var3 = true;
            }

            int var4 = 8388479;
            if (var3 && !RealmsMainScreen.this.shouldShowPopup()) {
                var4 = 6077788;
            }

            for(String var5 : var2.split("\\\\n")) {
                RealmsMainScreen.this.drawCenteredString(var5, RealmsMainScreen.this.width() / 2, var0 + var1, var4);
                var1 += 10;
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    class ShowPopupButton extends RealmsButton {
        public ShowPopupButton() {
            super(10, RealmsMainScreen.this.width() - 37, 6, 20, 20, RealmsScreen.getLocalizedString("mco.selectServer.info"));
        }

        @Override
        public void tick() {
            super.tick();
        }

        @Override
        public void render(int param0, int param1, float param2) {
            super.render(param0, param1, param2);
        }

        @Override
        public void renderButton(int param0, int param1, float param2) {
            RealmsMainScreen.this.renderMoreInfo(param0, param1, this.x(), this.y(), this.getProxy().isHovered());
        }

        @Override
        public void onPress() {
            RealmsMainScreen.this.popupOpenedByUser = !RealmsMainScreen.this.popupOpenedByUser;
        }
    }
}
