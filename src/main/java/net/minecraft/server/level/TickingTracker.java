package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;

public class TickingTracker extends ChunkTracker {
    public static final int MAX_LEVEL = 33;
    private static final int INITIAL_TICKET_LIST_CAPACITY = 4;
    protected final Long2ByteMap chunks = new Long2ByteOpenHashMap();
    private final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> tickets = new Long2ObjectOpenHashMap<>();

    public TickingTracker() {
        super(34, 16, 256);
        this.chunks.defaultReturnValue((byte)33);
    }

    private SortedArraySet<Ticket<?>> getTickets(long param0) {
        return this.tickets.computeIfAbsent(param0, param0x -> SortedArraySet.create(4));
    }

    private int getTicketLevelAt(SortedArraySet<Ticket<?>> param0) {
        return param0.isEmpty() ? 34 : param0.first().getTicketLevel();
    }

    public void addTicket(long param0, Ticket<?> param1) {
        SortedArraySet<Ticket<?>> var0 = this.getTickets(param0);
        int var1 = this.getTicketLevelAt(var0);
        var0.add(param1);
        if (param1.getTicketLevel() < var1) {
            this.update(param0, param1.getTicketLevel(), true);
        }

    }

    public void removeTicket(long param0, Ticket<?> param1) {
        SortedArraySet<Ticket<?>> var0 = this.getTickets(param0);
        var0.remove(param1);
        if (var0.isEmpty()) {
            this.tickets.remove(param0);
        }

        this.update(param0, this.getTicketLevelAt(var0), false);
    }

    public <T> void addTicket(TicketType<T> param0, ChunkPos param1, int param2, T param3) {
        this.addTicket(param1.toLong(), new Ticket<>(param0, param2, param3));
    }

    public <T> void removeTicket(TicketType<T> param0, ChunkPos param1, int param2, T param3) {
        Ticket<T> var0 = new Ticket<>(param0, param2, param3);
        this.removeTicket(param1.toLong(), var0);
    }

    public void replacePlayerTicketsLevel(int param0) {
        List<Pair<Ticket<ChunkPos>, Long>> var0 = new ArrayList<>();

        for(Entry<SortedArraySet<Ticket<?>>> var1 : this.tickets.long2ObjectEntrySet()) {
            for(Ticket<?> var2 : var1.getValue()) {
                if (var2.getType() == TicketType.PLAYER) {
                    var0.add(Pair.of(var2, var1.getLongKey()));
                }
            }
        }

        for(Pair<Ticket<ChunkPos>, Long> var3 : var0) {
            Long var4 = var3.getSecond();
            Ticket<ChunkPos> var5 = var3.getFirst();
            this.removeTicket(var4, var5);
            ChunkPos var6 = new ChunkPos(var4);
            TicketType<ChunkPos> var7 = var5.getType();
            this.addTicket(var7, var6, param0, var6);
        }

    }

    @Override
    protected int getLevelFromSource(long param0) {
        SortedArraySet<Ticket<?>> var0 = this.tickets.get(param0);
        return var0 != null && !var0.isEmpty() ? var0.first().getTicketLevel() : Integer.MAX_VALUE;
    }

    public int getLevel(ChunkPos param0) {
        return this.getLevel(param0.toLong());
    }

    @Override
    protected int getLevel(long param0) {
        return this.chunks.get(param0);
    }

    @Override
    protected void setLevel(long param0, int param1) {
        if (param1 >= 33) {
            this.chunks.remove(param0);
        } else {
            this.chunks.put(param0, (byte)param1);
        }

    }

    public void runAllUpdates() {
        this.runUpdates(Integer.MAX_VALUE);
    }

    public String getTicketDebugString(long param0) {
        SortedArraySet<Ticket<?>> var0 = this.tickets.get(param0);
        return var0 != null && !var0.isEmpty() ? var0.first().toString() : "no_ticket";
    }
}
