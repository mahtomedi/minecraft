package net.minecraft.server.level;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;

public class TicketType<T> {
    private final String name;
    private final Comparator<T> comparator;
    private final long timeout;
    public static final TicketType<Unit> START = create("start", (param0, param1) -> 0);
    public static final TicketType<Unit> DRAGON = create("dragon", (param0, param1) -> 0);
    public static final TicketType<ChunkPos> PLAYER = create("player", Comparator.comparingLong(ChunkPos::toLong));
    public static final TicketType<ChunkPos> FORCED = create("forced", Comparator.comparingLong(ChunkPos::toLong));
    public static final TicketType<ChunkPos> LIGHT = create("light", Comparator.comparingLong(ChunkPos::toLong));
    public static final TicketType<BlockPos> PORTAL = create("portal", Vec3i::compareTo, 300);
    public static final TicketType<Integer> POST_TELEPORT = create("post_teleport", Integer::compareTo, 5);
    public static final TicketType<ChunkPos> UNKNOWN = create("unknown", Comparator.comparingLong(ChunkPos::toLong), 1);

    public static <T> TicketType<T> create(String param0, Comparator<T> param1) {
        return new TicketType<>(param0, param1, 0L);
    }

    public static <T> TicketType<T> create(String param0, Comparator<T> param1, int param2) {
        return new TicketType<>(param0, param1, (long)param2);
    }

    protected TicketType(String param0, Comparator<T> param1, long param2) {
        this.name = param0;
        this.comparator = param1;
        this.timeout = param2;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public Comparator<T> getComparator() {
        return this.comparator;
    }

    public long timeout() {
        return this.timeout;
    }
}
