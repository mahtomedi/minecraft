package net.minecraft.world.level.entity;

import net.minecraft.server.level.ChunkHolder;

public enum Visibility {
    HIDDEN(false, false),
    TRACKED(true, false),
    TICKING(true, true);

    private final boolean accessible;
    private final boolean ticking;

    private Visibility(boolean param0, boolean param1) {
        this.accessible = param0;
        this.ticking = param1;
    }

    public boolean isTicking() {
        return this.ticking;
    }

    public boolean isAccessible() {
        return this.accessible;
    }

    public static Visibility fromFullChunkStatus(ChunkHolder.FullChunkStatus param0) {
        if (param0.isOrAfter(ChunkHolder.FullChunkStatus.ENTITY_TICKING)) {
            return TICKING;
        } else {
            return param0.isOrAfter(ChunkHolder.FullChunkStatus.BORDER) ? TRACKED : HIDDEN;
        }
    }
}
