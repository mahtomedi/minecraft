package net.minecraft.core;

public final class QuartPos {
    public static int fromBlock(int param0) {
        return param0 >> 2;
    }

    public static int toBlock(int param0) {
        return param0 << 2;
    }

    public static int fromSection(int param0) {
        return param0 << 2;
    }

    public static int toSection(int param0) {
        return param0 >> 2;
    }
}
