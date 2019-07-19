package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ByteFunction;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import net.minecraft.util.Mth;

public abstract class DynamicGraphMinFixedPoint {
    private final int levelCount;
    private final LongLinkedOpenHashSet[] queues;
    private final Long2ByteFunction computedLevels;
    private int firstQueuedLevel;
    private volatile boolean hasWork;

    protected DynamicGraphMinFixedPoint(int param0, final int param1, final int param2) {
        if (param0 >= 254) {
            throw new IllegalArgumentException("Level count must be < 254.");
        } else {
            this.levelCount = param0;
            this.queues = new LongLinkedOpenHashSet[param0];

            for(int var0 = 0; var0 < param0; ++var0) {
                this.queues[var0] = new LongLinkedOpenHashSet(param1, 0.5F) {
                    @Override
                    protected void rehash(int param0) {
                        if (param0 > param1) {
                            super.rehash(param0);
                        }

                    }
                };
            }

            this.computedLevels = new Long2ByteOpenHashMap(param2, 0.5F) {
                @Override
                protected void rehash(int param0) {
                    if (param0 > param2) {
                        super.rehash(param0);
                    }

                }
            };
            this.computedLevels.defaultReturnValue((byte)-1);
            this.firstQueuedLevel = param0;
        }
    }

    private int getKey(int param0, int param1) {
        int var0 = param0;
        if (param0 > param1) {
            var0 = param1;
        }

        if (var0 > this.levelCount - 1) {
            var0 = this.levelCount - 1;
        }

        return var0;
    }

    private void checkFirstQueuedLevel(int param0) {
        int var0 = this.firstQueuedLevel;
        this.firstQueuedLevel = param0;

        for(int var1 = var0 + 1; var1 < param0; ++var1) {
            if (!this.queues[var1].isEmpty()) {
                this.firstQueuedLevel = var1;
                break;
            }
        }

    }

    protected void removeFromQueue(long param0) {
        int var0 = this.computedLevels.get(param0) & 255;
        if (var0 != 255) {
            int var1 = this.getLevel(param0);
            int var2 = this.getKey(var1, var0);
            this.dequeue(param0, var2, this.levelCount, true);
            this.hasWork = this.firstQueuedLevel < this.levelCount;
        }
    }

    private void dequeue(long param0, int param1, int param2, boolean param3) {
        if (param3) {
            this.computedLevels.remove(param0);
        }

        this.queues[param1].remove(param0);
        if (this.queues[param1].isEmpty() && this.firstQueuedLevel == param1) {
            this.checkFirstQueuedLevel(param2);
        }

    }

    private void enqueue(long param0, int param1, int param2) {
        this.computedLevels.put(param0, (byte)param1);
        this.queues[param2].add(param0);
        if (this.firstQueuedLevel > param2) {
            this.firstQueuedLevel = param2;
        }

    }

    protected void checkNode(long param0) {
        this.checkEdge(param0, param0, this.levelCount - 1, false);
    }

    protected void checkEdge(long param0, long param1, int param2, boolean param3) {
        this.checkEdge(param0, param1, param2, this.getLevel(param1), this.computedLevels.get(param1) & 255, param3);
        this.hasWork = this.firstQueuedLevel < this.levelCount;
    }

    private void checkEdge(long param0, long param1, int param2, int param3, int param4, boolean param5) {
        if (!this.isSource(param1)) {
            param2 = Mth.clamp(param2, 0, this.levelCount - 1);
            param3 = Mth.clamp(param3, 0, this.levelCount - 1);
            boolean var0;
            if (param4 == 255) {
                var0 = true;
                param4 = param3;
            } else {
                var0 = false;
            }

            int var2;
            if (param5) {
                var2 = Math.min(param4, param2);
            } else {
                var2 = Mth.clamp(this.getComputedLevel(param1, param0, param2), 0, this.levelCount - 1);
            }

            int var4 = this.getKey(param3, param4);
            if (param3 != var2) {
                int var5 = this.getKey(param3, var2);
                if (var4 != var5 && !var0) {
                    this.dequeue(param1, var4, var5, false);
                }

                this.enqueue(param1, var2, var5);
            } else if (!var0) {
                this.dequeue(param1, var4, this.levelCount, true);
            }

        }
    }

    protected final void checkNeighbor(long param0, long param1, int param2, boolean param3) {
        int var0 = this.computedLevels.get(param1) & 255;
        int var1 = Mth.clamp(this.computeLevelFromNeighbor(param0, param1, param2), 0, this.levelCount - 1);
        if (param3) {
            this.checkEdge(param0, param1, var1, this.getLevel(param1), var0, true);
        } else {
            int var3;
            boolean var2;
            if (var0 == 255) {
                var2 = true;
                var3 = Mth.clamp(this.getLevel(param1), 0, this.levelCount - 1);
            } else {
                var3 = var0;
                var2 = false;
            }

            if (var1 == var3) {
                this.checkEdge(param0, param1, this.levelCount - 1, var2 ? var3 : this.getLevel(param1), var0, false);
            }
        }

    }

    protected final boolean hasWork() {
        return this.hasWork;
    }

    protected final int runUpdates(int param0) {
        if (this.firstQueuedLevel >= this.levelCount) {
            return param0;
        } else {
            while(this.firstQueuedLevel < this.levelCount && param0 > 0) {
                --param0;
                LongLinkedOpenHashSet var0 = this.queues[this.firstQueuedLevel];
                long var1 = var0.removeFirstLong();
                int var2 = Mth.clamp(this.getLevel(var1), 0, this.levelCount - 1);
                if (var0.isEmpty()) {
                    this.checkFirstQueuedLevel(this.levelCount);
                }

                int var3 = this.computedLevels.remove(var1) & 255;
                if (var3 < var2) {
                    this.setLevel(var1, var3);
                    this.checkNeighborsAfterUpdate(var1, var3, true);
                } else if (var3 > var2) {
                    this.enqueue(var1, var3, this.getKey(this.levelCount - 1, var3));
                    this.setLevel(var1, this.levelCount - 1);
                    this.checkNeighborsAfterUpdate(var1, var2, false);
                }
            }

            this.hasWork = this.firstQueuedLevel < this.levelCount;
            return param0;
        }
    }

    protected abstract boolean isSource(long var1);

    protected abstract int getComputedLevel(long var1, long var3, int var5);

    protected abstract void checkNeighborsAfterUpdate(long var1, int var3, boolean var4);

    protected abstract int getLevel(long var1);

    protected abstract void setLevel(long var1, int var3);

    protected abstract int computeLevelFromNeighbor(long var1, long var3, int var5);
}
