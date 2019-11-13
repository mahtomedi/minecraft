package net.minecraft.server.level;

import java.util.Objects;

public final class Ticket<T> implements Comparable<Ticket<?>> {
    private final TicketType<T> type;
    private final int ticketLevel;
    private final T key;
    private long createdTick;

    protected Ticket(TicketType<T> param0, int param1, T param2) {
        this.type = param0;
        this.ticketLevel = param1;
        this.key = param2;
    }

    public int compareTo(Ticket<?> param0) {
        int var0 = Integer.compare(this.ticketLevel, param0.ticketLevel);
        if (var0 != 0) {
            return var0;
        } else {
            int var1 = Integer.compare(System.identityHashCode(this.type), System.identityHashCode(param0.type));
            return var1 != 0 ? var1 : this.type.getComparator().compare(this.key, param0.key);
        }
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof Ticket)) {
            return false;
        } else {
            Ticket<?> var0 = (Ticket)param0;
            return this.ticketLevel == var0.ticketLevel && Objects.equals(this.type, var0.type) && Objects.equals(this.key, var0.key);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.ticketLevel, this.key);
    }

    @Override
    public String toString() {
        return "Ticket[" + this.type + " " + this.ticketLevel + " (" + this.key + ")] at " + this.createdTick;
    }

    public TicketType<T> getType() {
        return this.type;
    }

    public int getTicketLevel() {
        return this.ticketLevel;
    }

    protected void setCreatedTick(long param0) {
        this.createdTick = param0;
    }

    protected boolean timedOut(long param0) {
        long var0 = this.type.timeout();
        return var0 != 0L && param0 - this.createdTick > var0;
    }
}
