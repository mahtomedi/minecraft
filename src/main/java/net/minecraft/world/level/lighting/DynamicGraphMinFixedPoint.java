package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.function.LongPredicate;
import net.minecraft.util.Mth;

public abstract class DynamicGraphMinFixedPoint {
    public static final long SOURCE = Long.MAX_VALUE;
    private static final int NO_COMPUTED_LEVEL = 255;
    protected final int levelCount;
    private final LeveledPriorityQueue priorityQueue;
    private final Long2ByteMap computedLevels;
    private volatile boolean hasWork;

    protected DynamicGraphMinFixedPoint(int param0, int param1, final int param2) {
        if (param0 >= 254) {
            throw new IllegalArgumentException("Level count must be < 254.");
        } else {
            this.levelCount = param0;
            this.priorityQueue = new LeveledPriorityQueue(param0, param1);
            this.computedLevels = new Long2ByteOpenHashMap(param2, 0.5F) {
                @Override
                protected void rehash(int param0) {
                    if (param0 > param2) {
                        super.rehash(param0);
                    }

                }
            };
            this.computedLevels.defaultReturnValue((byte)-1);
        }
    }

    protected void removeFromQueue(long param0) {
        int var0 = this.computedLevels.remove(param0) & 255;
        if (var0 != 255) {
            int var1 = this.getLevel(param0);
            int var2 = this.calculatePriority(var1, var0);
            this.priorityQueue.dequeue(param0, var2, this.levelCount);
            this.hasWork = !this.priorityQueue.isEmpty();
        }
    }

    public void removeIf(LongPredicate param0) {
        LongList var0 = new LongArrayList();
        this.computedLevels.keySet().forEach(param2 -> {
            if (param0.test(param2)) {
                var0.add(param2);
            }

        });
        var0.forEach(this::removeFromQueue);
    }

    private int calculatePriority(int param0, int param1) {
        return Math.min(Math.min(param0, param1), this.levelCount - 1);
    }

    protected void checkNode(long param0) {
        this.checkEdge(param0, param0, this.levelCount - 1, false);
    }

    protected void checkEdge(long param0, long param1, int param2, boolean param3) {
        this.checkEdge(param0, param1, param2, this.getLevel(param1), this.computedLevels.get(param1) & 255, param3);
        this.hasWork = !this.priorityQueue.isEmpty();
    }

    private void checkEdge(long param0, long param1, int param2, int param3, int param4, boolean param5) {
        if (!this.isSource(param1)) {
            param2 = Mth.clamp(param2, 0, this.levelCount - 1);
            param3 = Mth.clamp(param3, 0, this.levelCount - 1);
            boolean var0 = param4 == 255;
            if (var0) {
                param4 = param3;
            }

            int var1;
            if (param5) {
                var1 = Math.min(param4, param2);
            } else {
                var1 = Mth.clamp(this.getComputedLevel(param1, param0, param2), 0, this.levelCount - 1);
            }

            int var3 = this.calculatePriority(param3, param4);
            if (param3 != var1) {
                int var4 = this.calculatePriority(param3, var1);
                if (var3 != var4 && !var0) {
                    this.priorityQueue.dequeue(param1, var3, var4);
                }

                this.priorityQueue.enqueue(param1, var4);
                this.computedLevels.put(param1, (byte)var1);
            } else if (!var0) {
                this.priorityQueue.dequeue(param1, var3, this.levelCount);
                this.computedLevels.remove(param1);
            }

        }
    }

    protected final void checkNeighbor(long param0, long param1, int param2, boolean param3) {
        int var0 = this.computedLevels.get(param1) & 255;
        int var1 = Mth.clamp(this.computeLevelFromNeighbor(param0, param1, param2), 0, this.levelCount - 1);
        if (param3) {
            this.checkEdge(param0, param1, var1, this.getLevel(param1), var0, param3);
        } else {
            boolean var2 = var0 == 255;
            int var3;
            if (var2) {
                var3 = Mth.clamp(this.getLevel(param1), 0, this.levelCount - 1);
            } else {
                var3 = var0;
            }

            if (var1 == var3) {
                this.checkEdge(param0, param1, this.levelCount - 1, var2 ? var3 : this.getLevel(param1), var0, param3);
            }
        }

    }

    protected final boolean hasWork() {
        return this.hasWork;
    }

    protected final int runUpdates(int param0) {
        if (this.priorityQueue.isEmpty()) {
            return param0;
        } else {
            while(!this.priorityQueue.isEmpty() && param0 > 0) {
                --param0;
                long var0 = this.priorityQueue.removeFirstLong();
                int var1 = Mth.clamp(this.getLevel(var0), 0, this.levelCount - 1);
                int var2 = this.computedLevels.remove(var0) & 255;
                if (var2 < var1) {
                    this.setLevel(var0, var2);
                    this.checkNeighborsAfterUpdate(var0, var2, true);
                } else if (var2 > var1) {
                    this.setLevel(var0, this.levelCount - 1);
                    if (var2 != this.levelCount - 1) {
                        this.priorityQueue.enqueue(var0, this.calculatePriority(this.levelCount - 1, var2));
                        this.computedLevels.put(var0, (byte)var2);
                    }

                    this.checkNeighborsAfterUpdate(var0, var1, false);
                }
            }

            this.hasWork = !this.priorityQueue.isEmpty();
            return param0;
        }
    }

    public int getQueueSize() {
        return this.computedLevels.size();
    }

    protected boolean isSource(long param0) {
        return param0 == Long.MAX_VALUE;
    }

    protected abstract int getComputedLevel(long var1, long var3, int var5);

    protected abstract void checkNeighborsAfterUpdate(long var1, int var3, boolean var4);

    protected abstract int getLevel(long var1);

    protected abstract void setLevel(long var1, int var3);

    protected abstract int computeLevelFromNeighbor(long var1, long var3, int var5);
}
