package net.minecraft.server.level;

public enum FullChunkStatus {
    INACCESSIBLE,
    FULL,
    BLOCK_TICKING,
    ENTITY_TICKING;

    public boolean isOrAfter(FullChunkStatus param0) {
        return this.ordinal() >= param0.ordinal();
    }
}
