package com.mojang.realmsclient.gui;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.gui.task.RepeatableTask;
import com.mojang.realmsclient.util.RealmsPersistence;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsDataFetcher {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft minecraft;
    private final RealmsClient realmsClient;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private volatile boolean stopped = true;
    private final RepeatableTask serverListUpdateTask = RepeatableTask.withImmediateRestart(this::updateServersList, Duration.ofSeconds(60L), this::isActive);
    private final RepeatableTask liveStatsTask = RepeatableTask.withImmediateRestart(this::updateLiveStats, Duration.ofSeconds(10L), this::isActive);
    private final RepeatableTask pendingInviteUpdateTask = RepeatableTask.withRestartDelayAccountingForInterval(
        this::updatePendingInvites, Duration.ofSeconds(10L), this::isActive
    );
    private final RepeatableTask trialAvailabilityTask = RepeatableTask.withRestartDelayAccountingForInterval(
        this::updateTrialAvailable, Duration.ofSeconds(60L), this::isActive
    );
    private final RepeatableTask unreadNewsTask = RepeatableTask.withRestartDelayAccountingForInterval(
        this::updateUnreadNews, Duration.ofMinutes(5L), this::isActive
    );
    private final RealmsPersistence newsLocalStorage;
    private final Set<RealmsServer> removedServers = Sets.newHashSet();
    private List<RealmsServer> servers = Lists.newArrayList();
    private RealmsServerPlayerLists livestats;
    private int pendingInvitesCount;
    private boolean trialAvailable;
    private boolean hasUnreadNews;
    private String newsLink;
    private ScheduledFuture<?> serverListScheduledFuture;
    private ScheduledFuture<?> pendingInviteScheduledFuture;
    private ScheduledFuture<?> trialAvailableScheduledFuture;
    private ScheduledFuture<?> liveStatsScheduledFuture;
    private ScheduledFuture<?> unreadNewsScheduledFuture;
    private final Map<RealmsDataFetcher.Task, Boolean> fetchStatus = new ConcurrentHashMap<>(RealmsDataFetcher.Task.values().length);

    public RealmsDataFetcher(Minecraft param0, RealmsClient param1) {
        this.minecraft = param0;
        this.realmsClient = param1;
        this.newsLocalStorage = new RealmsPersistence();
    }

    @VisibleForTesting
    protected RealmsDataFetcher(Minecraft param0, RealmsClient param1, RealmsPersistence param2) {
        this.minecraft = param0;
        this.realmsClient = param1;
        this.newsLocalStorage = param2;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public synchronized void init() {
        if (this.stopped) {
            this.stopped = false;
            this.cancelTasks();
            this.scheduleTasks();
        }

    }

    public synchronized void initWithSpecificTaskList() {
        if (this.stopped) {
            this.stopped = false;
            this.cancelTasks();
            this.fetchStatus.put(RealmsDataFetcher.Task.PENDING_INVITE, false);
            this.pendingInviteScheduledFuture = this.pendingInviteUpdateTask.schedule(this.scheduler);
            this.fetchStatus.put(RealmsDataFetcher.Task.TRIAL_AVAILABLE, false);
            this.trialAvailableScheduledFuture = this.trialAvailabilityTask.schedule(this.scheduler);
            this.fetchStatus.put(RealmsDataFetcher.Task.UNREAD_NEWS, false);
            this.unreadNewsScheduledFuture = this.unreadNewsTask.schedule(this.scheduler);
        }

    }

    public boolean isFetchedSinceLastTry(RealmsDataFetcher.Task param0) {
        Boolean var0 = this.fetchStatus.get(param0);
        return var0 != null && var0;
    }

    public void markClean() {
        this.fetchStatus.replaceAll((param0, param1) -> false);
    }

    public synchronized void forceUpdate() {
        this.stop();
        this.init();
    }

    public synchronized List<RealmsServer> getServers() {
        return ImmutableList.copyOf(this.servers);
    }

    public synchronized int getPendingInvitesCount() {
        return this.pendingInvitesCount;
    }

    public synchronized boolean isTrialAvailable() {
        return this.trialAvailable;
    }

    public synchronized RealmsServerPlayerLists getLivestats() {
        return this.livestats;
    }

    public synchronized boolean hasUnreadNews() {
        return this.hasUnreadNews;
    }

    public synchronized String newsLink() {
        return this.newsLink;
    }

    public synchronized void stop() {
        this.stopped = true;
        this.cancelTasks();
    }

    private void scheduleTasks() {
        for(RealmsDataFetcher.Task var0 : RealmsDataFetcher.Task.values()) {
            this.fetchStatus.put(var0, false);
        }

        this.serverListScheduledFuture = this.serverListUpdateTask.schedule(this.scheduler);
        this.pendingInviteScheduledFuture = this.pendingInviteUpdateTask.schedule(this.scheduler);
        this.trialAvailableScheduledFuture = this.trialAvailabilityTask.schedule(this.scheduler);
        this.liveStatsScheduledFuture = this.liveStatsTask.schedule(this.scheduler);
        this.unreadNewsScheduledFuture = this.unreadNewsTask.schedule(this.scheduler);
    }

    private void cancelTasks() {
        Stream.of(
                this.serverListScheduledFuture,
                this.pendingInviteScheduledFuture,
                this.trialAvailableScheduledFuture,
                this.liveStatsScheduledFuture,
                this.unreadNewsScheduledFuture
            )
            .filter(Objects::nonNull)
            .forEach(param0 -> {
                try {
                    param0.cancel(false);
                } catch (Exception var2) {
                    LOGGER.error("Failed to cancel Realms task", (Throwable)var2);
                }
    
            });
    }

    private synchronized void setServers(List<RealmsServer> param0) {
        int var0 = 0;

        for(RealmsServer var1 : this.removedServers) {
            if (param0.remove(var1)) {
                ++var0;
            }
        }

        if (var0 == 0) {
            this.removedServers.clear();
        }

        this.servers = param0;
    }

    public synchronized void removeItem(RealmsServer param0) {
        this.servers.remove(param0);
        this.removedServers.add(param0);
    }

    private boolean isActive() {
        return !this.stopped;
    }

    private void updateServersList() {
        try {
            List<RealmsServer> var0 = this.realmsClient.listWorlds().servers;
            if (var0 != null) {
                var0.sort(new RealmsServer.McoServerComparator(this.minecraft.getUser().getName()));
                this.setServers(var0);
                this.fetchStatus.put(RealmsDataFetcher.Task.SERVER_LIST, true);
            } else {
                LOGGER.warn("Realms server list was null");
            }
        } catch (Exception var2) {
            this.fetchStatus.put(RealmsDataFetcher.Task.SERVER_LIST, true);
            LOGGER.error("Couldn't get server list", (Throwable)var2);
        }

    }

    private void updatePendingInvites() {
        try {
            this.pendingInvitesCount = this.realmsClient.pendingInvitesCount();
            this.fetchStatus.put(RealmsDataFetcher.Task.PENDING_INVITE, true);
        } catch (Exception var2) {
            LOGGER.error("Couldn't get pending invite count", (Throwable)var2);
        }

    }

    private void updateTrialAvailable() {
        try {
            this.trialAvailable = this.realmsClient.trialAvailable();
            this.fetchStatus.put(RealmsDataFetcher.Task.TRIAL_AVAILABLE, true);
        } catch (Exception var2) {
            LOGGER.error("Couldn't get trial availability", (Throwable)var2);
        }

    }

    private void updateLiveStats() {
        try {
            this.livestats = this.realmsClient.getLiveStats();
            this.fetchStatus.put(RealmsDataFetcher.Task.LIVE_STATS, true);
        } catch (Exception var2) {
            LOGGER.error("Couldn't get live stats", (Throwable)var2);
        }

    }

    private void updateUnreadNews() {
        try {
            RealmsPersistence.RealmsPersistenceData var0 = this.fetchAndUpdateNewsStorage();
            this.hasUnreadNews = var0.hasUnreadNews;
            this.newsLink = var0.newsLink;
            this.fetchStatus.put(RealmsDataFetcher.Task.UNREAD_NEWS, true);
        } catch (Exception var2) {
            LOGGER.error("Couldn't update unread news", (Throwable)var2);
        }

    }

    private RealmsPersistence.RealmsPersistenceData fetchAndUpdateNewsStorage() {
        RealmsPersistence.RealmsPersistenceData var1;
        try {
            RealmsNews var0 = this.realmsClient.getNews();
            var1 = new RealmsPersistence.RealmsPersistenceData();
            var1.newsLink = var0.newsLink;
        } catch (Exception var41) {
            LOGGER.warn("Failed fetching news from Realms, falling back to local cache", (Throwable)var41);
            return this.newsLocalStorage.read();
        }

        RealmsPersistence.RealmsPersistenceData var4 = this.newsLocalStorage.read();
        boolean var5 = var1.newsLink == null || var1.newsLink.equals(var4.newsLink);
        if (var5) {
            return var4;
        } else {
            var1.hasUnreadNews = true;
            this.newsLocalStorage.save(var1);
            return var1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Task {
        SERVER_LIST,
        PENDING_INVITE,
        TRIAL_AVAILABLE,
        LIVE_STATS,
        UNREAD_NEWS;
    }
}
