package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;

public class DataLayer {
    @Nullable
    protected byte[] data;

    public DataLayer() {
    }

    public DataLayer(byte[] param0) {
        this.data = param0;
        if (param0.length != 2048) {
            throw new IllegalArgumentException("ChunkNibbleArrays should be 2048 bytes not: " + param0.length);
        }
    }

    protected DataLayer(int param0) {
        this.data = new byte[param0];
    }

    public int get(int param0, int param1, int param2) {
        return this.get(this.getIndex(param0, param1, param2));
    }

    public void set(int param0, int param1, int param2, int param3) {
        this.set(this.getIndex(param0, param1, param2), param3);
    }

    protected int getIndex(int param0, int param1, int param2) {
        return param1 << 8 | param2 << 4 | param0;
    }

    private int get(int param0) {
        if (this.data == null) {
            return 0;
        } else {
            int var0 = this.getPosition(param0);
            return this.isFirst(param0) ? this.data[var0] & 15 : this.data[var0] >> 4 & 15;
        }
    }

    private void set(int param0, int param1) {
        if (this.data == null) {
            this.data = new byte[2048];
        }

        int var0 = this.getPosition(param0);
        if (this.isFirst(param0)) {
            this.data[var0] = (byte)(this.data[var0] & 240 | param1 & 15);
        } else {
            this.data[var0] = (byte)(this.data[var0] & 15 | (param1 & 15) << 4);
        }

    }

    private boolean isFirst(int param0) {
        return (param0 & 1) == 0;
    }

    private int getPosition(int param0) {
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

    public boolean isEmpty() {
        return this.data == null;
    }
}
