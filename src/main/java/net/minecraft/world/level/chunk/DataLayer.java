package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.VisibleForDebug;

public final class DataLayer {
    public static final int LAYER_COUNT = 16;
    public static final int LAYER_SIZE = 128;
    public static final int SIZE = 2048;
    private static final int NIBBLE_SIZE = 4;
    @Nullable
    protected byte[] data;

    public DataLayer() {
    }

    public DataLayer(byte[] param0) {
        this.data = param0;
        if (param0.length != 2048) {
            throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("DataLayer should be 2048 bytes not: " + param0.length));
        }
    }

    protected DataLayer(int param0) {
        this.data = new byte[param0];
    }

    public int get(int param0, int param1, int param2) {
        return this.get(getIndex(param0, param1, param2));
    }

    public void set(int param0, int param1, int param2, int param3) {
        this.set(getIndex(param0, param1, param2), param3);
    }

    private static int getIndex(int param0, int param1, int param2) {
        return param1 << 8 | param2 << 4 | param0;
    }

    private int get(int param0) {
        if (this.data == null) {
            return 0;
        } else {
            int var0 = getByteIndex(param0);
            int var1 = getNibbleIndex(param0);
            return this.data[var0] >> 4 * var1 & 15;
        }
    }

    private void set(int param0, int param1) {
        if (this.data == null) {
            this.data = new byte[2048];
        }

        int var0 = getByteIndex(param0);
        int var1 = getNibbleIndex(param0);
        int var2 = ~(15 << 4 * var1);
        int var3 = (param1 & 15) << 4 * var1;
        this.data[var0] = (byte)(this.data[var0] & var2 | var3);
    }

    private static int getNibbleIndex(int param0) {
        return param0 & 1;
    }

    private static int getByteIndex(int param0) {
        return param0 >> 1;
    }

    public byte[] getData() {
        if (this.data == null) {
            this.data = new byte[2048];
        }

        return this.data;
    }

    public DataLayer copy() {
        return this.data == null ? new DataLayer() : new DataLayer((byte[])this.data.clone());
    }

    @Override
    public String toString() {
        StringBuilder var0 = new StringBuilder();

        for(int var1 = 0; var1 < 4096; ++var1) {
            var0.append(Integer.toHexString(this.get(var1)));
            if ((var1 & 15) == 15) {
                var0.append("\n");
            }

            if ((var1 & 0xFF) == 255) {
                var0.append("\n");
            }
        }

        return var0.toString();
    }

    @VisibleForDebug
    public String layerToString(int param0) {
        StringBuilder var0 = new StringBuilder();

        for(int var1 = 0; var1 < 256; ++var1) {
            var0.append(Integer.toHexString(this.get(var1)));
            if ((var1 & 15) == 15) {
                var0.append("\n");
            }
        }

        return var0.toString();
    }

    public boolean isEmpty() {
        return this.data == null;
    }
}
