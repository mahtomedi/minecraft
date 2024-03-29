package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;

public class NbtAccounter {
    private static final int MAX_STACK_DEPTH = 512;
    private final long quota;
    private long usage;
    private final int maxDepth;
    private int depth;

    public NbtAccounter(long param0, int param1) {
        this.quota = param0;
        this.maxDepth = param1;
    }

    public static NbtAccounter create(long param0) {
        return new NbtAccounter(param0, 512);
    }

    public static NbtAccounter unlimitedHeap() {
        return new NbtAccounter(Long.MAX_VALUE, 512);
    }

    public void accountBytes(long param0, long param1) {
        this.accountBytes(param0 * param1);
    }

    public void accountBytes(long param0) {
        if (this.usage + param0 > this.quota) {
            throw new NbtAccounterException(
                "Tried to read NBT tag that was too big; tried to allocate: " + this.usage + " + " + param0 + " bytes where max allowed: " + this.quota
            );
        } else {
            this.usage += param0;
        }
    }

    public void pushDepth() {
        if (this.depth >= this.maxDepth) {
            throw new NbtAccounterException("Tried to read NBT tag with too high complexity, depth > " + this.maxDepth);
        } else {
            ++this.depth;
        }
    }

    public void popDepth() {
        if (this.depth <= 0) {
            throw new NbtAccounterException("NBT-Accounter tried to pop stack-depth at top-level");
        } else {
            --this.depth;
        }
    }

    @VisibleForTesting
    public long getUsage() {
        return this.usage;
    }

    @VisibleForTesting
    public int getDepth() {
        return this.depth;
    }
}
