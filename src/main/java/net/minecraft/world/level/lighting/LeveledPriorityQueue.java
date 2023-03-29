package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;

public class LeveledPriorityQueue {
    private final int levelCount;
    private final LongLinkedOpenHashSet[] queues;
    private int firstQueuedLevel;

    public LeveledPriorityQueue(int param0, final int param1) {
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

        this.firstQueuedLevel = param0;
    }

    public long removeFirstLong() {
        LongLinkedOpenHashSet var0 = this.queues[this.firstQueuedLevel];
        long var1 = var0.removeFirstLong();
        if (var0.isEmpty()) {
            this.checkFirstQueuedLevel(this.levelCount);
        }

        return var1;
    }

    public boolean isEmpty() {
        return this.firstQueuedLevel >= this.levelCount;
    }

    public void dequeue(long param0, int param1, int param2) {
        LongLinkedOpenHashSet var0 = this.queues[param1];
        var0.remove(param0);
        if (var0.isEmpty() && this.firstQueuedLevel == param1) {
            this.checkFirstQueuedLevel(param2);
        }

    }

    public void enqueue(long param0, int param1) {
        this.queues[param1].add(param0);
        if (this.firstQueuedLevel > param1) {
            this.firstQueuedLevel = param1;
        }

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
}
