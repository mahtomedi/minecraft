package com.mojang.realmsclient.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.util.RealmsPersistence;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.realms.Realms;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsDataFetcher {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private volatile boolean stopped = true;
    private final RealmsDataFetcher.ServerListUpdateTask serverListUpdateTask = new RealmsDataFetcher.ServerListUpdateTask();
    private final RealmsDataFetcher.PendingInviteUpdateTask pendingInviteUpdateTask = new RealmsDataFetcher.PendingInviteUpdateTask();
    private final RealmsDataFetcher.TrialAvailabilityTask trialAvailabilityTask = new RealmsDataFetcher.TrialAvailabilityTask();
    private final RealmsDataFetcher.LiveStatsTask liveStatsTask = new RealmsDataFetcher.LiveStatsTask();
    private final RealmsDataFetcher.UnreadNewsTask unreadNewsTask = new RealmsDataFetcher.UnreadNewsTask();
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

    public synchronized void initWithSpecificTaskList(List<RealmsDataFetcher.Task> param0) {
        if (this.stopped) {
            this.stopped = false;
            this.cancelTasks();

            for(RealmsDataFetcher.Task var0 : param0) {
                this.fetchStatus.put(var0, false);
                switch(var0) {
                    case SERVER_LIST:
                        this.serverListScheduledFuture = this.scheduler.scheduleAtFixedRate(this.serverListUpdateTask, 0L, 60L, TimeUnit.SECONDS);
                        break;
                    case PENDING_INVITE:
                        this.pendingInviteScheduledFuture = this.scheduler.scheduleAtFixedRate(this.pendingInviteUpdateTask, 0L, 10L, TimeUnit.SECONDS);
                        break;
                    case TRIAL_AVAILABLE:
                        this.trialAvailableScheduledFuture = this.scheduler.scheduleAtFixedRate(this.trialAvailabilityTask, 0L, 60L, TimeUnit.SECONDS);
                        break;
                    case LIVE_STATS:
                        this.liveStatsScheduledFuture = this.scheduler.scheduleAtFixedRate(this.liveStatsTask, 0L, 10L, TimeUnit.SECONDS);
                        break;
                    case UNREAD_NEWS:
                        this.unreadNewsScheduledFuture = this.scheduler.scheduleAtFixedRate(this.unreadNewsTask, 0L, 300L, TimeUnit.SECONDS);
                }
            }
        }

    }

    public boolean isFetchedSinceLastTry(RealmsDataFetcher.Task param0) {
        Boolean var0 = this.fetchStatus.get(param0);
        return var0 == null ? false : var0;
    }

    public void markClean() {
        for(RealmsDataFetcher.Task var0 : this.fetchStatus.keySet()) {
            this.fetchStatus.put(var0, false);
        }

    }

    public synchronized void forceUpdate() {
        this.stop();
        this.init();
    }

    public synchronized List<RealmsServer> getServers() {
        return Lists.newArrayList(this.servers);
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

        this.serverListScheduledFuture = this.scheduler.scheduleAtFixedRate(this.serverListUpdateTask, 0L, 60L, TimeUnit.SECONDS);
        this.pendingInviteScheduledFuture = this.scheduler.scheduleAtFixedRate(this.pendingInviteUpdateTask, 0L, 10L, TimeUnit.SECONDS);
        this.trialAvailableScheduledFuture = this.scheduler.scheduleAtFixedRate(this.trialAvailabilityTask, 0L, 60L, TimeUnit.SECONDS);
        this.liveStatsScheduledFuture = this.scheduler.scheduleAtFixedRate(this.liveStatsTask, 0L, 10L, TimeUnit.SECONDS);
        this.unreadNewsScheduledFuture = this.scheduler.scheduleAtFixedRate(this.unreadNewsTask, 0L, 300L, TimeUnit.SECONDS);
    }

    private void cancelTasks() {
        try {
            if (this.serverListScheduledFuture != null) {
                this.serverListScheduledFuture.cancel(false);
            }

            if (this.pendingInviteScheduledFuture != null) {
                this.pendingInviteScheduledFuture.cancel(false);
            }

            if (this.trialAvailableScheduledFuture != null) {
                this.trialAvailableScheduledFuture.cancel(false);
            }

            if (this.liveStatsScheduledFuture != null) {
                this.liveStatsScheduledFuture.cancel(false);
            }

            if (this.unreadNewsScheduledFuture != null) {
                this.unreadNewsScheduledFuture.cancel(false);
            }
        } catch (Exception var2) {
            LOGGER.error("Failed to cancel Realms tasks", (Throwable)var2);
        }

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

    private void sort(List<RealmsServer> param0) {
        param0.sort(new RealmsServer.McoServerComparator(Realms.getName()));
    }

    private boolean isActive() {
        return !this.stopped;
    }

    @OnlyIn(Dist.CLIENT)
    class LiveStatsTask implements Runnable {
        private LiveStatsTask() {
        }

        @Override
        public void run() {
            if (RealmsDataFetcher.this.isActive()) {
                this.getLiveStats();
            }

        }

        private void getLiveStats() {
            try {
                RealmsClient var0 = RealmsClient.createRealmsClient();
                if (var0 != null) {
                    RealmsDataFetcher.this.livestats = var0.getLiveStats();
                    RealmsDataFetcher.this.fetchStatus.put(RealmsDataFetcher.Task.LIVE_STATS, true);
                }
            } catch (Exception var2) {
                RealmsDataFetcher.LOGGER.error("Couldn't get live stats", (Throwable)var2);
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    class PendingInviteUpdateTask implements Runnable {
        private PendingInviteUpdateTask() {
        }

        @Override
        public void run() {
            if (RealmsDataFetcher.this.isActive()) {
                this.updatePendingInvites();
            }

        }

        private void updatePendingInvites() {
            try {
                RealmsClient var0 = RealmsClient.createRealmsClient();
                if (var0 != null) {
                    RealmsDataFetcher.this.pendingInvitesCount = var0.pendingInvitesCount();
                    RealmsDataFetcher.this.fetchStatus.put(RealmsDataFetcher.Task.PENDING_INVITE, true);
                }
            } catch (Exception var2) {
                RealmsDataFetcher.LOGGER.error("Couldn't get pending invite count", (Throwable)var2);
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    class ServerListUpdateTask implements Runnable {
        private ServerListUpdateTask() {
        }

        @Override
        public void run() {
            if (RealmsDataFetcher.this.isActive()) {
                this.updateServersList();
            }

        }

        private void updateServersList() {
            try {
                RealmsClient var0 = RealmsClient.createRealmsClient();
                if (var0 != null) {
                    List<RealmsServer> var1 = var0.listWorlds().servers;
                    if (var1 != null) {
                        RealmsDataFetcher.this.sort(var1);
                        RealmsDataFetcher.this.setServers(var1);
                        RealmsDataFetcher.this.fetchStatus.put(RealmsDataFetcher.Task.SERVER_LIST, true);
                    } else {
                        RealmsDataFetcher.LOGGER.warn("Realms server list was null or empty");
                    }
                }
            } catch (Exception var3) {
                RealmsDataFetcher.this.fetchStatus.put(RealmsDataFetcher.Task.SERVER_LIST, true);
                RealmsDataFetcher.LOGGER.error("Couldn't get server list", (Throwable)var3);
            }

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

    @OnlyIn(Dist.CLIENT)
    class TrialAvailabilityTask implements Runnable {
        private TrialAvailabilityTask() {
        }

        @Override
        public void run() {
            if (RealmsDataFetcher.this.isActive()) {
                this.getTrialAvailable();
            }

        }

        private void getTrialAvailable() {
            try {
                RealmsClient var0 = RealmsClient.createRealmsClient();
                if (var0 != null) {
                    RealmsDataFetcher.this.trialAvailable = var0.trialAvailable();
                    RealmsDataFetcher.this.fetchStatus.put(RealmsDataFetcher.Task.TRIAL_AVAILABLE, true);
                }
            } catch (Exception var2) {
                RealmsDataFetcher.LOGGER.error("Couldn't get trial availability", (Throwable)var2);
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    class UnreadNewsTask implements Runnable {
        private UnreadNewsTask() {
        }

        @Override
        public void run() {
            if (RealmsDataFetcher.this.isActive()) {
                this.getUnreadNews();
            }

        }

        private void getUnreadNews() {
            try {
                RealmsClient var0 = RealmsClient.createRealmsClient();
                if (var0 != null) {
                    RealmsNews var1 = null;

                    try {
                        var1 = var0.getNews();
                    } catch (Exception var5) {
                    }

                    RealmsPersistence.RealmsPersistenceData var2 = RealmsPersistence.readFile();
                    if (var1 != null) {
                        String var3 = var1.newsLink;
                        if (var3 != null && !var3.equals(var2.newsLink)) {
                            var2.hasUnreadNews = true;
                            var2.newsLink = var3;
                            RealmsPersistence.writeFile(var2);
                        }
                    }

                    RealmsDataFetcher.this.hasUnreadNews = var2.hasUnreadNews;
                    RealmsDataFetcher.this.newsLink = var2.newsLink;
                    RealmsDataFetcher.this.fetchStatus.put(RealmsDataFetcher.Task.UNREAD_NEWS, true);
                }
            } catch (Exception var6) {
                RealmsDataFetcher.LOGGER.error("Couldn't get unread news", (Throwable)var6);
            }

        }
    }
}
