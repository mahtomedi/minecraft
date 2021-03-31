package net.minecraft.world.level.lighting;

import net.minecraft.world.level.chunk.DataLayer;

public class FlatDataLayer extends DataLayer {
    public static final int SIZE = 128;

    public FlatDataLayer() {
        super(128);
    }

    public FlatDataLayer(DataLayer param0, int param1) {
        super(128);
        System.arraycopy(param0.getData(), param1 * 128, this.data, 0, 128);
    }

    @Override
    protected int getIndex(int param0, int param1, int param2) {
        return param2 << 4 | param0;
    }

    @Override
    public byte[] getData() {
        byte[] var0 = new byte[2048];

        for(int var1 = 0; var1 < 16; ++var1) {
            System.arraycopy(this.data, 0, var0, var1 * 128, 128);
        }

        return var0;
    }
}
