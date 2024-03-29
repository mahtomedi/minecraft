package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;

public abstract class DistanceManager {
    static final Logger LOGGER = LogUtils.getLogger();
    static final int PLAYER_TICKET_LEVEL = ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING);
    private static final int INITIAL_TICKET_LIST_CAPACITY = 4;
    final Long2ObjectMap<ObjectSet<ServerPlayer>> playersPerChunk = new Long2ObjectOpenHashMap<>();
    final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> tickets = new Long2ObjectOpenHashMap<>();
    private final DistanceManager.ChunkTicketTracker ticketTracker = new DistanceManager.ChunkTicketTracker();
    private final DistanceManager.FixedPlayerDistanceChunkTracker naturalSpawnChunkCounter = new DistanceManager.FixedPlayerDistanceChunkTracker(8);
    private final TickingTracker tickingTicketsTracker = new TickingTracker();
    private final DistanceManager.PlayerTicketTracker playerTicketManager = new DistanceManager.PlayerTicketTracker(32);
    final Set<ChunkHolder> chunksToUpdateFutures = Sets.newHashSet();
    final ChunkTaskPriorityQueueSorter ticketThrottler;
    final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> ticketThrottlerInput;
    final ProcessorHandle<ChunkTaskPriorityQueueSorter.Release> ticketThrottlerReleaser;
    final LongSet ticketsToRelease = new LongOpenHashSet();
    final Executor mainThreadExecutor;
    private long ticketTickCounter;
    private int simulationDistance = 10;

    protected DistanceManager(Executor param0, Executor param1) {
        ProcessorHandle<Runnable> var0 = ProcessorHandle.of("player ticket throttler", param1::execute);
        ChunkTaskPriorityQueueSorter var1 = new ChunkTaskPriorityQueueSorter(ImmutableList.of(var0), param0, 4);
        this.ticketThrottler = var1;
        this.ticketThrottlerInput = var1.getProcessor(var0, true);
        this.ticketThrottlerReleaser = var1.getReleaseProcessor(var0);
        this.mainThreadExecutor = param1;
    }

    protected void purgeStaleTickets() {
        ++this.ticketTickCounter;
        ObjectIterator<Entry<SortedArraySet<Ticket<?>>>> var0 = this.tickets.long2ObjectEntrySet().fastIterator();

        while(var0.hasNext()) {
            Entry<SortedArraySet<Ticket<?>>> var1 = var0.next();
            Iterator<Ticket<?>> var2 = var1.getValue().iterator();
            boolean var3 = false;

            while(var2.hasNext()) {
                Ticket<?> var4 = var2.next();
                if (var4.timedOut(this.ticketTickCounter)) {
                    var2.remove();
                    var3 = true;
                    this.tickingTicketsTracker.removeTicket(var1.getLongKey(), var4);
                }
            }

            if (var3) {
                this.ticketTracker.update(var1.getLongKey(), getTicketLevelAt(var1.getValue()), false);
            }

            if (var1.getValue().isEmpty()) {
                var0.remove();
            }
        }

    }

    private static int getTicketLevelAt(SortedArraySet<Ticket<?>> param0) {
        return !param0.isEmpty() ? param0.first().getTicketLevel() : ChunkLevel.MAX_LEVEL + 1;
    }

    protected abstract boolean isChunkToRemove(long var1);

    @Nullable
    protected abstract ChunkHolder getChunk(long var1);

    @Nullable
    protected abstract ChunkHolder updateChunkScheduling(long var1, int var3, @Nullable ChunkHolder var4, int var5);

    public boolean runAllUpdates(ChunkMap param0) {
        this.naturalSpawnChunkCounter.runAllUpdates();
        this.tickingTicketsTracker.runAllUpdates();
        this.playerTicketManager.runAllUpdates();
        int var0 = Integer.MAX_VALUE - this.ticketTracker.runDistanceUpdates(Integer.MAX_VALUE);
        boolean var1 = var0 != 0;
        if (var1) {
        }

        if (!this.chunksToUpdateFutures.isEmpty()) {
            this.chunksToUpdateFutures.forEach(param1 -> param1.updateFutures(param0, this.mainThreadExecutor));
            this.chunksToUpdateFutures.clear();
            return true;
        } else {
            if (!this.ticketsToRelease.isEmpty()) {
                LongIterator var2 = this.ticketsToRelease.iterator();

                while(var2.hasNext()) {
                    long var3 = var2.nextLong();
                    if (this.getTickets(var3).stream().anyMatch(param0x -> param0x.getType() == TicketType.PLAYER)) {
                        ChunkHolder var4 = param0.getUpdatingChunkIfPresent(var3);
                        if (var4 == null) {
                            throw new IllegalStateException();
                        }

                        CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> var5 = var4.getEntityTickingChunkFuture();
                        var5.thenAccept(
                            param1 -> this.mainThreadExecutor.execute(() -> this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> {
                                    }, var3, false)))
                        );
                    }
                }

                this.ticketsToRelease.clear();
            }

            return var1;
        }
    }

    void addTicket(long param0, Ticket<?> param1) {
        SortedArraySet<Ticket<?>> var0 = this.getTickets(param0);
        int var1 = getTicketLevelAt(var0);
        Ticket<?> var2 = var0.addOrGet(param1);
        var2.setCreatedTick(this.ticketTickCounter);
        if (param1.getTicketLevel() < var1) {
            this.ticketTracker.update(param0, param1.getTicketLevel(), true);
        }

    }

    void removeTicket(long param0, Ticket<?> param1) {
        SortedArraySet<Ticket<?>> var0 = this.getTickets(param0);
        if (var0.remove(param1)) {
        }

        if (var0.isEmpty()) {
            this.tickets.remove(param0);
        }

        this.ticketTracker.update(param0, getTicketLevelAt(var0), false);
    }

    public <T> void addTicket(TicketType<T> param0, ChunkPos param1, int param2, T param3) {
        this.addTicket(param1.toLong(), new Ticket<>(param0, param2, param3));
    }

    public <T> void removeTicket(TicketType<T> param0, ChunkPos param1, int param2, T param3) {
        Ticket<T> var0 = new Ticket<>(param0, param2, param3);
        this.removeTicket(param1.toLong(), var0);
    }

    public <T> void addRegionTicket(TicketType<T> param0, ChunkPos param1, int param2, T param3) {
        Ticket<T> var0 = new Ticket<>(param0, ChunkLevel.byStatus(FullChunkStatus.FULL) - param2, param3);
        long var1 = param1.toLong();
        this.addTicket(var1, var0);
        this.tickingTicketsTracker.addTicket(var1, var0);
    }

    public <T> void removeRegionTicket(TicketType<T> param0, ChunkPos param1, int param2, T param3) {
        Ticket<T> var0 = new Ticket<>(param0, ChunkLevel.byStatus(FullChunkStatus.FULL) - param2, param3);
        long var1 = param1.toLong();
        this.removeTicket(var1, var0);
        this.tickingTicketsTracker.removeTicket(var1, var0);
    }

    private SortedArraySet<Ticket<?>> getTickets(long param0) {
        return this.tickets.computeIfAbsent(param0, param0x -> SortedArraySet.create(4));
    }

    protected void updateChunkForced(ChunkPos param0, boolean param1) {
        Ticket<ChunkPos> var0 = new Ticket<>(TicketType.FORCED, ChunkMap.FORCED_TICKET_LEVEL, param0);
        long var1 = param0.toLong();
        if (param1) {
            this.addTicket(var1, var0);
            this.tickingTicketsTracker.addTicket(var1, var0);
        } else {
            this.removeTicket(var1, var0);
            this.tickingTicketsTracker.removeTicket(var1, var0);
        }

    }

    public void addPlayer(SectionPos param0, ServerPlayer param1) {
        ChunkPos var0 = param0.chunk();
        long var1 = var0.toLong();
        this.playersPerChunk.computeIfAbsent(var1, param0x -> new ObjectOpenHashSet()).add(param1);
        this.naturalSpawnChunkCounter.update(var1, 0, true);
        this.playerTicketManager.update(var1, 0, true);
        this.tickingTicketsTracker.addTicket(TicketType.PLAYER, var0, this.getPlayerTicketLevel(), var0);
    }

    public void removePlayer(SectionPos param0, ServerPlayer param1) {
        ChunkPos var0 = param0.chunk();
        long var1 = var0.toLong();
        ObjectSet<ServerPlayer> var2 = this.playersPerChunk.get(var1);
        var2.remove(param1);
        if (var2.isEmpty()) {
            this.playersPerChunk.remove(var1);
            this.naturalSpawnChunkCounter.update(var1, Integer.MAX_VALUE, false);
            this.playerTicketManager.update(var1, Integer.MAX_VALUE, false);
            this.tickingTicketsTracker.removeTicket(TicketType.PLAYER, var0, this.getPlayerTicketLevel(), var0);
        }

    }

    private int getPlayerTicketLevel() {
        return Math.max(0, ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING) - this.simulationDistance);
    }

    public boolean inEntityTickingRange(long param0) {
        return ChunkLevel.isEntityTicking(this.tickingTicketsTracker.getLevel(param0));
    }

    public boolean inBlockTickingRange(long param0) {
        return ChunkLevel.isBlockTicking(this.tickingTicketsTracker.getLevel(param0));
    }

    protected String getTicketDebugString(long param0) {
        SortedArraySet<Ticket<?>> var0 = this.tickets.get(param0);
        return var0 != null && !var0.isEmpty() ? var0.first().toString() : "no_ticket";
    }

    protected void updatePlayerTickets(int param0) {
        this.playerTicketManager.updateViewDistance(param0);
    }

    public void updateSimulationDistance(int param0) {
        if (param0 != this.simulationDistance) {
            this.simulationDistance = param0;
            this.tickingTicketsTracker.replacePlayerTicketsLevel(this.getPlayerTicketLevel());
        }

    }

    public int getNaturalSpawnChunkCount() {
        this.naturalSpawnChunkCounter.runAllUpdates();
        return this.naturalSpawnChunkCounter.chunks.size();
    }

    public boolean hasPlayersNearby(long param0) {
        this.naturalSpawnChunkCounter.runAllUpdates();
        return this.naturalSpawnChunkCounter.chunks.containsKey(param0);
    }

    public String getDebugStatus() {
        return this.ticketThrottler.getDebugStatus();
    }

    private void dumpTickets(String param0) {
        try (FileOutputStream var0 = new FileOutputStream(new File(param0))) {
            for(Entry<SortedArraySet<Ticket<?>>> var1 : this.tickets.long2ObjectEntrySet()) {
                ChunkPos var2 = new ChunkPos(var1.getLongKey());

                for(Ticket<?> var3 : var1.getValue()) {
                    var0.write((var2.x + "\t" + var2.z + "\t" + var3.getType() + "\t" + var3.getTicketLevel() + "\t\n").getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException var10) {
            LOGGER.error("Failed to dump tickets to {}", param0, var10);
        }

    }

    @VisibleForTesting
    TickingTracker tickingTracker() {
        return this.tickingTicketsTracker;
    }

    public void removeTicketsOnClosing() {
        ImmutableSet<TicketType<?>> var0 = ImmutableSet.of(TicketType.UNKNOWN, TicketType.POST_TELEPORT, TicketType.LIGHT);
        ObjectIterator<Entry<SortedArraySet<Ticket<?>>>> var1 = this.tickets.long2ObjectEntrySet().fastIterator();

        while(var1.hasNext()) {
            Entry<SortedArraySet<Ticket<?>>> var2 = var1.next();
            Iterator<Ticket<?>> var3 = var2.getValue().iterator();
            boolean var4 = false;

            while(var3.hasNext()) {
                Ticket<?> var5 = var3.next();
                if (!var0.contains(var5.getType())) {
                    var3.remove();
                    var4 = true;
                    this.tickingTicketsTracker.removeTicket(var2.getLongKey(), var5);
                }
            }

            if (var4) {
                this.ticketTracker.update(var2.getLongKey(), getTicketLevelAt(var2.getValue()), false);
            }

            if (var2.getValue().isEmpty()) {
                var1.remove();
            }
        }

    }

    public boolean hasTickets() {
        return !this.tickets.isEmpty();
    }

    class ChunkTicketTracker extends ChunkTracker {
        private static final int MAX_LEVEL = ChunkLevel.MAX_LEVEL + 1;

        public ChunkTicketTracker() {
            super(MAX_LEVEL + 1, 16, 256);
        }

        @Override
        protected int getLevelFromSource(long param0) {
            SortedArraySet<Ticket<?>> var0 = DistanceManager.this.tickets.get(param0);
            if (var0 == null) {
                return Integer.MAX_VALUE;
            } else {
                return var0.isEmpty() ? Integer.MAX_VALUE : var0.first().getTicketLevel();
            }
        }

        @Override
        protected int getLevel(long param0) {
            if (!DistanceManager.this.isChunkToRemove(param0)) {
                ChunkHolder var0 = DistanceManager.this.getChunk(param0);
                if (var0 != null) {
                    return var0.getTicketLevel();
                }
            }

            return MAX_LEVEL;
        }

        @Override
        protected void setLevel(long param0, int param1) {
            ChunkHolder var0 = DistanceManager.this.getChunk(param0);
            int var1 = var0 == null ? MAX_LEVEL : var0.getTicketLevel();
            if (var1 != param1) {
                var0 = DistanceManager.this.updateChunkScheduling(param0, param1, var0, var1);
                if (var0 != null) {
                    DistanceManager.this.chunksToUpdateFutures.add(var0);
                }

            }
        }

        public int runDistanceUpdates(int param0) {
            return this.runUpdates(param0);
        }
    }

    class FixedPlayerDistanceChunkTracker extends ChunkTracker {
        protected final Long2ByteMap chunks = new Long2ByteOpenHashMap();
        protected final int maxDistance;

        protected FixedPlayerDistanceChunkTracker(int param0) {
            super(param0 + 2, 16, 256);
            this.maxDistance = param0;
            this.chunks.defaultReturnValue((byte)(param0 + 2));
        }

        @Override
        protected int getLevel(long param0) {
            return this.chunks.get(param0);
        }

        @Override
        protected void setLevel(long param0, int param1) {
            byte var0;
            if (param1 > this.maxDistance) {
                var0 = this.chunks.remove(param0);
            } else {
                var0 = this.chunks.put(param0, (byte)param1);
            }

            this.onLevelChange(param0, var0, param1);
        }

        protected void onLevelChange(long param0, int param1, int param2) {
        }

        @Override
        protected int getLevelFromSource(long param0) {
            return this.havePlayer(param0) ? 0 : Integer.MAX_VALUE;
        }

        private boolean havePlayer(long param0) {
            ObjectSet<ServerPlayer> var0 = DistanceManager.this.playersPerChunk.get(param0);
            return var0 != null && !var0.isEmpty();
        }

        public void runAllUpdates() {
            this.runUpdates(Integer.MAX_VALUE);
        }

        private void dumpChunks(String param0) {
            try (FileOutputStream var0 = new FileOutputStream(new File(param0))) {
                for(it.unimi.dsi.fastutil.longs.Long2ByteMap.Entry var1 : this.chunks.long2ByteEntrySet()) {
                    ChunkPos var2 = new ChunkPos(var1.getLongKey());
                    String var3 = Byte.toString(var1.getByteValue());
                    var0.write((var2.x + "\t" + var2.z + "\t" + var3 + "\n").getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException var9) {
                DistanceManager.LOGGER.error("Failed to dump chunks to {}", param0, var9);
            }

        }
    }

    class PlayerTicketTracker extends DistanceManager.FixedPlayerDistanceChunkTracker {
        private int viewDistance;
        private final Long2IntMap queueLevels = Long2IntMaps.synchronize(new Long2IntOpenHashMap());
        private final LongSet toUpdate = new LongOpenHashSet();

        protected PlayerTicketTracker(int param0) {
            super(param0);
            this.viewDistance = 0;
            this.queueLevels.defaultReturnValue(param0 + 2);
        }

        @Override
        protected void onLevelChange(long param0, int param1, int param2) {
            this.toUpdate.add(param0);
        }

        public void updateViewDistance(int param0) {
            for(it.unimi.dsi.fastutil.longs.Long2ByteMap.Entry var0 : this.chunks.long2ByteEntrySet()) {
                byte var1 = var0.getByteValue();
                long var2 = var0.getLongKey();
                this.onLevelChange(var2, var1, this.haveTicketFor(var1), var1 <= param0);
            }

            this.viewDistance = param0;
        }

        private void onLevelChange(long param0, int param1, boolean param2, boolean param3) {
            if (param2 != param3) {
                Ticket<?> var0 = new Ticket<>(TicketType.PLAYER, DistanceManager.PLAYER_TICKET_LEVEL, new ChunkPos(param0));
                if (param3) {
                    DistanceManager.this.ticketThrottlerInput
                        .tell(ChunkTaskPriorityQueueSorter.message(() -> DistanceManager.this.mainThreadExecutor.execute(() -> {
                                if (this.haveTicketFor(this.getLevel(param0))) {
                                    DistanceManager.this.addTicket(param0, var0);
                                    DistanceManager.this.ticketsToRelease.add(param0);
                                } else {
                                    DistanceManager.this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> {
                                    }, param0, false));
                                }
    
                            }), param0, () -> param1));
                } else {
                    DistanceManager.this.ticketThrottlerReleaser
                        .tell(
                            ChunkTaskPriorityQueueSorter.release(
                                () -> DistanceManager.this.mainThreadExecutor.execute(() -> DistanceManager.this.removeTicket(param0, var0)), param0, true
                            )
                        );
                }
            }

        }

        @Override
        public void runAllUpdates() {
            super.runAllUpdates();
            if (!this.toUpdate.isEmpty()) {
                LongIterator var0 = this.toUpdate.iterator();

                while(var0.hasNext()) {
                    long var1 = var0.nextLong();
                    int var2 = this.queueLevels.get(var1);
                    int var3 = this.getLevel(var1);
                    if (var2 != var3) {
                        DistanceManager.this.ticketThrottler.onLevelChange(new ChunkPos(var1), () -> this.queueLevels.get(var1), var3, param1 -> {
                            if (param1 >= this.queueLevels.defaultReturnValue()) {
                                this.queueLevels.remove(var1);
                            } else {
                                this.queueLevels.put(var1, param1);
                            }

                        });
                        this.onLevelChange(var1, var3, this.haveTicketFor(var2), this.haveTicketFor(var3));
                    }
                }

                this.toUpdate.clear();
            }

        }

        private boolean haveTicketFor(int param0) {
            return param0 <= this.viewDistance;
        }
    }
}
