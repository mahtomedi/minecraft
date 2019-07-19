package net.minecraft.world.level.chunk;

public class OldDataLayer {
    public final byte[] data;
    private final int depthBits;
    private final int depthBitsPlusFour;

    public OldDataLayer(byte[] param0, int param1) {
        this.data = param0;
        this.depthBits = param1;
        this.depthBitsPlusFour = param1 + 4;
    }

    public int get(int param0, int param1, int param2) {
        int var0 = param0 << this.depthBitsPlusFour | param2 << this.depthBits | param1;
        int var1 = var0 >> 1;
        int var2 = var0 & 1;
        return var2 == 0 ? this.data[var1] & 15 : this.data[var1] >> 4 & 15;
    }
}
