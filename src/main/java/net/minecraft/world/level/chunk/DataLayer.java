package net.minecraft.world.level.chunk;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.VisibleForDebug;

public class DataLayer {
    public static final int LAYER_COUNT = 16;
    public static final int LAYER_SIZE = 128;
    public static final int SIZE = 2048;
    private static final int NIBBLE_SIZE = 4;
    @Nullable
    protected byte[] data;
    private int defaultValue;

    public DataLayer() {
        this(0);
    }

    public DataLayer(int param0) {
        this.defaultValue = param0;
    }

    public DataLayer(byte[] param0) {
        this.data = param0;
        this.defaultValue = 0;
        if (param0.length != 2048) {
            throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("DataLayer should be 2048 bytes not: " + param0.length));
        }
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
            return this.defaultValue;
        } else {
            int var0 = getByteIndex(param0);
            int var1 = getNibbleIndex(param0);
            return this.data[var0] >> 4 * var1 & 15;
        }
    }

    private void set(int param0, int param1) {
        byte[] var0 = this.getData();
        int var1 = getByteIndex(param0);
        int var2 = getNibbleIndex(param0);
        int var3 = ~(15 << 4 * var2);
        int var4 = (param1 & 15) << 4 * var2;
        var0[var1] = (byte)(var0[var1] & var3 | var4);
    }

    private static int getNibbleIndex(int param0) {
        return param0 & 1;
    }

    private static int getByteIndex(int param0) {
        return param0 >> 1;
    }

    public void fill(int param0) {
        this.defaultValue = param0;
        this.data = null;
    }

    private static byte packFilled(int param0) {
        byte var0 = (byte)param0;

        for(int var1 = 4; var1 < 8; var1 += 4) {
            var0 = (byte)(var0 | param0 << var1);
        }

        return var0;
    }

    public byte[] getData() {
        if (this.data == null) {
            this.data = new byte[2048];
            if (this.defaultValue != 0) {
                Arrays.fill(this.data, packFilled(this.defaultValue));
            }
        }

        return this.data;
    }

    public DataLayer copy() {
        return this.data == null ? new DataLayer(this.defaultValue) : new DataLayer((byte[])this.data.clone());
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

    public boolean isDefinitelyHomogenous() {
        return this.data == null;
    }

    public boolean isDefinitelyFilledWith(int param0) {
        return this.data == null && this.defaultValue == param0;
    }

    public boolean isEmpty() {
        return this.data == null && this.defaultValue == 0;
    }
}
